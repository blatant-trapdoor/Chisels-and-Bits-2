package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.items.BitBagItem;
import nl.dgoossens.chiselsandbits2.common.items.BitBeakerItem;
import nl.dgoossens.chiselsandbits2.common.items.PaletteItem;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BitStorageImpl implements BitStorage {
    protected IndexedHashMap<Block, Long> blocks = new IndexedHashMap<>();
    protected IndexedHashMap<Fluid, Long> fluids = new IndexedHashMap<>();
    protected List<Color> bookmarks = new ArrayList<>();

    private List<IItemMode> selectedCache = null;

    public int getBlockIndex(Block blk) {
        int i = 0;
        for(Block b : blocks.keySet()) {
            if(blk.equals(b)) return i;
            i++;
        }
        return 0;
    }

    public Block getBlock(int index) {
        return blocks.getAt(index);
    }

    public int getFluidIndex(Fluid blk) {
        int i = 0;
        for(Fluid b : fluids.keySet()) {
            if(blk.equals(b)) return i;
            i++;
        }
        return 0;
    }

    public Fluid getFluid(int index) {
        return fluids.getAt(index);
    }

    public List<IItemMode> listTypesAsItemModes(Item item) {
        boolean block = item instanceof BitBagItem,
                fluid = item instanceof BitBeakerItem,
                colour = item instanceof PaletteItem;

        if (selectedCache == null) {
            selectedCache = new ArrayList<>();
            if (block) {
                for(Block b : blocks.keySet())
                    selectedCache.add(SelectedItemMode.fromBlock(b));
            }
            if (fluid) {
                for(Fluid f : fluids.keySet())
                    selectedCache.add(SelectedItemMode.fromFluid(f));
            }
            if (colour) {
                for(Color c : bookmarks)
                    selectedCache.add(SelectedItemMode.fromColour(c));
            }

            //Fill up remaining slots with the none slot.
            for (int j = selectedCache.size(); j < (block ? ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBag.get() : fluid ? ChiselsAndBits2.getInstance().getConfig().typeSlotsPerBeaker.get() : ChiselsAndBits2.getInstance().getConfig().bookmarksPerPalette.get()); j++) {
                if (block) selectedCache.add(SelectedItemMode.NONE_BAG);
                if (fluid) selectedCache.add(SelectedItemMode.NONE_BEAKER);
                if (colour) selectedCache.add(SelectedItemMode.NONE_BOOKMARK);
            }
        }
        return selectedCache;
    }

    public List<Block> listBlocks() {
        return blocks.keySet();
    }

    public List<Fluid> listFluids() {
        return fluids.keySet();
    }

    public List<Color> listColours() {
        return bookmarks;
    }

    public long getAmount(final Block type) {
        return blocks.getMap().getOrDefault(type, 0L);
    }

    public void setAmount(final Block type, final long amount) {
        blocks.add(type, Math.max(0, Math.min(amount, ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get())));
        selectedCache = null;
    }

    public long getAmount(final Fluid type) {
        return fluids.getMap().getOrDefault(type, 0L);
    }

    public void setAmount(final Fluid type, final long amount) {
        fluids.add(type, Math.max(0, Math.min(amount, ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get())));
        selectedCache = null;
    }

    public void addBookmark(final Color color) {
        bookmarks.add(color);
    }

    public void setBookmark(final int index, final Color color) {
        bookmarks.set(index, color);
    }

    public void clearBookmark(final int index) {
        bookmarks.remove(index);
    }
}
