package com.elmfer.prmod.parkour;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.mixin.EntityMixins;
import com.elmfer.prmod.parkour.RecordingSession.SessionState;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.render.ParticleArrow;
import com.elmfer.prmod.render.ParticleArrowLoop;
import com.elmfer.prmod.render.ParticleFinish;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.Vec3d;

public class PlaybackSession implements ParkourSession {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public final Recording recording;

    private ParticleArrow arrow;
    private ParticleFinish finish;
    private boolean playing = false;
    private boolean waitingForPlayer = false;
    private int frameNumber = 0;
    private Frame currentFrame = null;
    private int playbackCountdown = 0;
    private Vec3d startingPos;
    private boolean initiated = false;

    public PlaybackSession(Recording recording) {
        this.recording = recording;
        Frame startingFrame = recording.get(recording.startingFrame);
        startingPos = new Vec3d(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
        frameNumber = recording.startingFrame;

    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isWaitingForPlayer() {
        return waitingForPlayer;
    }

    /**
     * Set starting frame prior to starting playback. Sets the starting position to
     * that frame.
     * 
     * @param framePos The frame number to start at
     */
    public void startAt(int framePos) {
        boolean wasWaiting = waitingForPlayer;
        stop();

        framePos = Math.max(Math.min(framePos, recording.size() - 2), 0);

        Frame startingFrame = recording.get(framePos);

        startingPos = new Vec3d(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
        frameNumber = framePos;
        recording.startingFrame = framePos;
        initiated = false;

        if (wasWaiting) {
            spawnParticles();
            waitingForPlayer = true;
        }
    }

    /**
     * Called when a keybind or other event triggers the 'Record' action of this
     * session.
     * 
     * @return This session, or a new recording session if not playing
     */
    @Override
    public ParkourSession onRecord() {
        if (playing)
            return this;

        despawnParticles();
        RecordingSession session = new RecordingSession();
        session.onRecord();
        return session;
    }

    /**
     * Called when a keybind or other event triggers the 'Play' action of this
     * session.
     * 
     * @return This playback session
     */
    @Override
    public ParkourSession onPlay() {
        if (!playing && !waitingForPlayer) {
            waitingForPlayer = true;
            spawnParticles();
        } else if (playing || waitingForPlayer)
            stop();

        return this;
    }

    /**
     * Called when a keybind or other event triggers the 'Override' action of this
     * session.
     * 
     * @return This session if current session is not playing, or new recording
     *         session otherwise.
     */
    @Override
    public ParkourSession onOverride() {
        if (!playing)
            return this;

        RecordingSession overridingSession = new RecordingSession();

        overridingSession.recording = new Recording(recording.initPos);
        overridingSession.recordingToOverride = recording;
        overridingSession.isRecording = true;
        overridingSession.state = SessionState.READY_TO_FINISH_RECORDING;
        overridingSession.overrideStart = frameNumber - 1;
        overridingSession.recording.setName(recording.getName());

        stop();

        return overridingSession;
    }

    @Override
    public void onClientTick() {
        if (MC.isPaused())
            return;

        playbackCountdown = Math.max(0, playbackCountdown - 1);

        if (shouldPlaybackStart()) {
            playing = true;
            playbackCountdown = 10;

            MC.player.input = new ControlledInput();
            MC.player.setVelocity(new Vec3d(0, 0, 0));

            frameNumber = recording.startingFrame;

            waitingForPlayer = false;
            return;
        }

        if (!playing || playbackCountdown > 0)
            return;

        if (frameNumber < recording.size()) {
            if (!initiated) {
                MC.player.setPos(startingPos.x, startingPos.y, startingPos.z);
                if (arrow.isAlive())
                    arrow.markDead();
                initiated = true;
            }

            currentFrame = recording.get(frameNumber);
            currentFrame.setMovementInput(MC.player.input, MC.player);
            EventHandler.keyInputHUD.setFrame(currentFrame);

//          mc.player.setPos(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
            frameNumber++;
            return;
        }

        if (Config.isLoopMode() && recording.isLoop()) {
            initiated = false;
            frameNumber = recording.startingFrame;
            return;
        }

        stop();
    }

    @Override
    public void onRenderTick() {
        if (MC.isPaused())
            return;

        if (playbackCountdown > 0) {
            interpolatePlayerToStartingPosition();
            return;
        }

        if (!playing)
            return;

        float partialTicks = MC.getRenderTickCounter().getTickProgress(false);
        Frame prevFrame = recording.get(Math.max(0, frameNumber - 2));

        MC.player.lastHeadYaw = GraphicsHelper.lerpAngle(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
        MC.player.setYaw(MC.player.lastHeadYaw);
        MC.player.lastPitch = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
        MC.player.setPitch(MC.player.lastPitch);

        if (isPlayerOutOfSyncWithPlayback(prevFrame)) {
            TextContent errorMessageContent = new TranslatableTextContent("com.prmod.playback_failed",
                    "Playback failed", new Object[0]);

            MutableText errorMessage = MutableText.of(errorMessageContent);
            errorMessage.setStyle(errorMessage.getStyle().withColor(0xff0000));

            MC.inGameHud.getChatHud().addMessage(errorMessage);

            stop();
            return;
        }

        Vec3d currentPos = new Vec3d(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
        ((EntityMixins) MC.player).setPosDirect(currentPos);
        EntityDimensions dimensions = ((EntityMixins) MC.player).getDimensions();
        MC.player.setBoundingBox(dimensions.getBoxAt(currentPos));
    }

    /**
     * Sometimes in a multiplayer server, the player could lag or be yanked to a
     * previous position. To prevent any potential illegal movements (and risk of
     * being kicked from the server), this method will determine if playback is not
     * going as intended by seeing if the player's position is not in sync with the
     * playback. It sort of takes into account player teleportation to prevent false
     * positives.
     * 
     * @param frame Frame to compare against
     * @return True if player is not in the intended position (e.g. multiplayer
     *         server kicks back player to a previous position)
     */
    private boolean isPlayerOutOfSyncWithPlayback(Frame frame) {
        Vec3d playerPos = MC.player.getPos();
        Vec3d framePos = new Vec3d(frame.posX, frame.posY, frame.posZ);

        return 5.0 < playerPos.distanceTo(framePos) && playerPos.distanceTo(framePos) < 7.0;
    }

    /**
     * Smoothly interpolate the player's current position into the starting
     * position.
     */
    private void interpolatePlayerToStartingPosition() {
        float partialTicks = MC.getRenderTickCounter().getTickProgress(false);

        float countdownAmount = (10 - playbackCountdown + partialTicks) / 10;
        Frame firstFrame = recording.get(Math.max(0, recording.startingFrame - 1));

        MC.player.setYaw(GraphicsHelper.lerpAngle(countdownAmount, MC.player.headYaw, firstFrame.headYaw));
        MC.player.setPitch(GraphicsHelper.lerp(countdownAmount, MC.player.getPitch(), firstFrame.headPitch));
        MC.player.lastBodyYaw = MC.player.lastHeadYaw = MC.player.headYaw = MC.player.getYaw();
        MC.player.lastPitch = MC.player.getPitch();

        Vec3d pos = MC.player.getPos();
        double posX = GraphicsHelper.lerp(countdownAmount, pos.x, firstFrame.posX);
        double posY = GraphicsHelper.lerp(countdownAmount, pos.y, firstFrame.posY);
        double posZ = GraphicsHelper.lerp(countdownAmount, pos.z, firstFrame.posZ);
        pos = new Vec3d(posX, posY, posZ);

        ((EntityMixins) MC.player).setPosDirect(pos);
        EntityDimensions dimensions = ((EntityMixins) MC.player).getDimensions();
        MC.player.setBoundingBox(dimensions.getBoxAt(pos));
    }

    private boolean shouldPlaybackStart() {
        return waitingForPlayer && startingPos.distanceTo(MC.player.getPos()) < 0.25;
    }

    private void stop() {
        despawnParticles();
        playing = false;
        waitingForPlayer = false;

        // Vec3 playerPos = mc.player.getPositionVec();
        // double motionX = playerPos.x - mc.player.prevPosX;
        // double motionY = playerPos.y - mc.player.prevPosY;
        // double motionZ = playerPos.z - mc.player.prevPosZ;
        // mc.player.setVelocity(motionX, motionY, motionZ);

        MC.player.input = new KeyboardInput(MC.options);
    }

    private void spawnParticles() {
        despawnParticles();

        boolean inLoopMode = recording.isLoop() && Config.isLoopMode();

        if (inLoopMode)
            arrow = new ParticleArrowLoop(MC.world, startingPos.x, startingPos.y, startingPos.z);
        else
            arrow = new ParticleArrow(MC.world, startingPos.x, startingPos.y, startingPos.z);

        MC.particleManager.addParticle(arrow);

        if (!inLoopMode) {
            finish = new ParticleFinish(MC.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
            MC.particleManager.addParticle(finish);
        }
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    private void despawnParticles() {
        if (finish != null)
            finish.markDead();
        if (arrow != null)
            arrow.markDead();
    }

    @Override
    public boolean isActive() {
        return playing;
    }

    @Override
    public void cleanUp() {
        despawnParticles();
    }
}
