package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.network.packets.PacketChisel;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChiselEvent {
    /**
     * We track the last time the player clicked to chisel to determine when 300ms have passed before
     * we allow another click. This event is client-sided so we only need a single variable.
     */
    private static long lastClick = System.currentTimeMillis();

    //Waiting for Forge PR adding the ClickInputEvent
    /*@SubscribeEvent
    public static void onClick(InputEvent.ClickInputEvent e) {
        if(e.isMiddleClick()) {
            //If we're middle clicking and
            if(ChiselsAndBits2.getKeybindings().selectBitType.getKey().equals(ChiselsAndBits2.getKeybindings().selectBitType.getDefault())) e.setCancelled(true);
            return;
        }
        if(e.getRaytrace() == null || e.getRaytrace().getType() != RayTraceResult.Type.BLOCK) return;
        final PlayerEntity player = ChiselsAndBits2.getClient().getPlayer();
        if(!(player.getHeldItemMainhand().getItem() instanceof ChiselItem)) return;

        if(System.currentTimeMillis()-lastClick < 300) return;
        lastClick = System.currentTimeMillis();

        final BitOperation operation = e.isLeftClick() ? BitOperation.REMOVE : (ChiselModeManager.getMenuActionMode(player.getHeldItemMainhand()).equals(MenuAction.REPLACE) ? BitOperation.REPLACE : BitOperation.PLACE);
        startChiselingBlock(e.getRaytrace(), ChiselModeManager.getMode(player.getHeldItemMainhand()), player, operation);
        e.setCanceled(true);
    }*/

    /**
     * Handle the block chiselling with the given bit operation.
     */
    public static void startChiselingBlock(final BlockRayTraceResult rayTrace, final ItemMode mode, final PlayerEntity player, final BitOperation operation) {
        if(!player.world.isRemote) throw new UnsupportedOperationException("Block chiseling can only be started on the client-side.");

        final BitLocation location = new BitLocation(rayTrace, false, operation);
        final BlockPos pos = location.getBlockPos(); //We get the location from the bitlocation because that takes placement offset into account. (placement can go into neighbouring block)
        final BlockState state = player.world.getBlockState(pos);
        if(!ChiselUtil.canChiselBlock(state)) return;
        if(!ChiselUtil.canChiselPosition(pos, player, state, rayTrace.getFace())) return;
        useChisel(operation, mode, player, player.world, rayTrace.getFace(), location);
    }

    /**
     * Uses the chisel on a specific bit of a specific block.
     * Does everything short of updating the voxel data. (and updating the durability of the used tool)
     */
    private static void useChisel(final BitOperation operation, final IItemMode mode, final PlayerEntity player, final World world, final Direction face, final BitLocation location) {
        final PacketChisel pc = new PacketChisel(operation, location, face, mode);
        final int modifiedBits = pc.doAction(player);
        if(modifiedBits != 0) {
            ChiselsAndBits2.getClient().breakSound(world, location.getBlockPos(), ModUtil.getStateById(modifiedBits));
            NetworkRouter.sendToServer(pc);
        }
    }
}
