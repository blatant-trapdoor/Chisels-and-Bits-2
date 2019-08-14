package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;

/**
 * Largely copied from https://github.com/AlgorithmX2/Chisels-and-Bits/blob/1.12/src/main/java/mod/chiselsandbits/helpers/ModUtil.java
 */
@Deprecated
public class MathUtil {
    private final static float DEG_TO_RAD = 0.017453292f;

    /**
     * Should no longer be needed as we get the ray trace from the InputEvent.ClickInputEvent
     */
    @Deprecated
    public static RayTraceResult getRayTraceResult(final BlockState state, final BlockPos pos, final PlayerEntity playerIn) {
        double reachDistance = playerIn.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();

        final double x = playerIn.prevPosX + ( playerIn.posX - playerIn.prevPosX );
        final double y = playerIn.prevPosY + ( playerIn.posY - playerIn.prevPosY ) + playerIn.getEyeHeight();
        final double z = playerIn.prevPosZ + ( playerIn.posZ - playerIn.prevPosZ );

        final float playerPitch = playerIn.prevRotationPitch + ( playerIn.rotationPitch - playerIn.prevRotationPitch );
        final float playerYaw = playerIn.prevRotationYaw + ( playerIn.rotationYaw - playerIn.prevRotationYaw );

        final float yawRayX = MathHelper.sin( -playerYaw * DEG_TO_RAD - (float) Math.PI );
        final float yawRayZ = MathHelper.cos( -playerYaw * DEG_TO_RAD - (float) Math.PI );

        final float pitchMultiplier = -MathHelper.cos( -playerPitch * DEG_TO_RAD );
        final float eyeRayY = MathHelper.sin( -playerPitch * DEG_TO_RAD );
        final float eyeRayX = yawRayX * pitchMultiplier;
        final float eyeRayZ = yawRayZ * pitchMultiplier;

        final Vec3d from = new Vec3d(x, y, z);
        final Vec3d to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        //For some weird reason they decided to have the getRaytraceShape return VoxelShape.empty by default. So I need to use the collision shape for RAYTRACKING instead of the RAYTRACE shape. Great!
        return state.getBlock().getCollisionShape(state, playerIn.world, pos, ISelectionContext.forEntity(playerIn)).rayTrace(from, to, pos);
    }
}
