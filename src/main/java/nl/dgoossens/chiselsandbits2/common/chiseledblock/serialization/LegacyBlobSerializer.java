package nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Legacy compatibility with worlds upgraded by the WorldFixer from 1.12
 */
@Deprecated
public class LegacyBlobSerializer extends BlobSerializer {
    @Override
    protected int readStateID(final PacketBuffer buffer) {
        final String name = buffer.readString(2047);
        buffer.readString(2047);
        return getStateIDFromName(name);
    }

    @Override
    protected void writeStateID(final PacketBuffer buffer, final int key) {
        throw new UnsupportedOperationException("Can't write LEGACY blob serializer!");
    }

    @Override
    public VoxelVersions getVersion() {
        return VoxelVersions.LEGACY;
    }

    public static int getStateIDFromName(final String name) {
        final String parts[] = name.split("[?&]");
        try {
            parts[0] = URLDecoder.decode(parts[0], "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> states = new HashMap<>();
        // rebuild state...
        for (int x = 1; x < parts.length; ++x) {
            try {
                if (parts[x].length() > 0) {
                    final String nameval[] = parts[x].split("[=]");
                    if (nameval.length == 2) {
                        nameval[0] = URLDecoder.decode(nameval[0], "UTF-8");
                        nameval[1] = URLDecoder.decode(nameval[1], "UTF-8");
                        states.put(nameval[0], nameval[1]);
                    }
                }
            } catch (final Exception err) {
                err.printStackTrace();
            }
        }

        String s = parts[0];
        switch(parts[0]) {
            case "minecraft:concrete":
            {
                if(states.containsKey("color")) {
                    String st = states.remove("color");
                    if(st.equals("silver")) s = "minecraft:light_gray_concrete";
                    else s = "minecraft:"+st+"_concrete";
                }
                break;
            }
        }
        final Block blk = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
        if (blk == null) return 0;

        BlockState state = blk.getDefaultState();
        if (state == null)
            return 0;

        for(Map.Entry<String, String> e : states.entrySet()) {
            state = withState(state, blk, new String[]{e.getKey(), e.getValue()});
        }

        return BitUtil.getBlockId(state);
    }

    private static BlockState withState(final BlockState state, final Block blk, final String[] nameval) {
        final IProperty prop = state.getProperties().stream().filter(f -> f.getName().equalsIgnoreCase(nameval[0])).findAny().orElse(null);
        if (prop == null) {
            System.out.println("Couldn't resolve property '" + nameval[0] + "' for block type " + blk);
            return state;
        }

        final Optional pv = prop.parseValue(nameval[1]);
        if (pv.isPresent()) {
            return state.with(prop, (Comparable) pv.get());
        } else {
            System.out.println("'" + nameval[1] + "' is not a valid value of '" + nameval[0] + "' for " + blk);
            return state;
        }
    }
}
