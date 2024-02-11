package com.elmfer.prmod.config;

import com.elmfer.prmod.ui.NumberLineView.TimeStampFormat;

public class Config {
	public static boolean isLoopMode() {
		return false;
	}
	
	public static boolean isHiddenIp() {
		return false;
	}
	
	public static boolean showInputs() {
		return true;
	}
	
	public static TimeStampFormat getTimeStampFormat() {
		return TimeStampFormat.DEFAULT;
	}
	
	public static void setLoopMode(boolean loopMode) {
	}
	
	public static void setHiddenIp(boolean hiddenIp) {
	}
	
	public static void setShowInputs(boolean showInputs) {
	}
	
	public static void setTimeStampFormat(TimeStampFormat format) {
	}
}
