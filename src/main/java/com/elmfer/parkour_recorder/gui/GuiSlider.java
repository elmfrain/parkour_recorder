package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.ScaledResolution;

public class GuiSlider extends net.minecraft.client.gui.GuiSlider
{
	private static GuiResponder defaultResponder = new GuiResponder()
	{			
		@Override
		public void setEntryValue(int id, String value) {
		}
		
		@Override
		public void setEntryValue(int id, float value) {
		}
		
		@Override
		public void setEntryValue(int id, boolean value) {

		}
	};
	
	public int zLevel = 0;
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	private final String prefix;
	private final String suffix;
	
	public GuiSlider(int id, int xPos, int yPos, String prefix, String suffix, float minVal, float maxVal, float currentVal)
	{
		super(defaultResponder, id, xPos, yPos, "", minVal, maxVal, currentVal, null);
		this.prefix = prefix;
		this.suffix = suffix;
		zLevel = GuiButton.currentZLevel;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(visible)
		{
			float trackFracHeight = 0.8f;
			int trackMargin = (int) (height - height * trackFracHeight);
			int backgroundColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.5f);
			int knobColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.7f);
			
			int knobWidth = GuiStyle.Gui.margin();
			int knobX = (int) ((width - knobWidth) * getSliderPosition() - knobWidth / 2) + knobWidth / 2;
			
			preRender(mouseX, mouseY, partialTicks);
			
			mouseDragged(mc, mouseX, mouseY);
			
			drawRect(x, y + trackMargin, x + width, y + height - trackMargin, backgroundColor);
			drawRect(knobX, y, knobX + knobWidth, height, knobColor);
			
			drawCenteredString(mc.fontRenderer, getDisplayString(), width / 2, height / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		int mX = (int) (mouseX - xTranslate);
		
		if((enabled && visible && hovered) || isMouseDown)
		{
			setSliderPosition(Math.min(Math.max(0, (mX - (this.x + 4)) / (float)(this.width - 8)), 1));

            this.displayString = this.getDisplayString();
            this.isMouseDown = true;
            return true;
		}
		else return false;
	}
	
	@Override
    protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY)
    {
		int mX = (int) (mouseX - xTranslate);
		
        if (this.visible)
        {
            if (this.isMouseDown)
            {
                setSliderPosition(Math.min(Math.max(0, (mX - (this.x + 4)) / (float)(this.width - 8)), 1));
               
            }
        }
    }
	
	protected void preRender(int mouseX, int mouseY, float partialTicks)
	{
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    	IntBuffer viewport = BufferUtils.createIntBuffer(16);
    	double factor = res.getScaleFactor();
    	
    	GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
    	GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
    	viewportX = (int) (viewport.get(0) / factor);
    	viewportY = (int) ((Minecraft.getMinecraft().displayHeight - viewport.get(1) - viewport.get(3)) / factor);
    	xTranslate = matrix.get(12) + viewportX;
    	yTranslate = matrix.get(13) + viewportY;
    	viewportWidth = (int) (viewport.get(2) / factor);
    	viewportHeight = (int) (viewport.get(3) / factor);
    	int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
    	hovered = zLevel == GuiButton.currentZLevel && mX >= x && mY >= y && mX < x + width && mY < y + height && mouseInViewport(mouseX, mouseY);
	}
	
	private String getDisplayString()
    {
        return prefix + getSliderValue() + suffix;
    }
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
