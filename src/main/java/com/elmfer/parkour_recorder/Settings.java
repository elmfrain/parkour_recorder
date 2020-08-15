package com.elmfer.parkour_recorder;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Type;
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
		keybindRecord = new KeyBinding("keybind.record", GLFW.GLFW_KEY_R, ParkourRecorderMod.MOD_NAME);
		keybindPlay = new KeyBinding("keybind.play", GLFW.GLFW_KEY_P, ParkourRecorderMod.MOD_NAME);
		keybindOverride = new KeyBinding("keybind.override", Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, ParkourRecorderMod.MOD_NAME);
		keybindReloadShaders = new KeyBinding("Reload Resources", GLFW.GLFW_KEY_O, "Parkour Mod");
		keybindSave = new KeyBinding("keybind.save_recording", GLFW.GLFW_KEY_M, ParkourRecorderMod.MOD_NAME);
		keybindLoad = new KeyBinding("keybind.load_recording", GLFW.GLFW_KEY_N, ParkourRecorderMod.MOD_NAME);
		keybindTimeline = new KeyBinding("keybind.timeline", GLFW.GLFW_KEY_B, ParkourRecorderMod.MOD_NAME);
		
		ClientRegistry.registerKeyBinding(keybindRecord);
		ClientRegistry.registerKeyBinding(keybindPlay);
		ClientRegistry.registerKeyBinding(keybindOverride);
		//ClientRegistry.registerKeyBinding(keybindReloadShaders);//
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
