package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Region;
import nl.dgoossens.chiselsandbits2.client.render.ChiselLayer;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockBaked;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import org.lwjgl.opengl.GL11;

import java.lang.ref.SoftReference;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundRenderer implements Callable<Tessellator> {
    private final static Random RAND = new Random();

    private final static BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
    private final static Queue<CBTessellatorRefHold> previousTessellators = new LinkedBlockingQueue<>();
    private final List<ChiseledBlockTileEntity> myPrivateList;

    private final Region cache;
    private final BlockPos chunkOffset;

    static class CBTessellatorRefNode {
        private boolean done = false;
        public CBTessellatorRefNode() { ChiseledBlockTER.activeTess.incrementAndGet(); }
        public void dispose() {
            if(!done) {
                ChiseledBlockTER.activeTess.decrementAndGet();
                done = true;
            }
        }
        @Override
        protected void finalize() throws Throwable { dispose(); }
    }

    static class CBTessellatorRefHold {
        SoftReference<Tessellator> myTess;
        CBTessellatorRefNode node;
        public CBTessellatorRefHold(final CBTessellator cbTessellator) { myTess = new SoftReference<>(cbTessellator); node = cbTessellator.node; }
        public Tessellator get() { return myTess == null ? null : myTess.get(); }
        public void dispose() {
            if(myTess != null) {
                node.dispose();
                myTess = null;
            }
        }
        @Override
        protected void finalize() throws Throwable { dispose(); }
    }

    static class CBTessellator extends Tessellator {
        CBTessellatorRefNode node = new CBTessellatorRefNode();
        public CBTessellator(final int bufferSize) { super(bufferSize); }
    }

    public static void submitTessellator(final Tessellator t) {
        if(t instanceof CBTessellator) previousTessellators.add(new CBTessellatorRefHold((CBTessellator) t));
        else throw new RuntimeException("Invalid TESS submitted for re-use!");
    }

    public BackgroundRenderer(final Region cache, final BlockPos chunkOffset, final List<ChiseledBlockTileEntity> myList) {
        myPrivateList = myList; this.cache = cache; this.chunkOffset = chunkOffset;
    }

    @Override
    public Tessellator call() throws Exception {
        Tessellator tessellator = null;

        do {
            do {
                final CBTessellatorRefHold holder = previousTessellators.poll();
                if(holder != null) {
                    tessellator = holder.get();
                    if(tessellator == null)
                        holder.dispose();
                }
            }
            while(tessellator == null && !previousTessellators.isEmpty());

            // no previous queues?
            if(tessellator == null) {
                synchronized(CBTessellator.class) {
                    if(ChiseledBlockTER.activeTess.get() < ChiseledBlockTER.getMaxTessalators())
                        tessellator = new CBTessellator(2109952);
                    else Thread.sleep(10);
                }
            }
        }
        while(tessellator == null);
        final BufferBuilder buffer = tessellator.getBuffer();

        try {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            buffer.setTranslation(-chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ());
        } catch(final IllegalStateException e) { e.printStackTrace(); }

        for(final ChiseledBlockTileEntity tx : myPrivateList) {
            if(!tx.isRemoved()) {
                for(final ChiselLayer lx : ChiselLayer.values()) {
                    final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel(tx, lx);
                    if(!model.isEmpty()) {
                        //We don't use the model data argument at the end.
                        blockRenderer.getBlockModelRenderer().renderModel(cache, model, tx.getBlockState(), tx.getPos(), buffer, true, RAND, RAND.nextLong(), tx.getModelData());

                        if(Thread.interrupted()) {
                            buffer.finishDrawing();
                            submitTessellator(tessellator);
                            return null;
                        }
                    }
                }

                //TODO final VoxelNeighborRenderTracker rTracker = estate.getValue(BlockChiseled.UProperty_VoxelNeighborState);
                //if(rTracker != null) {
                //    for(final BlockRenderLayer brl : mcLayers) {
                //        rTracker.setAbovelimit(brl, faceCount[brl.ordinal()]);
                //        faceCount[brl.ordinal()] = 0;
                //    }
                //}
            }
        }

        if(Thread.interrupted()) {
            buffer.finishDrawing();
            submitTessellator(tessellator);
            return null;
        }
        return tessellator;
    }
}
