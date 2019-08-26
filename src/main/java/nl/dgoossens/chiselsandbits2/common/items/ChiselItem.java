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
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.modes.BitOperation;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.network.packets.PacketChisel;

import javax.annotation.Nullable;
import java.util.List;

public class ChiselItem extends TypedItem {
    public ChiselItem(Item.Properties builder) {
        super(builder);
    }

    @Override
    public ItemMode.Type getAssociatedType() {
        return ItemMode.Type.CHISEL;
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
     * Handle the block chiselling on left click.
     */
    public static boolean startChiselingBlock(final BlockRayTraceResult rayTrace, final ItemMode mode, final PlayerEntity player) {
        if(!player.world.isRemote) throw new UnsupportedOperationException("Block chiseling can only be started on the client-side.");

        final BlockPos pos = rayTrace.getPos();
        final BlockState state = player.world.getBlockState(pos);
        if(!ChiselUtil.canChiselBlock(state)) return true;
        if(!ChiselUtil.canChiselPosition(pos, player, state, rayTrace.getFace())) return true;
        Vec3d hitBit = rayTrace.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
        useChisel(BitOperation.REMOVE, mode, player, player.world, pos, rayTrace.getFace(), hitBit);
        return true;
    }

     /**
     * Handle placement on right click.
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack item, ItemUseContext context) {
        final BlockPos pos = context.getPos();

        if(!ChiselUtil.canChiselBlock(context.getWorld().getBlockState(pos)))
            return ActionResultType.FAIL;

        if(context.getPlayer()==null || !ChiselUtil.canChiselPosition(pos, context.getPlayer(), context.getWorld().getBlockState(pos), context.getFace()))
            return ActionResultType.FAIL;

        if(context.getWorld().isRemote) {
            final RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
            if(rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
                Vec3d hitBit = rtr.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
                //TODO place in the bit in front instead of in the bit selected, also re-do checks if the block can be changed
                useChisel(BitOperation.PLACE, ItemMode.getMode(item), context.getPlayer(), context.getWorld(), pos, ((BlockRayTraceResult) rtr).getFace(), hitBit);
            }
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Uses the chisel on a specific bit of a specific block.
     * Does everything short of updating the voxel data. (and updating the durability of the used tool)
     */
    static void useChisel(final BitOperation operation, final ItemMode mode, final PlayerEntity player, final World world, final BlockPos pos, final Direction side, final Vec3d hitBit) {
        final BitLocation location = new BitLocation(new BlockRayTraceResult(hitBit, side, pos, false), false, operation);
        final PacketChisel pc = new PacketChisel(operation, location, side, mode);
        final int modifiedBits = pc.doAction(player);
        if(modifiedBits != 0) {
            ChiselsAndBits2.getClient().breakSound(world, pos, ModUtil.getStateById(modifiedBits));
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
}
