package com.elmfer.parkourhelper.parkour;

import com.elmfer.parkourhelper.ParkourFrame;
import com.elmfer.parkourhelper.Recording;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.Vec3d;

public class RecordingSession implements IParkourSession {

	protected static final Minecraft mc = Minecraft.getMinecraft();
	protected Recording recording = null;
	protected boolean onOverride = false;
	protected boolean isRecording = false;
	private byte nbRecordPresses = 0;

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
			recording = new Recording(mc.player.getPositionVector(), new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ));
			isRecording = true;
			nbRecordPresses++;
			break;
		case 1:
			isRecording = false;
			onOverride = false;
			recording.lastPos = mc.player.getPositionVector();
			recording.lastVel = new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
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
		nbRecordPresses = 1;
		onRecord();
		PlaybackSession playback = new PlaybackSession(recording);
		playback.onPlay();
		return playback;
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

}
