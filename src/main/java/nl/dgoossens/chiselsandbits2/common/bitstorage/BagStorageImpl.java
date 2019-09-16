package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.block.Block;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BagStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedBlockItemMode;

import java.util.*;

public class BagStorageImpl implements BagStorage {
    protected Map<Block, Long> content = new HashMap<>();
    private List<IItemMode> selectedCache = null;

    public List<IItemMode> listTypesAsItemModes() {
        if(selectedCache==null) {
            selectedCache = new ArrayList<>();
            listTypes().forEach(f ->
                selectedCache.add(SelectedBlockItemMode.fromBlock(f))
            );
            selectedCache.sort(Comparator.comparing(f -> f instanceof SelectedBlockItemMode ? f.getName() : ""));
            for(int j = selectedCache.size(); j < ChiselsAndBits2.getConfig().typeSlotsPerBag.get(); j++)
                selectedCache.add(SelectedBlockItemMode.NONE_BAG); //Fill up remaining slots with the none slot.
        }
        return selectedCache;
    }
    public Set<Block> listTypes() { return content.keySet(); }
    public long getAmount(final Block type) { return content.get(type); }
    public void setAmount(final Block type, final long amount) { content.put(type, amount); selectedCache=null; }
}
