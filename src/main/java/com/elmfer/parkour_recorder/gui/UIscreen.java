package com.elmfer.parkour_recorder.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.Window;

import net.minecraft.client.gui.GuiScreen;

public abstract class UIscreen extends GuiScreen implements UIinput.Listener
{	
	public UIscreen()
	{
		UIinput.addListener(this);
	}
	
	@Override
	public void finalize()
	{
		close();
	}
	
	@Override
	public void onGuiClosed()
	{
		Window.closeWindows();
		UIinput.clearListeners();
		Widget.clearWidgets();
	}
	
	@Override
	public void onKeyPressed(int keyCode)
	{
		if(!Window.areWindowsOpen() && keyCode == Keyboard.KEY_ESCAPE)
		{
			mc.displayGuiScreen(null);
		}
	}
	
	@Override
	public void handleInput() throws IOException
	{
		//Do nothing, lol
		//UIinput is doing all the input stuff for us
	}
}
