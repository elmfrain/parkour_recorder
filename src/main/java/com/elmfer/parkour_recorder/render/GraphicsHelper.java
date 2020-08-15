package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;

public class GraphicsHelper {
	
	public static final MatrixStack identity = new MatrixStack();
	
	public static int getIntColor(float red, float green, float blue, float alpha)
	{
		int value = 0;
		value |= (floatToByte(alpha) << 24);
		value |= (floatToByte(red) << 16);
		value |= (floatToByte(green) << 8);
		value |= floatToByte(blue);
		return value;
	}
	
	public static int getIntColor(Vector4f color)
	{
		return getIntColor(color.getX(), color.getY(), color.getZ(), color.getW());
	}
	
	public static int getIntColor(Vector3f color, float alpha)
	{
		return getIntColor(color.getX(), color.getY(), color.getZ(), alpha);
	}
	
	public static Vector4f getFloatColor(int color)
	{
		float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        
        return new Vector4f(f1, f2, f3, f);
	}
	
	public static void fill(int left, int top, int right, int bottom, int color)
	{
		AbstractGui.func_238467_a_(identity, left, top, right, bottom, color);
	}
	
	public static void fillGradient(int left, int top, int right, int bottom, int color1, int color2)
	{
		float f =  (float)(color1 >> 24 & 255) / 255.0F;
		float f1 = (float)(color1 >> 16 & 255) / 255.0F;
		float f2 = (float)(color1 >> 8 & 255) / 255.0F;
		float f3 = (float)(color1 & 255) / 255.0F;
		float f4 = (float)(color2 >> 24 & 255) / 255.0F;
		float f5 = (float)(color2 >> 16 & 255) / 255.0F;
		float f6 = (float)(color2 >> 8 & 255) / 255.0F;
		float f7 = (float)(color2 & 255) / 255.0F;
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		RenderSystem.defaultBlendFunc();
		GL11.glShadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos((double)right, (double)top,    0.0f).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos((double)left,  (double)top,    0.0f).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos((double)left,  (double)bottom, 0.0f).color(f5, f6, f7, f4).endVertex();
		bufferbuilder.pos((double)right, (double)bottom, 0.0f).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GL11.glShadeModel(7424);
		RenderSystem.disableBlend();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		RenderSystem.enableTexture();
	}
	
	public static void vLine(int p_vLine_1_, int p_vLine_2_, int p_vLine_3_, int p_vLine_4_)
	{
		if (p_vLine_3_ < p_vLine_2_) {
	         int i = p_vLine_2_;
	         p_vLine_2_ = p_vLine_3_;
	         p_vLine_3_ = i;
	      }

	      fill(p_vLine_1_, p_vLine_2_ + 1, p_vLine_1_ + 1, p_vLine_3_, p_vLine_4_);
	}
	
	public static void drawString(FontRenderer font, String text, int x, int y, int color)
	{
		font.func_238405_a_(identity, text, (float)x, (float)y, color);
	}
	
	public static void drawCenteredString(FontRenderer font, String text, int x, int y, int color)
	{
		font.func_238405_a_(identity, text, (float)(x - font.getStringWidth(text) / 2), (float)y, color);
	}
	
	public static void renderToolTip(Screen screenIn, String text, int mouseX, int mouseY)
	{
		screenIn.func_238652_a_(identity, new StringTextComponent(text), mouseX, mouseY);
	}
	
	@SuppressWarnings("deprecation")
	public static void renderBackground()
	{
		MainWindow res = Minecraft.getInstance().getMainWindow();
		
		if (Minecraft.getInstance().world != null) {
			fillGradient(0, 0, res.getScaledWidth(), res.getScaledHeight(), -1072689136, -804253680);
		} else {
			Tessellator tessellator = Tessellator.getInstance();
		      BufferBuilder bufferbuilder = tessellator.getBuffer();
		      Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.field_230663_f_);
		      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		      float width = res.getScaledWidth();
		      float height = res.getScaledHeight();
		      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		      bufferbuilder.pos(0.0D, height, 0.0D).tex(0.0F, height / 32.0F + 0.0f).color(64, 64, 64, 255).endVertex();
		      bufferbuilder.pos(width, height, 0.0D).tex(width / 32.0F, height / 32.0F).color(64, 64, 64, 255).endVertex();
		      bufferbuilder.pos(width, 0.0D, 0.0D).tex(width / 32.0F, 0.0f).color(64, 64, 64, 255).endVertex();
		      bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		      tessellator.draw();
		}
	}
	
	public static void gradientRectToRight(int left, int top, int right, int bottom, int startColor, int endColor)
	{
		float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GL11.glShadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)right, (double)top, 0.0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)left, (double)bottom, 0.0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0f).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GL11.glShadeModel(7424);
        RenderSystem.disableBlend();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderSystem.enableTexture();
	}
	
	private static int floatToByte(float value)
	{
		if(value < 0) value = 0;
		else if(value > 1) value = 1;
		
		return (int) (value * 255);
	}
}
