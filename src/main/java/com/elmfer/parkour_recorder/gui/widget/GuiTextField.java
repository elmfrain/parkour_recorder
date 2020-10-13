package com.elmfer.parkour_recorder.gui.widget;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextField extends TextFieldWidget {
	
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	public GuiTextField(FontRenderer fontRenderer, int x, int y, int width, int height)
	{
		super(fontRenderer, x, y, width, height, "");
	}
	
	public GuiTextField(FontRenderer fontRenderer, int x, int y)
	{
		super(fontRenderer, x, y, 100, 20, "");
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
    	return super.mouseClicked(mX, mY, mouseButton);
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		preRender(mouseX, mouseY, partialTicks);
		int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
		super.renderButton(mX, mY, partialTicks);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return active && visible && isHovered;
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
	
	protected void preRender(int mouseX, int mouseY, float partialTicks)
    {
		MainWindow res = Minecraft.getInstance().getMainWindow();
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
    	isHovered = mX >= x && mY >= y && mX < x + width && mY < y + height && mouseInViewport(mouseX, mouseY);
    }
}
