package com.elmfer.parkourhelper.parkour;

import com.elmfer.parkourhelper.EventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.Vec3d;

public class RecordingSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getMinecraft();
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
			mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
			if(recording == null)
				recording = new Recording(mc.player.getPositionVector(), new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ));
			isRecording = true;
			nbRecordPresses++;
			break;
		case 1:		
			recording.lastPos = mc.player.getPositionVector();
			recording.lastVel = new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
			
			if(onOverride)
			{
				String name = recordingToOverride.originalName != null ? recordingToOverride.originalName + " - " : "";
				Recording record = new Recording(recordingToOverride.initPos, recordingToOverride.initVel);
				record.lastPos = recording.lastPos;
				record.lastVel = recording.lastVel;
				record.originalName = recordingToOverride.originalName;
				record.addAll(recordingToOverride.subList(0, overrideStart));
				record.addAll(recording);
				record.rename(name + Recording.getFormattedTime());
				
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
			break;
		case 2:
			RecordingSession newSession = new RecordingSession();
			newSession.onRecord();
			return newSession;
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
			onRecord();
		}
		return this;
	}

	@Override
	public void onClientTick()
	{
		if(isRecording && !mc.isGamePaused())
			recording.add(new ParkourFrame(mc.gameSettings, mc.player));
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
