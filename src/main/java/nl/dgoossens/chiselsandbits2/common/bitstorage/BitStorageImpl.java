package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BitStorageImpl implements BitStorage {
    protected LinkedHashMap<Block, Long> blocks = new LinkedHashMap<>();
    protected LinkedHashMap<Fluid, Long> fluids = new LinkedHashMap<>();
    protected List<Color> bookmarks = new ArrayList<>();

    private List<IItemMode> selectedCache = null;

    public List<IItemMode> listTypesAsItemModes() {
        if(selectedCache==null) {
            selectedCache = new ArrayList<>();
            if(!blocks.isEmpty()) {
                blocks.keySet().forEach(f ->
                        selectedCache.add(SelectedItemMode.fromBlock(f))
                );
            }
            if(!fluids.isEmpty()) {
                fluids.keySet().forEach(f ->
                        selectedCache.add(SelectedItemMode.fromFluid(f))
                );
            }
            if(!bookmarks.isEmpty()) {
                bookmarks.forEach(f ->
                        selectedCache.add(SelectedItemMode.fromColour(f)));
            }

            //Fill up remaining slots with the none slot.
            for(int j = selectedCache.size(); j < ChiselsAndBits2.getConfig().typeSlotsPerBag.get(); j++) {
                if(!blocks.isEmpty()) selectedCache.add(SelectedItemMode.NONE_BAG);
                if(!fluids.isEmpty()) selectedCache.add(SelectedItemMode.NONE_BEAKER);
                if(!bookmarks.isEmpty()) selectedCache.add(SelectedItemMode.NONE_BOOKMARK);
            }
        }
        return selectedCache;
    }
    public List<Block> listBlocks() { return new ArrayList<>(blocks.keySet()); }
    public List<Fluid> listFluids() { return new ArrayList<>(fluids.keySet()); }
    public List<Color> listColours() { return bookmarks; }
    public long getAmount(final Block type) { return blocks.get(type); }
    public void setAmount(final Block type, final long amount) { blocks.put(type, amount); selectedCache=null; }
    public long getAmount(final Fluid type) { return fluids.get(type); }
    public void setAmount(final Fluid type, final long amount) { fluids.put(type, amount); selectedCache=null; }
    public void addBookmark(final Color color) { bookmarks.add(color); }
    public void setBookmark(final int index, final Color color) { bookmarks.set(index, color); }
    public void clearBookmark(final int index) { bookmarks.remove(index); }
}
