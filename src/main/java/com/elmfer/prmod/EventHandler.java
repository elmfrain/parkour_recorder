package com.elmfer.prmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class EventHandler {

	public static void registerEventHandlers() {
		ScreenEvents.BEFORE_INIT.register(EventHandler::onOpenScreen);
		HudRenderCallback.EVENT.register(EventHandler::onRenderHUD);
		ClientTickEvents.START_CLIENT_TICK.register(EventHandler::onTick);
	}
	
	private static void onOpenScreen(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
		ParkourRecorder.LOGGER.info("opened screen!");
	}
	
	private static void onRenderHUD(DrawContext context, float partialTicks) {
		
	}
	
	private static void onTick(MinecraftClient client) {
		
	}
}
