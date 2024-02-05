package com.elmfer.prmod;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

public class KeyBinds {

	private static KeyBinds singleton;

	KeyBinding kbRecord;
	KeyBinding kbPlay;
	KeyBinding kbOverride;
	KeyBinding kbShowMenu;

	private KeyBinds() {
		createKeybinds();
	}

	public void registerKeybinds() {
		KeyBindingHelper.registerKeyBinding(kbRecord);
		KeyBindingHelper.registerKeyBinding(kbPlay);
		KeyBindingHelper.registerKeyBinding(kbOverride);
		KeyBindingHelper.registerKeyBinding(kbShowMenu);
	}

	public static KeyBinds getKeyBinds() {
		if (singleton == null)
			singleton = new KeyBinds();

		return singleton;
	}

	private void createKeybinds() {
		kbRecord = new KeyBinding("com.prmod.keybind.record", GLFW.GLFW_KEY_R, ParkourRecorder.MOD_NAME);
		kbPlay = new KeyBinding("com.prmod.keybind.play", GLFW.GLFW_KEY_P, ParkourRecorder.MOD_NAME);
		kbOverride = new KeyBinding("com.prmod.keybind.override", GLFW.GLFW_MOUSE_BUTTON_4, ParkourRecorder.MOD_NAME);
		kbShowMenu = new KeyBinding("com.prmod.keybind.showMenu", GLFW.GLFW_KEY_M, ParkourRecorder.MOD_NAME);
	}
}
