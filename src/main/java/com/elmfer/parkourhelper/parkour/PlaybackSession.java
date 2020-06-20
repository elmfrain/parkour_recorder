package com.elmfer.parkourhelper.parkour;

import com.elmfer.parkourhelper.ControlledMovementInput;
import com.elmfer.parkourhelper.ParkourFrame;
import com.elmfer.parkourhelper.Recording;
import com.elmfer.parkourhelper.render.ParticleArrow;
import com.elmfer.parkourhelper.render.ParticleFinish;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.Vec3d;

public class PlaybackSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getMinecraft();
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
		if(!isPlaying && !waitingForPlayer)
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
			int size = recording.size();
			for(int i = frameNumber ; i < size ; i++)
			{
				recording.remove(frameNumber);
			}
			stop();
			
			RecordingSession overridingSession = new RecordingSession();
			overridingSession.recording = recording;
			overridingSession.onOverride = true;
			overridingSession.isRecording = true;
			
			return overridingSession;
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(waitingForPlayer && recording.initPos.distanceTo(mc.player.getPositionVector()) < 0.25)
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
			mc.player.rotationYaw = currentFrame.headYaw;
			mc.player.rotationPitch = currentFrame.headPitch;
			mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
			double motionX = mc.player.posX - mc.player.prevPosX;
			double motionY = mc.player.posY - mc.player.prevPosY;
			double motionZ = mc.player.posZ - mc.player.prevPosZ;
			mc.player.setVelocity(motionX, motionY, motionZ);
		}
	}
	
	private void stop()
	{
		despawnParticles();
		isPlaying = false;
		mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
	}

	private void spawnParticles()
	{
		despawnParticles();
		
		arrow = new ParticleArrow(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
		mc.effectRenderer.addEffect(arrow);
		
		finish = new ParticleFinish(mc.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
		mc.effectRenderer.addEffect(finish);
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
}
