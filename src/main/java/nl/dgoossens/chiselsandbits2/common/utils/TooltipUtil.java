package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import java.util.List;

public class TooltipUtil {
    /**
     * Automatically builds the tooltip of an item by parsing the language entry.
     * ; -> new line
     * $ -> next keybinding name
     */
    public static void addItemInformation(List<ITextComponent> textComponents, String languageKey, KeyBinding... keys) {
        String fullString = I18n.format(getLanguagePrefix("item")+languageKey);
        StringBuilder past = new StringBuilder();
        int j = 0;
        for(char c : fullString.toCharArray()) {
            if(c==';') {
                //New line
                StringTextComponent st = new StringTextComponent(past.toString());
                st.getStyle().setColor(TextFormatting.GRAY);
                textComponents.add(st);
                past = new StringBuilder();
                continue;
            }
            if(c=='$') {
                //Next keybinding name
                if(keys.length==j) continue; //If we got no more keybinds we can't put them in.
                past.append(getKeyName(keys[j]).getFormattedText());
                j++;
                continue;
            }
            past.append(c);
        }
        StringTextComponent st = new StringTextComponent(past.toString());
        st.getStyle().setColor(TextFormatting.GRAY);
        textComponents.add(st);
    }

    /**
     * Gets the prefix for in language files.
     * @param start The first word, e.g. if "item" then this method will return
     *              "item.chiselsandbits2."
     */
    public static String getLanguagePrefix(String start) { return start+"."+ChiselsAndBits2.MOD_ID+"."; }

    /**
     * Gets the showable display name for a keybind for item tooltips or other places.
     */
    public static ITextComponent getKeyName(final KeyBinding keyBinding) {
        if(keyBinding==null) return new TranslationTextComponent(getLanguagePrefix("general")+"no_keybinding");
        if(keyBinding.getKey().getKeyCode()==0 && keyBinding.getDefault().getKeyCode()!=0) return new TranslationTextComponent(keyBinding.getDefault().getTranslationKey());
        return new TranslationTextComponent(keyBinding.getTranslationKey());
    }
}
