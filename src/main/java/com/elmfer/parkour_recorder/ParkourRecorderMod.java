package com.elmfer.parkour_recorder;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.ButtonListView;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(name = ParkourRecorderMod.MOD_NAME, version = ParkourRecorderMod.MOD_VERSION, modid = ParkourRecorderMod.MOD_ID)
public class ParkourRecorderMod 
{
	public static final String MOD_ID = "parkour_recorder";
	public static final String MOD_NAME = "Parkour Recorder Mod";
	public static final String MOD_VERSION = "1.1.2.0-1.12.2";
	
	@Instance
	public static ParkourRecorderMod instance;
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(event.getSide() == Side.CLIENT)
		{
			MinecraftForge.EVENT_BUS.register(com.elmfer.parkour_recorder.EventHandler.class);
			MinecraftForge.EVENT_BUS.register(ButtonListView.class);
			Settings.getSettings();
			ConfigManager.init();
		}
	}
}