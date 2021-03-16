package com.elmfer.parkour_recorder.parkour;

/**
 * Lists saving formats for correctly deserializing saves.
 * The naming convention is simply a mod version that had changed how things were saved.
 **/
public enum SavingFormat 
{
	V1_0_0_0(1),
	V1_0_1_0(0);
	
	//Latest format; dipicted from last enum in list
	public static final SavingFormat LATEST = SavingFormat.values()[SavingFormat.values().length - 1];
	
	public final int ID;
	
	private SavingFormat(int id)
	{
		ID = id;
	}
	
	/**
	 * Returns format from id. Anything greater than zero gives the oldest format.
	 * Each consecutive format will have an id one less than the previous one.
	 **/
	public static SavingFormat getFormatFromID(int id)
	{
		if(0 < id) return V1_0_0_0;
		
		//Skip the oldest format and check each format 
		for(SavingFormat format : SavingFormat.values())
		{
			if(format.ID == 1) continue;
			else if(format.ID == id) return format;
		}
		
		return null;
	}
}