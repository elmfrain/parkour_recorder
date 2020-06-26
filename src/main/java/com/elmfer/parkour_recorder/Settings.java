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
	
	private Settings()
	{
		keybindRecord = new KeyBinding("Record Parkour", 19, "Parkour Mod");
		keybindPlay = new KeyBinding("Play Parkour", 25, "Parkour Mod");
		keybindOverride = new KeyBinding("Override Session", -96, "Parkour Mod");
		keybindReloadShaders = new KeyBinding("Reload Resources", 24, "Parkour Mod");
		keybindSave = new KeyBinding("Save Recording", 50, "Parkour Mod");
		keybindLoad = new KeyBinding("Load Recordings", 49, "Parkour Mod");
		
		ClientRegistry.registerKeyBinding(keybindRecord);
		ClientRegistry.registerKeyBinding(keybindPlay);
		ClientRegistry.registerKeyBinding(keybindOverride);
		//ClientRegistry.registerKeyBinding(keybindReloadShaders);
		ClientRegistry.registerKeyBinding(keybindSave);
		ClientRegistry.registerKeyBinding(keybindLoad);
	}
	public static Settings getSettings()
	{
		if(instance == null) instance = new Settings();
		return instance;
	}
}
