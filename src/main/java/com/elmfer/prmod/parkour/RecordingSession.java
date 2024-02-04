package com.elmfer.prmod.parkour;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.render.ParticleArrowLoop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;

public class RecordingSession implements ParkourSession {

	public static final MinecraftClient mc = MinecraftClient.getInstance();
	
	protected Recording recording = null;
	protected Recording recordingToOverride = null;
	
	private ParticleArrowLoop arrow;
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
	public ParkourSession onRecord() 
	{
		switch(nbRecordPresses) 
		{
		case 0:
			mc.player.input = new Input();
			if(recording == null)
				recording = new Recording(mc.player.getPos());
			isRecording = true;
			if (Config.isLoopMode())
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
			recording.lastPos = mc.player.getPos();
			
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
	public ParkourSession onPlay()
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
	public ParkourSession onOverride()
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
		if(isRecording && !mc.isPaused())
		{
			recording.add(new Frame(mc.options, mc.player));
			if(waitingForPlayer && recording.initPos.distanceTo(mc.player.getPos()) < 0.25)
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

		arrow = new ParticleArrowLoop(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
		mc.particleManager.addParticle(arrow);
	}

	private void despawnParticles()
	{
		if(arrow != null) arrow.markDead();
	}

	@Override
	public boolean isActive()
	{
		return isRecording;
	}

	@Override
	public void cleanUp()
	{	
	}

}