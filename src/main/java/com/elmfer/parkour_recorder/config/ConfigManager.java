package com.elmfer.parkour_recorder.config;

import java.io.File;

import com.elmfer.parkour_recorder.ParkourRecorderMod;
import com.elmfer.parkour_recorder.gui.TimelineViewport.TimeStampFormat;

import net.minecraftforge.common.config.Configuration;

/**The mod's configuration manager.**/
public class ConfigManager
{
	public static final String CONFIG_FILE_EXTENSION = ".cfg";
	public static final String FILE_NAME = "config/" + ParkourRecorderMod.MOD_ID + CONFIG_FILE_EXTENSION;
	
	public static final String TIMELINE_SCREEN_CATEGORY = "timeline_gui";
	public static final String TIME_STAMP_FORMAT = "timeStampFormat";
	
	public static void saveTimeFormat(TimeStampFormat format)
	{
		Configuration config = getConfig();
		try
		{
			config.load();
			config.get(TIMELINE_SCREEN_CATEGORY, TIME_STAMP_FORMAT, TimeStampFormat.DEFAULT.NAME);
			config.setCategoryComment(TIMELINE_SCREEN_CATEGORY, "Values from the Timeline Screen's settings menu.");
			config.getCategory(TIMELINE_SCREEN_CATEGORY).get(TIME_STAMP_FORMAT).set(format.NAME);
		}
		catch(Exception e) { System.out.printf("Cannot save [%1$] to config!\n", TIME_STAMP_FORMAT); }
		finally { config.save(); }
	}
	
	public static TimeStampFormat loadTimeFormat()
	{
		Configuration config = getConfig();
		try
		{
			config.load();
			if (config.getCategory(TIMELINE_SCREEN_CATEGORY).containsKey(TIME_STAMP_FORMAT))
			{
				String formatName = config.get(TIMELINE_SCREEN_CATEGORY, TIME_STAMP_FORMAT, TimeStampFormat.DEFAULT.NAME).getString();
				return TimeStampFormat.getFormatFromName(formatName);
			}
		
		}
		catch(Exception e) { System.out.printf("Cannot save [%1$] to config!\n", TIME_STAMP_FORMAT); }
		finally { config.save(); }
		return TimeStampFormat.DEFAULT;
	}
	
	private static Configuration getConfig()
	{
		return new Configuration(new File(FILE_NAME));
	}
}
