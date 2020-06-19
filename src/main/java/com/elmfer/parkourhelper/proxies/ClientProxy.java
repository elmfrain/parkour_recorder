package com.elmfer.parkourhelper.proxies;

import com.elmfer.parkourhelper.EventHandler;
import com.elmfer.parkourhelper.ParkourHelperMod;
import com.elmfer.parkourhelper.Settings;
import com.elmfer.parkourhelper.render.ModelManager;
import com.elmfer.parkourhelper.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(EventHandler.class);
		Settings.getSettings();
	}
	public void init(FMLInitializationEvent event)
	{
		
	}
	public void postInit(FMLPostInitializationEvent event)
	{
		
	}
}
