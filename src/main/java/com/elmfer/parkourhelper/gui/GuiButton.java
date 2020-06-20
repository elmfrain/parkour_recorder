package com.elmfer.parkourhelper.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.elmfer.parkourhelper.render.GraphicsHelper;
import com.elmfer.parkourhelper.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButton extends net.minecraft.client.gui.GuiButton{
	
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;

	public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
	{
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}
	
	public GuiButton(int buttonId, int x, int y, String buttonText)
	{
		super(buttonId, x, y, 100, 20, buttonText);
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
    	return this.enabled && this.visible && this.hovered;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
        	preRender(mc, mouseX, mouseY, partialTicks);
            FontRenderer fontrenderer = mc.fontRenderer;
            int color = hovered && enabled ? GraphicsHelper.getIntColor(0.3f, 0.3f, 0.3f, 0.3f) : GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.3f);
            
        	drawRect(this.x, this.y, this.x + width, this.y + height, color);
            
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0)
            {
                j = packedFGColour;
            }
            else
            if (!this.enabled)
            {
                j = 10526880;
            }
            else if (this.hovered)
            {
                j = 16777120;
            }
            
            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }
	
	protected void preRender(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    	IntBuffer viewport = BufferUtils.createIntBuffer(16);
    	int factor = (new ScaledResolution(mc)).getScaleFactor();
    	
    	GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
    	GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
    	viewportX = viewport.get(0) / factor;
    	viewportY = (mc.displayHeight - viewport.get(1) - viewport.get(3)) / factor;
    	xTranslate = matrix.get(12) + viewportX;
    	yTranslate = matrix.get(13) + viewportY;
    	viewportWidth = viewport.get(2) / factor;
    	viewportHeight = viewport.get(3) / factor;
    	int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
    	this.hovered = mX >= this.x && mY >= this.y && mX < this.x + this.width && mY < this.y + this.height && mouseInViewport(mouseX, mouseY);
	}
	
	private boolean mouseInViewport(int mouseX, int mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
