package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitOperation;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.MenuAction;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.network.packets.PacketChisel;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChiselEvent {
    /**
     * We track the last time the player clicked to chisel to determine when 300ms have passed before
     * we allow another click. This event is client-sided so we only need a single variable.
     */
    private static long lastClick = System.currentTimeMillis();

    /*@SubscribeEvent
    public static void onClick(InputEvent.ClickInputEvent e) {
        if(e.isPickBlock()) {
            //If we're middle clicking and the keybind for selectbittype wasn't changed we cancel it and do our own implementation.
            if(ChiselsAndBits2.getKeybindings().selectBitType.getKey().equals(ChiselsAndBits2.getKeybindings().selectBitType.getDefault()))
                e.setCancelled(true);
            return;
        }
        RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
        if(rtr == null || rtr.getType() != RayTraceResult.Type.BLOCK) return;
        final PlayerEntity player = ChiselsAndBits2.getClient().getPlayer();
        if(!(player.getHeldItemMainhand().getItem() instanceof ChiselItem)) return;
        e.setCanceled(true);

        if(System.currentTimeMillis()-lastClick < 300) return;
        lastClick = System.currentTimeMillis();

        final BitOperation operation = e.isAttack() ? BitOperation.REMOVE : (ChiselModeManager.getMenuActionMode(player.getHeldItemMainhand()).equals(MenuAction.REPLACE) ? BitOperation.REPLACE : BitOperation.PLACE);
        startChiselingBlock((BlockRayTraceResult) rtr, ChiselModeManager.getMode(player.getHeldItemMainhand()), player, operation);
    }*/

    /**
     * Will be removed when the proper ClickInputEvent gets added so we don't have to deal with
     * stupid minecraft bugs and de-syncs.
     */
    @Deprecated
    @SubscribeEvent
    public static void temporaryClick(PlayerInteractEvent e) {
        boolean leftClick = e instanceof PlayerInteractEvent.LeftClickBlock;
        if (!leftClick && !(e instanceof PlayerInteractEvent.RightClickBlock)) return;

        RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
        if (rtr == null || rtr.getType() != RayTraceResult.Type.BLOCK) return;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(player.getHeldItemMainhand().getItem() instanceof ChiselItem)) return;
        e.setCanceled(true);

        if (System.currentTimeMillis() - lastClick < 300) return;
        lastClick = System.currentTimeMillis();

        final BitOperation operation = leftClick ? BitOperation.REMOVE : (ChiselModeManager.getMenuActionMode(player.getHeldItemMainhand()).equals(MenuAction.REPLACE) ? BitOperation.REPLACE : BitOperation.PLACE);
        startChiselingBlock((BlockRayTraceResult) rtr, ChiselModeManager.getMode(player.getHeldItemMainhand()), player, operation);
    }

    /**
     * Handle the block chiselling with the given bit operation.
     */
    public static void startChiselingBlock(final BlockRayTraceResult rayTrace, final IItemMode mode, final PlayerEntity player, final BitOperation operation) {
        if (!player.world.isRemote)
            throw new UnsupportedOperationException("Block chiseling can only be started on the client-side.");

        final BitLocation location = new BitLocation(rayTrace, true, operation);
        final BlockPos pos = location.getBlockPos(); //We get the location from the bitlocation because that takes placement offset into account. (placement can go into neighbouring block)
        final BlockState state = player.world.getBlockState(pos);
        if (!ChiselUtil.canChiselBlock(state)) return;
        if (!ChiselUtil.canChiselPosition(pos, player, state, rayTrace.getFace())) return;
        useChisel(operation, mode, player.world, rayTrace.getFace(), location);
    }

    /**
     * Uses the chisel on a specific bit of a specific block.
     * Does everything short of updating the voxel data. (and updating the durability of the used tool)
     */
    private static void useChisel(final BitOperation operation, final IItemMode mode, final World world, final Direction face, final BitLocation location) {
        final PacketChisel pc = new PacketChisel(operation, location, face, mode);
        //The chiseled block redirects the breaking sound.
        ChiselsAndBits2.getInstance().getClient().breakSound(world, location.getBlockPos(), world.getBlockState(location.getBlockPos()));
        NetworkRouter.sendToServer(pc);
    }
}
