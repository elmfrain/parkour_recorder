package com.elmfer.parkour_recorder;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkour_recorder.gui.MenuScreen;
import com.elmfer.parkour_recorder.gui.UIinput;
import com.elmfer.parkour_recorder.gui.UIrender.Stencil;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.parkour.IParkourSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.parkour.SessionHUD;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.ScreenOpenEvent;
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
	public static void onOpenGui(ScreenOpenEvent event)
	{
		Widget.setCurrentZLevel(0);
		
		if(mc.getMainRenderTarget() != null && !mc.getMainRenderTarget().isStencilEnabled())
		{
			mc.getMainRenderTarget().enableStencil();
			System.out.println("[Parkour Recorder] : Stencil enabled: " + mc.getMainRenderTarget().isStencilEnabled());
		}
	}
	
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
		
		if(event.phase == Phase.END)
		{
			ModelManager.onRenderTick();
			Widget.updateWidgetsOnRenderTick();
			if(UIinput.pollInputs()) Stencil.clear();
		}
	}
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event)
	{	
		if(event.phase == Phase.START)
		{
			Widget.updateWidgetsOnClientTick();
		}
		
		if(event.phase == Phase.START && mc.player != null)
		{
			hud.fadedness += hud.increaseOpacity ? 25 : 0;
			hud.fadedness = Math.max(0, hud.fadedness - 5);
			session.onClientTick();
			
			Settings settings = Settings.getSettings();
			
			if(settings.keybindPlay.consumeClick())
				session = session.onPlay();
			
			if(settings.keybindRecord.consumeClick())
				session = session.onRecord();
			
			if(settings.keybindOverride.consumeClick())
				session = session.onOverride();
			
			if(settings.keybindReloadShaders.consumeClick())
				reloadResources();
			
			if(settings.keybindMainMenu.isDown())
				Minecraft.getInstance().setScreen(new MenuScreen());
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
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/play_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/rewind_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/pause_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/start_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/end_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/settings_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/prev_frame_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/next_frame_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/checkpoint.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/prev_checkpoint_button.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/next_checkpoint_button.ply"));
	}
}
