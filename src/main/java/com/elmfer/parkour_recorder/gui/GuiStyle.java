package com.elmfer.parkour_recorder.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class GuiStyle
{
	private static Font font = Minecraft.getInstance().font;
	private static Window res = Minecraft.getInstance().getWindow();
	
	public static class Gui
	{
		public static int bodyMargin()
		{
			return (int) (80 / res.getGuiScale());
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
			return res.getGuiScaledWidth() / 2;
		}
		
		public static int titleHeight()
		{
			return font.lineHeight * 2;
		}
	}
}
