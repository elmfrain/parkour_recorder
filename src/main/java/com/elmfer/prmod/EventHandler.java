package com.elmfer.prmod;

import java.util.ArrayList;

import com.elmfer.prmod.animation.Smoother;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.mesh.Meshes;
import com.elmfer.prmod.parkour.KeyInputHUD;
import com.elmfer.prmod.parkour.ParkourSession;
import com.elmfer.prmod.parkour.PlaybackSession;
import com.elmfer.prmod.parkour.Recording;
import com.elmfer.prmod.parkour.RecordingSession;
import com.elmfer.prmod.parkour.SessionHUD;
import com.elmfer.prmod.ui.MenuScreen;
import com.elmfer.prmod.ui.UIInput;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.widgets.Widget;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class EventHandler {

	public static final int MAX_HISTORY_LENGTH = 16;
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public static ParkourSession session = new RecordingSession();
	public static ArrayList<Recording> recordHistory = new ArrayList<>();

	private static Smoother keyInputHUDpos = new Smoother();

	public static void registerEventHandlers() {
		ScreenEvents.BEFORE_INIT.register(EventHandler::onOpenScreen);
		HudRenderCallback.EVENT.register(EventHandler::onRenderHUD);
		ClientTickEvents.START_CLIENT_TICK.register(EventHandler::onTick);
		ClientLifecycleEvents.CLIENT_STARTED.register(EventHandler::onClientStarted);
		ClientLifecycleEvents.CLIENT_STOPPING.register(EventHandler::onClientStopping);
		WorldRenderEvents.START.register(EventHandler::onStartRenderWorld);
		WorldRenderEvents.END.register(EventHandler::onEndRenderWorld);
	}

	private static void onOpenScreen(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
		Widget.setCurrentZLevel(0);
	}

	private static void onRenderHUD(DrawContext context, float partialTicks) {
		float uiWidth = UIRender.getUIwidth();

		SessionHUD.render();

		boolean showKeyInputHUD = !(mc.currentScreen instanceof MenuScreen);
		if (session instanceof PlaybackSession && ((PlaybackSession) session).isPlaying() && showKeyInputHUD)
			keyInputHUDpos.grab(uiWidth - 10 - KeyInputHUD.size);
		else
			keyInputHUDpos.grab(uiWidth + 5);

		float keyInputHUDposX = keyInputHUDpos.getValuef();
		if (Config.showInputs() && keyInputHUDposX < uiWidth) {
			KeyInputHUD.posY = 30;
			KeyInputHUD.posX = keyInputHUDposX;
			KeyInputHUD.render();
		}
	}
	
	private static void onStartRenderWorld(WorldRenderContext context) {
		UIRender.newFrame();
		session.onRenderTick();
	}
	
	private static void onEndRenderWorld(WorldRenderContext context) {
		// ModelManger.onRenderTick();
		Widget.updateWidgetsOnRenderTick();
		UIInput.pollInputs();
		UIRender.renderBatch();
	}

	private static void onTick(MinecraftClient client) {
		Widget.updateWidgetsOnClientTick();

		if (mc.player == null) {
			recordHistory.clear();
			session = new RecordingSession();
			return;
		}

		SessionHUD.fadedness += SessionHUD.increaseOpacity ? 25 : 0;
		SessionHUD.fadedness = Math.max(0, SessionHUD.fadedness - 5);
		session.onClientTick();

		KeyBinds keyBinds = KeyBinds.getKeyBinds();

		if (keyBinds.kbPlay.wasPressed())
			session = session.onPlay();

		if (keyBinds.kbRecord.wasPressed())
			session = session.onRecord();

		if (keyBinds.kbOverride.wasPressed())
			session = session.onOverride();

		if (keyBinds.kbShowMenu.wasPressed())
			mc.setScreen(new MenuScreen());
	}
	
	private static void onClientStarted(MinecraftClient client) {
		Meshes.loadMeshes();
		Meshes.loadIcons();
	}
	
	private static void onClientStopping(MinecraftClient client) {
		Config.save();
		Config.waitForSave();
	}

	public static void addToHistory(Recording recording) {
		if (recordHistory.size() >= MAX_HISTORY_LENGTH) {
			recordHistory.remove(0);
		}
		recordHistory.add(recording);
	}
}
