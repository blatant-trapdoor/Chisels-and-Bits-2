package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.stream.Stream;

public class ModKeybindings {
    private static final IKeyConflictContext CONFLICT = new IKeyConflictContext() {
        //None of our keybinds ever conflict with anything else, so we do this to stop those pesky red markings.
        @Override
        public boolean isActive() {
            return true;
        }
        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true; //For some reason true means that everything is fine. Instead of true meaning there's a conflict, alright.
        }
    };
    private static final String CATEGORY = "Chisels & Bits 2";

    public KeyBinding
            undo = new KeyBinding("Undo", CONFLICT, KeyModifier.CONTROL, getKey(90), CATEGORY), //Ctrl+Z
            redo = new KeyBinding("Redo", CONFLICT, KeyModifier.CONTROL, getKey(89), CATEGORY), //Ctrl+Y
            offgridPlacement = new KeyBinding("Offgrid Placement", CONFLICT, KeyModifier.NONE, getKey(340), CATEGORY), //Left Shift
            modeMenu = new KeyBinding("Menu", CONFLICT, KeyModifier.NONE, getKey(342), CATEGORY), //Left Alt
            copyPattern = new KeyBinding("Copy Pattern", CONFLICT, KeyModifier.SHIFT, InputMappings.Type.MOUSE.getOrMakeInput(1), CATEGORY), //Shift Rightclick
            clearTapeMeasure = new KeyBinding("Clear Measurements", CONFLICT, KeyModifier.SHIFT, InputMappings.Type.MOUSE.getOrMakeInput(0), CATEGORY) //Shift Leftclick
            ;

    static InputMappings.Input getKey(int key) { return InputMappings.Type.KEYSYM.getOrMakeInput(key); }

    public ModKeybindings() {
        Stream.of(getClass().getFields())
                .filter(f -> KeyBinding.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        return (KeyBinding) f.get(this);
                    } catch(Exception x) { x.printStackTrace(); }
                    return null;
                })
                .forEach(ClientRegistry::registerKeyBinding);
    }
}
