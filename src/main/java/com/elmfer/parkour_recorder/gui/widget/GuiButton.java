package com.elmfer.parkour_recorder.gui.widget;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.animation.compositon.Composition;
import com.elmfer.parkour_recorder.gui.GuiStyle;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Vector3f;

public class GuiButton extends Button {
	
	public static int currentZLevel = 0;
	public int zLevel = 0;
	public boolean highlighed = false;
	public Vector3f highlightTint = new Vector3f(0.0f, 0.45f, 0.0f);
	
	protected Composition animation = new Composition();
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
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	public GuiButton(int x, int y, String text, IPressable pressedCallback)
	{
		super(x, y, 100, GuiStyle.Gui.buttonHeight(), text, pressedCallback);
		zLevel = currentZLevel;
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		if(visible)
		{
			animation.tick();
			if(isHovered) { animation.queue("hovered"); animation.play(); animation.apply();}
			else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
			if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
			else { animation.queue("highlight"); animation.rewind(); animation.apply();}
			if(!active) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
			
			preRender(mouseX, mouseY, partialTicks);
			FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
			
			Vector3f c = new Vector3f(0.0f, 0.0f, 0.0f);
			Vector3f hoveredcolor = new Vector3f(0.3f, 0.3f, 0.3f);
			hoveredcolor.mul((float) animation.getTimeline("hovered").getFracTime());
			Vector3f highlightColor = highlightTint.copy();
			highlightColor.mul((float) (animation.getTimeline("highlight").getFracTime() * 0.6));
			c.add(hoveredcolor);
			c.add(highlightColor);
			int color = getIntColor(c, 0.4f);
			
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
