package com.elmfer.parkour_recorder;

import com.elmfer.parkour_recorder.gui.GuiButtonList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ParkourRecorderMod.MOD_ID)
public class ParkourRecorderMod 
{
	public static final String MOD_ID = "parkour_recorder";
	public static final String MOD_NAME = "Parkour Recorder Mod";
	public static final String MOD_VERSION = "0.0.4.16-1.15.2-rc9";
	
	public ParkourRecorderMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus modLoadingBus = FMLJavaModLoadingContext.get().getModEventBus();
		modLoadingBus.addListener(this::onSetup);
	}
	
	private void onSetup(FMLClientSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
		MinecraftForge.EVENT_BUS.register(GuiButtonList.class);
		Settings.getSettings();
	}
}