package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.IMenuAction;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

public enum MenuAction implements IMenuAction {
    PLACE,
    SWAP,

    UNDO,
    REDO,

    ROLL_X,
    ROLL_Z,

    WHITE(16383998),
    BLACK(1908001),
    CYAN(1481884),
    LIGHT_BLUE(3847130),
    YELLOW(16701501),
    PINK(15961002),
    GRAY(4673362),
    BROWN(8606770),
    LIGHT_GRAY(10329495),
    RED(11546150),
    MAGENTA(13061821),
    ORANGE(16351261),
    LIME(8439583),
    PURPLE(8991416),
    BLUE(3949738),
    GREEN(6192150);

    private int colour = 0;

    MenuAction() {
        this(0);
    }

    MenuAction(int col) {
        colour = col;
        ChiselsAndBits2.getInstance().getAPI().getItemPropertyRegistry().registerMenuAction(this);
    }
    
    public int getColour() {
        return colour;
    }

    @Override
    public boolean hasIcon() {
        switch(this) {
            case PLACE:
            case SWAP:
            case UNDO:
            case REDO:
            case ROLL_X:
            case ROLL_Z:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean hasHotkey() {
        //Tape measure colours do not have hotkeys.
        return colour == 0;
    }

    @Override
    public void trigger() {
        switch (this) {
            case UNDO:
                ChiselsAndBits2.getInstance().getClient().getUndoTracker().undo();
                break;
            case REDO:
                ChiselsAndBits2.getInstance().getClient().getUndoTracker().redo();
                break;
            case ROLL_X:
                System.out.println("ROLL_X");
                break;
            case ROLL_Z:
                System.out.println("ROLL_Z");
                break;
            case PLACE:
            case SWAP:
                //Swap to the other one.
                ItemModeUtil.changeMenuActionMode(Minecraft.getInstance().player, this.equals(MenuAction.PLACE) ? MenuAction.SWAP : MenuAction.PLACE);
                break;
            case BLACK:
            case WHITE:
            case BLUE:
            case BROWN:
            case CYAN:
            case GRAY:
            case GREEN:
            case LIGHT_BLUE:
            case LIGHT_GRAY:
            case LIME:
            case MAGENTA:
            case ORANGE:
            case PINK:
            case PURPLE:
            case RED:
            case YELLOW:
                //Change the colour of the tape measure.
                ItemModeUtil.changeMenuActionMode(Minecraft.getInstance().player, this);
                break;
        }
    }
}
