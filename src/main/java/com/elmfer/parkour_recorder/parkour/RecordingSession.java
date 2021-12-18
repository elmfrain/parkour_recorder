package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.EventHandler;
import com.mojang.math.Vector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec3;

public class RecordingSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getInstance();
	protected Recording recording = null;
	protected Recording recordingToOverride = null;
	protected int overrideStart = 0;
	protected boolean onOverride = false;
	protected boolean isRecording = false;
	protected byte nbRecordPresses = 0;

	public Recording getRecording()
	{ return recording; }
	
	public boolean isRecording()
	{ return isRecording; }
	
	@Override
	public IParkourSession onRecord() 
	{
		switch(nbRecordPresses) 
		{
		case 0:
			mc.player.input = new KeyboardInput(mc.options);
			if(recording == null)
				recording = new Recording(mc.player.getPosition(0.0f));
			isRecording = true;
			nbRecordPresses++;
			break;
		case 1:
			Vec3 v = mc.player.getPosition(0.0f);
			recording.lastPos = new Vector3d(v.x, v.y, v.z);
			
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
				nbRecordPresses = 1;
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
			nbRecordPresses = 1;
			return onRecord();
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(isRecording && !mc.isPaused())
			recording.add(new ParkourFrame(mc.options, mc.player));
	}

	@Override
	public void onRenderTick()
	{
		
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
