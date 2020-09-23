package com.elmfer.parkour_recorder;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Settings {
	
	private static Settings instance;
	
	public KeyBinding keybindRecord;
	public KeyBinding keybindPlay;
	public KeyBinding keybindOverride;
	public KeyBinding keybindReloadShaders;
	public KeyBinding keybindSave;
	public KeyBinding keybindLoad;
	public KeyBinding keybindTimeline;
	
	private Settings()
	{
		keybindRecord = new KeyBinding("keybind.record", 19, ParkourRecorderMod.MOD_NAME);
		keybindPlay = new KeyBinding("keybind.play", 25, ParkourRecorderMod.MOD_NAME);
		keybindOverride = new KeyBinding("keybind.override", -95, ParkourRecorderMod.MOD_NAME);
		keybindReloadShaders = new KeyBinding("Reload Resources", 24, ParkourRecorderMod.MOD_NAME);
		keybindSave = new KeyBinding("keybind.save_recording", 50, ParkourRecorderMod.MOD_NAME);
		keybindLoad = new KeyBinding("keybind.load_recording", 49, ParkourRecorderMod.MOD_NAME);
		keybindTimeline = new KeyBinding("keybind.timeline", 48, ParkourRecorderMod.MOD_NAME);
		
		ClientRegistry.registerKeyBinding(keybindRecord);
		ClientRegistry.registerKeyBinding(keybindPlay);
		ClientRegistry.registerKeyBinding(keybindOverride);
		ClientRegistry.registerKeyBinding(keybindReloadShaders);//
		ClientRegistry.registerKeyBinding(keybindSave);
		ClientRegistry.registerKeyBinding(keybindLoad);
		ClientRegistry.registerKeyBinding(keybindTimeline);
	}
	public static Settings getSettings()
	{
		if(instance == null) instance = new Settings();
		return instance;
	}
}
