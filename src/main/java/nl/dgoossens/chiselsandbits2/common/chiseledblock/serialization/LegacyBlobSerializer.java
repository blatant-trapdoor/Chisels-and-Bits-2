package nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraft.util.datafix.versions.V0099;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.*;

/**
 * Legacy compatibility with worlds upgraded by the WorldFixer from 1.12
 */
@Deprecated
public class LegacyBlobSerializer extends BlobSerializer {
    private static Gson gson = new GsonBuilder().create();

    public static void load() {
        //Set up data fixers
        
    }

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

    private static class NBTState {
        //Capital letters because we need to fit the json Mojang uses
        String Name = "";
        Map<String, String> Properties;
    }

    public static int getStateIDFromName(final String input) {
        NBTState state = new NBTState();
        final String parts[] = input.split("[?&]");
        try {
            state.Name = URLDecoder.decode(parts[0], "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (int x = 1; x < parts.length; ++x) {
            try {
                if (parts[x].length() > 0) {
                    final String[] nameval = parts[x].split("[=]", 2);
                    nameval[0] = URLDecoder.decode(nameval[0], "UTF-8");
                    nameval[1] = URLDecoder.decode(nameval[1], "UTF-8");
                    if(state.Properties == null) state.Properties = new HashMap<>();
                    state.Properties.put(nameval[0], nameval[1]);
                }
            } catch (final Exception err) {
                err.printStackTrace();
            }
        }

        Dynamic<?> dyn = BlockStateFlatteningMap.updateNBT(BlockStateFlatteningMap.makeDynamic(gson.toJson(state)));
        final Block blk = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(dyn.get("Name").asString("minecraft:air"))); //Get the block name
        if (blk == null) return 0;

        BlockState blockState = blk.getDefaultState();
        if (blockState == null)
            return 0;

        Optional<? extends Dynamic<?>> props = dyn.get("Properties").get();
        if(props.isPresent()) {
            for(Map.Entry e :  props.get().getMapValues().orElse(new HashMap<>()).entrySet()) {
                final IProperty prop = blockState.getProperties().stream().filter(f -> f.getName().equalsIgnoreCase(e.getKey().toString())).findAny().orElse(null);
                if (prop == null) {
                    System.out.println("Couldn't resolve property '" + e.getKey() + "' for block type " + blk);
                    continue;
                }
                Optional val = prop.parseValue(e.getValue().toString());
                if(!val.isPresent()) continue;
                blockState = blockState.with(prop, (Comparable) val.get());
            }
        }

        return BitUtil.getBlockId(blockState);
    }
}
