package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.ControlledMovementInput;
import com.elmfer.parkour_recorder.render.ParticleArrow;
import com.elmfer.parkour_recorder.render.ParticleFinish;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
		if(mc.isGamePaused()) return;
		double distance = recording.initPos.distanceTo(mc.player.getPositionVec());
		if(waitingForPlayer && distance < 1.0)
		{
			double speed = (1.0 - distance) * 0.2;
			Vec3d prevMotion = mc.player.getMotion();
			mc.player.setMotion(prevMotion.x, prevMotion.y * 0.8, prevMotion.z);
			
			Vec3d pushVel = recording.initPos.subtract(mc.player.getPositionVec()).mul(speed, speed, speed);
			mc.player.addVelocity(pushVel.x, pushVel.y, pushVel.z);
			
			if(waitingForPlayer && distance < 0.075)
			{
				isPlaying = true;
				mc.player.movementInput = new ControlledMovementInput();
				frameNumber = 0;
				waitingForPlayer = false;
			}
		}
		if(isPlaying)
		{
			if(frameNumber < recording.size())
			{
				if(frameNumber == 0)
				{
					mc.player.setPositionAndUpdate(recording.initPos.x, recording.initPos.y, recording.initPos.z);
					if(arrow.isAlive()) arrow.setExpired();
				}
				currentFrame = recording.get(frameNumber);
				if(mc.player.getPositionVec().distanceTo(new Vec3d(currentFrame.posX, currentFrame.posY, currentFrame.posZ)) > 1.0)
				{
					mc.player.sendMessage(new TranslationTextComponent("warn.playback_failed").applyTextStyles(TextFormatting.DARK_RED, TextFormatting.BOLD));
					stop();
					frameNumber = 0;
				}
				mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
				
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
		double distance = recording.initPos.distanceTo(mc.player.getPositionVec());
		float partialTicks = mc.getRenderPartialTicks();
		
		if(waitingForPlayer && !mc.isGamePaused() && distance < 0.5)
		{
			Vec3d playerPos = mc.player.getPositionVec();
			double lerpedX = MathHelper.lerp(partialTicks, mc.player.prevPosX, playerPos.x);
			double lerpedY = MathHelper.lerp(partialTicks, mc.player.prevPosY, playerPos.y);
			double lerpedZ = MathHelper.lerp(partialTicks, mc.player.prevPosZ, playerPos.z);
			Vec3d lerpedPos = new Vec3d(lerpedX, lerpedY, lerpedZ);
			double lerpedDist = recording.initPos.distanceTo(lerpedPos);
			double amount = Math.pow(0.5 - lerpedDist, 2.0);
			
			mc.player.rotationYaw = (float) MathHelper.lerp(amount, mc.player.rotationYaw, recording.get(0).headYaw);
			mc.player.rotationPitch = (float) MathHelper.lerp(amount, mc.player.rotationPitch, recording.get(0).headPitch);
		}
		else if(isPlaying && !mc.isGamePaused())
		{	
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
