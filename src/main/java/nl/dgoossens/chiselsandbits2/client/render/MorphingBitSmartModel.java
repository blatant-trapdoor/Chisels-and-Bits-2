package nl.dgoossens.chiselsandbits2.client.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.util.concurrent.TimeUnit;

public class MorphingBitSmartModel extends BaseSmartModel {
    private static final LoadingCache<Integer, MorphingBitBaked> cache = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build(new CacheLoader<Integer, MorphingBitBaked>() {
                @Override
                public MorphingBitBaked load(Integer key) {
                    return new MorphingBitBaked(key);
                }
            });

    /**
     * Get the morphing bit model for a given selected bit type, if bit equals {@link nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob#AIR_BIT} then
     * the gray default will be returned.
     */
    public static MorphingBitBaked getCachedModel(final int bit) {
        try {
            return cache.get(bit);
        } catch(Exception x) {
            x.printStackTrace();
            return new MorphingBitBaked(bit);
        }
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity) {
        return getCachedModel(entity instanceof PlayerEntity ? ItemModeUtil.getGlobalSelectedBit((PlayerEntity) entity) : world == null || world.isRemote ? ItemModeUtil.getGlobalSelectedBit(Minecraft.getInstance().player) : VoxelBlob.AIR_BIT);
    }
}
