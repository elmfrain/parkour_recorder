package com.elmfer.prmod.parkour;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.render.ParticleArrowLoop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;

public class RecordingSession implements ParkourSession {

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    protected Recording recording = null;
    protected Recording recordingToOverride = null;

    protected int overrideStart = 0;
    protected boolean isRecording = false;
    protected SessionState state = SessionState.READY_TO_RECORD;

    private ParticleArrowLoop arrow;
    private boolean waitingForPlayer = false;

    public Recording getRecording() {
        return recording;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isOverriding() {
        return recordingToOverride != null;
    }

    public boolean isWaitingForPlayer() {
        return waitingForPlayer;
    }

    /**
     * Called when a keybind or other event triggers the 'Record' action of this
     * session.
     * 
     * @return This session, or a new playback session if the recording had been
     *         finalized.
     */
    @Override
    public ParkourSession onRecord() {
        switch (state) {
        case READY_TO_RECORD:
            if (!(mc.player.input instanceof KeyboardInput))
                mc.player.input = new KeyboardInput(mc.options);

            if (recording == null)
                recording = new Recording(mc.player.getPos());

            isRecording = true;

            if (Config.isLoopMode()) {
                spawnParticles();
                state = SessionState.WAITING_TO_FINISH_LOOPED_RECORDING;
                break;
            }

            state = SessionState.READY_TO_FINISH_RECORDING;

            break;
        case WAITING_TO_FINISH_LOOPED_RECORDING:
            waitingForPlayer = true;
            break;
        case READY_TO_FINISH_RECORDING:
            recording.lastPos = mc.player.getPos();

            if (isOverriding()) {
                String name = recordingToOverride.originalName != null ? recordingToOverride.originalName + " - " : "";
                Recording record = recordingToOverride.subList(0, overrideStart);
                record.lastPos = recording.lastPos;
                record.addAll(recording);
                record.setName(name + Recording.getFormattedTime());

                EventHandler.addToHistory(record);
            } else {
                String name = recording.originalName != null ? recording.originalName + " - " : "";
                recording.rename(name + Recording.getFormattedTime());
                EventHandler.addToHistory(recording);
            }

            isRecording = false;

            despawnParticles();

            return new PlaybackSession(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
        }

        return this;
    }

    /**
     * Called when a keybind or other event triggers the 'Play' action of this
     * session.
     * 
     * @return A new playback session if the current recording is not empty, or this
     *         session otherwise.
     */
    @Override
    public ParkourSession onPlay() {
        if (recording == null)
            return this;

        if (isRecording) {
            state = SessionState.READY_TO_FINISH_RECORDING;
            onRecord();
        }
        PlaybackSession playback = new PlaybackSession(
                EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
        playback.onPlay();

        return playback;
    }

    /**
     * Called when a keybind or other event triggers the 'Override' action of this
     * session.
     * 
     * @return This session if current session is not recording, or new playback
     *         session otherwise.
     */
    @Override
    public ParkourSession onOverride() {
        if (!isOverriding() || !isRecording)
            return this;

        state = SessionState.READY_TO_FINISH_RECORDING;
        return onRecord();
    }

    @Override
    public void onClientTick() {
        if (!isRecording || mc.isPaused())
            return;

        recording.add(new Frame(mc.options, mc.player));

        if (shouldFinalizeLoopedRecording()) {
            waitingForPlayer = false;

            state = SessionState.READY_TO_FINISH_RECORDING;

            EventHandler.session = onRecord();

            // Set last pos to init pos to mark as loop recording data
            recording.lastPos = recording.initPos;
        }
    }

    @Override
    public void onRenderTick() {

    }

    private boolean shouldFinalizeLoopedRecording() {
        return waitingForPlayer && recording.initPos.distanceTo(mc.player.getPos()) < 0.25;
    }

    private void spawnParticles() {
        despawnParticles();

        arrow = new ParticleArrowLoop(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
        mc.particleManager.addParticle(arrow);
    }

    private void despawnParticles() {
        if (arrow != null)
            arrow.markDead();
    }

    @Override
    public boolean isActive() {
        return isRecording;
    }

    @Override
    public void cleanUp() {
    }

    protected static enum SessionState {
        READY_TO_RECORD, WAITING_TO_FINISH_LOOPED_RECORDING, READY_TO_FINISH_RECORDING
    }

}