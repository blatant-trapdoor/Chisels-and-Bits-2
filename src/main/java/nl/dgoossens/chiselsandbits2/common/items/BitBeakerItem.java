package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BeakerCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.registry.ModItemGroups;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class BitBeakerItem extends TypedItem {
    public BitBeakerItem() {super(new Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2)); }

    @Override
    public ItemModeType getAssociatedType() {
        return ItemModeType.SELECTED_FLUID;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "bit_beaker.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getKeybindings().scoopFluid,
                ChiselsAndBits2.getKeybindings().modeMenu);
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, final CompoundNBT nbt) {
        return new BeakerCapabilityProvider();
    }
}
