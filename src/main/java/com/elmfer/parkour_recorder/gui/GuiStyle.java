package com.elmfer.parkour_recorder.gui;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector4f;

public class GuiStyle
{
	private static FontRenderer font = Minecraft.getInstance().fontRenderer;
	private static MainWindow res = Minecraft.getInstance().getMainWindow();
	
	public static class Gui
	{
		public static int bodyMargin()
		{
			return (int) (80 / res.getGuiScaleFactor());
		}
		
		public static int margin()
		{
			return 10;
		}
		
		public static int smallMargin()
		{
			return 2;
		}
		
		public static Vector4f backroundColor()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 0.3f);
		}
		
		public static int shortButtonHeight()
		{
			return 14;
		}
		
		public static int buttonHeight()
		{
			return 20;
		}
		
		public static int fadeHeight()
		{
			return 15;
		}
		
		public static Vector4f fade1()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 0.4f);
		}
		
		public static Vector4f fade2()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
		}
	}
	
	public static class AlertBox
	{
		public static Vector4f backroundColor()
		{
			return new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);
		}
		
		public static Vector4f borderColor()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
		}	
		
		public static Vector4f fade1()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 0.9f);
		}
		
		public static Vector4f fade2()
		{
			return new Vector4f(0.0f, 0.0f, 0.0f, 0.4f);
		}
		
		public static int boxWidth()
		{
			return res.getScaledWidth() / 2;
		}
		
		public static int titleHeight()
		{
			return font.FONT_HEIGHT * 2;
		}
	}
}
