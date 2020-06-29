package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;

public class GuiButton extends Button {
	
	public static int currentZLevel = 0;
	public int zLevel = 0;
	
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	public GuiButton(int x, int y, int width, int height, String text, IPressable pressedCallback)
	{
		super(x, y, width, height, text, pressedCallback);
		zLevel = currentZLevel;
	}
	
	public GuiButton(int x, int y, String text, IPressable pressedCallback)
	{
		super(x, y, 100, 20, text, pressedCallback);
		zLevel = currentZLevel;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		if(visible)
		{
			preRender(mouseX, mouseY, partialTicks);
			FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
			int color = isHovered && active ? getIntColor(0.3f, 0.3f, 0.3f, 0.3f) : getIntColor(0.0f, 0.0f, 0.0f, 0.3f);
			
			int j = 14737632;
			if (!active)
                j = 10526880;
            else if (isHovered)
                j = 16777120;
			
			fill(x, y, x + width, y + height, color);
			
			drawCenteredString(fontRenderer, getMessage(), x + width / 2, y + (height - 8) / 2, j);
		}
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return active && visible && isHovered;
	}
	
	@Override
	protected boolean clicked(double mouseX, double mouseY) {
	      return isMouseOver(mouseX, mouseY);
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
    	isHovered = zLevel == currentZLevel && mX >= x && mY >= y && mX < x + width && mY < y + height && mouseInViewport(mouseX, mouseY);
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
