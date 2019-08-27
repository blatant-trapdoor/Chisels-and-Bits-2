package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Blocks;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ItemModeType {
    //Type names must be identical to the startsWith() of the ItemMode!
    CHISEL,
    PATTERN,
    TAPEMEASURE,
    WRENCH,
    BLUEPRINT,
    MALLET,

    SELECTED_BLOCK
    ;

    /**
     * Get all item modes associated with this type.
     */
    public Set<IItemMode> getItemModes() {
        if(this==SELECTED_BLOCK) return new HashSet<>();
        return Stream.of(ItemMode.values()).filter(f -> f.name().startsWith(name())).collect(Collectors.toSet());
    }

    /**
     * Get all item modes associated with this type.
     * Keep them sorted the same way they are in the enum is.
     */
    public List<IItemMode> getSortedItemModes() {
        if(this==SELECTED_BLOCK) {
            ArrayList<IItemMode> sorted = new ArrayList<>();
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.STONE));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.CYAN_CONCRETE));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.RED_SAND));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.LIME_CONCRETE_POWDER));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.STRIPPED_ACACIA_WOOD));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.CRAFTING_TABLE));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.BIRCH_LOG));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.MAGENTA_WOOL));
            sorted.add(SelectedBlockItemMode.fromBlock(Blocks.WET_SPONGE));
            for(int j = 9; j < ChiselsAndBits2.getConfig().typeSlotsPerBag.get(); j++) {
               sorted.add(SelectedBlockItemMode.NONE); //Fill up remaining slots with the none slot.
            }
            return sorted;
        }
        return Stream.of(ItemMode.values()).filter(f -> f.name().startsWith(name())).collect(Collectors.toList());
    }
}
