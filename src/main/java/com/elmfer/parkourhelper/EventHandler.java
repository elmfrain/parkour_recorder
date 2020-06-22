package com.elmfer.parkourhelper;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkourhelper.gui.GuiSaveSelection;
import com.elmfer.parkourhelper.gui.GuiSaveSession;
import com.elmfer.parkourhelper.parkour.IParkourSession;
import com.elmfer.parkourhelper.parkour.PlaybackSession;
import com.elmfer.parkourhelper.parkour.Recording;
import com.elmfer.parkourhelper.parkour.RecordingSession;
import com.elmfer.parkourhelper.parkour.SessionHUD;
import com.elmfer.parkourhelper.render.ModelManager;
import com.elmfer.parkourhelper.render.ParticleArrow;
import com.elmfer.parkourhelper.render.ParticleFinish;
import com.elmfer.parkourhelper.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class EventHandler {
	
	public static final int MAX_HISTORY_SIZE = 16;
	static Minecraft mc = Minecraft.getMinecraft();
	static SessionHUD hud = new SessionHUD();
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
				mc.displayGuiScreen(new GuiSaveSelection());
			
			if(settings.keybindSave.isPressed())
				mc.displayGuiScreen(new GuiSaveSession());
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
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/arrow.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/box.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/finish.ply"));
	}
}
