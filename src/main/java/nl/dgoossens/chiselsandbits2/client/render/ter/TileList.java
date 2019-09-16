package nl.dgoossens.chiselsandbits2.client.render.ter;

import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TileList implements Iterable<ChiseledBlockTileEntity> {
    private static class MWR<T> extends WeakReference<ChiseledBlockTileEntity> {
        public MWR(final ChiseledBlockTileEntity referent) { super(referent); }

        @Override
        public int hashCode() {
            final ChiseledBlockTileEntity h = get();
            if(h==null) return super.hashCode();
            return h.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if(this == obj) return true;

            final ChiseledBlockTileEntity o = get();
            if(o != null) {
                final Object b = obj instanceof WeakReference ? ((WeakReference<?>) obj).get() : obj;
                if(b instanceof ChiseledBlockTileEntity)
                    return ((ChiseledBlockTileEntity) b).getPos().equals(o.getPos());
                return o == b;
            }
            return false;
        }
    };

    private final ArrayList<WeakReference<ChiseledBlockTileEntity>> tiles = new ArrayList<>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public Lock getReadLock() { return r; }
    public Lock getWriteLock() { return w; }

    @Override
    public Iterator<ChiseledBlockTileEntity> iterator() {
        final Iterator<WeakReference<ChiseledBlockTileEntity>> o = tiles.iterator();
        return new Iterator<ChiseledBlockTileEntity>() {
            ChiseledBlockTileEntity which;

            @Override
            public ChiseledBlockTileEntity next() { return which; }

            @Override
            public void remove() {
                // nope!
            }

            @Override
            public boolean hasNext() {
                while(o.hasNext()) {
                    final WeakReference<ChiseledBlockTileEntity> w = o.next();
                    final ChiseledBlockTileEntity t = w.get();

                    if(t != null) {
                        which = t;
                        return true;
                    } else {
                        ChiseledBlockTER.addNextFrameTask(() ->  {
                            getWriteLock().lock();
                            try {
                                tiles.remove(w);
                            } finally {
                                getWriteLock().unlock();
                            }
                        });
                    }
                }
                return false;
            }
        };
    }

    public void add(final ChiseledBlockTileEntity which) {
        tiles.add(new MWR<ChiseledBlockTileEntity>(which));
    }
    public void remove(final ChiseledBlockTileEntity which) {
        tiles.remove(new MWR<ChiseledBlockTileEntity>(which));
    }
    public boolean contains(final ChiseledBlockTileEntity which) {
        return tiles.contains(new MWR<ChiseledBlockTileEntity>(which));
    }

    public boolean isEmpty() { return !iterator().hasNext(); }

    public List<ChiseledBlockTileEntity> createCopy() {
        final ArrayList<ChiseledBlockTileEntity> t = new ArrayList<>(tiles.size());
        getReadLock().lock();
        try {
            for(final ChiseledBlockTileEntity x : this) t.add(x);
            return t;
        } finally {
            getReadLock().unlock();
        }
    }
}
