package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.client.render.ter.ChiseledBlockTER;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ClientSide {
    //--- GENERAL SETUP ---
    public void setup(final FMLCommonSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(ChiseledBlockTileEntity.class, new ChiseledBlockTER());
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

    //--- DRAW LAST ---
    private byte frameId = Byte.MIN_VALUE;

    /**
     * Return the current frame's id, please note that this id is arbitrary.
     * The id is a byte that is supposed to roll over. (so don't worry)
     *
     * This frame id is stored in TER data to make sure TER's don't render
     * twice or more a frame.
     */
    public byte getFrameId() { return frameId; }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void drawLast(final RenderWorldLastEvent e) {
        frameId++; //Increase the frame id every time a new frame is drawn.
    }

    //--- ITEM SCROLL ---
    /*@SubscribeEvent  Waiting for Forge PR...
    @OnlyIn(Dist.CLIENT)
    public void wheelEvent(final InputEvent.MouseScrollEvent me) {
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
    private BitLocation drawStart;
    private ItemMode lastTool;

    public BitLocation getStartPos() { return drawStart; }
    public void pointAt(@Nonnull final ItemMode type, @Nonnull final BitLocation pos) {
        if (drawStart == null) {
            drawStart = pos;
            lastTool = type;
        }
    }
}
