package nl.dgoossens.chiselsandbits2.client;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.SpriteIconPositioning;
import nl.dgoossens.chiselsandbits2.api.modes.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.client.gui.RadialMenu;
import nl.dgoossens.chiselsandbits2.client.render.overlay.BlockColorChiseled;
import nl.dgoossens.chiselsandbits2.client.render.ter.ChiseledBlockTER;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import org.apache.commons.io.IOUtils;
import sun.nio.ch.IOUtil;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class ClientSide {
    //--- GENERAL SETUP ---
    public void setup() {
        ClientRegistry.bindTileEntitySpecialRenderer(ChiseledBlockTileEntity.class, new ChiseledBlockTER());
        Minecraft.getInstance().getBlockColors().register(new BlockColorChiseled(), ChiselsAndBits2.getBlocks().CHISELED_BLOCK);

        //We've got both normal and mod event bus events.
        MinecraftForge.EVENT_BUS.register(getClass());
        FMLJavaModLoadingContext.get().getModEventBus().register(getClass());
    }

    //--- SPRITES ---
    private static final HashMap<ItemMode, SpriteIconPositioning> chiselModeIcons = new HashMap<>();
    public static SpriteIconPositioning getIconForMode(final ItemMode mode) {
        return chiselModeIcons.get(mode);
    }

    @SubscribeEvent
    public static void registerIconTextures(final TextureStitchEvent.Pre e) {
        for(final ItemMode itemMode : ItemMode.values()) {
            final SpriteIconPositioning sip = new SpriteIconPositioning();

            final ResourceLocation sprite = new ResourceLocation( ChiselsAndBits2.MOD_ID, "icons/" + itemMode.getTypelessName().toLowerCase() );
            final ResourceLocation png = new ResourceLocation( ChiselsAndBits2.MOD_ID, "textures/icons/" + itemMode.getTypelessName().toLowerCase() + ".png" );

            sip.sprite = e.getMap().getSprite( sprite );

            try {
                final IResource iresource = Minecraft.getInstance().getResourceManager().getResource(png);
                final BufferedImage bi;
                try {
                    bi = ImageIO.read(iresource.getInputStream());
                } finally {
                    IOUtils.closeQuietly(iresource.getInputStream());
                }

                int bottom = 0;
                int right = 0;
                sip.left = bi.getWidth();
                sip.top = bi.getHeight();

                for ( int x = 0; x < bi.getWidth(); x++ )
                {
                    for ( int y = 0; y < bi.getHeight(); y++ )
                    {
                        final int color = bi.getRGB( x, y );
                        final int a = color >> 24 & 0xff;
                        if ( a > 0 )
                        {
                            sip.left = Math.min( sip.left, x );
                            right = Math.max( right, x );

                            sip.top = Math.min( sip.top, y );
                            bottom = Math.max( bottom, y );
                        }
                    }
                }

                sip.height = bottom - sip.top + 1;
                sip.width = right - sip.left + 1;

                sip.left /= bi.getWidth();
                sip.width /= bi.getWidth();
                sip.top /= bi.getHeight();
                sip.height /= bi.getHeight();
            } catch(final IOException ex) {
                sip.height = 1;
                sip.width = 1;
                sip.left = 0;
                sip.top = 0;
            }
            chiselModeIcons.put(itemMode, sip);
        }
    }

    //--- UTILITY METHODS ---
    public PlayerEntity getPlayer() { return Minecraft.getInstance().player; }
    public TextureAtlasSprite getMissingIcon() {
        return Minecraft.getInstance().getTextureMap().getSprite(new ResourceLocation("")); //The missing sprite is returned when an error occurs whilst searching for the texture.
    }
    public void breakSound(final World world, final BlockPos pos, final BlockState state) {
        final Block block = state.getBlock();
        final SoundType soundType = block.getSoundType(state, world, pos, getPlayer());
        world.playSound( pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                soundType.getBreakSound(), SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0F) / 16.0F,
                soundType.getPitch() * 0.9F, false);
    }

    //--- TICK & RENDERING ---
    private RadialMenu radialMenu = new RadialMenu();
    public RadialMenu getRadialMenu() { return radialMenu; }

    //We currently don't use this because MC doesn't render multiple times per frame anymore. (still investigating)
    /*private static byte frameId = Byte.MIN_VALUE;

    **
     * Return the current frame's id, please note that this id is arbitrary.
     * The id is a byte that is supposed to roll over. (so don't worry)
     *
     * This frame id is stored in TER data to make sure TER's don't render
     * twice or more a frame.
     *
    public static byte getFrameId() { return frameId; }*/

    /**
     * For the logic whether or not the radial menu should be opened etc.
     */
    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent e) {
        final PlayerEntity player = Minecraft.getInstance().player;
        if(player==null) return; //We're not in-game yet if this happens..
        if(player.getHeldItemMainhand().getItem() instanceof IItemMenu) {
            RadialMenu radialMenu = ChiselsAndBits2.getClient().getRadialMenu();
            //If you've recently clicked (click = force close) but you're not pressing the button anymore we can reset the click state.
            if(radialMenu.hasClicked() && !radialMenu.isPressingButton())
                radialMenu.setClicked(false);

            radialMenu.setPressingButton(ChiselsAndBits2.getKeybindings().modeMenu.isKeyDown());
            if(radialMenu.isPressingButton() && !radialMenu.hasClicked()) {
                //While the key is down, increase the visibility
                radialMenu.setActionUsed(false);
                radialMenu.raiseVisibility();
            } else {
                if(!radialMenu.isActionUsed()) {
                    if(radialMenu.hasSwitchTo() || radialMenu.hasAction()) {
                        final float volume = ChiselsAndBits2.getConfig().radialMenuVolume.get().floatValue();
                        if (volume >= 0.0001f)
                            Minecraft.getInstance().getSoundHandler().play(new SimpleSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, volume, 1.0f, player.getPosition()));
                    }

                    if(radialMenu.hasSwitchTo())
                        ChiselModeManager.changeChiselMode(radialMenu.getSwitchTo());

                    if(radialMenu.hasAction()) {
                        switch(radialMenu.getAction()) {
                            case UNDO:
                                System.out.println("UNDO");
                                break;
                            case REDO:
                                System.out.println("REDO"); //TODO add undo/redo buttons
                                break;
                        }
                    }
                }

                radialMenu.setActionUsed(true);
                radialMenu.decreaseVisibility();
            }
        }

        //TODO if undo/redo is pressed, do undo/redo
    }

    /**
     * For rendering the ghost selected menu option on the
     * item portrait.
     * Also for rendering the radial menu when the time comes.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void drawLast(final RenderGameOverlayEvent.Post e) {
        if(e.getType() == RenderGameOverlayEvent.ElementType.ALL) {

            Minecraft.getInstance().getProfiler().startSection("chiselsandbit2-radialmenu");
            final PlayerEntity player = Minecraft.getInstance().player;
            if(player.getHeldItemMainhand().getItem() instanceof IItemMenu) {
                RadialMenu radialMenu = ChiselsAndBits2.getClient().getRadialMenu();
                final MainWindow window = e.getWindow();
                radialMenu.configure(window); //Setup the height/width scales
                radialMenu.updateGameFocus();
                if(radialMenu.isVisible()) {
                    if(radialMenu.getMinecraft().isGameFocused())
                        KeyBinding.unPressAllKeys();

                    int i = (int)(radialMenu.getMinecraft().mouseHelper.getMouseX() * (double)window.getScaledWidth() / (double)window.getWidth());
                    int j = (int)(radialMenu.getMinecraft().mouseHelper.getMouseY() * (double)window.getScaledHeight() / (double)window.getHeight());

                    //This comment makes note that the code below is horrible from a forge perspective, but it's great.
                    ForgeHooksClient.drawScreen(radialMenu, i, j, e.getPartialTicks());
                }
            }
            Minecraft.getInstance().getProfiler().endSection();
        }
    }

    /**
     * For rendering the block placement ghost.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void drawLast(final RenderWorldLastEvent e) {
        //frameId++; //Increase the frame id every time a new frame is drawn.
        if(Minecraft.getInstance().gameSettings.hideGUI) return;

    }

    //--- ITEM SCROLL ---
    /*@SubscribeEvent  Waiting for Forge PR...
    @OnlyIn(Dist.CLIENT)
    public static void wheelEvent(final InputEvent.MouseScrollEvent me) {
        final int dwheel = me.getScrollDelta() < 0 ? -1 : me.getScrollDelta() > 0 ? 1 : 0;
        if ( me.isCanceled() || dwheel == 0 ) {
            return;
        }

        final PlayerEntity player = getPlayer();
        final ItemStack is = player.getHeldItemMainhand();

        if ( dwheel != 0 && is != null && is.getItem() instanceof IItemScrollWheel && player.isSneaking() )
        {
            ( (IItemScrollWheel) is.getItem() ).scroll( player, is, dwheel );
            me.setCanceled( true );
        }
    }*/

    //--- DRAW START / START POS ---
    /*private BitLocation drawStart;
    private ItemMode lastTool;

    public BitLocation getStartPos() { return drawStart; }
    public void pointAt(@Nonnull final ItemMode type, @Nonnull final BitLocation pos) {
        if (drawStart == null) {
            drawStart = pos;
            lastTool = type;
        }
    }*/
}
