package com.elmfer.parkour_recorder;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

public class Settings {
	
	private static Settings instance;
	
	public KeyMapping keybindRecord;
	public KeyMapping keybindPlay;
	public KeyMapping keybindOverride;
	public KeyMapping keybindReloadShaders;
	public KeyMapping keybindMainMenu;
	
	private Settings()
	{
		keybindRecord = new KeyMapping("com.elmfer.keybind.record", GLFW.GLFW_KEY_R, ParkourRecorderMod.MOD_NAME);
		keybindPlay = new KeyMapping("com.elmfer.keybind.play", GLFW.GLFW_KEY_P, ParkourRecorderMod.MOD_NAME);
		keybindOverride = new KeyMapping("com.elmfer.keybind.override", GLFW.GLFW_MOUSE_BUTTON_4, ParkourRecorderMod.MOD_NAME);
		keybindReloadShaders = new KeyMapping("Reload Resources", 24, ParkourRecorderMod.MOD_NAME);
		keybindMainMenu = new KeyMapping("com.elmfer.keybind.main_menu", GLFW.GLFW_KEY_M, ParkourRecorderMod.MOD_NAME);
		
		ClientRegistry.registerKeyBinding(keybindRecord);
		ClientRegistry.registerKeyBinding(keybindPlay);
		ClientRegistry.registerKeyBinding(keybindOverride);
		//ClientRegistry.registerKeyBinding(keybindReloadShaders);//
		ClientRegistry.registerKeyBinding(keybindMainMenu);
	}
	public static Settings getSettings()
	{
		if(instance == null) instance = new Settings();
		return instance;
	}
}
