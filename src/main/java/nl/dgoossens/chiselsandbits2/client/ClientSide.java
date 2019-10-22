package nl.dgoossens.chiselsandbits2.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.client.gui.RadialMenu;
import nl.dgoossens.chiselsandbits2.client.render.RenderingAssistant;
import nl.dgoossens.chiselsandbits2.client.render.overlay.BagBeakerItemColor;
import nl.dgoossens.chiselsandbits2.client.render.overlay.ChiseledBlockColor;
import nl.dgoossens.chiselsandbits2.client.render.overlay.ChiseledBlockItemColor;
import nl.dgoossens.chiselsandbits2.client.render.ter.ChiseledBlockTER;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselTypeIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.*;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;
import nl.dgoossens.chiselsandbits2.common.registry.ModItems;
import nl.dgoossens.chiselsandbits2.common.registry.ModKeybindings;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientSide {
    private static final double BIT_SIZE = 1.0 / 16.0;
    private static final double BLOCK_SIZE = 1.0;
    private static final double HALF_BIT = BIT_SIZE / 2.0f;

    //--- SPRITES ---
    private static final HashMap<IItemMode, SpriteIconPositioning> chiselModeIcons = new HashMap<>();
    private static final HashMap<MenuAction, SpriteIconPositioning> menuActionIcons = new HashMap<>();
    public static TextureAtlasSprite trashIcon;
    public static TextureAtlasSprite sortIcon;
    public static TextureAtlasSprite swapIcon;
    public static TextureAtlasSprite placeIcon;

    //--- TAPE MEASURE ---
    private List<Measurement> tapeMeasurements = new ArrayList<>();
    private BitLocation tapeMeasureCache;

    public void useTapeMeasure(BlockRayTraceResult rayTrace) {
        final BitLocation location = new BitLocation(rayTrace, true, BitOperation.REMOVE);
        if(tapeMeasureCache == null) {
            //First Selection
            tapeMeasureCache = location;
        } else {
            //Second Selection
            final PlayerEntity player = Minecraft.getInstance().player;
            final ItemStack stack = player.getHeldItemMainhand();

            //Security measure.
            if(!(stack.getItem() instanceof TapeMeasureItem)) return;

            while(tapeMeasurements.size() >= ChiselsAndBits2.getInstance().getConfig().tapeMeasureLimit.get()) {
                tapeMeasurements.remove(0); //Remove the oldest one.
            }
            tapeMeasurements.add(new Measurement(tapeMeasureCache, location, ChiselModeManager.getMenuActionMode(stack), (ItemMode) ChiselModeManager.getMode(stack), player.dimension));
            tapeMeasureCache = null;
        }
    }

    /**
     * Renders the tape measure boxes.
     */
    public void renderTapeMeasureBoxes(float partialTicks) {
        final PlayerEntity player = Minecraft.getInstance().player;
        for(Measurement box : tapeMeasurements) {
            if(!player.dimension.equals(box.dimension)) continue;
            renderSelectionBox(true, player, box.first, box.second, partialTicks, BitOperation.REMOVE, new Color(box.colour.getColour()), box.mode);
        }
    }

    /**
     * Represents a box drawn by the tape measure.
     */
    public static class Measurement {
        private BitLocation first, second;
        private DimensionType dimension;
        private MenuAction colour;
        private ItemMode mode;

        public Measurement(BitLocation first, BitLocation second, MenuAction colour, ItemMode mode, DimensionType dimension) {
            this.first = first;
            this.second = second;
            this.colour = colour;
            this.mode = mode;
            this.dimension = dimension;
        }
    }

    //--- TICK & RENDERING ---
    private RadialMenu radialMenu = new RadialMenu();

    public static SpriteIconPositioning getIconForMode(final IItemMode mode) {
        return chiselModeIcons.get(mode);
    }

    public static SpriteIconPositioning getIconForAction(final MenuAction action) {
        return menuActionIcons.get(action);
    }

    @SubscribeEvent   //TODO waiting for PR https://github.com/MinecraftForge/MinecraftForge/pull/6032
    public static void registerIconTextures(final TextureStitchEvent.Pre e) {
        /*swapIcon = e.getMap().addSprite( new ResourceLocation(ChiselsAndBits2.MOD_ID, "icons/swap"));
        placeIcon = e.getMap().addSprite( new ResourceLocation(ChiselsAndBits2.MOD_ID, "icons/place"));
        trashIcon = e.getMap().addSprite( new ResourceLocation(ChiselsAndBits2.MOD_ID, "icons/trash"));
        sortIcon = e.getMap().addSprite( new ResourceLocation(ChiselsAndBits2.MOD_ID, "icons/sort"));

        for(final MenuAction menuAction : MenuAction.values()) {
            menuActionIcons.put(menuAction, new SpriteIconPositioning(e.getMap().addSprite( new ResourceLocation(ChiselsAndBits2.MOD_ID, "icons/"+menuAction.name().toLowerCase()))));
        }

        for(final ItemMode itemMode : ItemMode.values()) {
            final SpriteIconPositioning sip = new SpriteIconPositioning();

            final ResourceLocation png = new ResourceLocation( ChiselsAndBits2.MOD_ID, "textures/icons/" + itemMode.getTypelessName().toLowerCase() + ".png" );

            sip.sprite = e.getMap().addSprite( new ResourceLocation( ChiselsAndBits2.MOD_ID, "icons/" + itemMode.getTypelessName().toLowerCase() ) );

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
        }*/
    }

    /**
     * Handles hotkey presses.
     */
    @SubscribeEvent
    public static void onKeyInput(final InputEvent.KeyInputEvent e) {
        final ModKeybindings keybindings = ChiselsAndBits2.getInstance().getKeybindings();
        for (ItemMode im : keybindings.modeHotkeys.keySet()) {
            KeyBinding kb = keybindings.modeHotkeys.get(im);
            if (kb.isPressed() && kb.getKeyModifier().isActive(KeyConflictContext.IN_GAME))
                ChiselModeManager.changeItemMode(im);
        }
        for (MenuAction ma : keybindings.actionHotkeys.keySet()) {
            KeyBinding kb = keybindings.actionHotkeys.get(ma);
            if (kb.isPressed() && kb.getKeyModifier().isActive(KeyConflictContext.IN_GAME))
                handleMenuAction(ma);
        }

        if (Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof TapeMeasureItem &&
                keybindings.clearTapeMeasure.isPressed() &&
                keybindings.clearTapeMeasure.getKeyModifier().isActive(KeyConflictContext.IN_GAME)) {
            ChiselsAndBits2.getInstance().getClient().tapeMeasurements.clear();
        }
    }

    /**
     * For the logic whether or not the radial menu should be opened etc.
     */
    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent e) {
        final PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return; //We're not in-game yet if this happens..
        RadialMenu radialMenu = ChiselsAndBits2.getInstance().getClient().getRadialMenu();
        if ((player.getHeldItemMainhand().getItem() instanceof IItemMenu) || radialMenu.isVisible()) { //Also decrease visibility if you've scrolled of the item.
            //If you've recently clicked (click = force close) but you're not pressing the button anymore we can reset the click state.
            if (radialMenu.hasClicked() && !radialMenu.isPressingButton())
                radialMenu.setClicked(false);

            KeyBinding modeMenu = ChiselsAndBits2.getInstance().getKeybindings().modeMenu;
            //isKeyDown breaks when using ALT for some reason, so we do it custom.
            if (modeMenu.isDefault()) radialMenu.setPressingButton(Screen.hasAltDown());
            else radialMenu.setPressingButton(modeMenu.isKeyDown());

            if (radialMenu.isPressingButton() && !radialMenu.hasClicked() && (player.getHeldItemMainhand().getItem() instanceof IItemMenu)) {
                //While the key is down, increase the visibility
                radialMenu.setActionUsed(false);
                radialMenu.raiseVisibility();
            } else {
                if (!radialMenu.isActionUsed()) {
                    if (radialMenu.hasSwitchTo() || radialMenu.hasAction()) {
                        final float volume = ChiselsAndBits2.getInstance().getConfig().radialMenuVolume.get().floatValue();
                        if (volume >= 0.0001f)
                            Minecraft.getInstance().getSoundHandler().play(new SimpleSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, volume, 1.0f, player.getPosition()));
                    }

                    if (radialMenu.hasSwitchTo())
                        ChiselModeManager.changeItemMode(radialMenu.getSwitchTo());

                    if (radialMenu.hasAction())
                        handleMenuAction(radialMenu.getAction());
                }

                radialMenu.setActionUsed(true);
                radialMenu.decreaseVisibility();
            }
        }
        radialMenu.updateGameFocus();
    }

    /**
     * Handles the usage of a menu action.
     */
    private static void handleMenuAction(final MenuAction action) {
        switch (action) {
            case UNDO:
                System.out.println("UNDO");
                break;
            case REDO:
                System.out.println("REDO");
                break;
            case ROLL_X:
                System.out.println("ROLL_X");
                break;
            case ROLL_Y:
                System.out.println("ROLL_Y");
                break;
            case ROLL_Z:
                System.out.println("ROLL_Z");
                break;
            case PLACE:
            case REPLACE:
                ChiselModeManager.changeMenuActionMode(action);
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
                ChiselModeManager.changeMenuActionMode(action);
                break;
        }
    }

    /**
     * For rendering the ghost selected menu option on the
     * item portrait.
     * Also for rendering the radial menu when the time comes.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void drawLast(final RenderGameOverlayEvent.Post e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft.getInstance().getProfiler().startSection("chiselsandbit2-radialmenu");
            RadialMenu radialMenu = ChiselsAndBits2.getInstance().getClient().getRadialMenu();
            if (radialMenu.isVisible()) { //Render if it's visible.
                final MainWindow window = e.getWindow();
                radialMenu.configure(window); //Setup the height/width scales
                if (radialMenu.isVisible()) {
                    if (radialMenu.getMinecraft().isGameFocused())
                        KeyBinding.unPressAllKeys();

                    int i = (int) (radialMenu.getMinecraft().mouseHelper.getMouseX() * (double) window.getScaledWidth() / (double) window.getWidth());
                    int j = (int) (radialMenu.getMinecraft().mouseHelper.getMouseY() * (double) window.getScaledHeight() / (double) window.getHeight());

                    //This comment makes note that the code below is horrible from a forge perspective, but it's great.
                    ForgeHooksClient.drawScreen(radialMenu, i, j, e.getPartialTicks());
                }
            }
            Minecraft.getInstance().getProfiler().endSection();
        }

        if (e.getType() == RenderGameOverlayEvent.ElementType.HOTBAR && ChiselsAndBits2.getInstance().getConfig().enableToolbarIcons.get()) {
            Minecraft.getInstance().getProfiler().startSection("chiselsandbit2-toolbaricons");
            final PlayerEntity player = Minecraft.getInstance().player;
            if (!player.isSpectator()) {
                final ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
                GlStateManager.translatef(0, 0, 50);
                GlStateManager.scalef(0.5f, 0.5f, 1);
                GlStateManager.color4f(1, 1, 1, 1.0f);
                Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                RenderHelper.enableGUIStandardItemLighting();
                for (int slot = 8; slot >= 0; --slot) {
                    if (player.inventory.mainInventory.get(slot).getItem() instanceof IItemMenu) {
                        final IItemMode mode = ChiselModeManager.getMode(player.inventory.mainInventory.get(slot));
                        final int x = (e.getWindow().getScaledWidth() / 2 - 90 + slot * 20 + 2) * 2;
                        final int y = (e.getWindow().getScaledHeight() - 16 - 3) * 2;

                        final TextureAtlasSprite sprite = chiselModeIcons.get(mode) == null ? null : chiselModeIcons.get(mode).sprite;
                        if (mode instanceof SelectedItemMode) {
                            if (mode.equals(SelectedItemMode.NONE_BAG)) continue;
                            ir.renderItemIntoGUI(((SelectedItemMode) mode).getStack(), x, y);
                        } else {
                            //Don't render null sprite.
                            if (sprite == null) continue;

                            GlStateManager.translatef(0, 0, 200); //The item models are also rendered 150 higher
                            GlStateManager.enableBlend();
                            int blitOffset = 0;
                            try {
                                Field f = AbstractGui.class.getDeclaredField("blitOffset");
                                f.setAccessible(true);
                                blitOffset = (int) f.get(Minecraft.getInstance().ingameGUI);
                            } catch (Exception rx) {
                                rx.printStackTrace();
                            }
                            AbstractGui.blit(x + 2, y + 2, blitOffset, 16, 16, sprite);
                            GlStateManager.disableBlend();
                            GlStateManager.translatef(0, 0, -200);
                        }
                    }
                }
                GlStateManager.scalef(2, 2, 1);
                GlStateManager.translatef(0, 0, -50);
                RenderHelper.disableStandardItemLighting();
            }
            Minecraft.getInstance().getProfiler().endSection();
        }
    }

    /**
     * For drawing our custom highlight bounding boxes!
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void drawHighlights(final DrawBlockHighlightEvent e) {
        if(Minecraft.getInstance().objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
            final PlayerEntity player = Minecraft.getInstance().player;
            //As this is rendering code and it gets called many times per tick, I try to minimise local variables.
            boolean tapeMeasure = player.getHeldItemMainhand().getItem() instanceof TapeMeasureItem;
            if (tapeMeasure || player.getHeldItemMainhand().getItem() instanceof ChiselItem) {
                final RayTraceResult rayTrace = ChiselUtil.rayTrace(player);
                if (rayTrace == null || rayTrace.getType() != RayTraceResult.Type.BLOCK)
                    return;

                final World world = Minecraft.getInstance().world;
                final BitLocation location = new BitLocation((BlockRayTraceResult) rayTrace, true, BitOperation.REMOVE); //We always show the removal box, never the placement one.
                final TileEntity data = world.getTileEntity(location.blockPos);

                //We only show this box if this block is chiselable and this block at this position is chiselable.
                if (!tapeMeasure && !ChiselUtil.canChiselBlock(world.getBlockState(location.blockPos))) return;
                //The highlight not showing up when you can't chisel in a specific block isn't worth all of the code that needs to be checked for it.
                //if(!ChiselUtil.canChiselPosition(location.getBlockPos(), player, state, ((BlockRayTraceResult) mop).getFace())) return;

                //Rendering drawn region bounding box
                final BitLocation other = tapeMeasure ? ChiselsAndBits2.getInstance().getClient().tapeMeasureCache : ChiselsAndBits2.getInstance().getClient().selectionStart;
                final BitOperation operation = tapeMeasure ? BitOperation.REMOVE : ChiselsAndBits2.getInstance().getClient().operation;
                if ((tapeMeasure || ChiselModeManager.getMode(player.getHeldItemMainhand()).equals(ItemMode.CHISEL_DRAWN_REGION)) && other != null) {
                    ChiselsAndBits2.getInstance().getClient().renderSelectionBox(tapeMeasure, player, location, other, e.getPartialTicks(), operation, new Color(ChiselModeManager.getMenuActionMode(player.getHeldItemMainhand()).getColour()), (ItemMode) ChiselModeManager.getMode(player.getHeldItemMainhand()));
                    e.setCanceled(true);
                    return;
                }
                //Tape measure never displays the small cube.
                if(tapeMeasure) return;

                //This method call is super complicated, but it saves having way more local variables than necessary.
                // (although I don't know if limiting local variables actually matters)
                RenderingAssistant.drawSelectionBoundingBoxIfExists(
                        ChiselTypeIterator.create(
                                VoxelBlob.DIMENSION, location.bitX, location.bitY, location.bitZ,
                                new VoxelRegionSrc(world, location.blockPos, 1),
                                ChiselModeManager.getMode(player.getHeldItemMainhand()),
                                ((BlockRayTraceResult) rayTrace).getFace(),
                                false
                        ).getBoundingBox(
                                !(data instanceof ChiseledBlockTileEntity) ? (new VoxelBlob().fill(ModUtil.getStateId(world.getBlockState(location.blockPos))))
                                        : ((ChiseledBlockTileEntity) data).getBlob(), true
                        ),
                        location.blockPos, player, e.getPartialTicks(), false, 0, 0, 0, 102, 32);
                e.setCanceled(true);
            }
        }
    }

    /**
     * Renders the selection boxes as used by the tape measure and drawn region mode.
     */
    public void renderSelectionBox(boolean tapeMeasure, PlayerEntity player, BitLocation location, BitLocation other, float partialTicks, BitOperation operation, @Nullable Color c, @Nullable ItemMode mode) {
        AxisAlignedBB bb = null;

        //Don't do these calculations if we don't have to.
        if (!tapeMeasure || !mode.equals(ItemMode.TAPEMEASURE_DISTANCE)) {
            ChiselIterator oneEnd, otherEnd;
            if(mode.equals(ItemMode.TAPEMEASURE_BLOCK)) {
                boolean x = location.blockPos.getX() > other.blockPos.getX(), y = location.blockPos.getY() > other.blockPos.getY(), z = location.blockPos.getZ() > other.blockPos.getZ();
                oneEnd = ChiselTypeIterator.create(VoxelBlob.DIMENSION, x ? 15 : 0, y ? 15 : 0, z ? 15 : 0, VoxelBlob.NULL_BLOB, ItemMode.CHISEL_SINGLE, Direction.UP, operation.equals(BitOperation.PLACE));
                otherEnd = ChiselTypeIterator.create(VoxelBlob.DIMENSION, !x ? 15 : 0, !y ? 15 : 0, !z ? 15 : 0, VoxelBlob.NULL_BLOB, ItemMode.CHISEL_SINGLE, Direction.UP, operation.equals(BitOperation.PLACE));
            } else {
                oneEnd = ChiselTypeIterator.create(VoxelBlob.DIMENSION, location.bitX, location.bitY, location.bitZ, VoxelBlob.NULL_BLOB, ItemMode.CHISEL_SINGLE, Direction.UP, operation.equals(BitOperation.PLACE));
                otherEnd = ChiselTypeIterator.create(VoxelBlob.DIMENSION, other.bitX, other.bitY, other.bitZ, VoxelBlob.NULL_BLOB, ItemMode.CHISEL_SINGLE, Direction.UP, operation.equals(BitOperation.PLACE));
            }

            final AxisAlignedBB a = oneEnd.getBoundingBox(VoxelBlob.NULL_BLOB, false).offset(location.blockPos.getX(), location.blockPos.getY(), location.blockPos.getZ());
            final AxisAlignedBB b = otherEnd.getBoundingBox(VoxelBlob.NULL_BLOB, false).offset(other.blockPos.getX(), other.blockPos.getY(), other.blockPos.getZ());

            bb = a.union(b);
        }

        if(tapeMeasure) {
            //Draw length indicator
            final Vec3d v = player.getEyePosition(partialTicks);
            final double x = v.getX();
            final double y = v.getY();
            final double z = v.getZ();

            if(mode.equals(ItemMode.TAPEMEASURE_DISTANCE)) {
                final Vec3d a = buildTapeMeasureDistanceVector(location);
                final Vec3d b = buildTapeMeasureDistanceVector(other);

                RenderingAssistant.drawLineWithColor(a, b, BlockPos.ZERO, player, partialTicks, false, c.getRed(), c.getGreen(), c.getBlue(), 102, 32);

                GlStateManager.disableDepthTest();
                GlStateManager.disableCull();

                final double length = a.distanceTo(b) + BIT_SIZE;
                renderTapeMeasureLabel(partialTicks, (a.getX() + b.getX()) * 0.5 - x, (a.getY() + b.getY()) * 0.5 - y, (a.getZ() + b.getZ()) * 0.5 - z, length, c.getRed(), c.getGreen(), c.getBlue());

                GlStateManager.enableDepthTest();
                GlStateManager.enableCull();
            } else {
                RenderingAssistant.drawSelectionBoundingBoxIfExists(bb, BlockPos.ZERO, player, partialTicks, false, c.getRed(), c.getGreen(), c.getBlue(), 102, 32);

                GlStateManager.disableDepthTest();
                GlStateManager.disableCull();

                final double lengthX = bb.maxX - bb.minX;
                final double lengthY = bb.maxY - bb.minY;
                final double lengthZ = bb.maxZ - bb.minZ;
                renderTapeMeasureLabel(partialTicks, bb.minX - x, (bb.maxY + bb.minY) * 0.5 - y, bb.minZ - z, lengthY, c.getRed(), c.getGreen(), c.getBlue());
                renderTapeMeasureLabel(partialTicks, (bb.minX + bb.maxX) * 0.5 - x, bb.minY - y, bb.minZ - z, lengthX, c.getRed(), c.getGreen(), c.getBlue());
                renderTapeMeasureLabel(partialTicks, bb.minX - x, bb.minY - y, (bb.minZ + bb.maxZ) * 0.5 - z, lengthZ, c.getRed(), c.getGreen(), c.getBlue());

                GlStateManager.enableDepthTest();
                GlStateManager.enableCull();
            }
        } else {
            final double maxSize = ChiselsAndBits2.getInstance().getConfig().maxDrawnRegionSize.get() + 0.001;
            if (bb.maxX - bb.minX <= maxSize && bb.maxY - bb.minY <= maxSize && bb.maxZ - bb.minZ <= maxSize) {
                RenderingAssistant.drawSelectionBoundingBoxIfExists(bb, BlockPos.ZERO, player, partialTicks, false, 0, 0, 0, 102, 32);
            }
        }
    }

    /**
     * Renders the label next to the tape measure bounding box.
     */
    private void renderTapeMeasureLabel(final float partialTicks, final double x, final double y, final double z, final double len, final int red, final int green, final int blue) {
        final double letterSize = 5.0;
        final double zScale = 0.001;

        final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        final String size = formatTapeMeasureLabel(len);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y + getScale(len) * letterSize, z);
        final Entity view = Minecraft.getInstance().getRenderViewEntity();
        if (view != null) {
            final float yaw = view.prevRotationYaw + (view.rotationYaw - view.prevRotationYaw) * partialTicks;
            GlStateManager.rotated(180 + -yaw, 0f, 1f, 0f);

            final float pitch = view.prevRotationPitch + (view.rotationPitch - view.prevRotationPitch) * partialTicks;
            GlStateManager.rotated(-pitch, 1f, 0f, 0f);
        }
        GlStateManager.scaled(getScale(len), -getScale(len), zScale);
        GlStateManager.translated(-fontRenderer.getStringWidth(size) * 0.5, 0, 0);
        fontRenderer.drawStringWithShadow(size, 0, 0, red << 16 | green << 8 | blue);
        GlStateManager.popMatrix();
    }

    /**
     * Get the scale of the tape measure label based on the length of the measured area.
     */
    private double getScale(final double maxLen) {
        final double maxFontSize = 0.04;
        final double minFontSize = 0.004;

        final double delta = Math.min(1.0, maxLen / 4.0);
        double scale = maxFontSize * delta + minFontSize * (1.0 - delta);
        if (maxLen < 0.25)
            scale = minFontSize;

        return Math.min(maxFontSize, scale);
    }

    /**
     * Format the label of the tape measure into the proper format.
     */
    private String formatTapeMeasureLabel(final double d) {
        final double blocks = Math.floor(d);
        final double bits = d - blocks;

        final StringBuilder b = new StringBuilder();

        if (blocks > 0)
            b.append((int) blocks).append("m");

        if (bits * 16 > 0.9999) {
            if (b.length() > 0)
                b.append(" ");
            b.append((int) (bits * 16)).append("b");
        }

        return b.toString();
    }

    /**
     * Builds the vector pointing to a bit location's bit.
     */
    private Vec3d buildTapeMeasureDistanceVector(BitLocation a) {
        final double ax = a.blockPos.getX() + BIT_SIZE * a.bitX + HALF_BIT;
        final double ay = a.blockPos.getY() + BIT_SIZE * a.bitY + HALF_BIT;
        final double az = a.blockPos.getZ() + BIT_SIZE * a.bitZ + HALF_BIT;
        return new Vec3d(ax, ay, az);
    }

    /**
     * For rendering the block placement ghost and static tape measurements.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void drawLast(final RenderWorldLastEvent e) {
        if (Minecraft.getInstance().gameSettings.hideGUI) return;

        //Draw tape measure boxes
        ChiselsAndBits2.getInstance().getClient().renderTapeMeasureBoxes(e.getPartialTicks());

        final PlayerEntity player = Minecraft.getInstance().player;
        final RayTraceResult mop = Minecraft.getInstance().objectMouseOver;
        if(!(mop instanceof BlockRayTraceResult)) return;
        final World world = player.world;
        final ItemStack currentItem = player.getHeldItemMainhand();
        final Direction face = ((BlockRayTraceResult) mop).getFace();

        //TODO add pattern ghost rendering!

        if(currentItem.getItem() instanceof BlockItem && ((BlockItem) currentItem.getItem()).getBlock() instanceof ChiseledBlock) {
            if (mop.getType() != RayTraceResult.Type.BLOCK) return;
            if (!currentItem.hasTag()) return;
            BlockPos offset = ((BlockRayTraceResult) mop).getPos();

            //TODO allow for ghost showing merge options
            boolean canMerge = false;
            //TODO determine if it can merge
            //If you can't place it we render it in red.
            boolean isPlaceable = ChiselHandler.isBlockReplaceable(player, world, offset, face, false) || (canMerge && world.getTileEntity(offset) instanceof ChiseledBlockTileEntity);

            if (player.isSneaking()) {
                final BitLocation bl = new BitLocation((BlockRayTraceResult) mop, true, BitOperation.PLACE);
                ChiselsAndBits2.getInstance().getClient().showGhost(currentItem, bl.blockPos, face, new BlockPos(bl.bitX, bl.bitY, bl.bitZ), e.getPartialTicks(), !isPlaceable);
            } else {
                //If we can already place where we're looking we don't have to move.
                if(!canMerge && !isPlaceable)
                    offset = offset.offset(((BlockRayTraceResult) mop).getFace());

                isPlaceable = ChiselHandler.isBlockReplaceable(player, world, offset, face, false) || (canMerge && world.getTileEntity(offset) instanceof ChiseledBlockTileEntity);
                ChiselsAndBits2.getInstance().getClient().showGhost(currentItem, offset, face, BlockPos.ZERO, e.getPartialTicks(), isPlaceable);
            }
        }
    }

    private IBakedModel ghostCache = null;
    private BlockPos previousPosition;
    private BlockPos previousPartial;
    private int displayStatus = 0;
    private IntegerBox modelBounds;

    /**
     * Shows the ghost of the chiseled block in item at the position offset by the partial in bits.
     */
    private void showGhost(ItemStack item, BlockPos pos, Direction face, BlockPos partial, float partialTicks, boolean isPlaceable) {
        final PlayerEntity player = Minecraft.getInstance().player;
        IBakedModel model = null;
        if(ghostCache != null && pos.equals(previousPosition) && partial.equals(previousPartial))
            model = ghostCache;
        else {
            previousPosition = pos;
            previousPartial = partial;

            final NBTBlobConverter c = new NBTBlobConverter();
            c.readChiselData(item.getChildTag(ModUtil.NBT_BLOCKENTITYTAG), VoxelVersions.getDefault());
            VoxelBlob blob = c.getVoxelBlob();
            modelBounds = blob.getBounds();

            model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(item, player.getEntityWorld(), player);
            ghostCache = model;

            if ( displayStatus != 0 ) {
                GlStateManager.deleteLists( displayStatus, 1 );
                displayStatus = 0;
            }
        }

        final Vec3d v = player.getEyePosition(partialTicks);
        final double x = v.getX();
        final double y = v.getY();
        final double z = v.getZ();

        GlStateManager.pushMatrix();
        GlStateManager.translated(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
        if (!partial.equals(BlockPos.ZERO)) {
            final BlockPos t = ModUtil.getPartialOffset(face, partial, modelBounds);
            final double fullScale = 1.0 / VoxelBlob.DIMENSION;
            GlStateManager.translated(t.getX() * fullScale, t.getY() * fullScale, t.getZ() * fullScale);
        }

        if (displayStatus == 0) {
            displayStatus = GLAllocation.generateDisplayLists(1);
            GlStateManager.newList(displayStatus, GL11.GL_COMPILE_AND_EXECUTE);
            RenderingAssistant.renderGhostModel(model, player.world, pos, !isPlaceable);
            GlStateManager.endList();
        } else
            GlStateManager.callList(displayStatus);

        GlStateManager.popMatrix();
    }

    /**
     * Handles calling the scroll methods on all items implementing IItemScrollWheel.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void wheelEvent(final InputEvent.MouseScrollEvent me) {
        final int dwheel = me.getScrollDelta() < 0 ? -1 : me.getScrollDelta() > 0 ? 1 : 0;
        if (me.isCanceled() || dwheel == 0) return;

        final PlayerEntity player = Minecraft.getInstance().player;
        final ItemStack is = player.getHeldItemMainhand();

        if (is.getItem() instanceof IItemScrollWheel && player.isSneaking()) {
            ((IItemScrollWheel) is.getItem()).scroll(player, is, dwheel);
            me.setCanceled(true);
        }
    }

    //--- GENERAL SETUP ---
    public void setup() {
        ClientRegistry.bindTileEntitySpecialRenderer(ChiseledBlockTileEntity.class, new ChiseledBlockTER());
        Minecraft.getInstance().getBlockColors().register(new ChiseledBlockColor(),
                ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK);
        Minecraft.getInstance().getItemColors().register(new ChiseledBlockItemColor(),
                Item.getItemFromBlock(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK));

        final ModItems i = ChiselsAndBits2.getInstance().getItems();
        Minecraft.getInstance().getItemColors().register(new BagBeakerItemColor(1),
                i.WHITE_BIT_BAG, i.BLACK_BIT_BAG, i.BLUE_BIT_BAG, i.BROWN_BIT_BAG, i.CYAN_BIT_BAG, i.GRAY_BIT_BAG,
                i.GREEN_BIT_BAG, i.LIGHT_BLUE_BIT_BAG, i.LIGHT_GRAY_BIT_BAG, i.LIME_BIT_BAG, i.MAGENTA_BIT_BAG,
                i.ORANGE_BIT_BAG, i.PINK_BIT_BAG, i.PURPLE_BIT_BAG, i.RED_BIT_BAG, i.YELLOW_BIT_BAG);

        if (ChiselsAndBits2.showUnfinishedFeatures())
            Minecraft.getInstance().getItemColors().register(new BagBeakerItemColor(1),
                    i.WHITE_TINTED_BIT_BEAKER, i.BLACK_TINTED_BIT_BEAKER, i.BLUE_TINTED_BIT_BEAKER, i.BROWN_TINTED_BIT_BEAKER, i.CYAN_TINTED_BIT_BEAKER, i.GRAY_TINTED_BIT_BEAKER,
                    i.GREEN_TINTED_BIT_BEAKER, i.LIGHT_BLUE_TINTED_BIT_BEAKER, i.LIGHT_GRAY_TINTED_BIT_BEAKER, i.LIME_TINTED_BIT_BEAKER, i.MAGENTA_TINTED_BIT_BEAKER,
                    i.ORANGE_TINTED_BIT_BEAKER, i.PINK_TINTED_BIT_BEAKER, i.PURPLE_TINTED_BIT_BEAKER, i.RED_TINTED_BIT_BEAKER, i.YELLOW_TINTED_BIT_BEAKER);

        //We've got both normal and mod event bus events.
        MinecraftForge.EVENT_BUS.register(getClass());
        FMLJavaModLoadingContext.get().getModEventBus().register(getClass());
    }

    //--- TAPE MEASURE / DRAWN REGION SELECTION ---
    private BitLocation selectionStart;
    private BitOperation operation;

    public void setSelectionStart(BitOperation operation, BitLocation bitLoc) {
        selectionStart = bitLoc;
        this.operation = operation;
    }

    public boolean hasSelectionStart(BitOperation operation) {
        if(!operation.equals(this.operation)) return false;
        return selectionStart != null;
    }

    public BitLocation getSelectionStart(BitOperation operation) {
        if(!operation.equals(this.operation)) return null;
        return selectionStart;
    }

    public void resetSelectionStart() {
        selectionStart = null;
        operation = null;
    }

    //--- UTILITY METHODS ---
    public TextureAtlasSprite getMissingIcon() {
        //The missing sprite is returned when an error occurs whilst searching for the texture.
        return Minecraft.getInstance().getTextureMap().getSprite(new ResourceLocation(""));
    }

    //--- ITEM SCROLL ---
    public RadialMenu getRadialMenu() {
        return radialMenu;
    }
}
