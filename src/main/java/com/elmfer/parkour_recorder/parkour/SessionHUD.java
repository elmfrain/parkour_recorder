package com.elmfer.parkour_recorder.parkour;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;

public class SessionHUD extends AbstractGui
{
	public int fadedness = 0;
	public boolean increaseOpacity = false;
	
	public void render()
	{
		increaseOpacity = false;
		String s = I18n.format("hud.session.stopped");
		if(EventHandler.session instanceof RecordingSession)
		{
			if(((RecordingSession) EventHandler.session).onOverride)
			{
				s = I18n.format("hud.session.overriding");
				String name = ((RecordingSession) EventHandler.session).recording.getName();
				name = name == null ? "[" + I18n.format("recording.unamed") + "]" : name;
				s += ": " + name;
				increaseOpacity = true;
			}
			else if(((RecordingSession) EventHandler.session).isRecording)
			{
				 s = I18n.format("hud.session.recording");
				 increaseOpacity = true;
			}
		}
		else if(EventHandler.session instanceof PlaybackSession)
		{
			if(((PlaybackSession) EventHandler.session).isPlaying())
			{
				increaseOpacity = true;
				s = I18n.format("hud.session.playing");
			}
			else if(((PlaybackSession) EventHandler.session).isWaitingForPlayer())
			{
				increaseOpacity = true;
				s = I18n.format("hud.session.waiting_for_player");
			}
			String name = ((PlaybackSession) EventHandler.session).recording.getName();
			name = name == null ? "[" + I18n.format("recording.unamed") + "]" : name;
			s += " - " + name;
		}
		fadedness = Math.min(200, fadedness);
		if(fadedness > 5)
		{
			Minecraft mc = Minecraft.getInstance();
			MainWindow res = mc.getMainWindow();
			
			int border = 10;
			int lip = 2;
			int stringWidth = mc.fontRenderer.getStringWidth(s);
			int stringHeight = mc.fontRenderer.FONT_HEIGHT;
			
			int width = res.getScaledWidth();
			float fade = Math.min(100, fadedness) / 100.0f;
			int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, 1.0f * fade);
			int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f * fade);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GraphicsHelper.fill(width - stringWidth - border - lip, border - lip, width - border + lip, border + stringHeight + lip, c1);
			GraphicsHelper.drawString(mc.fontRenderer, s, width - stringWidth - border, border, c);
		}
	}
}
