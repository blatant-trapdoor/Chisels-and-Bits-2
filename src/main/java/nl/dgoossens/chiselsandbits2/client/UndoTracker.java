package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.network.client.CUndoPacket;

import java.util.*;

/**
 * Tracks recent bit operations and allows you to undo them!
 */
public class UndoTracker {
    private int level = -1;
    private boolean recording = true;
    private boolean grouping = false;
    private boolean hasCreatedGroup = false;
    private final List<UndoStep> undoLevels = new ArrayList<>();
    private RuntimeException groupStarted;

    /**
     * Cleans up the undo tracker.
     */
    public void clean() {
        level = -1;
        grouping = false;
        hasCreatedGroup = false;
        undoLevels.clear();
        groupStarted = null;
    }

    /**
     * Adds a new set of bits changed to be tracked.
     */
    public void add(final World world, final BlockPos pos, final VoxelBlobStateReference before, final VoxelBlobStateReference after) {
        if(pos != null && world != null && world.isRemote && recording) {
            //Fix level pointer
            if(undoLevels.size() > level && !undoLevels.isEmpty()) {
                final int end = Math.max(-1, level);
                for(int x = undoLevels.size() - 1; x > end; --x)
                    undoLevels.remove(x);
            }
            while(undoLevels.size() > ChiselsAndBits2.getInstance().getConfig().maxUndoLevel.get())
                undoLevels.remove(0);

            if(level >= undoLevels.size()) level = undoLevels.size() - 1;

            //Check if group, otherwise add new step.
            if(grouping && hasCreatedGroup) {
                final UndoStep current = undoLevels.get(undoLevels.size() - 1);
                final UndoStep newest = new UndoStep(world, pos, before, after);
                undoLevels.set(undoLevels.size() - 1, newest);
                newest.chain(current);
                return;
            }

            undoLevels.add(new UndoStep(world, pos, before, after));
            hasCreatedGroup = true;
            level = undoLevels.size() - 1;
        }
    }

    /**
     * Triggers one undo operation.
     */
    public void undo() {
        if(level > -1) {
            final UndoStep step = undoLevels.get(level);
            final PlayerEntity player = Minecraft.getInstance().player;

            if(step.isCorrect(player) && replayChanges(player, step, true)) level--;
        } else {
            Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".undo.nothing_to_undo"), true);
        }
    }

    /**
     * Triggers one redo operation.
     */
    public void redo() {
        if(level > -1) {
            final UndoStep step = undoLevels.get(level);
            final PlayerEntity player = Minecraft.getInstance().player;

            if(step.isCorrect(player) && replayChanges(player, step, false)) level++;
        } else {
            Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".undo.nothing_to_redo"), true);
        }
    }

    /**
     * Starts a new group which will group all operations together into one
     * undo operation until the group is closed.
     */
    public void beginGroup(PlayerEntity player) {
        if(ignorePlayer(player)) return;

        if(grouping) throw new RuntimeException("Exception opening a group, previous group already started.", groupStarted);

        groupStarted = new RuntimeException("Group was not closed properly");
        groupStarted.fillInStackTrace();

        grouping = true;
        hasCreatedGroup = false;
    }

    /**
     * Closes the current group.
     */
    public void endGroup(PlayerEntity player) {
        if(ignorePlayer(player)) return;

        if(!grouping) throw new RuntimeException("Closing undo group, but no undo group was started!");

        groupStarted = null;
        grouping = false;
    }

    /**
     * Checks if a player should be ignored.
     */
    private boolean ignorePlayer(PlayerEntity player) {
        //We always ignore this on the server side.
        return player.world == null || !player.world.isRemote || FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    /**
     * Replays the changes of the previous step.
     */
    private boolean replayChanges(final PlayerEntity player, UndoStep step, final boolean backwards) {
        boolean done = false;

        while(step != null && replayAction(player, step.getPosition(), backwards ? step.getAfter() : step.getBefore(), backwards ? step.getBefore() : step.getAfter())) {
            step = step.getChained();
            if(step == null)
                done = true;
        }

        return done;
    }

    /**
     * Replays a single action.
     */
    private boolean replayAction(final PlayerEntity player, final BlockPos pos, final VoxelBlobStateReference before, final VoxelBlobStateReference after) {
        try {
            recording = false;
            final CUndoPacket packet = new CUndoPacket(pos, before, after);
            if(packet.handle(player)) {
                ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(packet);
                return true;
            }
            return false;
        } finally {
            recording = true;
        }
    }
}
