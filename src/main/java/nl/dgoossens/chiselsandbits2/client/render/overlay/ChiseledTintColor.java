package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelType;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

/**
 * General parent for all items that need colouring based on bit type.
 */
public class ChiseledTintColor {
    public static final int TINT_MASK = 0xff;
    public static final int TINT_BITS = 8;

    /**
     * Get the tint colour to display based on the bit type embedded in the tint.
     */
    public int getColor(int tint) {
        if(tint == -1) return -1;

        //If this block has a colour we hide it in the tint value. See ModelQuadLayer.java
        int v = tint >> TINT_BITS;
        int tintValue = tint & TINT_MASK;

        if(VoxelType.isColoured(tint))
            return BitUtil.getColourState(tint).hashCode();

        if(VoxelType.isFluid(tint)) {
            final IFluidState fstate = BitUtil.getFluidState(tint);
            FluidStack f = new FluidStack(fstate.getFluid(), 1);
            return fstate.getFluid().getAttributes().getColor(f);
        }

        return getDefaultColor(v, tintValue);
    }

    /**
     * Get the default item colour, can be overwritten if need be for a block.
     */
    public int getDefaultColor(int v, int tintValue) {
        return Minecraft.getInstance().getItemColors().getColor(new ItemStack(BitUtil.getBlockState(v).getBlock().asItem()), tintValue);
    }
}
