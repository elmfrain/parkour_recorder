package com.elmfer.parkour_recorder.gui.widget;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.animation.compositon.Composition;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;

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
		super(x, y, width, height, new StringTextComponent(text), pressedCallback);
		zLevel = currentZLevel;
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	public GuiButton(int x, int y, String text, IPressable pressedCallback)
	{
		super(x, y, 100, 20, new StringTextComponent(text), pressedCallback);
		zLevel = currentZLevel;
		animation.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
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
		if(visible())
		{
			animation.tick();
			if(isHovered()) { animation.queue("hovered"); animation.play(); animation.apply();}
			else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
			if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
			else { animation.queue("highlight"); animation.rewind(); animation.apply();}
			if(!active()) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
			
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
			if (!active())
                j = 10526880;
            else if (isHovered())
                j = 16777120;
			
			GraphicsHelper.fill(x(), y(), x() + getWidth(), y() + height(), color);
			
			GraphicsHelper.drawCenteredString(fontRenderer, getMessage(), x() + getWidth() / 2, y() + (height() - 8) / 2, j);
		}
	}
	
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return active() && visible() && isHovered();
	}
	
	protected boolean clicked(double mouseX, double mouseY)
	{
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
    	setHovered(zLevel == currentZLevel && mX >= x() && mY >= y() && mX < x() + getWidth() && mY < y() + height() && mouseInViewport(mouseX, mouseY));
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
