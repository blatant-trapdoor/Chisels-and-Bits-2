package nl.dgoossens.chiselsandbits2.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.bit.BitStorage;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BagContainer;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;

public class BitBagScreen extends ContainerScreen<BagContainer> {
    private final PlayerEntity player;
    private final ItemStack item;

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ChiselsAndBits2.MOD_ID, "textures/gui/bit_bag.png"); //TODO currently identical to chest
    private final int inventorySlots;

    public BitBagScreen(BagContainer container, PlayerEntity p, ItemStack i) {
        super(container, p.inventory, NarratorChatListener.field_216868_a); //Narrator need not read up a message when we open it.
        player = p;
        item = i;
        inventorySlots = i.getCapability(StorageCapabilityProvider.STORAGE).map(BitStorage::getSlots).orElse(0);
        this.ySize = 114 + getRowCount() * 18;
    }

    private int getRowCount() {
        return this.inventorySlots / 9 + 1;
    }

    @Override
    public void render(int mouseX, int mouseY, float mouseZ) {
        this.renderBackground();
        super.render(mouseX, mouseY, mouseZ);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getFormattedText(), 8.0F, 6.0F, 4210752);
        this.font.drawString(this.item.getDisplayName().getFormattedText(), 8.0F, (float)(this.ySize - 96 + 2), 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, getRowCount() * 18 + 17);
        this.blit(i, j + getRowCount() * 18 + 17, 0, 126, this.xSize, 96);
    }
}
