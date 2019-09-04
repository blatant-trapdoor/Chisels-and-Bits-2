package nl.dgoossens.chiselsandbits2.client.gui;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.client.ClientSide;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RadialMenu extends Screen {
    private final float TIME_SCALE = 0.01f;

    private float visibility = 0.0f;
    private Stopwatch lastChange = Stopwatch.createStarted();
    private IItemMode switchTo = null;
    private MenuAction doAction = null;
    private boolean actionUsed = false;
    private boolean gameFocused = true;
    private boolean pressedButton = false;
    private boolean clicked = false;
    private MainWindow window;

    public RadialMenu() {
        super(new StringTextComponent("Radial Menu"));
        minecraft = Minecraft.getInstance();
        font = Minecraft.getInstance().fontRenderer;
    }
    public void configure(final MainWindow window) { this.window = window; }

    @Override
    public Minecraft getMinecraft() {
        return minecraft==null ? Minecraft.getInstance() : minecraft;
    }
    public FontRenderer getFontRenderer() {
        return font==null ? Minecraft.getInstance().fontRenderer : font;
    }
    private float clampVis(final float f) {
        return Math.max( 0.0f, Math.min( 1.0f, f ) );
    }

    public void raiseVisibility() {
        visibility = clampVis( visibility + lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
        lastChange = Stopwatch.createStarted();
    }

    public void decreaseVisibility() {
        visibility = clampVis( visibility - lastChange.elapsed( TimeUnit.MILLISECONDS ) * TIME_SCALE );
        lastChange = Stopwatch.createStarted();
    }

    public boolean isVisible() { return visibility > 0.001; }
    public boolean hasSwitchTo() { return switchTo!=null; }
    public IItemMode getSwitchTo() { return switchTo; }
    public void setSwitchTo(IItemMode d) { switchTo=d; }
    public void setActionUsed(boolean b) { actionUsed=b; }
    public boolean isActionUsed() { return actionUsed; }
    public boolean hasAction() { return doAction!=null; }
    public MenuAction getAction() { return doAction; }
    public boolean hasClicked() { return clicked; }
    public void setClicked(boolean c) { if(hasAction()||hasSwitchTo()) clicked=c; } //Only see as click if we've highlighted something
    public void setPressingButton(boolean d) { pressedButton=d; }
    public boolean isPressingButton() { return pressedButton; }

    public void updateGameFocus() {
        //Switch the game focus state if the game is focus doesn't match the visibility.
        if((gameFocused && isVisible()) || (!gameFocused && !isVisible())) {
            gameFocused = !gameFocused;
            getMinecraft().setGameFocused(gameFocused);
            if(!gameFocused) {
                getMinecraft().mouseHelper.ungrabMouse();
                if(ChiselsAndBits2.getConfig().enableVivecraftCompatibility.get()) getMinecraft().currentScreen = this;
            } else {
                getMinecraft().mouseHelper.grabMouse();
                if(ChiselsAndBits2.getConfig().enableVivecraftCompatibility.get()) getMinecraft().displayGuiScreen(null);
            }
        }
    }

    private static class MenuButton {
        double x1, x2;
        double y1, y2;
        boolean highlighted;
        final MenuAction action;
        TextureAtlasSprite icon;
        int color;
        String name;
        Direction textSide;

        public MenuButton(final MenuAction action, final double x, final double y, final TextureAtlasSprite ico, final Direction textSide) {
            this(action, x, y, 0xffffff, textSide);
            this.icon = ico;
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

    static class MenuRegion {
        public final IItemMode mode;
        public double x1, x2;
        public double y1, y2;
        public boolean highlighted;

        public MenuRegion(final IItemMode mode) { this.mode = mode; }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if(!(getMinecraft().player.getHeldItemMainhand().getItem() instanceof IItemMenu)) return;
        GlStateManager.pushMatrix();
        GlStateManager.translatef( 0.0F, 0.0F, 200.0F );

        final int start = (int) ( visibility * 98 ) << 24;
        final int end = (int) ( visibility * 128 ) << 24;

        fillGradient( 0, 0, window.getWidth(), window.getHeight(), start, end );

        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0 );
        GlStateManager.shadeModel( GL11.GL_SMOOTH );
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR );


        final double middle_x = ((double) window.getScaledWidth()) / 2;
        final double middle_y = ((double) window.getScaledHeight()) / 2;
        final double vecX = mouseX - middle_x;
        final double vecY = mouseY - middle_y;
        double radians = Math.atan2( vecY, vecX );

        final double ring_inner_edge = 20;
        final double ring_outer_edge = 50;
        final double text_distance = 65;
        final double quarterCircle = Math.PI / 2.0;

        if ( radians < -quarterCircle )
        {
            radians = radians + Math.PI * 2;
        }

        final ArrayList<MenuRegion> modes = new ArrayList<>();
        final ArrayList<MenuButton> btns = new ArrayList<>();
        //Setup mode regions
        for(IItemMode m : ChiselModeManager.getMode(getMinecraft().player.getHeldItemMainhand()).getType().getItemModes(ChiselsAndBits2.getClient().getPlayer().getHeldItemMainhand()))
            modes.add(new MenuRegion(m));

        //Setup buttons
        btns.add(new MenuButton(MenuAction.UNDO, text_distance, -20, ClientSide.undoIcon, Direction.EAST));
        btns.add(new MenuButton(MenuAction.REDO, text_distance, 4, ClientSide.redoIcon, Direction.EAST));

        ItemModeType tool = ChiselModeManager.getMode(getMinecraft().player.getHeldItemMainhand()).getType();
        if(tool == ItemModeType.PATTERN) {
            btns.add(new MenuButton(MenuAction.ROLL_X, -text_distance - 18, -20, ClientSide.roll_x, Direction.WEST));
            btns.add(new MenuButton(MenuAction.ROLL_Z, -text_distance - 18, 4, ClientSide.roll_z, Direction.WEST));
        }

        if(tool == ItemModeType.CHISEL) {
            btns.add(new MenuButton(MenuAction.PLACE, -text_distance - 18, -20, ClientSide.roll_x, Direction.WEST));
            btns.add(new MenuButton(MenuAction.REPLACE, -text_distance - 18, 4, ClientSide.roll_z, Direction.WEST));
        }

        if(tool == ItemModeType.TAPEMEASURE) {
            final int colorSize = DyeColor.values().length / 4 * 24 - 4;
            double underring = -ring_outer_edge - 34;
            double bntPos = -colorSize;
            final int bntSize = 24;
            Direction textSide = Direction.UP;
            for(final DyeColor color : DyeColor.values()) {
                final MenuAction action = MenuAction.valueOf(color.name());
                if(bntPos > colorSize) {
                    underring = ring_outer_edge;
                    bntPos = -colorSize;
                    textSide = Direction.DOWN;
                }
                btns.add(new MenuButton(action, bntPos, underring, action.getColour(), textSide));
                bntPos += bntSize;
            }
        }

        switchTo = null;
        doAction = null;

        if(!modes.isEmpty()) {
            final int totalModes = Math.max(3, modes.size());
            int currentMode = 0;
            final double fragment = Math.PI * 0.005;
            final double fragment2 = Math.PI * 0.0025;
            final double perObject = 2.0 * Math.PI / totalModes;

            for(final MenuRegion mnuRgn : modes) {
                final double begin_rad = currentMode * perObject - quarterCircle;
                final double end_rad = (currentMode + 1) * perObject - quarterCircle;

                mnuRgn.x1 = Math.cos(begin_rad);
                mnuRgn.x2 = Math.cos(end_rad);
                mnuRgn.y1 = Math.sin(begin_rad);
                mnuRgn.y2 = Math.sin(end_rad);

                final double x1m1 = Math.cos(begin_rad + fragment) * ring_inner_edge;
                final double x2m1 = Math.cos(end_rad - fragment) * ring_inner_edge;
                final double y1m1 = Math.sin(begin_rad + fragment) * ring_inner_edge;
                final double y2m1 = Math.sin(end_rad - fragment) * ring_inner_edge;

                final double x1m2 = Math.cos(begin_rad + fragment2) * ring_outer_edge;
                final double x2m2 = Math.cos(end_rad - fragment2) * ring_outer_edge;
                final double y1m2 = Math.sin(begin_rad + fragment2) * ring_outer_edge;
                final double y2m2 = Math.sin(end_rad - fragment2) * ring_outer_edge;

                final float a = 0.5f*visibility;
                float f = 0f;

                final boolean quad = inTriangle(
                        x1m1, y1m1,
                        x2m2, y2m2,
                        x2m1, y2m1,
                        vecX, vecY) || inTriangle(
                        x1m1, y1m1,
                        x1m2, y1m2,
                        x2m2, y2m2,
                        vecX, vecY);

                if(begin_rad <= radians && radians <= end_rad && quad) {
                    f = 1;
                    mnuRgn.highlighted = true;
                    switchTo = mnuRgn.mode;
                }

                buffer.pos(middle_x + x1m1, middle_y + y1m1, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + x2m1, middle_y + y2m1, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + x2m2, middle_y + y2m2, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + x1m2, middle_y + y1m2, blitOffset).color(f, f, f, a).endVertex();

                currentMode++;
            }
        }

        for(final MenuButton btn : btns) {
            {
                final float a = 0.5f*visibility;
                float f = 0f;

                if(btn.x1 <= vecX && btn.x2 >= vecX && btn.y1 <= vecY && btn.y2 >= vecY) {
                    f = 1;
                    btn.highlighted = true;
                    doAction = btn.action;
                }

                buffer.pos(middle_x + btn.x1, middle_y + btn.y1, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + btn.x1, middle_y + btn.y2, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + btn.x2, middle_y + btn.y2, blitOffset).color(f, f, f, a).endVertex();
                buffer.pos(middle_x + btn.x2, middle_y + btn.y1, blitOffset).color(f, f, f, a).endVertex();
            }

            //Temporary flat colour drawing because we have no white sprite.
            {
                final float f = switchTo == null ? 1.0f : 0.5f;
                final float a = 1.0f*visibility;

                final double u1 = 0;
                final double u2 = 16;
                final double v1 = 0;
                final double v2 = 16;

                final double btnx1 = btn.x1 + 1;
                final double btnx2 = btn.x2 - 1;
                final double btny1 = btn.y1 + 1;
                final double btny2 = btn.y2 - 1;

                final float red = f * ((btn.color >> 16 & 0xff) / 255.0f);
                final float green = f * ((btn.color >> 8 & 0xff) / 255.0f);
                final float blue = f * ((btn.color & 0xff) / 255.0f);

                if(btn.icon==null) {
                    buffer.pos(middle_x + btnx1, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx1, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx2, middle_y + btny2, blitOffset).color(red, green, blue, a).endVertex();
                    buffer.pos(middle_x + btnx2, middle_y + btny1, blitOffset).color(red, green, blue, a).endVertex();
                }
            }
        }

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.translatef(0.0F, 0.0F, 5.0F);
        GlStateManager.enableTexture();
        GlStateManager.color4f(1, 1, 1, 1.0f);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.bindTexture(Minecraft.getInstance().getTextureMap().getGlTextureId());

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for(final MenuRegion mnuRgn : modes) {
            final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (ring_outer_edge * 0.6 + 0.4 * ring_inner_edge);
            final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (ring_outer_edge * 0.6 + 0.4 * ring_inner_edge);

            final SpriteIconPositioning sip = ClientSide.getIconForMode(mnuRgn.mode);
            if(sip == null) continue;

            final double scalex = 15 * sip.width * 0.5;
            final double scaley = 15 * sip.height * 0.5;
            final double x1 = x - scalex;
            final double x2 = x + scalex;
            final double y1 = y - scaley;
            final double y2 = y + scaley;

            final TextureAtlasSprite sprite = sip.sprite;

            final float f = 1.0f;
            final float a = 1.0f*visibility;

            final double u1 = sip.left * 16.0;
            final double u2 = (sip.left + sip.width) * 16.0;
            final double v1 = sip.top * 16.0;
            final double v2 = (sip.top + sip.height) * 16.0;

            buffer.pos(middle_x + x1, middle_y + y1, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x1, middle_y + y2, blitOffset).tex(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x2, middle_y + y2, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2)).color(f, f, f, a).endVertex();
            buffer.pos(middle_x + x2, middle_y + y1, blitOffset).tex(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1)).color(f, f, f, a).endVertex();
        }

        for(final MenuButton btn : btns) {
            final float f = switchTo == null ? 1.0f : 0.5f;
            final float a = 1.0f*visibility;

            final double u1 = 0;
            final double u2 = 16;
            final double v1 = 0;
            final double v2 = 16;

            final double btnx1 = btn.x1 + 1;
            final double btnx2 = btn.x2 - 1;
            final double btny1 = btn.y1 + 1;
            final double btny2 = btn.y2 - 1;

            final float red = f * ((btn.color >> 16 & 0xff) / 255.0f);
            final float green = f * ((btn.color >> 8 & 0xff) / 255.0f);
            final float blue = f * ((btn.color & 0xff) / 255.0f);

            if(btn.icon!=null) {
                buffer.pos(middle_x + btnx1, middle_y + btny1, blitOffset).tex(btn.icon.getInterpolatedU(u1), btn.icon.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx1, middle_y + btny2, blitOffset).tex(btn.icon.getInterpolatedU(u1), btn.icon.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny2, blitOffset).tex(btn.icon.getInterpolatedU(u2), btn.icon.getInterpolatedV(v2)).color(red, green, blue, a).endVertex();
                buffer.pos(middle_x + btnx2, middle_y + btny1, blitOffset).tex(btn.icon.getInterpolatedU(u2), btn.icon.getInterpolatedV(v1)).color(red, green, blue, a).endVertex();
            }
        }

        tessellator.draw();

        final ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        final double w = 8;
        RenderHelper.enableGUIStandardItemLighting();
        for(final MenuRegion mnuRgn : modes) {
            if(mnuRgn.highlighted) {
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5;
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5;

                int fixed_x = (int) (x * text_distance);
                final int fixed_y = (int) (y * text_distance);
                final String text = mnuRgn.mode.getLocalizedName();

                if(x <= -0.2) {
                    fixed_x -= getFontRenderer().getStringWidth(text);
                } else if(-0.2 <= x && x <= 0.2) {
                    fixed_x -= getFontRenderer().getStringWidth(text) / 2;
                }

                getFontRenderer().drawStringWithShadow(text, (int) middle_x + fixed_x, (int) middle_y + fixed_y, 0xffffffff);
            }
            if(mnuRgn.mode instanceof SelectedBlockItemMode) {
                if(mnuRgn.mode.equals(SelectedBlockItemMode.NONE)) continue;

                //Selectable blocks should render the item that's inside!
                final double x = (mnuRgn.x1 + mnuRgn.x2) * 0.5 * (ring_outer_edge * 0.6 + 0.4 * ring_inner_edge);
                final double y = (mnuRgn.y1 + mnuRgn.y2) * 0.5 * (ring_outer_edge * 0.6 + 0.4 * ring_inner_edge);
                ir.renderItemIntoGUI(((SelectedBlockItemMode) mnuRgn.mode).getStack(), (int)Math.round(middle_x + x - w), (int)Math.round(middle_y + y - w));
            }
        }
        RenderHelper.disableStandardItemLighting();

        for(final MenuButton btn : btns) {
            if(btn.highlighted) {
                final String text = btn.name;
                if(btn.textSide == Direction.WEST) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + btn.x1 - 8) - getFontRenderer().getStringWidth(text), (int) (middle_y + btn.y1 + 6), 0xffffffff);
                } else if(btn.textSide == Direction.EAST) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + btn.x2 + 8), (int) (middle_y + btn.y1 + 6), 0xffffffff);
                } else if(btn.textSide == Direction.UP) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + (btn.x1 + btn.x2) * 0.5 - getFontRenderer().getStringWidth(text) * 0.5), (int) (middle_y + btn.y1 - 14), 0xffffffff);
                } else if(btn.textSide == Direction.DOWN) {
                    getFontRenderer().drawStringWithShadow(text, (int) (middle_x + (btn.x1 + btn.x2) * 0.5 - getFontRenderer().getStringWidth(text) * 0.5), (int) (middle_y + btn.y1 + 24), 0xffffffff);
                }
            }
        }

        GlStateManager.popMatrix();
    }

    private boolean inTriangle(
            final double x1,
            final double y1,
            final double x2,
            final double y2,
            final double x3,
            final double y3,
            final double x,
            final double y )
    {
        final double ab = ( x1 - x ) * ( y2 - y ) - ( x2 - x ) * ( y1 - y );
        final double bc = ( x2 - x ) * ( y3 - y ) - ( x3 - x ) * ( y2 - y );
        final double ca = ( x3 - x ) * ( y1 - y ) - ( x1 - x ) * ( y3 - y );
        return sign( ab ) == sign( bc ) && sign( bc ) == sign( ca );
    }

    private int sign(
            final double n )
    {
        return n > 0 ? 1 : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0)
            setClicked(true);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /*@SubscribeEvent //TODO Backup event, that isn't even PR'd in yet.
    public static void onClickWithGuiOpen(InputEvent.ClickInputEvent e) {
        if(ChiselsAndBits2.getClient().getRadialMenu().isVisible()) {
            ChiselsAndBits2.getClient().getRadialMenu().setClicked(true);
            e.setCanceled(true);
        }
    }*/
}
