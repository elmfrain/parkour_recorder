package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.ControlledMovementInput;
import com.elmfer.parkour_recorder.render.ParticleArrow;
import com.elmfer.parkour_recorder.render.ParticleFinish;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlaybackSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getInstance();
	public final Recording recording;
	private ParticleArrow arrow;
	private ParticleFinish finish;
	private boolean isPlaying = false;
	private boolean waitingForPlayer = false;
	private int frameNumber = 0;
	private ParkourFrame currentFrame = null;
	
	public PlaybackSession(Recording recording)
	{
		this.recording = recording;
	}
	
	public boolean isPlaying()
	{ return isPlaying; }
	
	public boolean isWaitingForPlayer()
	{ return waitingForPlayer; }
	
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
		if(waitingForPlayer)
		{
			waitingForPlayer = false;
			stop();
		}
		else if(!isPlaying && !waitingForPlayer)
		{
			waitingForPlayer = true;
			spawnParticles();
		}
		else if(isPlaying)
			stop();
		return this;
	}

	@Override
	public IParkourSession onOverride()
	{
		if(isPlaying)
		{	
			RecordingSession overridingSession = new RecordingSession();
			overridingSession.recording = new Recording(recording.initPos, recording.initVel);
			overridingSession.recordingToOverride = recording;
			overridingSession.onOverride = true;
			overridingSession.isRecording = true;
			overridingSession.nbRecordPresses = 1;
			overridingSession.overrideStart = frameNumber;
			overridingSession.recording.rename(recording.getName());
			stop();
			
			return overridingSession;
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(waitingForPlayer && recording.initPos.distanceTo(mc.player.getPositionVec()) < 0.25)
		{
			isPlaying = true;
			mc.player.movementInput = new ControlledMovementInput();
			frameNumber = 0;
			waitingForPlayer = false;
		}
		if(isPlaying && !mc.isGamePaused())
		{
			if(frameNumber < recording.size())
			{
				if(frameNumber == 0)
				{
					mc.player.setPositionAndUpdate(recording.initPos.x, recording.initPos.y, recording.initPos.z);
					if(arrow.isAlive()) arrow.setExpired();
				}
				currentFrame = recording.get(frameNumber);
				
				currentFrame.setInput(mc.player.movementInput, mc.player);
				frameNumber++;
			}
			else 
				stop();
		}
	}

	@Override
	public void onRenderTick()
	{
		if(isPlaying && !mc.isGamePaused())
		{
			mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
			
			float partialTicks = mc.getRenderPartialTicks();
			ParkourFrame prevFrame = recording.get(Math.max(0, frameNumber - 2));
			
			mc.player.rotationYaw = MathHelper.lerp(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
			mc.player.rotationPitch = MathHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
		}
	}
	
	private void stop()
	{
		despawnParticles();
		isPlaying = false;
		
		Vec3d playerPos = mc.player.getPositionVec();
		double motionX = playerPos.x - mc.player.prevPosX;
		double motionY = playerPos.y - mc.player.prevPosY;
		double motionZ = playerPos.z - mc.player.prevPosZ;
		mc.player.setVelocity(motionX, motionY, motionZ);
		
		mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
	}

	private void spawnParticles()
	{
		despawnParticles();
		
		arrow = new ParticleArrow(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
		mc.particles.addEffect(arrow);
		
		finish = new ParticleFinish(mc.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
		mc.particles.addEffect(finish);
	}
	
	private void despawnParticles()
	{
		if(finish != null) finish.setExpired();
		if(arrow != null) arrow.setExpired();
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
