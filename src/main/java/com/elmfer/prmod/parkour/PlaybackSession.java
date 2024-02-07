package com.elmfer.prmod.parkour;

import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.render.ParticleArrow;
import com.elmfer.prmod.render.ParticleArrowLoop;
import com.elmfer.prmod.render.ParticleFinish;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.Vec3d;

public class PlaybackSession implements ParkourSession {

	public static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public final Recording recording;
	private ParticleArrow arrow;
	private ParticleFinish finish;
	private boolean isPlaying = false;
	private boolean waitingForPlayer = false;
	private int frameNumber = 0;
	private Frame currentFrame = null;
	private int playbackCountdown = 0;
	private Vec3d startingPos;
	private boolean initiated = false;
	
	public PlaybackSession(Recording recording)
	{
		this.recording = recording;
		Frame startingFrame = recording.get(recording.startingFrame);
		startingPos = new Vec3d(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
		frameNumber = recording.startingFrame;
		
	}
	
	public boolean isPlaying()
	{ return isPlaying; }
	
	public boolean isWaitingForPlayer()
	{ return waitingForPlayer; }
	
	public void startAt(int framePos)
	{
		boolean wasWaiting = waitingForPlayer;
		stop();
		
		framePos = Math.max(Math.min(framePos, recording.size() - 2), 0);
		Frame startingFrame = recording.get(framePos);
		startingPos = new Vec3d(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
		frameNumber = framePos;
		recording.startingFrame = framePos;
		initiated = false;
		
		if(wasWaiting)
		{
			spawnParticles();
			waitingForPlayer = true;
		}
	}
	
	@Override
	public ParkourSession onRecord()
	{
		if(!isPlaying)
		{
			despawnParticles();
			RecordingSession session = new RecordingSession();
			session.onRecord();
			return session;
		}
		return this;
	}

	@Override
	public ParkourSession onPlay()
	{
		if(!isPlaying && !waitingForPlayer)
		{
			waitingForPlayer = true;
			spawnParticles();
		}
		else if(isPlaying || waitingForPlayer)
			stop();
		return this;
	}

	@Override
	public ParkourSession onOverride()
	{
		if(isPlaying)
		{	
			RecordingSession overridingSession = new RecordingSession();
			overridingSession.recording = new Recording(recording.initPos);
			overridingSession.recordingToOverride = recording;
			overridingSession.onOverride = true;
			overridingSession.isRecording = true;
			overridingSession.nbRecordPresses = 1;
			overridingSession.overrideStart = frameNumber - 1;
			overridingSession.recording.setName(recording.getName());
			stop();
			
			return overridingSession;
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(mc.isPaused())
			return;
		
		playbackCountdown = Math.max(0, playbackCountdown - 1);
		if(waitingForPlayer && startingPos.distanceTo(mc.player.getPos()) < 0.25)
		{
			isPlaying = true;
			playbackCountdown = 10;
			mc.player.input = new ControlledInput();
			mc.player.setVelocity(new Vec3d(0, 0, 0));
			frameNumber = recording.startingFrame;
			waitingForPlayer = false;
		}
		if(isPlaying && playbackCountdown == 0)
		{
			if(frameNumber < recording.size())
			{
				if(!initiated)
				{
					mc.player.setPos(startingPos.x, startingPos.y, startingPos.z);
					if(arrow.isAlive()) arrow.markDead();
					initiated = true;
				}
				currentFrame = recording.get(frameNumber);
				
				currentFrame.setMovementInput(mc.player.input, mc.player);
				KeyInputHUD.setFrame(currentFrame);
//				mc.player.setPos(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
				frameNumber++;
			}
			else if (Config.isLoopMode() && recording.isLoop())
			{
				initiated = false;
				frameNumber = recording.startingFrame;
			}
			else
				stop();
		}
	}

	@Override
	public void onRenderTick()
	{
		if(mc.isPaused())
			return;
			
		float partialTicks = mc.getTickDelta();
		if(playbackCountdown > 0)
		{
			float countdownAmount = (10 - playbackCountdown + partialTicks) / 10;
			Frame firstFrame = recording.get(Math.max(0, recording.startingFrame - 1));
			
			mc.player.setYaw(GraphicsHelper.lerpAngle(countdownAmount, mc.player.headYaw, firstFrame.headYaw));
			mc.player.setPitch(GraphicsHelper.lerp(countdownAmount, mc.player.getPitch(), firstFrame.headPitch));
			mc.player.prevHeadYaw = mc.player.getYaw();
			mc.player.prevPitch = mc.player.getPitch();
			
			Vec3d pos = mc.player.getPos();
			double posX = GraphicsHelper.lerp(countdownAmount, pos.x, firstFrame.posX);
			double posY = GraphicsHelper.lerp(countdownAmount, pos.y, firstFrame.posY);
			double posZ = GraphicsHelper.lerp(countdownAmount, pos.z, firstFrame.posZ);
			
			mc.player.setPos(posX, posY, posZ);
		}
		else if(isPlaying)
		{
			Frame prevFrame = recording.get(Math.max(0, frameNumber - 2));
			
			mc.player.prevHeadYaw = GraphicsHelper.lerpAngle(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
			mc.player.setYaw(mc.player.prevHeadYaw);
			mc.player.prevPitch = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
			mc.player.setPitch(mc.player.prevPitch);
			
			Vec3d playerPos = mc.player.getPos();
			Vec3d framePos = new Vec3d(prevFrame.posX, prevFrame.posY, prevFrame.posZ);
			if(5.0 < playerPos.distanceTo(framePos) && playerPos.distanceTo(framePos) < 7.0)
			{
				TextContent errorMessageContent = new TranslatableTextContent("com.prmod.playback_failed", "Playback failed", new Object[0]);
				MutableText errorMessage = MutableText.of(errorMessageContent);
				errorMessage.setStyle(errorMessage.getStyle().withColor(0xff0000));
				mc.inGameHud.getChatHud().addMessage(null);
				stop();
			}
			else
				mc.player.setPos(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
		}
	}
	
	private void stop()
	{
		despawnParticles();
		isPlaying = false;
		waitingForPlayer = false;

		//Vec3 playerPos = mc.player.getPositionVec();
		//double motionX = playerPos.x - mc.player.prevPosX;
		//double motionY = playerPos.y - mc.player.prevPosY;
		//double motionZ = playerPos.z - mc.player.prevPosZ;
		//mc.player.setVelocity(motionX, motionY, motionZ);
		
		mc.player.input = new KeyboardInput(mc.options);
	}

	private void spawnParticles()
	{
		despawnParticles();
		
		boolean inLoopMode = recording.isLoop() && Config.isLoopMode();
		
		if(inLoopMode)
			arrow = new ParticleArrowLoop(mc.world, startingPos.x, startingPos.y, startingPos.z);
		else
			arrow = new ParticleArrow(mc.world, startingPos.x, startingPos.y, startingPos.z);
		mc.particleManager.addParticle(arrow);
		
		if(!inLoopMode)
		{
			finish = new ParticleFinish(mc.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
			mc.particleManager.addParticle(finish);
		}
	}
	
	public int getFrameNumber()
	{
		return frameNumber;
	}
	
	private void despawnParticles()
	{
		if(finish != null) finish.markDead();
		if(arrow != null) arrow.markDead();
	}

	@Override
	public boolean isActive()
	{
		return isPlaying;
	}

	@Override
	public void cleanUp()
	{
		despawnParticles();
	}
}
