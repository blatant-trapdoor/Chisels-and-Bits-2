package nl.dgoossens.chiselsandbits2.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.client.render.models.BaseSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.models.CacheClearable;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.*;

public class MorphingBitSmartModel extends BaseSmartModel implements CacheClearable {
    private static final Map<Integer, MorphingBitBaked> cache = new HashMap<>();

    /**
     * Get the morphing bit model for a given selected bit type, if bit equals {@link nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob#AIR_BIT} then
     * the gray default will be returned.
     */
    public static MorphingBitBaked getCachedModel(final int bit) {
        return cache.computeIfAbsent(bit, MorphingBitBaked::new);
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity) {
        return getCachedModel(entity instanceof PlayerEntity ? ItemModeUtil.getSelectedBit((PlayerEntity) entity) : world == null || world.isRemote ? ItemModeUtil.getSelectedBit(Minecraft.getInstance().player) : VoxelBlob.AIR_BIT);
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
