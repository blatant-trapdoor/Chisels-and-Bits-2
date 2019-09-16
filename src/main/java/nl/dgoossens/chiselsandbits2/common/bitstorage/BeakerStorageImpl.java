package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.fluid.Fluid;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BeakerStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedBlockItemMode;

import java.util.*;

public class BeakerStorageImpl implements BeakerStorage {
    protected Map<Fluid, Long> content = new HashMap<>();
    private List<IItemMode> selectedCache = null;

    public List<IItemMode> listTypesAsItemModes() {
        if(selectedCache==null) {
            selectedCache = new ArrayList<>();
            listTypes().forEach(f ->
                selectedCache.add(SelectedBlockItemMode.fromFluid(f))
            );
            selectedCache.sort(Comparator.comparing(f -> f instanceof SelectedBlockItemMode ? f.getName() : ""));
            for(int j = selectedCache.size(); j < ChiselsAndBits2.getConfig().typeSlotsPerBag.get(); j++)
                selectedCache.add(SelectedBlockItemMode.NONE_BAG); //Fill up remaining slots with the none slot.
        }
        return selectedCache;
    }
    public Set<Fluid> listTypes() { return content.keySet(); }
    public long getAmount(final Fluid type) { return content.get(type); }
    public void setAmount(final Fluid type, final long amount) { content.put(type, amount); selectedCache=null; }
}
