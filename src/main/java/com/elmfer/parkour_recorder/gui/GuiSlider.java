package com.elmfer.parkour_recorder.gui;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class GuiSlider extends Slider
{
	public int zLevel = 0;
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	public GuiSlider(int xPos, int yPos, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler)
	{
		super(xPos, yPos, 100, GuiStyle.Gui.buttonHeight(), prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
		zLevel = GuiButton.currentZLevel;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(visible)
		{
			float trackFracHeight = 0.8f;
			int trackMargin = (int) (height - height * trackFracHeight);
			int backgroundColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.5f);
			int knobColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.7f);
			
			int knobWidth = GuiStyle.Gui.margin();
			int knobX = (int) ((width - knobWidth) * sliderValue - knobWidth / 2) + knobWidth / 2;
			
			preRender(mouseX, mouseY, partialTicks);
			
			renderBg(mc, mouseX, mouseY);
			
			fill(x, y + trackMargin, x + width, y + height - trackMargin, backgroundColor);
			fill(knobX, y, knobX + knobWidth, height, knobColor);
			if(drawString)
				drawCenteredString(mc.fontRenderer, getMessage(), width / 2, height / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return (active && visible && isHovered) || dragging;
	}
	
	@Override
    public void onClick(double mouseX, double mouseY)
    {
		int mX = (int) (mouseX - xTranslate);
		
		this.sliderValue = (mX - (this.x + 4)) / (float)(this.width - 8);
        updateSlider();
        this.dragging = true;
    }
	
	@Override
	protected boolean clicked(double mouseX, double mouseY)
	{
	      return isMouseOver(mouseX, mouseY);
	}
	
	@Override
    protected void renderBg(Minecraft minecraft, int mouseX, int mouseY)
    {
		int mX = (int) (mouseX - xTranslate);
		
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (mX - (this.x + 4)) / (float)(this.width - 8);
                updateSlider();
            }
        }
    }
	
	protected void preRender(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		float[] matrix = new float[16];
    	int[] viewport = new int[16];
    	double factor = res.getGuiScaleFactor();
    	
    	GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, matrix);
    	GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
    	viewportX = (int) (viewport[0] / factor);
    	viewportY = (int) ((res.getHeight() - viewport[1] - viewport[3]) / factor);
    	xTranslate = matrix[12] + viewportX;
    	yTranslate = matrix[13] + viewportY;
    	viewportWidth = (int) (viewport[2] / factor);
    	viewportHeight = (int) (viewport[3] / factor);
    	int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
    	isHovered = zLevel == GuiButton.currentZLevel && mX >= x && mY >= y && mX < x + width && mY < y + height && mouseInViewport(mouseX, mouseY);
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
