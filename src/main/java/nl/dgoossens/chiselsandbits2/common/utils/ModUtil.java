package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Random;

public class ModUtil {
    private final static Random RAND = new Random();
    //@Nonnull
    //public static final String NBT_SIDE = "side";

    @Nonnull
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";

    public static BlockState getStateById(
            final int blockStateID )
    {
        return Block.getStateById( blockStateID );
    }

    public static int getStateId(
            final BlockState state )
    {
        return Math.max( 0, Block.getStateId( state ) );
    }

    public static boolean isEmpty(
            final ItemStack itemStack )
    {
        return itemStack == null || itemStack.isEmpty();
    }
}
