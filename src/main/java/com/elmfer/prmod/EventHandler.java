package com.elmfer.prmod;

import java.util.ArrayList;

import com.elmfer.prmod.parkour.ParkourSession;
import com.elmfer.prmod.parkour.Recording;
import com.elmfer.prmod.parkour.RecordingSession;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class EventHandler {

	public static final int MAX_HISTORY_LENGTH = 16;
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ParkourSession session = new RecordingSession();
	public static ArrayList<Recording> recordHistory = new ArrayList<>();
	
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
	
	public static void addToHistory(Recording recording) {
		if (recordHistory.size() >= MAX_HISTORY_LENGTH) {
			recordHistory.remove(0);
		}
		recordHistory.add(recording);
	}
}
