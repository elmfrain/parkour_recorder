package com.elmfer.prmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class EventHandler {

	public static void registerEventHandlers() {
		ClientTickEvents.START_CLIENT_TICK.register(EventHandler::onTick);
	}
	
	private static void onTick(MinecraftClient client) {
		
	}
}
