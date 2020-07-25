package com.elmfer.parkour_recorder.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GraphicsHelper {
	
	
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
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)right, (double)top, 0.0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)left, (double)bottom, 0.0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0f).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
	}
	
	private static int floatToByte(float value)
	{
		if(value < 0) value = 0;
		else if(value > 1) value = 1;
		
		return (int) (value * 255);
	}
}
