package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.EventHandler;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.render.ParticleArrow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;

public class RecordingSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getInstance();
	protected Recording recording = null;
	protected Recording recordingToOverride = null;
	private ParticleArrow arrow;
	protected int overrideStart = 0;
	protected boolean onOverride = false;
	protected boolean isRecording = false;
	private boolean waitingForPlayer = false;
	protected byte nbRecordPresses = 0;

	public Recording getRecording()
	{ return recording; }
	
	public boolean isRecording()
	{ return isRecording; }

	public boolean isWaitingForPlayer()
	{ return waitingForPlayer; }

	@Override
	public IParkourSession onRecord() 
	{
		switch(nbRecordPresses) 
		{
		case 0:
			mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
			if(recording == null)
				recording = new Recording(mc.player.getPositionVec());
			isRecording = true;
			if (ConfigManager.isLoopMode())
			{
				spawnParticles();
				nbRecordPresses = 1;
			} else
				nbRecordPresses = 2;
			break;
		case 1:
			waitingForPlayer = true;
			break;
		case 2:
			recording.lastPos = mc.player.getPositionVec();
			
			if(onOverride)
			{
				String name = recordingToOverride.originalName != null ? recordingToOverride.originalName + " - " : "";
				Recording record = recordingToOverride.subList(0, overrideStart);
				record.lastPos = recording.lastPos;
				record.addAll(recording);
				record.setName(name + Recording.getFormattedTime());
				
				EventHandler.addToHistory(record);
			}
			else
			{
				String name = recording.originalName != null ? recording.originalName + " - " : "";
				recording.rename(name + Recording.getFormattedTime());
				EventHandler.addToHistory(recording);
			}
			
			isRecording = false;
			onOverride = false;

			despawnParticles();
			
			nbRecordPresses++;
			
			return new PlaybackSession(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
		}
		
		return this;
	}

	@Override
	public IParkourSession onPlay()
	{
		if(recording != null)
		{
			if(isRecording)
			{
				nbRecordPresses = 2;
				onRecord();
			}
			PlaybackSession playback = new PlaybackSession(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
			playback.onPlay();
			return playback;
		}
		return this;
	}

	@Override
	public IParkourSession onOverride()
	{
		if(onOverride && isRecording)
		{
			nbRecordPresses = 2;
			return onRecord();
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(isRecording && !mc.isGamePaused())
		{
			recording.add(new ParkourFrame(mc.gameSettings, mc.player));
			if(waitingForPlayer && recording.initPos.distanceTo(mc.player.getPositionVec()) < 0.25)
			{
				waitingForPlayer = false;
				// Finish session
				nbRecordPresses = 2;
				EventHandler.session = onRecord();
				// Set last pos to init pos to mark as loop recording data
				recording.lastPos = recording.initPos;
			}
		}
	}

	@Override
	public void onRenderTick()
	{
		
	}

	private void spawnParticles()
	{
		despawnParticles();

		arrow = new ParticleArrow(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
		mc.particles.addEffect(arrow);
	}

	private void despawnParticles()
	{
		if(arrow != null) arrow.setExpired();
	}

	@Override
	public boolean isSessionActive()
	{
		return isRecording;
	}

	@Override
	public void cleanUp()
	{	
	}

}
