package com.elmfer.parkour_recorder.parkour;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.UIrender;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;

public class SessionHUD extends GuiComponent
{
	public int fadedness = 0;
	public boolean increaseOpacity = false;
	
	public void render()
	{
		increaseOpacity = false;
		String s = I18n.get("com.elmfer.stopped");
		if(EventHandler.session instanceof RecordingSession)
		{
			if(((RecordingSession) EventHandler.session).onOverride)
			{
				s = I18n.get("com.elmfer.overriding");
				String name = ((RecordingSession) EventHandler.session).recording.getName();
				name = name == null ? "[" + I18n.get("com.elmfer.unamed") + "]" : name;
				s += ": " + name;
				increaseOpacity = true;
			}
			else if(((RecordingSession) EventHandler.session).isRecording)
			{
				 s = I18n.get("com.elmfer.recording");
				 increaseOpacity = true;
			}
		}
		else if(EventHandler.session instanceof PlaybackSession)
		{
			if(((PlaybackSession) EventHandler.session).isPlaying())
			{
				increaseOpacity = true;
				s = I18n.get("com.elmfer.playing");
			}
			else if(((PlaybackSession) EventHandler.session).isWaitingForPlayer())
			{
				increaseOpacity = true;
				s = I18n.get("com.elmfer.waiting_for_player");
			}
			String name = ((PlaybackSession) EventHandler.session).recording.getName();
			name = name == null ? "[" + I18n.get("com.elmfer.unamed") + "]" : name;
			s += " - " + name;
		}
		fadedness = Math.min(200, fadedness);
		if(fadedness > 5)
		{
			Minecraft mc = Minecraft.getInstance();
			Window res = mc.getWindow();
			
			int border = 10;
			int lip = 2;
			int stringWidth = mc.font.width(s);
			int stringHeight = mc.font.lineHeight;
			
			int width = res.getGuiScaledWidth();
			float fade = Math.min(100, fadedness) / 100.0f;
			int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, fade);
			int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f * fade);
			GL11.glEnable(GL11.GL_BLEND);
			
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA,  GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			UIrender.drawRect(width - stringWidth - border - lip, border - lip, width - border + lip, border + stringHeight + lip, c1);
			
			UIrender.drawString(s, width - stringWidth - border, border, c);
		}
	}
}
