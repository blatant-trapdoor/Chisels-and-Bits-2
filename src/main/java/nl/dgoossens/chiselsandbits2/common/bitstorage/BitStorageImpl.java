package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.bit.BitStorage;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelType;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BitStorageImpl implements BitStorage {
    //TODO it remains a mystery how we're gonna determine which voxel type a given bit storage is.
    private VoxelType stored = VoxelType.BLOCKSTATE;
    private HashMap<Integer, Pair<VoxelWrapper, Long>> contents;

    //Gets the amount of slots this bit storage has
    @Override
    public int getSlots() {
        switch(stored) {
            case BLOCKSTATE: return ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBag.get();
            case FLUIDSTATE: return ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBeaker.get();
            case COLOURED: return ChiselsAndBits2.getInstance().getConfig().bookmarksPerPalette.get();
            default: return 8;
        }
    }

    @Override
    public int getOccupiedSlotCount() {
        int count = 0;
        for(int i = 0; i < getSlots(); i++)
            if(contents.get(i) != null)
                count++;
        return count;
    }

    //Get the maximal capacity in a single slot.
    private long getMaxCapacity() {
        return ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get();
    }

    @Override
    public int findSlot(VoxelWrapper w) {
        for(int i = 0; i < getSlots(); i++) {
            if(contents.get(i) == null) return i;

            VoxelWrapper a = contents.get(i).getLeft();
            if(!a.equals(w)) continue;
            return i;
        }
        return -1;
    }

    public BitStorageImpl() {
        contents = new HashMap<>();
        for(int i = 0; i < getSlots(); i++)
            contents.put(0, null);
    }

    private void checkServerside() {
        if(Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER)
            throw new RuntimeException("Code ran on client-side whilst it has to be ran on server-side!");
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
    public void validate() {
        checkServerside();

        for(int i = 0; i < getSlots(); i++) {
            //Extend the contents if somehow the slots size changed. Remove empty slots.
            if(!contents.containsKey(i) || (contents.containsKey(i) && contents.get(i) != null && contents.get(i).getRight() <= 0))
                contents.put(i, null);
        }

        for(int i = 0; i < getSlots(); i++) {
            Pair<VoxelWrapper, Long> p = contents.get(i);
            if(p == null) continue;

            //Test if this is the only appearance of this voxel wrapper
            for(int j = 0; j < getSlots(); j++) {
                if(i == j) continue;
                Pair<VoxelWrapper, Long> pa = contents.get(j);
                if(pa != null) {
                    if(pa.getLeft().equals(p.getLeft())) {
                        //We need to fix this! Merge the entries.
                        contents.put(j, null);
                        p = Pair.of(p.getLeft(), p.getValue() + pa.getRight());
                    }
                }
            }
            contents.put(i, p);
        }
    }

    @Override
    public long add(final VoxelWrapper w, final long amount) {
        checkServerside();

        int slot = findSlot(w);
        if(slot < 0 || slot > contents.size()) return 0;
        Pair<VoxelWrapper, Long> pair = contents.get(slot);
        if(pair == null) pair = Pair.of(w, 0L);

        long available = getMaxCapacity() - pair.getRight();
        long use = Math.min(amount, available);
        //Limit max that can be removed.
        if(-use > get(w)) use = -get(w);
        //Special case for negative amounts to make sure they don't make negative amounts
        if(pair.getRight() + use < 0)
            use = -pair.getRight();

        pair = Pair.of(pair.getLeft(), pair.getRight() + use);
        contents.put(slot, pair.getValue() <= 0 ? null : pair);

        return amount - use;
    }

    @Override
    public void set(final VoxelWrapper w, final long amount) {
        checkServerside();

        int slot = findSlot(w);
        if(slot < 0 || slot > contents.size()) return;
        Pair<VoxelWrapper, Long> pair = contents.get(slot);
        if(pair == null) pair = Pair.of(w, 0L);
        pair = Pair.of(pair.getLeft(), amount);
        contents.put(slot, pair.getValue() <= 0 ? null : pair);
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
        if(slot < 0 || slot > contents.size()) return 0;
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
    public VoxelWrapper getSlotContent(int slot) {
        if(slot < 0 || slot > contents.size()) return VoxelWrapper.empty();
        Pair<VoxelWrapper, Long> p = contents.get(slot);
        if(p != null) return p.getLeft();
        return VoxelWrapper.empty();
    }

    @Override
    public int getSlot(final VoxelWrapper w) {
        int i = findSlot(w);
        if(i < 0 || i > contents.size()) return 0;
        if(contents.get(i) == null) return -1;
        return i;
    }

    @Override
    public void setSlot(final int index, final VoxelWrapper w, final long amount) {
        if(index < 0 || index > contents.size()) return;
        contents.put(index, Pair.of(w, amount));
    }

    @Override
    public void clearSlot(final int index) {
        if(index < 0 || index > contents.size()) return;
        contents.put(index, null);
    }
}

