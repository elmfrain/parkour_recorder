package com.elmfer.parkourhelper.parkour;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkourhelper.EventHandler;
import com.elmfer.parkourhelper.Settings;
import com.elmfer.parkourhelper.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class SessionHUD extends Gui 
{
	
	int lastRecordingSize = 0;
	
	public void render()
	{
		Settings settings = Settings.getSettings();
		
		String s = I18n.format("hud.session.stopped");
		if(EventHandler.session instanceof RecordingSession)
		{
			if(((RecordingSession) EventHandler.session).onOverride)
			{
				s = I18n.format("hud.session.overriding");
				if(((RecordingSession) EventHandler.session).recording.getName() != null)
				{
					String name = ((RecordingSession) EventHandler.session).recording.getName();
					name = name == null ? "[" + I18n.format("recording.unamed") + "]" : name;
					s += ": " + name;
				}
			}
			else if(((RecordingSession) EventHandler.session).isRecording) s = I18n.format("hud.session.recording");
			
		}
		else if(EventHandler.session instanceof PlaybackSession)
		{
			if(((PlaybackSession) EventHandler.session).isPlaying()) s = I18n.format("hud.session.playing");
			if(((PlaybackSession) EventHandler.session).isWaitingForPlayer()) s = I18n.format("hud.session.waiting_for_player");
			String name = ((PlaybackSession) EventHandler.session).recording.getName();
			name = name == null ? "[" + I18n.format("recording.unamed") + "]" : name;
			s += " - " + name;
		}
		
		Minecraft mc = Minecraft.getMinecraft();
		
		int border = 10;
		int lip = 2;
		int stringWidth = mc.fontRenderer.getStringWidth(s);
		int stringHeight = mc.fontRenderer.FONT_HEIGHT;
		
		ScaledResolution res = new ScaledResolution(mc);
		int width = res.getScaledWidth();
		int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, 1.0f);
		int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		drawRect(res.getScaledWidth() - stringWidth - border - lip, border - lip, res.getScaledWidth() - border + lip, border + stringHeight + lip, c1);
		mc.fontRenderer.drawString(s, res.getScaledWidth() - stringWidth - border, border, c);
	}
}
