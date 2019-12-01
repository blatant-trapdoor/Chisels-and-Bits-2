package nl.dgoossens.chiselsandbits2.common.impl;

import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.IMenuAction;
import nl.dgoossens.chiselsandbits2.api.item.ItemModeEnum;
import nl.dgoossens.chiselsandbits2.api.item.ItemPropertyAPI;

import java.util.HashSet;
import java.util.Set;

public class ItemPropertyAPIImpl implements ItemPropertyAPI {
    private Set<ItemModeEnum> itemModes = new HashSet<>();
    private Set<IItemModeType> itemModeTypes = new HashSet<>();
    private Set<IMenuAction> menuActions = new HashSet<>();

    @Override
    public void registerMode(ItemModeEnum itemMode) {
        itemModes.add(itemMode);
    }

    @Override
    public void registerModeType(IItemModeType itemModeType) {
        itemModeTypes.add(itemModeType);
    }

    @Override
    public void registerMenuAction(IMenuAction menuAction) {
        menuActions.add(menuAction);
    }

    @Override
    public Set<IItemModeType> getModeTypes() {
        return itemModeTypes;
    }

    @Override
    public Set<ItemModeEnum> getModes() {
        return itemModes;
    }

    @Override
    public Set<IMenuAction> getMenuActions() {
        return menuActions;
    }
}
