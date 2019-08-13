package nl.dgoossens.chiselsandbits2.common.items;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.modes.BitOperation;
import nl.dgoossens.chiselsandbits2.api.modes.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;
import nl.dgoossens.chiselsandbits2.common.utils.MathUtil;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.network.packets.PacketChisel;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChiselItem extends Item implements IItemScrollWheel {
    public ChiselItem(Item.Properties builder) {
        super(builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "chisel.help",
                Minecraft.getInstance().gameSettings.keyBindAttack,
                ChiselsAndBits2.getKeybindings().modeMenu
        );
    }

    /**
     * We track the last time the player clicked to chisel to determine when 150ms have passed before
     * we allow another click. Static to be sure we don't waste memory if two item instances are
     * ever created somehow.
     */
    private static Map<UUID, Long> lastClick = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock e) {
        if(e.getCancellationResult()==ActionResultType.FAIL) return;
        if(!(e.getItemStack().getItem() instanceof ChiselItem)) return;
        if(startChiselingBlock(e.getPos(), ItemMode.getChiselMode(e.getItemStack()), e.getPlayer())) {
            if(e.getPlayer().abilities.isCreativeMode) e.setCanceled(true);
            else e.setUseItem(Event.Result.DENY);
        }
    }

    /**
     * Handle the block chiselling on left click.
     */
    public static boolean startChiselingBlock(final BlockPos pos, final ItemMode mode, final PlayerEntity player) {
        if(!player.world.isRemote) return true; //We only want to run this on the client.

        if(System.currentTimeMillis()-lastClick.getOrDefault(player.getUniqueID(), 0L) < 300) return true;
        lastClick.put(player.getUniqueID(), System.currentTimeMillis());

        final BlockState state = player.world.getBlockState(pos);
        if(!ChiselUtil.canChiselBlock(state)) return true;
        final RayTraceResult rtr = MathUtil.getRayTraceResult(state, pos, player);
        if(rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
            Vec3d hitBit = rtr.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
            useChisel(BitOperation.REMOVE, mode, player, player.world, pos, ((BlockRayTraceResult) rtr).getFace(), hitBit);
        }
        return true;
    }

     /**
     * Handle placement on right click.
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack item, ItemUseContext context) {
        if(!context.getPlayer().canPlayerEdit(context.getPos(), context.getFace(), item)) return ActionResultType.FAIL;
        if(context.getWorld().isRemote) {
            final BlockPos pos = context.getPos();
            final BlockState state = context.getWorld().getBlockState(pos);
            final RayTraceResult rtr = MathUtil.getRayTraceResult(state, pos, context.getPlayer());
            if(rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
                Vec3d hitBit = rtr.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
                useChisel(BitOperation.PLACE, ItemMode.getChiselMode(item), context.getPlayer(), context.getWorld(), pos, ((BlockRayTraceResult) rtr).getFace(), hitBit);
            }
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Uses the chisel on a specific bit of a specific block.
     * Does everything short of updating the voxel data. (and updating the durability)
     */
    static void useChisel(final BitOperation operation, final ItemMode mode, final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final Vec3d hitBit) {
        final BitLocation location = new BitLocation(new BlockRayTraceResult(hitBit, side, pos, false), false, operation);
        final PacketChisel pc = new PacketChisel(operation, location, side, mode);
        final int modifiedBits = pc.doAction( player );
        if(modifiedBits != 0) {
            ChiselsAndBits2.getClient().breakSound(world, pos, modifiedBits);
            NetworkRouter.sendToServer(pc);
        }
    }

    /**
     * Hitting entities takes durability damage. (same as ItemTool)
     */
    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damageItem(1, attacker, l -> l.sendBreakAnimation(EquipmentSlotType.MAINHAND));
        return true;
    }

    /**
     * The enchantability should be equal to that of a golden item.
     * The item can also be enchanted with any enchantment that can go onto durability items:
     * mending, unbreaking, curse of vanishing
     */
    @Override
    public int getItemEnchantability() {
        return ItemTier.GOLD.getEnchantability();
    }

    /**
     * Make the item repairable in anvils using gold.
     */
    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if(Tags.Items.INGOTS_GOLD.contains(repair.getItem())) return true; //Can repair with gold.
        return super.getIsRepairable(toRepair, repair);
    }

    /**
     * Here we customise the attack speed and attack damage to show up in the tooltip,
     * attack damage is 1 and attack speed is 2.
     */
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        if(slot == EquipmentSlotType.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, () -> "Tool modifier", 0, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, () -> "Tool modifier", -2, AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return displayName + " - " + WordUtils.capitalizeFully(ItemMode.getChiselMode( item ).name());
    }

    @Override
    public void scroll(
            final PlayerEntity player,
            final ItemStack stack,
            final int dwheel )
    {
        final ItemMode mode = ChiselModeManager.getMode( player );
        ChiselModeManager.scrollOption( ItemMode.Type.CHISEL, mode, dwheel );
    }
}
