package nl.dgoossens.chiselsandbits2.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.block.BitOperation;
import nl.dgoossens.chiselsandbits2.api.item.DyedItemColour;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IBitModifyItem;
import nl.dgoossens.chiselsandbits2.api.item.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.api.radial.RadialMenu;
import nl.dgoossens.chiselsandbits2.api.item.IMenuAction;
import nl.dgoossens.chiselsandbits2.client.render.GhostModelRenderer;
import nl.dgoossens.chiselsandbits2.client.render.RenderingAssistant;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.api.bit.BitLocation;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.BlockPlacementLogic;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.impl.item.PlayerItemMode;
import nl.dgoossens.chiselsandbits2.common.impl.voxel.VoxelRegionSrc;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemMode;
import nl.dgoossens.chiselsandbits2.common.items.ChiseledBlockItem;
import nl.dgoossens.chiselsandbits2.common.items.TypedItem;
import nl.dgoossens.chiselsandbits2.common.network.client.COpenBitBagPacket;
import nl.dgoossens.chiselsandbits2.client.util.ClientItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.util.ItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;
import nl.dgoossens.chiselsandbits2.common.util.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.util.BitUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * All client side methods are split into two classes:
 *    - events are in ClientSide
 *    - everything else is in here
 */
public class ClientSideHelper {
    //--- GENERAL ---
    //General Constants
    protected static final double BIT_SIZE = 1.0 / 16.0;
    protected static final double HALF_BIT = BIT_SIZE / 2.0f;

    //Resource Locations for Icons
    protected static final HashMap<IItemMode, ResourceLocation> modeIconLocations = new HashMap<>();
    protected static final HashMap<IMenuAction, ResourceLocation> menuActionLocations = new HashMap<>();

    //Tape Measure
    protected List<Measurement> tapeMeasurements = new ArrayList<>();
    protected BitLocation tapeMeasureCache;
    protected BitLocation selectionStart;
    private BitOperation operation;

    //Ghost Model
    private GhostModelRenderer ghostModel = null;

    //Block Highlight
    private AxisAlignedBB selectionBoundingBox = null;

    /**
     * Cleans up some data when a player leaves the current save game.
     * To be more exact: this should be called whenever the previously assumed dimensions
     * are no longer the same. E.g. switching multiplayer servers.
     */
    public void clean() {
        tapeMeasurements.clear();
        tapeMeasureCache = null;
        selectionStart = null;
        operation = null;
        ghostModel = null;

        ChiselsAndBits2.getInstance().getUndoTracker().clean();
        RadialMenu.RADIAL_MENU.ifPresent(RadialMenu::cleanup);
    }

    /**
     * Get the current bit operation.
     */
    protected BitOperation getOperation(final IItemMode mode) {
        //Only return the cached mode if we are in drawn region mode
        if(operation != null && mode.equals(ItemMode.CHISEL_DRAWN_REGION)) return operation;
        //Morphing bit is mainly meant for placement so we show the placement highlight preferred over the removal highlight.
        if(Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof MorphingBitItem) return BitOperation.PLACE;
        return BitOperation.REMOVE;
    }

    /**
     * Server friendly get player method to get the client
     * main player.
     */
    public PlayerEntity getPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Get the resource location of the icon for the mode, will return null
     * if the mode has no icon.
     */
    public static ResourceLocation getModeIconLocation(final IItemMode mode) {
        return modeIconLocations.get(mode);
    }

    /**
     * Get the resource location of the icon for the menu action, will return null
     * if the action has no icon.
     */
    public static ResourceLocation getMenuActionIconLocation(final IMenuAction action) {
        return menuActionLocations.get(action);
    }

    /**
     * Returns true if this player inventory's hotbar has at least one item
     * which wants to render a toolbar icon.
     */
    public static boolean hasToolbarIconItem(PlayerInventory inventory) {
        for (int slot = 8; slot >= 0; --slot) {
            if (inventory.mainInventory.get(slot).getItem() instanceof IItemMenu && ((IItemMenu) inventory.mainInventory.get(slot).getItem()).showIconInHotbar())
                return true;
        }
        return false;
    }

    /**
     * Sets the starting location for a selection box. It's also important to specify the
     * operation as the bounding box is rendered different depending on if this is REMOVE or PLACE.
     */
    public void setSelectionStart(BitOperation operation, BitLocation bitLoc) {
        selectionStart = bitLoc;
        this.operation = operation;
    }

