package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.block.BitOperation;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IBitModifyItem;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.impl.ItemMode;
import nl.dgoossens.chiselsandbits2.common.impl.MenuAction;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.items.ChiselMimicItem;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;
import nl.dgoossens.chiselsandbits2.common.items.TypedItem;
import nl.dgoossens.chiselsandbits2.common.network.client.CPlaceBlockPacket;
import nl.dgoossens.chiselsandbits2.common.network.client.CWrenchBlockPacket;
import nl.dgoossens.chiselsandbits2.common.utils.ClientItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.RotationUtil;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChiselEvent {
    /**
     * We track the last time the player clicked to chisel to determine when 300ms have passed before
     * we allow another click. This event is client-sided so we only need a single variable.
     */
    private static long lastClick = System.currentTimeMillis();

    /**
     * Will be removed when the proper ClickInputEvent gets added so we don't have to deal with
     * stupid minecraft bugs and de-syncs.
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent e) {
        if(e instanceof PlayerInteractEvent.RightClickEmpty || e instanceof PlayerInteractEvent.LeftClickEmpty) {
            if(e.getPlayer().getHeldItemMainhand().getItem() instanceof MorphingBitItem && e.getPlayer().isSneaking()) {
                if (System.currentTimeMillis() - lastClick < 150) return;
                lastClick = System.currentTimeMillis();

                final ItemStack i = e.getPlayer().getHeldItemMainhand();
                final PlayerEntity player = e.getPlayer();

                boolean b = ((MorphingBitItem) i.getItem()).isLocked(i);
                //Set the currently selected type too just in case
                ItemPropertyUtil.setSelectedVoxelWrapper(player, i, ItemPropertyUtil.getGlobalSelectedVoxelWrapper(), false);
                ClientItemPropertyUtil.setLockState(!b);
                if(b) player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.unlocked_mb"), true);
                else player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.locked_mb"), true);

                e.setCanceled(true);
                return;
            }
        }

        boolean leftClick = e instanceof PlayerInteractEvent.LeftClickBlock;
        if (!leftClick && !(e instanceof PlayerInteractEvent.RightClickBlock)) return;

        final PlayerEntity player = Minecraft.getInstance().player;
        ItemStack i = player.getHeldItemMainhand();
        if(i.getItem() instanceof IBitModifyItem) {
            IBitModifyItem it = (IBitModifyItem) i.getItem();
            for(IBitModifyItem.ModificationType modificationType : IBitModifyItem.ModificationType.values()) {
                if(it.canPerformModification(modificationType) && it.validateUsedButton(modificationType, leftClick, i)) {
                    e.setCanceled(true);

                    if (System.currentTimeMillis() - lastClick < 150) return;
                    lastClick = System.currentTimeMillis();

                    switch(modificationType) {
                        case BUILD:
                        case EXTRACT:
                        {
                            if(!(i.getItem() instanceof ChiselMimicItem)) return;
                            ChiselMimicItem tit = (ChiselMimicItem) i.getItem();

                            //Chisel
                            RayTraceResult rtr = ChiselUtil.rayTrace(player);
                            if (!(rtr instanceof BlockRayTraceResult) || rtr.getType() != RayTraceResult.Type.BLOCK) return;

                            final BitOperation operation = leftClick ? BitOperation.REMOVE : (tit.isSwapping(i) ? BitOperation.SWAP : BitOperation.PLACE);
                            startChiselingBlock((BlockRayTraceResult) rtr, tit.getSelectedMode(i), player, operation, i);
                            break;
                        }
                        case ROTATE:
                        case MIRROR:
                        {
                            if(!(i.getItem() instanceof TypedItem)) return;
                            TypedItem tit = (TypedItem) i.getItem();

                            //Wrench
                            performBlockRotation(tit.getSelectedMode(i), player);
                            break;
                        }
                        case PLACE:
                        {
                            //Chiseled Block
                            IItemMode mode = ClientItemPropertyUtil.getGlobalCBM();
                            RayTraceResult rtr = ChiselUtil.rayTrace(player);
                            if (!(rtr instanceof BlockRayTraceResult) || rtr.getType() != RayTraceResult.Type.BLOCK) return;
                            performPlaceBlock(i, (BlockRayTraceResult) rtr, mode, player);
                            break;
                        }
                        case CUSTOM:
                        {
                            it.performCustomModification(leftClick, i);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle the block chiselling with the given bit operation.
     */
    public static void startChiselingBlock(final BlockRayTraceResult rayTrace, final IItemMode mode, final PlayerEntity player, final BitOperation operation, final ItemStack stack) {
        if (!player.world.isRemote)
            throw new UnsupportedOperationException("Block chiseling can only be started on the client-side.");

        final BitLocation location = new BitLocation(rayTrace, true, operation);
        final BlockPos pos = location.getBlockPos(); //We get the location from the bitlocation because that takes placement offset into account. (placement can go into neighbouring block)
        final BlockState state = player.world.getBlockState(pos);
        final Direction face = rayTrace.getFace();
        //We use a the constructor for BlockRayTraceResult from a method in BlockItemUseContext.
        BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d((double) pos.getX() + 0.5D + (double) face.getXOffset() * 0.5D, (double) pos.getY() + 0.5D + (double) face.getYOffset() * 0.5D, (double) pos.getZ() + 0.5D + (double) face.getZOffset() * 0.5D), face, pos, false)));
        if (!state.isReplaceable(context) && !ChiselsAndBits2.getInstance().getAPI().getRestrictions().canChiselBlock(state)) return; //You can place on replacable blocks.
        if (!ChiselUtil.canChiselPosition(pos, player, state, rayTrace.getFace())) return;

        if(ItemPropertyUtil.isItemMode(player.getHeldItemMainhand(), ItemMode.CHISEL_DRAWN_REGION)) {
            ClientSide clientSide = ChiselsAndBits2.getInstance().getClient();
            //If we don't have a selection start yet select the clicked location.
            if(!clientSide.hasSelectionStart(operation)) {
                clientSide.setSelectionStart(operation, location);
                return;
            }
        }

        //Default is -1 for remove operations, we only get a placed bit when not removing
        //We determine the placed bit on the client and include it in the packet so we can reuse the BlockItemUseContext from earlier.
        int placedBit = -1;
        if(!operation.equals(BitOperation.REMOVE)) {
            //If this is a locked morphing bit place that specific item
            if(stack.getItem() instanceof MorphingBitItem && ((MorphingBitItem) stack.getItem()).isLocked(stack)) placedBit = ((MorphingBitItem) stack.getItem()).getSelected(stack).getId();
            else placedBit = ItemPropertyUtil.getGlobalSelectedVoxelWrapper(player).getPlacementBitId(context);
        }

        //If we couldn't find a selected type, don't chisel.
        if (placedBit == VoxelBlob.AIR_BIT) {
            player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.no_selected_type"), true);
            return;
        }

        if(ItemPropertyUtil.isItemMode(player.getHeldItemMainhand(), ItemMode.CHISEL_DRAWN_REGION)) {
            final CChiselBlockPacket pc = new CChiselBlockPacket(operation, ChiselsAndBits2.getInstance().getClient().getSelectionStart(operation), location, face, ItemMode.CHISEL_DRAWN_REGION, placedBit);
            ChiselsAndBits2.getInstance().getClient().resetSelectionStart();
            ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(pc);
        } else if(mode instanceof ItemMode) {
            final CChiselBlockPacket pc = new CChiselBlockPacket(operation, location, face, (ItemMode) mode, placedBit);
            ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(pc);
        }
    }

    /**
     * Handles placing a chiseled block.
     */
    public static void performPlaceBlock(final ItemStack item, final BlockRayTraceResult rayTrace, final IItemMode mode, final PlayerEntity player) {
        if (!player.world.isRemote)
            throw new UnsupportedOperationException("Block placement can only be started on the client-side.");
        if(!(mode instanceof ItemMode))
            throw new UnsupportedOperationException("Unsupported placement mode type!");

        //Test if we can currently place
        final BitLocation bl = new BitLocation(rayTrace, true, BitOperation.PLACE);
        final Direction face = rayTrace.getFace();
        BlockPos offset = rayTrace.getPos();

        final NBTBlobConverter nbt = new NBTBlobConverter();
        nbt.readChiselData(item.getChildTag(ChiselUtil.NBT_BLOCKENTITYTAG), VoxelVersions.getDefault());

        boolean canPlace = true;
        if (player.isSneaking() && !ClientItemPropertyUtil.getGlobalCBM().equals(ItemMode.CHISELED_BLOCK_GRID)) {
            if (!BlockPlacementLogic.isPlaceableOffgrid(player, player.world, face, bl, player.getHeldItemMainhand())) canPlace = false;
        } else {
            if((!ChiselUtil.isBlockReplaceable(player, player.world, offset, face, false) && ClientItemPropertyUtil.getGlobalCBM() == ItemMode.CHISELED_BLOCK_GRID) || (!(player.world.getTileEntity(offset) instanceof ChiseledBlockTileEntity) && !BlockPlacementLogic.isNormallyPlaceable(player, player.world, offset, face, nbt)))
                offset = offset.offset(face);

            if(!BlockPlacementLogic.isNormallyPlaceable(player, player.world, offset, face, nbt))
                canPlace = false;
        }
        if(!canPlace) {
            player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.not_placeable"), true);
            return;
        }

        //Send placement packet
        final CPlaceBlockPacket pc = new CPlaceBlockPacket(offset, bl, face, (ItemMode) mode, player.isSneaking());
        ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(pc);
    }

    /**
     * Handle the block being rotated.
     */
    public static void performBlockRotation(final IItemMode mode, final PlayerEntity player) {
        if (!player.world.isRemote)
            throw new UnsupportedOperationException("Block rotation can only be started on the client-side.");
        if(!(mode instanceof ItemMode))
            throw new UnsupportedOperationException("Unsupported placement mode type!");

        final RayTraceResult rayTrace = Minecraft.getInstance().objectMouseOver;
        if(!(rayTrace instanceof BlockRayTraceResult) || rayTrace.getType() != RayTraceResult.Type.BLOCK)
            return;

        final BlockPos pos = ((BlockRayTraceResult) rayTrace).getPos();
        final BlockState state = player.world.getBlockState(pos);
        final Direction face = ((BlockRayTraceResult) rayTrace).getFace();

        if (!ChiselUtil.canChiselPosition(pos, player, state, face)) return;
        if (mode.equals(ItemMode.WRENCH_MIRROR)) {
            if (!RotationUtil.hasMirrorableState(state)) {
                player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.not_mirrorable"), true);
                return;
            }
        } else if(mode.equals(ItemMode.WRENCH_ROTATE) || mode.equals(ItemMode.WRENCH_ROTATECCW)) {
            if (!RotationUtil.hasRotatableState(state)) {
                player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.not_rotatable"), true);
                return;
            }
        }

        final CWrenchBlockPacket pc = new CWrenchBlockPacket(pos, face, (ItemMode) mode);
        ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(pc);
    }
}
