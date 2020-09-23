package com.elmfer.parkour_recorder.parkour;

public class Checkpoint {
	
	public final String name;
	public final int frameNumber;
	public int color = 0;
	
	public Checkpoint(String name, int frameNumber)
	{
		this.name = name;
		this.frameNumber = frameNumber;
	}

	public Checkpoint(Checkpoint copy)
	{
		name = copy.name;
		frameNumber = copy.frameNumber;
	}
}
