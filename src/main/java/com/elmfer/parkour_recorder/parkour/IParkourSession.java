package com.elmfer.parkour_recorder.parkour;

public interface IParkourSession {
	
	public IParkourSession onRecord();
	public IParkourSession onPlay();
	public IParkourSession onOverride();
	public void onClientTick();
	public void onRenderTick();
	public boolean isSessionActive();
	public void cleanUp();
}
