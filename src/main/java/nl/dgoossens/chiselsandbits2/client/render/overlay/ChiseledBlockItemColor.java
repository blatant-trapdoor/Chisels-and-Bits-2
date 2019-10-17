package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

public class ChiseledBlockItemColor implements IItemColor {
    @Override
    public int getColor(ItemStack item, int tint) {
        if(tint == -1) return -1;

        //If this block has a colour we hide it in the tint value. See ModelQuadLayer.java
        int v = tint >> ChiseledBlockColor.TINT_BITS;
        int tintValue = tint & ChiseledBlockColor.TINT_MASK;

        if(VoxelType.isColoured(tint))
            return ModUtil.getColourState(tint).hashCode();

        if(VoxelType.isFluid(tint)) {
            final IFluidState fstate = ModUtil.getFluidState(tint);
            FluidStack f = new FluidStack(fstate.getFluid(), 1);
            return fstate.getFluid().getAttributes().getColor(f);
        }

        final ItemStack titem = new ItemStack(ModUtil.getBlockState(v).getBlock().asItem());
        return Minecraft.getInstance().getItemColors().getColor(titem, tintValue);
    }
}
