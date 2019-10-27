package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.item.Item;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.api.VoxelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BitStorageImpl implements BitStorage {
    private HashMap<Integer, Pair<VoxelWrapper, Long>> contents;
    //TODO it remains a mystery how we're gonna determine which voxel type a given bit storage is.
    private VoxelType stored = VoxelType.BLOCKSTATE;
    private List<VoxelWrapper> list;
    private List<IItemMode> cache;

    private void resetCaches() {
        list = null;
        cache = null;
    }

    //Gets the amount of slots this bit storage has
    private int getSlots() {
        switch(stored) {
            case BLOCKSTATE: return ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBag.get();
            case FLUIDSTATE: return ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBeaker.get();
            case COLOURED: return ChiselsAndBits2.getInstance().getConfig().bookmarksPerPalette.get();
            default: return 8;
        }
    }

    //Get the maximal capacity in a single slot.
    private long getMaxCapacity() {
        return ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get();
    }

    /**
     * Finds the first available slot for a given type.
     */
    private int findSlot(VoxelWrapper w) {
        for(int i = 0; i < getSlots(); i++) {
            if(contents.get(i) == null) return i;
            VoxelWrapper a = contents.get(i).getLeft();
            if(a.getId() != w.getId()) continue;
            return i;
        }
        return -1;
    }

    public BitStorageImpl() {
        contents = new HashMap<>();
        for(int i = 0; i < getSlots(); i++)
            contents.put(0, null);
    }

    @Override
    public void setType(VoxelType voxelType) {
        stored = voxelType;
    }

    @Override
    public VoxelType getType() {
        return stored;
    }

    @Override
    public List<VoxelWrapper> list() {
        if(list == null)
            list = contents.values().parallelStream().filter(f -> f != null).map(Pair::getKey).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<IItemMode> listTypesAsItemModes(Item item) {
        if(cache == null) {
            cache = new ArrayList<>();
            for(int i = 0; i < getSlots(); i++) {
                //Extend the contents if somehow the slots size changed.
                if(!contents.containsKey(i)) contents.put(i, null);
                Pair<VoxelWrapper, Long> p = contents.get(i);
                if(p == null) cache.add(SelectedItemMode.NONE);
                else cache.add(SelectedItemMode.fromVoxelWrapper(p.getLeft()));
            }
        }
        return cache;
    }

    @Override
    public long add(final VoxelWrapper w, final long amount) {
        int slot = findSlot(w);
        if(slot == -1) return amount;
        Pair<VoxelWrapper, Long> pair = contents.get(slot);
        if(pair == null) pair = Pair.of(w, 0L);

        long available = getMaxCapacity() - pair.getRight();
        long use = Math.min(amount, available);
        //Special case for negative amounts to make sure they don't make negative amounts
        if(pair.getRight() + use < 0)
            use = -pair.getRight();
        pair = Pair.of(pair.getLeft(), pair.getRight() + use);
        contents.put(slot, pair.getValue() <= 0 ? null : pair);
        resetCaches();

        return amount - use;
    }

    @Override
    public void set(final VoxelWrapper w, final long amount) {
        int slot = findSlot(w);
        if(slot == -1) return;
        Pair<VoxelWrapper, Long> pair = contents.get(slot);
        if(pair == null) pair = Pair.of(w, 0L);
        pair = Pair.of(pair.getLeft(), amount);
        contents.put(slot, pair.getValue() <= 0 ? null : pair);
        resetCaches();
    }

    @Override
    public boolean has(VoxelWrapper b) {
        for(int i = 0; i < getSlots(); i++) {
            if(contents.get(i) == null) continue;
            VoxelWrapper a = contents.get(i).getLeft();
            if(a.getId() == b.getId()) return true;
        }
        return false;
    }

    @Override
    public long get(final VoxelWrapper w) {
        int slot = findSlot(w);
        if(slot == -1) return 0;
        Pair<VoxelWrapper, Long> p = contents.get(slot);
        long current = 0;
        if(p != null) current = p.getValue();
        return current;
    }

    @Override
    public long queryRoom(final VoxelWrapper w) {
        return getMaxCapacity() - get(w);
    }

    @Override
    public int getSlot(final VoxelWrapper w) {
        int i = findSlot(w);
        if(i == -1) return -1;
        if(contents.get(i) == null) return -1;
        return i;
    }

    @Override
    public void setSlot(final int index, final VoxelWrapper w, final long amount) {
        contents.put(index, Pair.of(w, amount));
    }

    @Override
    public void clearSlot(final int index) {
        contents.put(index, null);
    }
}

