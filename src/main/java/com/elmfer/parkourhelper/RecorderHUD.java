package com.elmfer.parkourhelper;

import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkourhelper.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;

public class RecorderHUD extends Gui 
{
	
	int lastRecordingSize = 0;
	
	public void render()
	{
		Settings settings = Settings.getSettings();
		
		String s = EventHandler.isRecording ? "Recording..." : EventHandler.onOverride ? "Overriding" : 
			EventHandler.isPlaying ? "Playing" : "Stopped";
		
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
		if(EventHandler.isPlaying)
		{
			int c2 = GraphicsHelper.getIntColor(0.2f, 0.6f, 0.2f, 0.3f);
			float progress = (EventHandler.frameNumber + mc.getRenderPartialTicks()) / EventHandler.recording.size();
			
			int amount = (int) ((stringWidth + 2 * lip) * progress);
			drawRect(width - border - stringWidth - lip, border - lip, width - border - stringWidth - lip + amount, border + stringHeight + lip, c2);
			drawRect(width - border - stringWidth - lip + amount, border - lip, width - border + lip, border + stringHeight + lip, c1);
		}else if(EventHandler.onOverride)
		{
			int c2 = GraphicsHelper.getIntColor(0.2f, 0.6f, 0.2f, 0.3f);
			int c3 = GraphicsHelper.getIntColor(1.0f, 0.68f, 0.02f, 0.3f);
			
			float progress = lastRecordingSize * 1.0f / (EventHandler.recording.size() + mc.getRenderPartialTicks());
			
			int amount = (int) ((stringWidth + 2 * lip) * progress);
			
			drawRect(width - border - stringWidth - lip, border - lip, width - border - stringWidth - lip + amount, border + stringHeight + lip, c2);
			drawRect(width - border - stringWidth - lip + amount, border - lip, width - border + lip, border + stringHeight + lip, c3);
		}else {
			drawRect(width - border - stringWidth - lip, border - lip, width - border + lip, border + stringHeight + lip, c1);
		}
		mc.fontRenderer.drawString(s, res.getScaledWidth() - stringWidth - border, border, c);
	}
}