    /**
     * Returns if there is currently a selection started with this given operation.
     * A new selection should be started if another operation type is used regardless if
     * a selection was already started.
     */
    public boolean hasSelectionStart(BitOperation operation) {
        if(!operation.equals(this.operation)) return false;
        return selectionStart != null;
    }

    /**
     * Get the starting point of the current selection given the operation.
     */
    public BitLocation getSelectionStart(BitOperation operation) {
        if(!operation.equals(this.operation)) return null;
        return selectionStart;
    }

    /**
     * Resets the selection completely.
     */
    public void resetSelectionStart() {
        selectionStart = null;
        operation = null;
    }

    /**
     * Uses the tape measure with a given pre specified ray trace where the player is looking.
     * Automatically handles whether or not a selection has already started and caching the measurement.
     */
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

            //Add the new tape measure to the list of actively rendered measurements
            while(tapeMeasurements.size() >= ChiselsAndBits2.getInstance().getConfig().tapeMeasureLimit.get()) {
                tapeMeasurements.remove(0); //Remove the oldest one.
            }
            tapeMeasurements.add(new Measurement(tapeMeasureCache, location, ((TapeMeasureItem) stack.getItem()).getColour(stack), ((TapeMeasureItem) stack.getItem()).getSelectedMode(stack), player.dimension));
            tapeMeasureCache = null;
        }
    }

    /**
     * Draws the highlights around blocks. Called from RenderWorldLastEvent to render on top of blocks as the normal event renders partially behind them.
     */
    public boolean drawBlockHighlight(MatrixStack matrix, IRenderTypeBuffer buffer, float partialTicks) {
        final PlayerEntity player = Minecraft.getInstance().player;
        final ItemStack stack = player.getHeldItemMainhand();
        //As this is rendering code and it gets called many times per tick, I try to minimise local variables.
        boolean tapeMeasure = stack.getItem() instanceof TapeMeasureItem;
        boolean renderHighlight = tapeMeasure;
        if(!renderHighlight && stack.getItem() instanceof IBitModifyItem) {
            IBitModifyItem i = (IBitModifyItem) stack.getItem();
            renderHighlight = i.canPerformModification(IBitModifyItem.ModificationType.BUILD) || i.canPerformModification(IBitModifyItem.ModificationType.EXTRACT);
        }
        if (renderHighlight && stack.getItem() instanceof TypedItem) { //has to be a typed item
            final RayTraceResult rayTrace = ChiselUtil.rayTrace(player, partialTicks);
            if (rayTrace.getType() != RayTraceResult.Type.BLOCK)
                return false;

            final World world = Minecraft.getInstance().world;
            final IItemMode mode = ((TypedItem) stack.getItem()).getSelectedMode(stack);
            final BitOperation operation = tapeMeasure ? BitOperation.REMOVE : ChiselsAndBits2.getInstance().getClient().getOperation(mode);
            final BitLocation location = new BitLocation((BlockRayTraceResult) rayTrace, true, operation);
            final TileEntity data = world.getTileEntity(location.blockPos);

            //We only show this box if this block is chiselable and this block at this position is chiselable.
            if (!tapeMeasure && !ChiselsAndBits2.getInstance().getAPI().getRestrictions().canChiselBlock(world.getBlockState(location.blockPos))) return false;

            //Rendering drawn region bounding box
            if (tapeMeasure || ItemPropertyUtil.isItemMode(stack, ItemMode.CHISEL_DRAWN_REGION)) {
                final BitLocation other = tapeMeasure ? ChiselsAndBits2.getInstance().getClient().tapeMeasureCache : ChiselsAndBits2.getInstance().getClient().selectionStart;
                if(other != null) {
                    ChiselsAndBits2.getInstance().getClient().renderSelectionBox(tapeMeasure, location, other, partialTicks, operation, mode, tapeMeasure ? new Color(((TapeMeasureItem) stack.getItem()).getColour(stack).getColour()) : new Color(0, 0, 0));
                    return true;
                }
            }
            //Tape measure never displays the small cube.
            if(tapeMeasure) return false;

            //Only recalculate if we need to, iterators can take quite a bit of calculation.
            //TODO reset the cached selectionBoundingBox value just like we reset the values for the ghost render
            if(selectionBoundingBox == null) {
                ChiselIterator iterator = mode.getIterator(new BlockPos(location.bitX, location.bitY, location.bitZ), ((BlockRayTraceResult) rayTrace).getFace(), operation, new VoxelRegionSrc(player, world, location.blockPos, 1));
                VoxelBlob blob = data instanceof ChiseledBlockTileEntity ? ((ChiseledBlockTileEntity) data).getVoxelBlob() : new VoxelBlob(BitUtil.getBlockId(world.getBlockState(location.blockPos)));
                selectionBoundingBox = iterator.getBoundingBox(blob).orElse(null);
            }
            RenderingAssistant.drawBoundingBox(matrix, buffer, selectionBoundingBox, location.blockPos);
            return true;
        }
        return false;
    }

    /**
     * Renders the tape measure boxes.
     */
    public void renderTapeMeasureBoxes(float partialTicks) {
        final PlayerEntity player = Minecraft.getInstance().player;
        for(Measurement box : tapeMeasurements) {
            if(!player.dimension.equals(box.dimension)) continue;
            renderSelectionBox(true, box.first, box.second, partialTicks, BitOperation.REMOVE, box.mode, new Color(box.colour.getColour()));
        }
    }

    /**
     * Renders the selection boxes as used by the tape measure and drawn region mode.
     */
    public void renderSelectionBox(boolean tapeMeasure, BitLocation first, BitLocation second, float partialTicks, BitOperation operation, IItemMode mode, Color color) {
        final ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
        final double x = renderInfo.getProjectedView().x;
        final double y = renderInfo.getProjectedView().y;
        final double z = renderInfo.getProjectedView().z;

        if(tapeMeasure && ItemMode.TAPEMEASURE_DISTANCE.equals(mode)) {
            final Vec3d a = buildTapeMeasureDistanceVector(first);
            final Vec3d b = buildTapeMeasureDistanceVector(second);

            RenderingAssistant.drawLine(a, b, color.getRed() / 255.0f, color.getGreen() / 225.0f, color.getBlue() / 255.0f);

            //GlStateManager.disableDepthTest();
            //GlStateManager.disableCull();

            //final double length = a.distanceTo(b) + BIT_SIZE;
            //renderTapeMeasureLabel(partialTicks, (a.getX() + b.getX()) * 0.5 - x, (a.getY() + b.getY()) * 0.5 - y, (a.getZ() + b.getZ()) * 0.5 - z, length, color.getRed(), color.getGreen(), color.getBlue());

            //GlStateManager.enableDepthTest();
            //GlStateManager.enableCull();
            return;
        }

        ChiselIterator oneEnd, otherEnd;
        if(ItemMode.TAPEMEASURE_BLOCK.equals(mode)) {
            boolean t = first.blockPos.getX() > second.blockPos.getX(), s = first.blockPos.getY() > second.blockPos.getY(), u = first.blockPos.getZ() > second.blockPos.getZ();
            oneEnd = ItemMode.CHISEL_SINGLE.getIterator(new BlockPos(t ? 15 : 0, s ? 15 : 0, u ? 15 : 0), Direction.UP, operation, null);
            otherEnd = ItemMode.CHISEL_SINGLE.getIterator(new BlockPos(t ? 15 : 0, s ? 15 : 0, u ? 15 : 0), Direction.UP, operation, null);
        } else {
            oneEnd = ItemMode.CHISEL_SINGLE.getIterator(new BlockPos(first.bitX, first.bitY, first.bitZ), Direction.UP, operation, null);
            otherEnd = ItemMode.CHISEL_SINGLE.getIterator(new BlockPos(second.bitX, second.bitY, second.bitZ), Direction.UP, operation, null);
        }

        Optional<AxisAlignedBB> blobA = oneEnd.getBoundingBox(VoxelBlob.FULL_BLOCK), blobB = otherEnd.getBoundingBox(VoxelBlob.FULL_BLOCK);
        if(!blobA.isPresent() || !blobB.isPresent()) return;
        final AxisAlignedBB a = blobA.get().offset(first.blockPos.getX(), first.blockPos.getY(), first.blockPos.getZ());
        final AxisAlignedBB b = blobB.get().offset(second.blockPos.getX(), second.blockPos.getY(), second.blockPos.getZ());
        AxisAlignedBB bb = a.union(b);

        if(tapeMeasure) {
            //TODO RenderingAssistant.drawBoundingBox(null, null, bb, BlockPos.ZERO, c.getRed() / 255.0f, c.getGreen() / 225.0f, c.getBlue() / 255.0f);

            GlStateManager.disableDepthTest();
            GlStateManager.disableCull();

            final double lengthX = bb.maxX - bb.minX;
            final double lengthY = bb.maxY - bb.minY;
            final double lengthZ = bb.maxZ - bb.minZ;
            renderTapeMeasureLabel(partialTicks, bb.minX - x, (bb.maxY + bb.minY) * 0.5 - y, bb.minZ - z, lengthY, color.getRed(), color.getGreen(), color.getBlue());
            renderTapeMeasureLabel(partialTicks, (bb.minX + bb.maxX) * 0.5 - x, bb.minY - y, bb.minZ - z, lengthX, color.getRed(), color.getGreen(), color.getBlue());
            renderTapeMeasureLabel(partialTicks, bb.minX - x, bb.minY - y, (bb.minZ + bb.maxZ) * 0.5 - z, lengthZ, color.getRed(), color.getGreen(), color.getBlue());

            GlStateManager.enableDepthTest();
            GlStateManager.enableCull();
        } else {
            final double maxSize = ChiselsAndBits2.getInstance().getConfig().maxDrawnRegionSize.get() + 0.001;
            if (bb.maxX - bb.minX <= maxSize && bb.maxY - bb.minY <= maxSize && bb.maxZ - bb.minZ <= maxSize) {
                //TODO RenderingAssistant.drawBoundingBox(null, null, bb, BlockPos.ZERO);
            }
        }
    }

    /**
     * Renders the label next to the tape measure bounding box.
     */
    protected void renderTapeMeasureLabel(final float partialTicks, final double x, final double y, final double z, final double len, final int red, final int green, final int blue) {
        final double letterSize = 5.0;
        final double zScale = 0.001;

        final FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        final String size = formatTapeMeasureLabel(len);

        //TODO RenderSystem might not be the best system.
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y + getScale(len) * letterSize, z);
        final Entity view = Minecraft.getInstance().getRenderViewEntity();
        if (view != null) {
            final float yaw = view.prevRotationYaw + (view.rotationYaw - view.prevRotationYaw) * partialTicks;
            RenderSystem.rotatef(180 + -yaw, 0f, 1f, 0f);

            final float pitch = view.prevRotationPitch + (view.rotationPitch - view.prevRotationPitch) * partialTicks;
            RenderSystem.rotatef(-pitch, 1f, 0f, 0f);
        }
        RenderSystem.scaled(getScale(len), -getScale(len), zScale);
        RenderSystem.translated(-fontRenderer.getStringWidth(size) * 0.5, 0, 0);
        fontRenderer.drawStringWithShadow(size, 0, 0, red << 16 | green << 8 | blue);
        RenderSystem.popMatrix();
    }

    /**
     * Get the scale of the tape measure label based on the length of the measured area.
     */
    protected double getScale(final double maxLen) {
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
    protected String formatTapeMeasureLabel(final double d) {
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
    protected Vec3d buildTapeMeasureDistanceVector(BitLocation a) {
        final double ax = a.blockPos.getX() + BIT_SIZE * a.bitX + HALF_BIT;
        final double ay = a.blockPos.getY() + BIT_SIZE * a.bitY + HALF_BIT;
        final double az = a.blockPos.getZ() + BIT_SIZE * a.bitZ + HALF_BIT;
        return new Vec3d(ax, ay, az);
    }

    /**
     * Renders the placement ghost when holding a chiseled block or pattern.
     */
    public void renderPlacementGhost(float partialTicks) {
        //If placement ghosts are disabled, don't render anything.
        if (!ChiselsAndBits2.getInstance().getConfig().enablePlacementGhost.get()) return;
        final PlayerEntity player = Minecraft.getInstance().player;
        final ItemStack currentItem = player.getHeldItemMainhand();

        if(!currentItem.isEmpty() && currentItem.getItem() instanceof ChiseledBlockItem) {
            if (!currentItem.hasTag()) return;

            RayTraceResult rtr = ChiselUtil.rayTrace(player, partialTicks);
            if(!(rtr instanceof BlockRayTraceResult) || rtr.getType() != RayTraceResult.Type.BLOCK)
                return;

            final BlockRayTraceResult r = (BlockRayTraceResult) rtr;
            final PlayerItemMode mode = ClientItemPropertyUtil.getChiseledBlockMode();
            final Direction face = r.getFace();
            BlockPos offset = r.getPos();

            if (player.isCrouching() && !ClientItemPropertyUtil.getChiseledBlockMode().equals(PlayerItemMode.CHISELED_BLOCK_GRID)) {
                final BitLocation bl = new BitLocation(r, true, BitOperation.PLACE);
                //We don't make this darker if we can't place here because the calculations are far too expensive to do every time.
                ChiselsAndBits2.getInstance().getClient().showGhost(currentItem, player, bl, face, partialTicks, true);
            } else {
                //If we can already place where we're looking we don't have to move.
                //In grid mode we take the adjacent block if we can't replace the target block.
                //TODO make this logic determined by the same method for visuals/internals
                if(mode == PlayerItemMode.CHISELED_BLOCK_GRID && !ChiselUtil.isBlockReplaceable(player.world, offset, player, face, false)) {
                    offset = offset.offset(face);
                } else if(!(player.world.getTileEntity(offset) instanceof ChiseledBlockTileEntity)) {
                    if(BlockPlacementLogic.isNotPlaceable(player, player.world, offset, face, mode, () -> {
                        final NBTBlobConverter nbt = new NBTBlobConverter();
                        nbt.readChiselData(currentItem.getChildTag(ChiselUtil.NBT_BLOCKENTITYTAG), VoxelVersions.getDefault());
                        return nbt;
                    })) {
                        offset = offset.offset(face);
                    }
                }
                ChiselsAndBits2.getInstance().getClient().showGhost(currentItem, player, new BitLocation(offset, 0, 0, 0), face, partialTicks, false);
            }
        }
    }

    /**
     * Shows the ghost of the chiseled block in item at the position offset by the partial in bits.
     */
    void showGhost(ItemStack item, PlayerEntity player, BitLocation bit, Direction face, float partialTicks, boolean offGrid) {
        //Create a new ghost model if this one is invalid or non-existant
        if(ghostModel == null || !ghostModel.isValid(player, bit, face, offGrid))
            ghostModel = new GhostModelRenderer(item, player, bit, face, offGrid);

        //Render the model
        ghostModel.render(partialTicks);
    }

    /**
     * Renders selection ghosts in the hotbar next to items with a selected item mode.
     */
    public void renderSelectedModePreviews(final MainWindow window) {
        Minecraft mc = Minecraft.getInstance();
        IngameGui gui = mc.ingameGUI;
        mc.getProfiler().startSection("chiselsandbit2-selectedModePreview");

        PlayerEntity player = (PlayerEntity) mc.getRenderViewEntity();
        RenderSystem.enableBlend();
        //Render for each slot in the hotbar
        for (int slot = 8; slot >= -1; --slot) {
            int left = (window.getScaledWidth() / 2 - 87 + slot * 20);
            if(slot == -1) left -= 9; //Move 9 extra to the left if this is the offhand as it's a bit further away
            int top = (window.getScaledHeight() - 18);
            ItemStack item = slot == -1 ? player.inventory.offHandInventory.get(0) : player.inventory.mainInventory.get(slot);
            //TODO add support for bit bag preview items
            if (item.getItem() instanceof TypedItem && ((TypedItem) item.getItem()).showIconInHotbar()) {
                final IItemMode mode = ((TypedItem) item.getItem()).getSelectedMode(item);

                //Don't render if this mode has no icon.
                final ResourceLocation sprite = getModeIconLocation(mode);
                if (sprite == null) continue;
                //Png files are 16x16 for Minecraft's stitcher's sake but they all start at the top left pixel and have the real texture height in code. This allows us to properly scale them in code.
                //We take the ratio between the target scale (if we scale the image to be targetxtarget pixels, half as big) then we take the lower ratio, there's also an upper limit otherwise the single bit looks massive
                //We also target 11 width and 8 height as having a high icon obstructs the hotbar more than a wide one
                double ratio = Math.min(0.75d, Math.min(11.0d / mode.getTextureWidth(), 8.0d / mode.getTextureHeight()));
                //We also multiply the width/height with a scale to make them look better again as the sprites are 16x16.
                int width = (int) Math.round(mode.getTextureWidth() * ratio * (16.0d / mode.getTextureWidth()));
                int height = (int) Math.round(mode.getTextureHeight() * ratio * (16.0d / mode.getTextureHeight()));
                AbstractGui.blit(left, top, gui.getBlitOffset() + 200, width, height, mc.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(sprite));
            }
        }
        RenderSystem.disableBlend();
        mc.getProfiler().endSection();
    }

    //--- SUB CLASSES ---
    /**
     * Represents a box drawn by the tape measure.
     */
    public static class Measurement {
        private BitLocation first, second;
        private DimensionType dimension;
        private DyedItemColour colour;
        private IItemMode mode;

        public Measurement(BitLocation first, BitLocation second, DyedItemColour colour, IItemMode mode, DimensionType dimension) {
            this.first = first;
            this.second = second;
            this.colour = colour;
            this.mode = mode;
            this.dimension = dimension;
        }
    }

    //--- BIT BAG ---
    public void openBitBag() {
        ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(new COpenBitBagPacket());
    }
}
