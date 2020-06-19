package com.elmfer.parkourhelper;

import com.elmfer.parkourhelper.proxies.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ParkourHelperMod.MOD_ID, name = ParkourHelperMod.MOD_NAME, version = ParkourHelperMod.MOD_VERSION)
public class ParkourHelperMod 
{
	
	public static final String MOD_ID = "parkourhelper";
	public static final String MOD_NAME = "Parkour Helper Mod";
	public static final String MOD_VERSION = "0.0.3.1-1.12.2";
	
	@SidedProxy(clientSide = "com.elmfer.parkourhelper.proxies.ClientProxy", serverSide = "com.elmfer.parkourhelper.proxies.ServerProxy")
	public static CommonProxy proxy;
	
	@Instance
	public static ParkourHelperMod modInstance;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
}
