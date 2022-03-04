package com.elmfer.parkour_recorder;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.ButtonListView;
import com.elmfer.parkour_recorder.gui.UIinput;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ParkourRecorderMod.MOD_ID)
public class ParkourRecorderMod 
{
	public static final String MOD_ID = "parkour_recorder";
	public static final String MOD_NAME = "Parkour Recorder Mod";
	public static final String MOD_VERSION = "1.1.2.0-1.18.2";
	
	public ParkourRecorderMod()
	{
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus modLoadingBus = FMLJavaModLoadingContext.get().getModEventBus();
		modLoadingBus.addListener(this::onSetup);
	}
	
	private void onSetup(FMLClientSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
		MinecraftForge.EVENT_BUS.register(ButtonListView.class);
		MinecraftForge.EVENT_BUS.register(UIinput.class);
		
		//Setup config
		ModLoadingContext.get().registerConfig(Type.CLIENT, ConfigManager.CONFIG_SPEC);
		ConfigManager.init(FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ConfigManager.CONFIG_EXTENSION));
		
		Settings.getSettings();
	}
}