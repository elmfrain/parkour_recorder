package com.elmfer.parkour_recorder.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class GuiTextField extends TextFieldWidget {
	
	private float xTranslate = 0.0f;
	private float yTranslate = 0.0f;
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	public GuiTextField(FontRenderer fontRenderer, int x, int y, int width, int height)
	{
		super(fontRenderer, x, y, width, height, new StringTextComponent(""));
	}
	
	public GuiTextField(FontRenderer fontRenderer, int x, int y)
	{
		super(fontRenderer, x, y, 100, 20, new StringTextComponent(""));
	}
	
	@Override
	public void func_230431_b_(MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_)
	{
		drawTextBox(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
	}
	
	@Override
	public boolean func_231047_b_(double p_231047_1_, double p_231047_3_)
	{
		return isMouseOver(p_231047_1_, p_231047_3_);
	}
	
	@Override
	public boolean func_231044_a_(double mouseX, double mouseY, int mouseButton)
	{
		return mouseClicked(mouseX, mouseY, mouseButton);
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
	
	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return enabled() && visible() && hovered();
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
    	return super.func_231044_a_(mX, mY, mouseButton);
	}
	
	public void drawTextBox(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		preRender(mouseX, mouseY, partialTicks);
		int mX = (int) (mouseX - xTranslate);
    	int mY = (int) (mouseY - yTranslate);
		super.func_230431_b_(stack, mouseX, mouseY, partialTicks);
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
    	setHovered(mX >= x() && mY >= y() && mX < x() + width() && mY < y() + height() && mouseInViewport(mouseX, mouseY));;
    }
}
