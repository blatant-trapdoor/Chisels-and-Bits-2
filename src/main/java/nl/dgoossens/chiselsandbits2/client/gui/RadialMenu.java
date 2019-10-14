package nl.dgoossens.chiselsandbits2.client.gui;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.client.ClientSide;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.StorageItem;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RadialMenu extends Screen {
    private final static float TIME_SCALE = 0.01f;

    private final static double RING_INNER_EDGE = 20;
    private final static double RING_OUTER_EDGE = 55;
    private final static double TEXT_DISTANCE = 65;
    private final static double HALF_PI = Math.PI * 0.5;

    private float visibility = 0.0f;
    private Stopwatch lastChange = Stopwatch.createStarted();
    private IItemMode switchTo = null;
    private MenuAction doAction = null;
    private List<MenuRegion> modes;
    private List<MenuButton> buttons;
    private boolean actionUsed = false;
    private boolean gameFocused = true;
    private boolean pressedButton = false;
    private boolean clicked = false;
    private MainWindow window;
    private long buttonLastHighlighted = 0L;

    public RadialMenu() {
        super(new StringTextComponent("Radial Menu"));
        minecraft = Minecraft.getInstance();
        if(minecraft != null) font = Minecraft.getInstance().fontRenderer;
    }

    public void updateGameFocus() {
        //Switch the game focus state if the game is focus doesn't match the visibility.
        if ((gameFocused && isVisible()) || (!gameFocused && !isVisible())) {
            gameFocused = !gameFocused;
            getMinecraft().setGameFocused(gameFocused);
            if (!gameFocused) {
                getMinecraft().mouseHelper.ungrabMouse();
                if (ChiselsAndBits2.getInstance().getConfig().enableVivecraftCompatibility.get()) getMinecraft().currentScreen = this;
            } else {
                getMinecraft().mouseHelper.grabMouse();
                if (ChiselsAndBits2.getInstance().getConfig().enableVivecraftCompatibility.get())
                    getMinecraft().displayGuiScreen(null);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!(getMinecraft().player.getHeldItemMainhand().getItem() instanceof IItemMenu)) return;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0F, 0.0F, 200.0F);

        final int start = (int) (visibility * 98) << 24;
        final int end = (int) (visibility * 128) << 24;
        fillGradient(0, 0, window.getWidth(), window.getHeight(), start, end);

        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        final double middle_x = ((double) window.getScaledWidth()) / 2;
        final double middle_y = ((double) window.getScaledHeight()) / 2;

        switchTo = null;
        doAction = null;
        modes = getShownModes();
        buttons = getShownButtons();

        //Set the invisibility for all rendered bars.
        getItemRenderer().setAlpha(visibility * 0.65f);

        renderBackgrounds(mouseX, mouseY, middle_x, middle_y, buffer);
        //Render flat coloured parts of icons and overlays.
        renderIcons(middle_x, middle_y, buffer, false);
        renderOverlay(middle_x, middle_y, buffer);
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.translatef(0.0F, 0.0F, 5.0F);
        GlStateManager.color4f(1, 1, 1, 1.0f);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.bindTexture(Minecraft.getInstance().getTextureMap().getGlTextureId());

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        renderIcons(middle_x, middle_y, buffer, true);
        tessellator.draw();

        //Render the overlays, there's no buffer here. This renders the texts and itemstacks.
        renderOverlay(middle_x, middle_y, null);

        if(getMinecraft().player.getHeldItemMainhand().getItem() instanceof StorageItem)
            renderCapacityBars(middle_x, middle_y);

        GlStateManager.popMatrix();
    }

    /**
     * Renders the semi-transparent backgrounds behind the buttons and regions.
     */
    private void renderBackgrounds(double mouseX, double mouseY, double middle_x, double middle_y, BufferBuilder buffer) {
        final double vecX = mouseX - middle_x;
        final double vecY = mouseY - middle_y;

        double radians = Math.atan2(vecY, vecX);
        if (radians < -HALF_PI)
            radians = radians + Math.PI * 2;

        if (!modes.isEmpty()) {
            final int totalModes = Math.max(3, modes.size());
            int currentMode = 0;
            final double perObject = 2.0 * Math.PI / totalModes;

            for (final MenuRegion mnuRgn : modes) {
                final double begin_rad = currentMode * perObject - HALF_PI;
                final double end_rad = (currentMode + 1) * perObject - HALF_PI;

                mnuRgn.x1 = Math.cos(begin_rad);
                mnuRgn.x2 = Math.cos(end_rad);
                mnuRgn.y1 = Math.sin(begin_rad);
                mnuRgn.y2 = Math.sin(end_rad);


                double fragment = Math.PI * 0.005;
                double fragment2 = Math.PI * 0.0025;

                final float a = 0.5f * visibility;
                float f = 0f;

                //Set the colour.
                switch(mnuRgn.type) {
                    case SELECTED:
                        f = 1;
                        fragment = Math.PI * 0.002;
                        fragment2 = Math.PI * 0.0005;
                        break;
                    case HIGHLIGHTED:
                        f = 0.8f;
                        break;
                }

                double x1m1 = Math.cos(begin_rad + fragment) * RING_INNER_EDGE;
                double x2m1 = Math.cos(end_rad - fragment) * RING_INNER_EDGE;
                double y1m1 = Math.sin(begin_rad + fragment) * RING_INNER_EDGE;
                double y2m1 = Math.sin(end_rad - fragment) * RING_INNER_EDGE;

                double x1m2 = Math.cos(begin_rad + fragment2) * RING_OUTER_EDGE;
                double x2m2 = Math.cos(end_rad - fragment2) * RING_OUTER_EDGE;
                double y1m2 = Math.sin(begin_rad + fragment2) * RING_OUTER_EDGE;
                double y2m2 = Math.sin(end_rad - fragment2) * RING_OUTER_EDGE;

                final boolean quad = inTriangle(x1m1, y1m1, x2m2, y2m2, x2m1, y2m1, vecX, vecY) || inTriangle(x1m1, y1m1, x1m2, y1m2, x2m2, y2m2, vecX, vecY);
                if (begin_rad <= radians && radians <= end_rad && quad) {
                    if(f < 0.8f) f = 0.8f;
                    mnuRgn.highlighted = true;
                    switchTo = mnuRgn.mode;
                }

                if(mnuRgn.mode.getType() == ItemModeType.SELECTED_BOOKMARK) {
                    if(f < 0.1f) f = 0.4f;
                    Color color = ((SelectedItemMode) mnuRgn.mode).getColour();
                    int red = (int) Math.round(color.getRed()*f), green = (int) Math.round(color.getGreen()*f), blue = (int) Math.round(color.getBlue()*f), alp = (int) Math.round(color.getAlpha()*a);

                    buffer.pos(middle_x + x1m1, middle_y + y1m1, blitOffset).color(red, green, blue ,alp).endVertex();
                    buffer.pos(middle_x + x2m1, middle_y + y2m1, blitOffset).color(red, green, blue ,alp).endVertex();
                    buffer.pos(middle_x + x2m2, middle_y + y2m2, blitOffset).color(red, green, blue ,alp).endVertex();
                    buffer.pos(middle_x + x1m2, middle_y + y1m2, blitOffset).color(red, green, blue ,alp).endVertex();
                } else {
                    buffer.pos(middle_x + x1m1, middle_y + y1m1, blitOffset).color(f, f, f, a).endVertex();
                    buffer.pos(middle_x + x2m1, middle_y + y2m1, blitOffset).color(f, f, f, a).endVertex();
                    buffer.pos(middle_x + x2m2, middle_y + y2m2, blitOffset).color(f, f, f, a).endVertex();
                    buffer.pos(middle_x + x1m2, middle_y + y1m2, blitOffset).color(f, f, f, a).endVertex();
                }

                currentMode++;
            }
        }

        for (final MenuButton btn : buttons) {
            final float a = 0.5f * visibility;
            float f = 0f;

            if (btn.x1 <= vecX && btn.x2 >= vecX && btn.y1 <= vecY && btn.y2 >= vecY) {
                f = 1;
                btn.highlighted = true;
                doAction = btn.action;
            }

            buffer.pos(middle_x + btn.x1, middle_y + btn.y1, blitOffset).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + btn.x1, middle_y + btn.y2, blitOffset).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + btn.x2, middle_y + btn.y2, blitOffset).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + btn.x2, middle_y + btn.y1, blitOffset).color(f, f, f, a).endVertex();
        }
    }

    /**
     * Renders the icons as retrieved from {@link ClientSide#getIconForAction(MenuAction)} or {@link ClientSide#getIconForMode(IItemMode)}.
     * Called both before and after textures are enabled for rendering the flat colours.
     */
    private void renderIcons(double middle_x, double middle_y, BufferBuilder buffer, boolean textures) {
        for (final MenuRegion mnuRgn : modes) {
            if(!textures) //Menu regions don't render wihout textures.
                continue;

            final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);
            final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);

            final SpriteIconPositioning sip = ClientSide.getIconForMode(mnuRgn.mode);
            if (sip == null)
                continue;

            final double scalex = 15 * sip.width * 0.5;
            final double scaley = 15 * sip.height * 0.5;
            final double x1 = x - scalex;
            final double x2 = x + scalex;
            final double y1 = y - scaley;
            final double y2 = y + scaley;

            final TextureAtlasSprite sprite = sip.sprite;

            final float f = 1.0f;
            final float a = 1.0f * visibility;

            final double u1 = sip.left * 16.0;
            final double u2 = (sip.left + sip.width) * 16.0;
            final double v1 = sip.top * 16.0;
            final double v2 = (sip.top + sip.height) * 16.0;

            buffer.pos(middle_x + x1, middle_y + y1, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x1, middle_y + y2, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x2, middle_y + y2, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x2, middle_y + y1, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
        }

        for (final MenuButton btn : buttons) {
            //Depending on whether we want textures or not, continue.
            if(btn.icon == null && textures) continue;
            if(btn.icon != null && !textures) continue;

            final float a = 0.8f * visibility;

            final double u1 = 0;
            final double u2 = 16;
            final double v1 = 0;
            final double v2 = 16;

            final double btnx1 = btn.x1 + 1;
            final double btnx2 = btn.x2 - 1;
            final double btny1 = btn.y1 + 1;
            final double btny2 = btn.y2 - 1;

            final float red = ((btn.color >> 16 & 0xff) / 255.0f);
            final float green = ((btn.color >> 8 & 0xff) / 255.0f);
            final float blue = ((btn.color & 0xff) / 255.0f);

            if (btn.icon != null) {
                TextureAtlasSprite sprite = btn.icon.sprite;
                buffer.pos(middle_x + btnx1, middle_y + btny1, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx1, middle_y + btny2, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny2, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny1, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
            } else {
                if(btn.color == 0) //0 equals no colour.
                    continue;

                buffer.pos(middle_x + btnx1, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx1, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();
            }
        }
    }

    /**
     * Render the texts next to highlighted elements and the item renders for selected item modes.
     */
    private void renderOverlay(double middle_x, double middle_y, BufferBuilder buffer) {
        boolean buttonHighlighted = false;
        for (final MenuButton btn : buttons) {
            if(buffer != null)
                continue;

            if (btn.highlighted) {
                buttonHighlighted = true;
                final String text = btn.name;
                int c = new Color(1.0f, 1.0f, 1.0f, visibility).hashCode();
                if (btn.textSide == Direction.WEST) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + btn.x1 - 8) - getFontRenderer().getStringWidth(text), (int) (middle_y + btn.y1 + 6), c);
                } else if (btn.textSide == Direction.EAST) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + btn.x2 + 8), (int) (middle_y + btn.y1 + 6), c);
                } else if (btn.textSide == Direction.UP) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + (btn.x1 + btn.x2) * 0.5 - getFontRenderer().getStringWidth(text) * 0.5), (int) (middle_y + btn.y1 - 14), c);
                } else if (btn.textSide == Direction.DOWN) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + (btn.x1 + btn.x2) * 0.5 - getFontRenderer().getStringWidth(text) * 0.5), (int) (middle_y + btn.y1 + 24), c);
                }
            }
        }
        if(buttonHighlighted) buttonLastHighlighted = System.currentTimeMillis();
        //Nicely fade it back in when you release a button.
        int remove = (int) Math.round((212.0d-64.0d) * (1.0-(Math.min((double)(System.currentTimeMillis()-buttonLastHighlighted), 3000.0d) / 3000.0d)));

        final double w = 8;
        RenderHelper.enableGUIStandardItemLighting();
        for (final MenuRegion mnuRgn : modes) {
            if (buffer == null && (mnuRgn.highlighted || mnuRgn.type.isSelected())) {
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5;
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5;
                Color base = new Color(255, 255, 255);
                if(mnuRgn.mode.getType() == ItemModeType.SELECTED_BOOKMARK)
                    base = ((SelectedItemMode) mnuRgn.mode).getColour();
                int color = new Color(base.getRed(), base.getGreen(), base.getBlue(),(int) Math.round((212-(!mnuRgn.highlighted ? remove : 0))*visibility*(((double)base.getAlpha())/255))).hashCode();

                int fixed_x = (int) (x * TEXT_DISTANCE);
                final int fixed_y = (int) (y * TEXT_DISTANCE);
                final String text = mnuRgn.mode.getLocalizedName();

                if (x <= -0.2) {
                    fixed_x -= getFontRenderer().getStringWidth(text);
                } else if (-0.2 <= x && x <= 0.2) {
                    fixed_x -= getFontRenderer().getStringWidth(text) / 2;
                }

                GlStateManager.enableBlend();
                GlStateManager.disableAlphaTest();
                getFontRenderer().drawStringWithShadow(text, (int) middle_x + fixed_x, (int) middle_y + fixed_y, color);
            }
            if (mnuRgn.mode instanceof SelectedItemMode) {
                if (SelectedItemMode.isNone(mnuRgn.mode)) continue;

                //Selectable blocks should render the item that's inside!
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);
                if(mnuRgn.mode.getType() == ItemModeType.SELECTED_BOOKMARK) {
                    //We're currently not using this system.
                    /*if(buffer==null)
                        continue;

                    Color color = ((SelectedItemMode) mnuRgn.mode).getColour();
                    int red = color.getRed(), green = color.getGreen(), blue = color.getBlue(), a = (int) Math.round(color.getAlpha()*0.6f);
                    final double btnx1 = x-8 + 1;
                    final double btnx2 = x-8 + 16 - 1;
                    final double btny1 = y-8 + 1;
                    final double btny2 = y-8 + 16 - 1;

                    buffer.pos(middle_x + btnx1, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx1, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx2, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx2, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();*/
                } else {
                    if(buffer!=null)
                        continue;

                    //We use minecraft's (actually forge's) item renderer instead of our own here because we can't support alpha in item rendering anyways.
                    getMinecraft().getItemRenderer().renderItemIntoGUI(((SelectedItemMode) mnuRgn.mode).getStack(), (int) Math.round(middle_x + x - w), (int) Math.round(middle_y + y - w));
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    /**
     * Render durability bars showing how full slots are.
     */
    private void renderCapacityBars(double middle_x, double middle_y) {
        ItemStack stack = getMinecraft().player.getHeldItemMainhand();
        BitStorage store = stack.getCapability(StorageCapabilityProvider.STORAGE).orElse(null);
        if(store == null) return; //If it's null we can't do nothin.

        for (final MenuRegion mnuRgn : modes) {
            if (mnuRgn.mode instanceof SelectedItemMode) {
                SelectedItemMode s = (SelectedItemMode) mnuRgn.mode;
                //None or bookmarks don't have amounts.
                if (SelectedItemMode.isNone(s) || s.getType() == ItemModeType.SELECTED_BOOKMARK) continue;

                //Selectable blocks should render the item that's inside!
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (RING_OUTER_EDGE * 0.6 + 0.4 * RING_INNER_EDGE);
                getItemRenderer().renderDurabilityBar(ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get() - (s.getType() == ItemModeType.SELECTED_BLOCK ? store.getAmount(s.getBlock()) : store.getAmount(s.getFluid())),
                        ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get(), (int) Math.round(middle_x + x - 9), (int) Math.round(middle_y + y - 7));
            }
        }
    }

    private boolean inTriangle(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final double x, final double y) {
        final double ab = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
        final double bc = (x2 - x) * (y3 - y) - (x3 - x) * (y2 - y);
        final double ca = (x3 - x) * (y1 - y) - (x1 - x) * (y3 - y);
        return sign(ab) == sign(bc) && sign(bc) == sign(ca);
    }

    private int sign(final double n) {
        return n > 0 ? 1 : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0)
            setClicked(true);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private static class MenuButton {
        final MenuAction action;
        double x1, x2;
        double y1, y2;
        boolean highlighted;
        SpriteIconPositioning icon;
        int color;
        String name;
        Direction textSide;

        public MenuButton(final MenuAction action, final double x, final double y, final Direction textSide) {
            this(action, x, y, 0, textSide);
            this.icon = ClientSide.getIconForAction(action);
        }

        public MenuButton(final MenuAction action, final double x, final double y, final int col, final Direction textSide) {
            this.name = action.getLocalizedName();
            this.action = action;
            x1 = x;
            x2 = x + 18;
            y1 = y;
            y2 = y + 18;
            color = col;
            this.textSide = textSide;
        }
    }

    static enum RegionType {
        SELECTED, //For the currently selected mode.
        HIGHLIGHTED, //For selected but not used selected item mode
        DEFAULT,
        ;

        public boolean isSelected() {
            return this == SELECTED || this == HIGHLIGHTED;
        }
    }

    static class MenuRegion {
        public final IItemMode mode;
        public RegionType type;
        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        public MenuRegion(final IItemMode mode, final ItemStack stack, final PlayerEntity player) {
            this.mode = mode;
            SelectedItemMode s = ChiselModeManager.getSelectedItem(stack);
            if (s == null) type = ChiselModeManager.getMode(stack).equals(mode) ? RegionType.SELECTED : RegionType.DEFAULT;
            else {
                if(!s.equals(mode) || SelectedItemMode.isNone(mode)) {
                    type = RegionType.DEFAULT;
                    return;
                }
                int b = ChiselHandler.getSelectedBit(player, null);
                if(b != s.getBitId()) {
                    //Highlighted = selected in this bitstorage but won't be used atm to build.
                    type = RegionType.HIGHLIGHTED;
                } else
                    //Selected in this storage and going to get used.
                    type = RegionType.SELECTED;
            }
        }
    }

    /*@SubscribeEvent
    public static void onClickWithGuiOpen(InputEvent.MouseInputEvent e) {
        if(e.getButton() == GLFW.GLFW_MOUSE_BUTTON_1 && ChiselsAndBits2.getInstance().getClient().getRadialMenu().isVisible()) {
            ChiselsAndBits2.getInstance().getClient().getRadialMenu().setClicked(true);
            e.setCanceled(true);
        }
    }*/

    public List<MenuRegion> getShownModes() {
        List<MenuRegion> modes = new ArrayList<>();

        //Setup mode regions
        for (IItemMode m : ChiselModeManager.getMode(getMinecraft().player.getHeldItemMainhand()).getType().getItemModes(getMinecraft().player.getHeldItemMainhand()))
            modes.add(new MenuRegion(m, getMinecraft().player.getHeldItemMainhand(), getMinecraft().player));

        return modes;
    }

    public List<MenuButton> getShownButtons() {
        List<MenuButton> buttons = new ArrayList<>();

        //Setup buttons
        buttons.add(new MenuButton(MenuAction.UNDO, TEXT_DISTANCE, -20, Direction.EAST));
        buttons.add(new MenuButton(MenuAction.REDO, TEXT_DISTANCE, 4, Direction.EAST));

        ItemModeType tool = ChiselModeManager.getMode(getMinecraft().player.getHeldItemMainhand()).getType();
        if (tool == ItemModeType.PATTERN) {
            buttons.add(new MenuButton(MenuAction.ROLL_X, -TEXT_DISTANCE - 18, -33, Direction.WEST));
            buttons.add(new MenuButton(MenuAction.ROLL_Y, -TEXT_DISTANCE - 18, -9, Direction.WEST));
            buttons.add(new MenuButton(MenuAction.ROLL_Z, -TEXT_DISTANCE - 18, 15, Direction.WEST));
        }

        if (tool == ItemModeType.CHISEL) {
            buttons.add(new MenuButton(MenuAction.PLACE, -TEXT_DISTANCE - 18, -20, Direction.WEST));
            buttons.add(new MenuButton(MenuAction.REPLACE, -TEXT_DISTANCE - 18, 4, Direction.WEST));
        }

        if (tool == ItemModeType.TAPEMEASURE) {
            final int colorSize = DyeColor.values().length / 4 * 24 - 4;
            double underring = -RING_OUTER_EDGE - 34;
            double bntPos = -colorSize;
            final int bntSize = 24;
            Direction textSide = Direction.UP;
            for (final DyeColor color : DyeColor.values()) {
                final MenuAction action = MenuAction.valueOf(color.name());
                if (bntPos > colorSize) {
                    underring = RING_OUTER_EDGE;
                    bntPos = -colorSize;
                    textSide = Direction.DOWN;
                }
                buttons.add(new MenuButton(action, bntPos, underring, action.getColour(), textSide));
                bntPos += bntSize;
            }
        }
        return buttons;
    }

    public void configure(final MainWindow window) {
        this.window = window;
    }

    @Override
    public Minecraft getMinecraft() {
        return minecraft == null ? Minecraft.getInstance() : minecraft;
    }

    private CustomItemRenderer cache;
    public CustomItemRenderer getItemRenderer() {
        if(cache == null) cache = new CustomItemRenderer(Minecraft.getInstance().getItemRenderer());
        return cache;
    }

    public FontRenderer getFontRenderer() {
        return font == null ? Minecraft.getInstance().fontRenderer : font;
    }

    private float clampVis(final float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public void raiseVisibility() {
        visibility = clampVis(visibility + lastChange.elapsed(TimeUnit.MILLISECONDS) * TIME_SCALE);
        lastChange = Stopwatch.createStarted();
    }

    public void decreaseVisibility() {
        visibility = clampVis(visibility - lastChange.elapsed(TimeUnit.MILLISECONDS) * TIME_SCALE);
        lastChange = Stopwatch.createStarted();
    }

    public boolean isVisible() {
        return visibility > 0.001;
    }

    public boolean hasSwitchTo() {
        return switchTo != null;
    }

    public IItemMode getSwitchTo() {
        return switchTo;
    }

    public boolean isActionUsed() {
        return actionUsed;
    }

    public void setActionUsed(boolean b) {
        actionUsed = b;
    }

    public boolean hasAction() {
        return doAction != null;
    }

    public MenuAction getAction() {
        return doAction;
    }

    public boolean hasClicked() {
        return clicked;
    }

    public void setClicked(boolean c) {
        if (hasAction() || hasSwitchTo()) clicked = c;
    } //Only see as click if we've highlighted something

    public boolean isPressingButton() {
        return pressedButton;
    }

    public void setPressingButton(boolean d) {
        pressedButton = d;
    }
}
