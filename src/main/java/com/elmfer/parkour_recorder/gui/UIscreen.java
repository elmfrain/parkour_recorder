package com.elmfer.parkour_recorder.gui;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class UIscreen extends Screen implements UIinput.Listener
{	
	public UIscreen()
	{
		super(new TranslatableComponent("com.elmfer.parkour_recorder"));
		UIinput.addListener(this);
	}
	
	@Override
	public void finalize()
	{
		close();
	}
	
	/**When gui closes.**/
	@Override
	public void removed()
	{
		Window.closeWindows();
		UIinput.clearListeners();
		Widget.clearWidgets();
	}
	
	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_)
	{
		return false;
	}
	
	@Override
	public void onKeyPressed(int keyCode)
	{
		if(!Window.areWindowsOpen() && keyCode == GLFW.GLFW_KEY_ESCAPE)
		{
			Minecraft mc = Minecraft.getInstance();
			mc.setScreen(null);
		}
	}
}
