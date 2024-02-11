package com.elmfer.prmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

public class ParkourRecorder implements ClientModInitializer {

	public static final String MOD_NAME = "Parkour Recorder";
	public static final String MOD_ID = "prmod";
	public static final String MOD_VERSION = "0.3.0a-mc1.20.4";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	@Override
	public void onInitializeClient() {
		EventHandler.registerEventHandlers();
		KeyBinds.getKeyBinds().registerKeybinds();
	}
}