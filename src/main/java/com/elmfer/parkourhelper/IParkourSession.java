package com.elmfer.parkourhelper;

public interface IParkourSession {
	
	public IParkourSession onRecord();
	public IParkourSession onPlay();
	public IParkourSession onOverride();
	public IParkourSession onClientTick();
	public IParkourSession onRenderTick();
}
