package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.animation.compositon.Composition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class GuiButton extends net.minecraft.client.gui.GuiButton {
	
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
	
	public GuiButton(int id, int x, int y, int width, int height, String text)
	{
		super(id, x, y, width, height, text);
		zLevel = currentZLevel;
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	public GuiButton(int id, int x, int y, String text)
	{
		super(id, x, y, 100, GuiStyle.Gui.buttonHeight(), text);
		zLevel = currentZLevel;
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if(visible)
		{
			animation.tick();
			if(hovered) { animation.queue("hovered"); animation.play(); animation.apply();}
			else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
			if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
			else { animation.queue("highlight"); animation.rewind(); animation.apply();}
			if(!enabled) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
			
			preRender(mouseX, mouseY, partialTicks);
			FontRenderer fontRenderer = mc.fontRenderer;
			
			Vector3f hoveredcolor = new Vector3f(0.3f, 0.3f, 0.3f);
			hoveredcolor.scale((float) animation.getTimeline("hovered").getFracTime());
			Vector3f highlightColor = new Vector3f(highlightTint);
			highlightColor.scale((float) (animation.getTimeline("highlight").getFracTime() * 0.6));
			Vector3f c = Vector3f.add(hoveredcolor, highlightColor, null);
			int color = getIntColor(c, 0.4f);
			
			int j = 14737632;
			if (!enabled)
                j = 10526880;
            else if (hovered)
                j = 16777120;
			
			drawRect(x, y, x + width, y + height, color);
			
			drawCenteredString(fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, j);
		}
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		return enabled && visible && hovered;
	}
	
	protected void preRender(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getMinecraft();
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
    	this.hovered = zLevel == GuiButton.currentZLevel && mX >= this.x && mY >= this.y && mX < this.x + this.width && mY < this.y + this.height && mouseInViewport(mouseX, mouseY);
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
