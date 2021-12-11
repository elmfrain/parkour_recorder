package com.elmfer.parkour_recorder;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Settings {
	
	private static Settings instance;
	
	public KeyBinding keybindRecord;
	public KeyBinding keybindPlay;
	public KeyBinding keybindOverride;
	public KeyBinding keybindReloadShaders;
	public KeyBinding keybindMainMenu;
	
	private Settings()
	{
		keybindRecord = new KeyBinding("com.elmfer.keybind.record", GLFW.GLFW_KEY_R, ParkourRecorderMod.MOD_NAME);
		keybindPlay = new KeyBinding("com.elmfer.keybind.play", GLFW.GLFW_KEY_P, ParkourRecorderMod.MOD_NAME);
		keybindOverride = new KeyBinding("com.elmfer.keybind.override", GLFW.GLFW_MOUSE_BUTTON_4, ParkourRecorderMod.MOD_NAME);
		keybindReloadShaders = new KeyBinding("Reload Resources", 24, ParkourRecorderMod.MOD_NAME);
		keybindMainMenu = new KeyBinding("com.elmfer.keybind.main_menu", GLFW.GLFW_KEY_MENU, ParkourRecorderMod.MOD_NAME);
		
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
