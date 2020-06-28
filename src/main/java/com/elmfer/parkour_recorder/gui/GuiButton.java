package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

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
		super(x, y, width, height, new StringTextComponent(text), pressedCallback);
		zLevel = currentZLevel;
	}
	
	public GuiButton(int x, int y, String text, IPressable pressedCallback)
	{
		super(x, y, 100, 20, new StringTextComponent(text), pressedCallback);
		zLevel = currentZLevel;
	}
	
	public void func_230431_b_(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_)
	{
		drawButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
	}
	
	protected boolean func_230992_c_(double p_230992_1_, double p_230992_3_)
	{
		return mousePressed(p_230992_1_, p_230992_3_);
	}
	
	public int x() { return field_230690_l_; }
	public void setX(int x) { field_230690_l_ = x; }
	
	public int y() { return field_230691_m_; }
	public void setY(int y) { field_230691_m_ = y; }
	
	public int width() { return field_230688_j_; }
	public void setWidth(int width) { field_230688_j_ = width; }
	
	public int height() { return field_230689_k_; }
	
	public String text() { return func_230458_i_().getString(); }
	
	public boolean enabled() { return field_230693_o_; }
	public void setEnabled(boolean enabled) { field_230693_o_ = enabled; }
	
	public boolean visible() { return field_230694_p_; }
	public void setVisible(boolean visible) { field_230694_p_ = visible; }
	
	public boolean hovered() { return field_230692_n_; }
	protected void setHovered(boolean hovered) { field_230692_n_ = hovered; }
	
	public void drawButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		if(visible())
		{
			preRender(mouseX, mouseY, partialTicks);
			FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
			int color = hovered() && enabled() ? getIntColor(0.3f, 0.3f, 0.3f, 0.3f) : getIntColor(0.0f, 0.0f, 0.0f, 0.3f);
			
			int j = 14737632;
			if (!enabled())
                j = 10526880;
            else if (hovered())
                j = 16777120;
			
			/**drawRect(MatrixStack, int left, int top, int right, int bottom)**/
			func_238467_a_(new MatrixStack(), x(), y(), x() + width(), y() + height(), color);
			
			/**drawCenteredString(MatrixStack, FontRenderer, String, int x, int y, int color)**/
			func_238471_a_(stack, fontRenderer, text(), x() + width() / 2, y() + (height() - 8) / 2, j);
		}
	}
	
	protected boolean mousePressed(double mouseX, double mouseY)
	{
		return enabled() && visible() && hovered();
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
    	setHovered(zLevel == currentZLevel && mX >= x() && mY >= y() && mX < x() + width() && mY < y() + height() && mouseInViewport(mouseX, mouseY));
	}
	
	private boolean mouseInViewport(double mouseX, double mouseY)
	{
		return mouseX >= viewportX && mouseY >= viewportY && mouseX < viewportX + viewportWidth && mouseY < viewportY + viewportHeight;
	}
}
