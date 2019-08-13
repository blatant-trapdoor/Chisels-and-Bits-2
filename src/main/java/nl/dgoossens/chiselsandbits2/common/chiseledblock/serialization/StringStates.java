package nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * Currently unused as there is no support for cross-world data saving.
 */
@Deprecated
public class StringStates {
    public static int getStateIDFromName(final String name) {
        final String parts[] = name.split("[?&]");

        try {
            parts[0] = URLDecoder.decode(parts[0], "UTF-8");
        } catch(final UnsupportedEncodingException e) { e.printStackTrace(); }

        final Block blk = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[0]));
        if(blk == null) return 0;

        BlockState state = blk.getDefaultState();
        if(state == null || state.isAir()) return 0;

        // rebuild state...
        for(int x = 1; x < parts.length; ++x) {
            try {
                if(parts[x].length() > 0) {
                    final String nameval[] = parts[x].split("[=]");
                    if(nameval.length == 2) {
                        nameval[0] = URLDecoder.decode(nameval[0], "UTF-8");
                        nameval[1] = URLDecoder.decode(nameval[1], "UTF-8");

                        state = withState(state, blk, nameval);
                    }
                }
            } catch(final Exception err) {
                err.printStackTrace();
            }
        }

        return ModUtil.getStateId(state);
    }

    private static BlockState withState(final BlockState state, final Block blk, final String[] nameval) {
        final IProperty prop = state.getProperties().parallelStream().filter(f -> f.getName().equals(nameval[0])).findAny().orElse(null);
        if(prop == null) {
            System.out.println(nameval[0] + " is not a valid property for " + ForgeRegistries.BLOCKS.getKey(blk));
            return state;
        }

        final Optional pv = prop.parseValue(nameval[1]);
        if(pv.isPresent()) {
            return state.with(prop, (Comparable) pv.get());
        } else {
            System.out.println(nameval[1] + " is not a valid value of " + nameval[0] + " for " + ForgeRegistries.BLOCKS.getKey(blk));
            return state;
        }
    }

    public static String getNameFromStateID(final int key) {
        final BlockState state = ModUtil.getStateById(key);
        final Block blk = state.getBlock();

        String sname = "air?";

        try {
            final StringBuilder stateName = new StringBuilder(URLEncoder.encode(ForgeRegistries.BLOCKS.getKey(blk).toString(), "UTF-8"));
            stateName.append('?');

            boolean first = true;
            for(final IProperty<?> P : state.getProperties()) {
                if(!first)
                    stateName.append('&');

                first = false;
                final Comparable<?> propVal = state.get(P);

                String saveAs;
                if(propVal instanceof IStringSerializable) saveAs = ((IStringSerializable) propVal).getName();
                else saveAs = propVal.toString();

                stateName.append(URLEncoder.encode(P.getName(), "UTF-8"));
                stateName.append('=');
                stateName.append(URLEncoder.encode(saveAs, "UTF-8"));
            }

            sname = stateName.toString();
        } catch(final UnsupportedEncodingException e) { e.printStackTrace(); }
        return sname;
    }
}
