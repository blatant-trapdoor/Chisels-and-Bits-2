package nl.dgoossens.chiselsandbits2.client.culling;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import javax.annotation.Nullable;

public class DummyEnvironmentWorldReader implements IEnviromentBlockReader {
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    public int getLightFor(LightType type, BlockPos pos) {
        return type==LightType.SKY ? 15 : 0;
    }

    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    public IFluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }
}
