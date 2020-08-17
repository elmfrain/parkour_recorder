package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ParticleArrow;
import com.elmfer.parkour_recorder.render.ParticleFinish;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class PlaybackSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getMinecraft();
	public final Recording recording;
	private ParticleArrow arrow;
	private ParticleFinish finish;
	private boolean isPlaying = false;
	private boolean waitingForPlayer = false;
	private int frameNumber = 0;
	private ParkourFrame currentFrame = null;
	private int playbackCountdown = 0;
	private Vec3d startingPos;
	private boolean initiated = false;
	
	public PlaybackSession(Recording recording)
	{
		this.recording = recording;
		ParkourFrame startingFrame = recording.get(recording.startingFrame);
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
		ParkourFrame startingFrame = recording.get(framePos);
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
			overridingSession.recording = new Recording(recording.initPos, recording.initVel);
			overridingSession.recordingToOverride = recording;
			overridingSession.onOverride = true;
			overridingSession.isRecording = true;
			overridingSession.nbRecordPresses = 1;
			overridingSession.overrideStart = frameNumber - 1;
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
			if(waitingForPlayer && startingPos.distanceTo(mc.player.getPositionVector()) < 0.25)
			{
				isPlaying = true;
				playbackCountdown = 10;
				mc.player.movementInput = new ControlledMovementInput();
				mc.player.setVelocity(0, 0, 0);
				frameNumber = recording.startingFrame;
				waitingForPlayer = false;
			}
			if(isPlaying && playbackCountdown == 0)
			{
				if(frameNumber < recording.size())
				{
					if(!initiated)
					{
						mc.player.setPositionAndUpdate(startingPos.x, startingPos.y, startingPos.z);
						if(arrow.isAlive()) arrow.setExpired();
						initiated = true;
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
				ParkourFrame firstFrame = recording.get(Math.max(0, recording.startingFrame - 1));
				
				mc.player.rotationYaw = GraphicsHelper.lerp(countdownAmount, mc.player.rotationYaw, firstFrame.headYaw);
				mc.player.rotationPitch = GraphicsHelper.lerp(countdownAmount, mc.player.rotationPitch, firstFrame.headPitch);
				
				Vec3d pos = mc.player.getPositionVector();
				double posX = GraphicsHelper.lerp(countdownAmount, pos.x, firstFrame.posX);
				double posY = GraphicsHelper.lerp(countdownAmount, pos.y, firstFrame.posY);
				double posZ = GraphicsHelper.lerp(countdownAmount, pos.z, firstFrame.posZ);
				
				mc.player.setPosition(posX, posY, posZ);
			}
			else if(isPlaying)
			{
				ParkourFrame prevFrame = recording.get(Math.max(0, frameNumber - 2));
				
				mc.player.prevRotationYaw = mc.player.rotationYaw = GraphicsHelper.lerp(partialTicks, prevFrame.headYaw, currentFrame.headYaw);
				mc.player.prevRotationPitch = mc.player.rotationPitch = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, currentFrame.headPitch);
				
				Vec3d playerPos = mc.player.getPositionVector();
				Vec3d framePos = new Vec3d(prevFrame.posX, prevFrame.posY, prevFrame.posZ);
				if(5.0 < playerPos.distanceTo(framePos) && playerPos.distanceTo(framePos) < 7.0)
				{
					ITextComponent errorMessage = new TextComponentTranslation("warn.playback_failed");
					errorMessage.setStyle((new Style()).setColor(TextFormatting.RED));
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
		waitingForPlayer = false;

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
		
		arrow = new ParticleArrow(mc.world, startingPos.x, startingPos.y, startingPos.z);
		mc.effectRenderer.addEffect(arrow);
		
		finish = new ParticleFinish(mc.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
		mc.effectRenderer.addEffect(finish);
	}
	
	public int getFrameNumber()
	{
		return frameNumber;
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
