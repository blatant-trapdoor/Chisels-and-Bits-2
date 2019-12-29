package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Region;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockBaked;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import org.lwjgl.opengl.GL11;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class BackgroundRenderer implements Callable<Tessellator> {
    private final static Random random = new Random();
    private final static BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
    private final static Queue<CBTessellatorRefHold> previousTessellators = new LinkedBlockingQueue<>();

    private final Collection<ChiseledBlockTileEntity> myPrivateList;
    private final Region cache;
    private final BlockPos chunkOffset;

    BackgroundRenderer(final Region cache, final BlockPos chunkOffset, final Collection<ChiseledBlockTileEntity> myList) {
        this.myPrivateList = myList;
        this.cache = cache;
        this.chunkOffset = chunkOffset;
    }

    static void submitTessellator(final Tessellator t) {
        previousTessellators.add(new CBTessellatorRefHold(t));
    }

    @Override
    public Tessellator call() {
        long t = System.currentTimeMillis();
        Tessellator tessellator = null;
        try {
            do {
                final CBTessellatorRefHold holder = previousTessellators.poll();
                if (holder != null) {
                    tessellator = holder.get();
                    if (tessellator == null)
                        holder.dispose();
                }
            }
            while (tessellator == null && !previousTessellators.isEmpty());

            // no previous queues?
            if (tessellator == null) {
                synchronized (Tessellator.class) {
                    tessellator = new Tessellator(2109952);
                }
            }
            final BufferBuilder buffer = tessellator.getBuffer();

            try {
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                buffer.setTranslation(-chunkOffset.getX(), -chunkOffset.getY(), -chunkOffset.getZ());
            } catch (final IllegalStateException e) {
                e.printStackTrace();
            }

            //TODO System.out.println("[CHUNK] Rendering chunk with "+myPrivateList.size()+" tiles.");
            for (final ChiseledBlockTileEntity tx : myPrivateList) {
                if (!tx.isRemoved()) {
                    final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel(tx);
                    if (!model.isEmpty()) {
                        blockRenderer.getBlockModelRenderer().renderModel(cache, model, tx.getBlockState(), tx.getPos(), buffer, true, random, random.nextLong(), tx.getModelData());
                        if (Thread.interrupted()) break;
                    }
                }
            }

            if (Thread.interrupted()) {
                buffer.finishDrawing();
                submitTessellator(tessellator);
                return null;
            }

            //TODO System.out.println("[TESS] Built tesselator in "+(System.currentTimeMillis()-t)+" ms.");
            return tessellator;
        } catch(Exception x) {
            //Catch exceptions and print them here.
            x.printStackTrace();
            return tessellator;
        }
    }

    static class CBTessellatorRefHold {
        SoftReference<Tessellator> myTess;

        private CBTessellatorRefHold(final Tessellator cbTessellator) {
            myTess = new SoftReference<>(cbTessellator);
        }

        private void dispose() {
            if (myTess != null)
                myTess = null;
        }

        public Tessellator get() {
            return myTess == null ? null : myTess.get();
        }

        @Override
        protected void finalize() {
            dispose();
        }
    }
}
