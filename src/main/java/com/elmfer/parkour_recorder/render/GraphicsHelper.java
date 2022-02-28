package com.elmfer.parkour_recorder.render;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

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
	
	public static float lerp(float partials, float prevValue, float value)
	{
		return prevValue + (value - prevValue) * partials;
	}
	
	public static double lerp(float partials, double prevValue, double value)
	{
		return prevValue + (value - prevValue) * partials;
	}
	
	// 2D Angle Interpolation (shortest distance) by shaunlebron
	// https://gist.github.com/shaunlebron/8832585
	private static double shortAngleDist(double prevValue, double value)
	{
		double max = 360.0;
		double da = (value - prevValue) % max;
		return 2 * da % max - da;
	}
	
	public static float lerpAngle(float partials, float prevValue, float value)
	{
		return prevValue + (float) shortAngleDist(prevValue, value) * partials;
	}
	
	public static double lerpAngle(float partials, double prevValue, double value)
	{
		return prevValue + shortAngleDist(prevValue, value) * partials;
	}
	
	public static Vector4f getFloatColor(int color)
	{
		float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        
        return new Vector4f(f1, f2, f3, f);
	}
	
	private static int floatToByte(float value)
	{
		if(value < 0) value = 0;
		else if(value > 1) value = 1;
		
		return (int) (value * 255);
	}
}
