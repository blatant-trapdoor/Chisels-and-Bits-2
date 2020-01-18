package nl.dgoossens.chiselsandbits2.client.render.chiseledblock;

import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.chiseledblock.ter.RenderCache;
import nl.dgoossens.chiselsandbits2.client.render.chiseledblock.ter.UploadTracker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Tracks all ongoing rendering tasks in a given world.
 */
public class WorldTracker {
    private final HashSet<RenderCache> futureTrackers = new HashSet<>();
    private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<>();

    /**
     * Returns true if a new future tracker can be added to this tracker.
     */
    public boolean acceptsNewTasks(RenderCache cache) {
        return futureTrackers.contains(cache) || futureTrackers.size() < ChiselsAndBits2.getInstance().getClient().getRenderingManager().getMaxTesselators();
    }

    public Queue<Runnable> getNextFrameTasks() {
        return nextFrameTasks;
    }

    public HashSet<RenderCache> getFutureTrackers() {
        return futureTrackers;
    }

    public Queue<UploadTracker> getUploaders() {
        return uploaders;
    }
}