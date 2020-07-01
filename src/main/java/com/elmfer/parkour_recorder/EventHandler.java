package com.elmfer.parkour_recorder;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkour_recorder.gui.LoadRecordingScreen;
import com.elmfer.parkour_recorder.gui.SaveRecordingScreen;
import com.elmfer.parkour_recorder.parkour.IParkourSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.parkour.SessionHUD;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {
	
	public static final int MAX_HISTORY_SIZE = 16;
	static Minecraft mc = Minecraft.getInstance();
	public static SessionHUD hud = new SessionHUD();
	public static IParkourSession session = new RecordingSession();
	public static List<Recording> recordHistory = new ArrayList<>();
	
	@SubscribeEvent
	public static void onOverlayRender(RenderGameOverlayEvent event)
	{
		if(event.getType() == ElementType.CHAT)
			hud.render();
	}
	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event)
	{
		if(event.phase == Phase.START && mc.player != null)
			session.onRenderTick();
	}
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event)
	{	
		if(event.phase == Phase.START && mc.player != null)
		{
			hud.fadedness += hud.increaseOpacity ? 25 : 0;
			hud.fadedness = Math.max(0, hud.fadedness - 5);
			session.onClientTick();
			
			Settings settings = Settings.getSettings();
			
			if(settings.keybindPlay.isPressed())
				session = session.onPlay();
			
			if(settings.keybindRecord.isPressed())
				session = session.onRecord();
			
			if(settings.keybindOverride.isPressed())
				session = session.onOverride();
			
			if(settings.keybindReloadShaders.isPressed())
				reloadResources();
			
			if(settings.keybindLoad.isPressed())
				Minecraft.getInstance().displayGuiScreen(new LoadRecordingScreen());
			
			if(settings.keybindSave.isPressed())
				Minecraft.getInstance().displayGuiScreen(new SaveRecordingScreen());
		}
		else if(mc.player == null)
		{
			recordHistory.clear();
			session = new RecordingSession();
		}
	}
	
	public static void addToHistory(Recording recording)
	{
		if(recordHistory.size() == MAX_HISTORY_SIZE)
		{
			recordHistory.remove(0);
		}
		recordHistory.add(recording);
	}
	
	private static void reloadResources()
	{
		ShaderManager.reloadShaders();
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/arrow.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/box.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/finish.ply"));
	}
}
