package com.elmfer.parkour_recorder;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkour_recorder.animation.Smoother;
import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.MenuScreen;
import com.elmfer.parkour_recorder.gui.UIinput;
import com.elmfer.parkour_recorder.gui.UIrender;
import com.elmfer.parkour_recorder.gui.UIrender.Stencil;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.parkour.IParkourSession;
import com.elmfer.parkour_recorder.parkour.KeyInputHUD;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.parkour.SessionHUD;
import com.elmfer.parkour_recorder.render.ModelManager;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {
	
	public static final int MAX_HISTORY_SIZE = 16;
	static Minecraft mc = Minecraft.getInstance();
	public static IParkourSession session = new RecordingSession();
	public static List<Recording> recordHistory = new ArrayList<>();
	
	private static Smoother keyInputHUDpos = new Smoother();
	
	@SubscribeEvent
	public static void onOpenGui(GuiOpenEvent event)
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
		{
			float uiWidth = UIrender.getUIwidth();
			
			SessionHUD.render();
			
			boolean showKeyInputHUD = !(mc.screen instanceof MenuScreen);
			if(session instanceof PlaybackSession && ((PlaybackSession) session).isPlaying() && showKeyInputHUD)
				keyInputHUDpos.grab(uiWidth - 10 - KeyInputHUD.size);
			else
				keyInputHUDpos.grab(uiWidth + 5);	
			
			float keyInputHUDposX = keyInputHUDpos.getValuef();
			if(ConfigManager.showInputs() && keyInputHUDposX < uiWidth)
			{
				KeyInputHUD.posY = 30;
				KeyInputHUD.posX = keyInputHUDposX;
				KeyInputHUD.render();
			}
		}
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
			SessionHUD.fadedness += SessionHUD.increaseOpacity ? 25 : 0;
			SessionHUD.fadedness = Math.max(0, SessionHUD.fadedness - 5);
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
			
			if(settings.keybindMainMenu.consumeClick())
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
		//ShaderManager.reloadShaders();
		ModelManager.clearCachedModels();
		System.out.println("Reloaded Resources");
	}
}
