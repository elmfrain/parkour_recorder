package com.elmfer.parkour_recorder.config;

import java.io.File;

import com.elmfer.parkour_recorder.ParkourRecorderMod;
import com.elmfer.parkour_recorder.gui.NumberLineView.TimeStampFormat;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**The mod's configuration manager.**/
public class ConfigManager
{
	public static final String FILE_NAME = "config/" + ParkourRecorderMod.MOD_ID + ".cfg";
	
	public static final String TIMELINE_SCREEN_CATEGORY = "timeline_gui";
	public static final String TIME_STAMP_FORMAT = "timeStampFormat";
	
	public static final String RECORDING_CATEGORY = "recording";
	public static final String ENABLE_LOOP = "enableLoop";
	
	public static final String MENU_CATEGORY = "menu";
	public static final String HIDE_IP = "hideIp";
	
	public static final String PLAYBACK_CATEGORY = "playback";
	public static final String SHOW_INPUT = "showInput";
	
	private static Configuration config = null;
	
	private static TimeStampFormat timeFormat = TimeStampFormat.DEFAULT;
	private static boolean loopMode = false;
	private static boolean hideIP = false;
	private static boolean showInputs = false;
	
	public static void saveTimeFormat(TimeStampFormat format)
	{
		timeFormat = format;
		saveConfig();
	}
	
	public static TimeStampFormat loadTimeFormat()
	{
		return timeFormat;
	}
	
	public static void init()
	{
		File configFile = new File(FILE_NAME);
		config = new Configuration(configFile);
		loadConfig();
	}
	
	public static boolean isLoopMode()
	{
		return loopMode;
	}
	
	public static boolean isHiddenIp()
	{
		return hideIP;
	}
	
	public static boolean showInputs()
	{
		return showInputs;
	}
	
	public static void saveLoopMode(boolean loopMode)
	{
		ConfigManager.loopMode = loopMode;
		saveConfig();
	}
	
	public static void saveHiddenIp(boolean hideIP)
	{
		ConfigManager.hideIP = hideIP;
		saveConfig();
	}
	
	public static void saveShowInputs(boolean showInputs)
	{
		ConfigManager.showInputs = showInputs;
		saveConfig();
	}
	
	private static void loadConfig()
	{
		syncConfig(true, true);
	}
	
	private static void saveConfig()
	{
		syncConfig(false, true);
	}
	
	private static void syncConfig(boolean loadConfig, boolean saveConfig)
	{
		if(loadConfig) config.load();
		
		Property pTimeStampFormat = config.get(TIMELINE_SCREEN_CATEGORY, TIME_STAMP_FORMAT, TimeStampFormat.DEFAULT.NAME);
		Property pEnableLoop = config.get(RECORDING_CATEGORY, ENABLE_LOOP, false);
		Property pHideIp = config.get(MENU_CATEGORY, HIDE_IP, false);
		Property pShowInputs = config.get(PLAYBACK_CATEGORY, SHOW_INPUT, false);
		
		if(loadConfig)
		{
			timeFormat = TimeStampFormat.getFormatFromName(pTimeStampFormat.getString());
			loopMode = pEnableLoop.getBoolean();
			hideIP = pHideIp.getBoolean();
			showInputs = pShowInputs.getBoolean();
		}
		if(saveConfig)
		{
			pTimeStampFormat.set(timeFormat.NAME);
			pEnableLoop.set(loopMode);
			pHideIp.set(hideIP);
			pShowInputs.set(showInputs);
			
			config.save();
		}
	}
}
