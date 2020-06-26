package com.elmfer.parkour_recorder.parkour;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;

public class SessionHUD extends AbstractGui
{
	
	int lastRecordingSize = 0;
	
	public void render()
	{
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
		
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		
		int border = 10;
		//int lip = 2;
		int stringWidth = mc.fontRenderer.getStringWidth(s);
		//int stringHeight = mc.fontRenderer.FONT_HEIGHT;
		
		//int width = res.getScaledWidth();
		int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, 1.0f);
		//int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		//drawRect(res.getScaledWidth() - stringWidth - border - lip, border - lip, res.getScaledWidth() - border + lip, border + stringHeight + lip, c1);
		//drawString
		func_238471_a_(new MatrixStack(), mc.fontRenderer, s, res.getScaledWidth() - stringWidth - border, border, c);
	}
}
