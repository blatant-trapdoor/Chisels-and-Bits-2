package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import nl.dgoossens.chiselsandbits2.api.ItemMode;
import nl.dgoossens.chiselsandbits2.api.MenuAction;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ModKeybindings {
    private static final IKeyConflictContext CONFLICT = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return true; //TODO make isActive only true if the item is held that is required for the keybinding
        }
        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true; //For some reason the keybinds don't trigger unless this is true.
        }
    };
    private static final String CATEGORY = "Chisels & Bits 2 - General";
    private static final String HOTKEYS = "Chisels & Bits 2 - Hotkeys";

    private static final InputMappings.Input NONE = InputMappings.INPUT_INVALID;

    public KeyBinding
            selectBitType = new KeyBinding("Select Bit Type", CONFLICT, InputMappings.Type.MOUSE.getOrMakeInput(2), CATEGORY), //Middle Mouseclick
            offgridPlacement = new KeyBinding("Offgrid Placement", CONFLICT, KeyModifier.NONE, getKey(340), CATEGORY), //Left Shift
            modeMenu = new KeyBinding("Radial Menu", CONFLICT, KeyModifier.NONE, getKey(342), CATEGORY), //Left Alt
            copyPattern = new KeyBinding("Copy Pattern", CONFLICT, KeyModifier.SHIFT, InputMappings.Type.MOUSE.getOrMakeInput(1), CATEGORY), //Shift Rightclick
            clearTapeMeasure = new KeyBinding("Clear Measurements", CONFLICT, KeyModifier.SHIFT, InputMappings.Type.MOUSE.getOrMakeInput(0), CATEGORY) //Shift Leftclick
            ;

    public Map<MenuAction, KeyBinding> actionHotkeys = new HashMap<>();
    public Map<ItemMode, KeyBinding> modeHotkeys = new HashMap<>();

    static InputMappings.Input getKey(int key) { return InputMappings.Type.KEYSYM.getOrMakeInput(key); }

    public ModKeybindings() {
        //Generate Hotkeys
        for(MenuAction ma : MenuAction.values())
            if(ma.hasHotkey())
                actionHotkeys.put(ma, new KeyBinding("general.chiselsandbits2.menuaction."+ma.name().toLowerCase()+".hotkey", CONFLICT, NONE, HOTKEYS));

        for(ItemMode im : ItemMode.values())
            if(im.hasHotkey())
                modeHotkeys.put(im, new KeyBinding("general.chiselsandbits2.itemmode."+im.getTypelessName().toLowerCase()+".hotkey", CONFLICT, NONE, HOTKEYS));

        //Register Everything
        Stream.of(getClass().getFields())
                .filter(f -> KeyBinding.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        return (KeyBinding) f.get(this);
                    } catch(Exception x) { x.printStackTrace(); }
                    return null;
                })
                .forEach(ClientRegistry::registerKeyBinding);
        actionHotkeys.values().forEach(ClientRegistry::registerKeyBinding);
        modeHotkeys.values().forEach(ClientRegistry::registerKeyBinding);
    }
}
