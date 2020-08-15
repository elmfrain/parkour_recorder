package com.elmfer.parkour_recorder.gui;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
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
		super(xPos, yPos, 100, GuiStyle.Gui.buttonHeight(), new StringTextComponent(prefix), new StringTextComponent(suf), minVal, maxVal, currentVal, showDec, drawStr, handler);
		zLevel = GuiButton.currentZLevel;
	}
	
	public void func_230431_b_(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_)
	{
		renderButton(p_230431_2_, p_230431_3_, p_230431_4_);
	}
	
	protected boolean func_230992_c_(double p_230992_1_, double p_230992_3_)
	{
		return isMouseOver(p_230992_1_, p_230992_3_);
	}
	
	public int x() { return field_230690_l_; }
	public void setX(int x) { field_230690_l_ = x; }
	
	public int y() { return field_230691_m_; }
	public void setY(int y) { field_230691_m_ = y; }
	
	public int getWidth() { return field_230688_j_; }
	public void setWidth(int width) { field_230688_j_ = width; }
	
	public int height() { return field_230689_k_; }
	
	public String getMessage() { return func_230458_i_().getString(); }
	
	public boolean active() { return field_230693_o_; }
	public void setActive(boolean enabled) { field_230693_o_ = enabled; }
	
	public boolean visible() { return field_230694_p_; }
	public void setVisible(boolean visible) { field_230694_p_ = visible; }
	
	public boolean isHovered() { return field_230692_n_; }
	protected void setHovered(boolean hovered) { field_230692_n_ = hovered; }

	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(visible())
		{
			float trackFracHeight = 0.8f;
			int trackMargin = (int) (height() - height() * trackFracHeight);
			int backgroundColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.5f);
			int knobColor = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.7f);
			
			int knobWidth = GuiStyle.Gui.margin();
			int knobX = (int) ((getWidth() - knobWidth) * sliderValue - knobWidth / 2) + knobWidth / 2;
			
			preRender(mouseX, mouseY, partialTicks);
			
			renderBg(mc, mouseX, mouseY);
			
			GraphicsHelper.fill(x(), y() + trackMargin, x() + getWidth(), y() + height() - trackMargin, backgroundColor);
			GraphicsHelper.fill(knobX, y(), knobX + knobWidth, height(), knobColor);
			if(drawString)
				GraphicsHelper.drawCenteredString(mc.fontRenderer, getMessage(), getWidth() / 2, height() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
	}
	
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return (active() && visible() && isHovered()) || dragging;
	}
	
	/**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
	@Override
    public void func_230982_a_(double mouseX, double mouseY)
    {
		int mX = (int) (mouseX - xTranslate);
		
		this.sliderValue = (mX - (this.x() + 4)) / (float)(this.getWidth() - 8);
        updateSlider();
        this.dragging = true;
    }
	
	@Override
	public boolean func_231047_b_(double mouseX, double mouseY)
	{
	      return isMouseOver(mouseX, mouseY);
	}
	
    protected void renderBg(Minecraft minecraft, int mouseX, int mouseY)
    {
		int mX = (int) (mouseX - xTranslate);
		
        if (this.visible())
        {
            if (this.dragging)
            {
                this.sliderValue = (mX - (this.x() + 4)) / (float)(this.getWidth() - 8);
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
    	setHovered(zLevel == GuiButton.currentZLevel && mX >= x() && mY >= y() && mX < x() + getWidth() && mY < y() + height() && mouseInViewport(mouseX, mouseY));
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
