package com.elmfer.prmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;

public class ParkourRecorder implements ClientModInitializer {

    public static final String MOD_NAME = "Parkour Recorder";
    public static final String MOD_ID = "prmod";
    public static final String MOD_VERSION = "1.1.3.0-1.20.4";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        EventHandler.registerEventHandlers();
        KeyBinds.getKeyBinds().registerKeybinds();
    }
}