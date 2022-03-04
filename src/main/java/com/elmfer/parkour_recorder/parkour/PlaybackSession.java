package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ParticleArrow;
import com.elmfer.parkour_recorder.render.ParticleArrowLoop;
import com.elmfer.parkour_recorder.render.ParticleFinish;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

public class PlaybackSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getInstance();
	public final Recording recording;
	private ParticleArrow arrow;
	private ParticleFinish finish;
	private boolean isPlaying = false;
	private boolean waitingForPlayer = false;
	private int frameNumber = 0;
	private ParkourFrame currentFrame = null;
	private int playbackCountdown = 0;
	private Vec3 startingPos;
	private boolean initiated = false;
	
	public PlaybackSession(Recording recording)
	{
		this.recording = recording;
		ParkourFrame startingFrame = recording.get(recording.startingFrame);
		startingPos = new Vec3(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
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
		ParkourFrame startingFrame = recording.get(framePos);
		startingPos = new Vec3(startingFrame.posX, startingFrame.posY, startingFrame.posZ);
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
	public IParkourSession onRecord()
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
	public IParkourSession onPlay()
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
	public IParkourSession onOverride()
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
		if(!mc.isPaused())
		{
			playbackCountdown = Math.max(0, playbackCountdown - 1);
			if(waitingForPlayer && startingPos.distanceTo(mc.player.getPosition(0.0f)) < 0.25)
			{
				isPlaying = true;
				playbackCountdown = 10;
				mc.player.input = new ControlledMovementInput();
				mc.player.setDeltaMovement(0, 0, 0);
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
						if(arrow.isAlive()) arrow.remove();
						initiated = true;
					}
					currentFrame = recording.get(frameNumber);
					
					currentFrame.setMovementInput(mc.player.input, mc.player);
					KeyInputHUD.setParkourFrame(currentFrame);
					//mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
					frameNumber++;
				}
				else if (ConfigManager.isLoopMode() && recording.isLoop())
				{
					initiated = false;
					frameNumber = recording.startingFrame;
				}
				else
					stop();
			}
		}
	}

	@Override
	public void onRenderTick()
	{
		if(!mc.isPaused())
		{	
			float partialTicks = mc.getFrameTime();
			if(playbackCountdown > 0)
			{
				float countdownAmount = (10 - playbackCountdown + partialTicks) / 10;
				ParkourFrame firstFrame = recording.get(Math.max(0, recording.startingFrame - 1));
				
				mc.player.setYRot(GraphicsHelper.lerpAngle(countdownAmount, mc.player.getYRot(), firstFrame.headYaw));
				mc.player.setXRot(GraphicsHelper.lerp(countdownAmount, mc.player.getXRot(), firstFrame.headPitch));
				
				Vec3 pos = mc.player.getPosition(0.0f);
				double posX = GraphicsHelper.lerp(countdownAmount, pos.x, firstFrame.posX);
				double posY = GraphicsHelper.lerp(countdownAmount, pos.y, firstFrame.posY);
				double posZ = GraphicsHelper.lerp(countdownAmount, pos.z, firstFrame.posZ);
				
				mc.player.setPos(posX, posY, posZ);
			}
			else if(isPlaying)
			{
				ParkourFrame prevFrame = recording.get(Math.max(0, frameNumber - 2));
				
				mc.player.yRotO = GraphicsHelper.lerpAngle(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
				mc.player.setYRot(mc.player.yRotO);
				mc.player.xRotO = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
				mc.player.setXRot(mc.player.xRotO);
				
				Vec3 playerPos = mc.player.getPosition(0.0f);
				Vec3 framePos = new Vec3(prevFrame.posX, prevFrame.posY, prevFrame.posZ);
				if(5.0 < playerPos.distanceTo(framePos) && playerPos.distanceTo(framePos) < 7.0)
				{
					Component errorMessage = ComponentUtils.fromMessage(new TranslatableComponent("com.elmfer.playback_failed"));
					errorMessage.getStyle().applyFormat(ChatFormatting.RED);
					mc.gui.getChat().addMessage(errorMessage);
					stop();
				}
				else
					mc.player.setPos(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
			}
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
		
		boolean inLoopMode = recording.isLoop() && ConfigManager.isLoopMode();
		
		if(inLoopMode)
			arrow = new ParticleArrowLoop(mc.level, startingPos.x, startingPos.y, startingPos.z);
		else
			arrow = new ParticleArrow(mc.level, startingPos.x, startingPos.y, startingPos.z);
		mc.particleEngine.add(arrow);
		
		if(!inLoopMode)
		{
			finish = new ParticleFinish(mc.level, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
			mc.particleEngine.add(finish);
		}
	}
	
	public int getFrameNumber()
	{
		return frameNumber;
	}
	
	private void despawnParticles()
	{
		if(finish != null) finish.remove();
		if(arrow != null) arrow.remove();
	}

	@Override
	public boolean isSessionActive()
	{
		return isPlaying;
	}

	@Override
	public void cleanUp()
	{
		despawnParticles();
	}
}
