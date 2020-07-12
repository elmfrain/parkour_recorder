package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.render.ParticleArrow;
import com.elmfer.parkour_recorder.render.ParticleFinish;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
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
	private int playbackCountdown = 0;
	
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
		if(!mc.isGamePaused())
		{
			playbackCountdown = Math.max(0, playbackCountdown - 1);
			if(waitingForPlayer && recording.initPos.distanceTo(mc.player.getPositionVec()) < 0.25)
			{
				isPlaying = true;
				playbackCountdown = 10;
				mc.player.movementInput = new ControlledMovementInput();
				mc.player.setMotion(0.0, 0.0, 0.0);
				frameNumber = 0;
				waitingForPlayer = false;
			}
			if(isPlaying && playbackCountdown == 0)
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
					//mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
					frameNumber++;
				}
				else 
					stop();
			}
		}
	}

	@Override
	public void onRenderTick()
	{
		if(!mc.isGamePaused())
		{	
			float partialTicks = mc.getRenderPartialTicks();
			if(playbackCountdown > 0)
			{
				float countdownAmount = (10 - playbackCountdown + partialTicks) / 10;
				ParkourFrame firstFrame = recording.get(0);
				
				mc.player.rotationYaw = MathHelper.lerp(countdownAmount, mc.player.rotationYaw, firstFrame.headYaw);
				mc.player.rotationPitch = MathHelper.lerp(countdownAmount, mc.player.rotationPitch, firstFrame.headPitch);
				
				Vec3d pos = mc.player.getPositionVec();
				double posX = MathHelper.lerp(countdownAmount, pos.x, firstFrame.posX);
				double posY = MathHelper.lerp(countdownAmount, pos.y, firstFrame.posY);
				double posZ = MathHelper.lerp(countdownAmount, pos.z, firstFrame.posZ);
				
				mc.player.setPosition(posX, posY, posZ);
			}
			else if(isPlaying)
			{
				ParkourFrame prevFrame = recording.get(Math.max(0, frameNumber - 2));
				
				mc.player.rotationYaw = MathHelper.lerp(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
				mc.player.rotationPitch = MathHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
				
				Vec3d playerPos = mc.player.getPositionVec();
				Vec3d framePos = new Vec3d(prevFrame.posX, prevFrame.posY, prevFrame.posZ);
				if(5.0 < playerPos.distanceTo(framePos) && playerPos.distanceTo(framePos) < 40.0)
				{
					ITextComponent errorMessage = new TranslationTextComponent("warn.playback_failed");
					errorMessage.applyTextStyle(TextFormatting.RED);
					mc.player.sendMessage(errorMessage);
					stop();
				}
				else
					mc.player.setPosition(currentFrame.posX, currentFrame.posY, currentFrame.posZ);
			}
		}
	}
	
	private void stop()
	{
		despawnParticles();
		isPlaying = false;

		//Vec3d playerPos = mc.player.getPositionVec();
		//double motionX = playerPos.x - mc.player.prevPosX;
		//double motionY = playerPos.y - mc.player.prevPosY;
		//double motionZ = playerPos.z - mc.player.prevPosZ;
		//mc.player.setVelocity(motionX, motionY, motionZ);
		
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
