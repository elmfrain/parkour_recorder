package com.elmfer.prmod.parkour;

import org.lwjgl.opengl.GL11;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.ui.UIRender;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;

public class SessionHUD
{
	public static int fadedness = 0;
	public static boolean increaseOpacity = false;
	
	public static void render()
	{
		increaseOpacity = false;
		String s = I18n.translate("com.prmod.stopped");
		if(EventHandler.session instanceof RecordingSession)
		{
			if(((RecordingSession) EventHandler.session).onOverride)
			{
				s = I18n.translate("com.prmod.overriding");
				String name = ((RecordingSession) EventHandler.session).recording.getName();
				name = name == null ? "[" + I18n.translate("com.prmod.unamed") + "]" : name;
				s += ": " + name;
				increaseOpacity = true;
			}
			else if(((RecordingSession) EventHandler.session).isWaitingForPlayer())
			{
				increaseOpacity = true;
				s = I18n.translate("com.prmod.waiting_for_player");
			}
			else if(((RecordingSession) EventHandler.session).isRecording)
			{
				 s = I18n.translate("com.prmod.recording");
				 increaseOpacity = true;
			}
		}
		else if(EventHandler.session instanceof PlaybackSession)
		{
			if(((PlaybackSession) EventHandler.session).isPlaying())
			{
				increaseOpacity = true;
				s = I18n.translate("com.prmod.playing");
			}
			else if(((PlaybackSession) EventHandler.session).isWaitingForPlayer())
			{
				increaseOpacity = true;
				s = I18n.translate("com.prmod.waiting_for_player");
			}
			String name = ((PlaybackSession) EventHandler.session).recording.getName();
			name = name == null ? "[" + I18n.translate("com.prmod.unamed") + "]" : name;
			s += " - " + name;
		}
		fadedness = Math.min(200, fadedness);
		if(fadedness > 5)
		{
			MinecraftClient mc = MinecraftClient.getInstance();
			Window res = mc.getWindow();
			
			int border = 10;
			int lip = 2;
			int stringWidth = mc.textRenderer.getWidth(s);
			int stringHeight = mc.textRenderer.fontHeight;
			
			int width = res.getScaledHeight();
			float fade = Math.min(100, fadedness) / 100.0f;
			int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, fade);
			int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f * fade);
			GL11.glEnable(GL11.GL_BLEND);
			
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA,  GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			boolean showLoopIcon =
					EventHandler.session instanceof PlaybackSession ? ((PlaybackSession) EventHandler.session).recording.isLoop() : true;
			
			if(Config.isLoopMode() && showLoopIcon)
			{
				UIRender.drawRect(width - stringWidth - border - lip * 3 - stringHeight, border - lip, width - border + lip, border + stringHeight + lip, c1);
				
				UIRender.drawIcon("loop_icon", width - border - stringWidth - stringHeight, border + border / 2, stringHeight, c);
			}
			else
				UIRender.drawRect(width - stringWidth - border - lip, border - lip, width - border + lip, border + stringHeight + lip, c1);
			
			UIRender.drawString(s, width - stringWidth - border, border, c);
		}
	}
}
