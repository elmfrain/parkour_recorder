package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.render.ModelManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

//Rendering implementation for UIs.
public class UIrender
{
	private static Minecraft mc = Minecraft.getMinecraft();

	private static void arrangePositions(float positions[])
	{
		if (positions[0] < positions[2])
		{
			float i = positions[0];
			positions[0] = positions[2];
			positions[2] = i;
		}

		if (positions[1] < positions[3])
		{
			float j = positions[1];
			positions[1] = positions[3];
			positions[3] = j;
		}
	}

	public static void drawRect(float left, float top, float right, float bottom, int color)
	{
		float positions[] = { left, top, right, bottom };
		arrangePositions(positions);

		Color c = new Color(color);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.color(c.r, c.g, c.b, c.a);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) positions[0], (double) positions[3], 0.0D).endVertex();
		bufferbuilder.pos((double) positions[2], (double) positions[3], 0.0D).endVertex();
		bufferbuilder.pos((double) positions[2], (double) positions[1], 0.0D).endVertex();
		bufferbuilder.pos((double) positions[0], (double) positions[1], 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor)
	{
		drawGradientRect(Direction.TO_BOTTOM, left, top, right, bottom, startColor, endColor);
	}

	public static void drawGradientRect(Direction direction, float left, float top, float right, float bottom,
			int startColor, int endColor)
	{
		Color c0 = new Color(startColor);
		Color c1 = new Color(endColor);

		float positions[] = { left, top, right, bottom };
		float verticies[] = { 0, 0, 0, 0, 0, 0, 0, 0 };
		arrangePositions(positions);
		direction.orient(left, top, right, bottom, verticies);

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos((double) verticies[0], (double) verticies[1], 0.0).color(c1.r, c1.g, c1.b, c1.a).endVertex();
		bufferbuilder.pos((double) verticies[2], (double) verticies[3], 0.0).color(c1.r, c1.g, c1.b, c1.a).endVertex();
		bufferbuilder.pos((double) verticies[4], (double) verticies[5], 0.0).color(c0.r, c0.g, c0.b, c0.a).endVertex();
		bufferbuilder.pos((double) verticies[6], (double) verticies[7], 0.0).color(c0.r, c0.g, c0.b, c0.a).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawHoveringText(String text, float x, float y)
	{
		drawHoveringText(Arrays.asList(text), x, y);
	}

	public static void drawHoveringText(List<String> lines, float x, float y)
	{
		if (!lines.isEmpty())
		{
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int i = 0;

			for (String s : lines)
			{
				int j = getStringWidth(s);

				if (j > i)
				{
					i = j;
				}
			}

			int l1 = (int) (x + 12);
			int i2 = (int) (y - 12);
			int k = 8;

			if (lines.size() > 1)
			{
				k += 2 + (lines.size() - 1) * 10;
			}

			if (l1 + i > getUIwidth())
			{
				l1 -= 28 + i;
			}

			if (i2 + k + 6 > getUIheight())
			{
				i2 = getUIheight() - k - 6;
			}

			// this.zLevel = 300.0F;
			// this.itemRender.zLevel = 300.0F;

			drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864, -267386864);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864, -267386864);
			drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864, -267386864);

			drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415, 1344798847);
			drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415, 1347420415);
			drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847, 1344798847);

			for (int k1 = 0; k1 < lines.size(); ++k1)
			{
				String s1 = lines.get(k1);
				drawString(s1, (float) l1, (float) i2, -1);

				if (k1 == 0)
				{
					i2 += 2;
				}

				i2 += 10;
			}

			// this.zLevel = 0.0F;
			// this.itemRender.zLevel = 0.0F;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	public static void drawVerticalLine(float x, float startY, float endY, int color)
	{
		if (endY < startY)
		{
			float i = startY;
			startY = endY;
			endY = i;
		}

		drawRect(x, startY + 1, x + 1, endY, color);
	}

	public static float getPartialTicks()
	{
		return mc.getRenderPartialTicks();
	}
	
	public static int getStringWidth(String text)
	{
		return mc.fontRenderer.getStringWidth(text);
	}

	public static int getStringHeight()
	{
		return mc.fontRenderer.FONT_HEIGHT;
	}

	public static int getStringHeight(String text)
	{
		return mc.fontRenderer.FONT_HEIGHT;
	}

	public static int getCharWidth(int character)
	{
		String strChar = new String(Character.toChars(character));
		return mc.fontRenderer.getStringWidth(strChar);
	}

	public static int getUIScaleFactor()
	{
		return (new ScaledResolution(Minecraft.getMinecraft())).getScaleFactor();
	}

	public static int getWindowWidth()
	{
		return Minecraft.getMinecraft().displayWidth;
	}

	public static int getWindowHeight()
	{
		return Minecraft.getMinecraft().displayHeight;
	}

	public static int getUIwidth()
	{
		return (new ScaledResolution(Minecraft.getMinecraft())).getScaledWidth();
	}

	public static int getUIheight()
	{
		return (new ScaledResolution(Minecraft.getMinecraft())).getScaledHeight();
	}

	public static void drawString(String text, float x, float y, int color)
	{
		drawString(Anchor.TOP_LEFT, text, x, y, color);
	}

	public static void drawString(Anchor anchor, String text, float x, float y, int color)
	{
		float newPositions[] = { 0, 0 };
		anchor.anchor(text, x, y, newPositions);

		mc.fontRenderer.drawString(text, (int) newPositions[0], (int) newPositions[1], color);
	}

	public static String getTextFormats(String src)
	{
		StringBuilder result = new StringBuilder();
		int[] unicodes = src.chars().toArray();
		
		for(int i = 0; i < unicodes.length; i++)
		{
			int unicode = unicodes[i];
			
			if(unicode == 167 && i + 1 < unicodes.length)
			{
				int formatKey = "0123456789abcdefklmnor".indexOf(String.valueOf((char) unicodes[i + 1]).toLowerCase());
				
				if(0 <= formatKey && formatKey < 22)
				{
					result.appendCodePoint(167);
					result.appendCodePoint(unicodes[i + 1]);
				}
				i++;
			}
		}
		
		return result.toString();
	}
	
	public static String splitStringToFit(String src, float maxWidth, String delimiter)
	{
		int[] unicodes = src.chars().toArray();
		
		float cursor = 0.0f;
		boolean boldStyle = false;
		
		int i = 0;
		int dlimLen = delimiter.length();
		int dlimCount = 0;
		int lstDlim = 0;
		for(i = 0; i < unicodes.length;)
		{
			int unicode = unicodes[i];
			
			StringBuilder dlimCheck = new StringBuilder();
			for(int j = i; j < Math.min(dlimLen + i, unicodes.length); j++) dlimCheck.appendCodePoint(unicodes[j]);
			
			if(0 < dlimLen && delimiter.equals(dlimCheck.toString()))
			{
				dlimCount++;
				lstDlim = i + dlimLen;
			}
			
			if(unicode == 167 && i + 1 < unicodes.length)
			{
				int formatKey = "0123456789abcdefklmnor".indexOf(String.valueOf((char) unicodes[i + 1]).toLowerCase());
				
				if(formatKey < 16) 
					boldStyle = false;
				else if(formatKey == 17) 
					boldStyle = true;
				i++;
			}
			else
			{
				cursor += getCharWidth(unicode) + (boldStyle ? 1 : 0);
				
				if(maxWidth < cursor)
				{
					int numRead = i;
					if(0 < dlimLen && 0 < dlimCount) numRead = lstDlim;
					
					StringBuilder result = new StringBuilder();
					for(int j = 0; j < numRead; j++)
						result.appendCodePoint(unicodes[j]);
					
					return result.toString();
				}
			}
			i++;
		}
		
		return src;
	}
	
	public static void drawIcon(String iconKey, float x, float y, float scale, int color)
	{
		Color c = new Color(color);

		GlStateManager.enableLighting();
		GlStateManager.enableColorMaterial();

		FloatBuffer vec4Color = BufferUtils.createFloatBuffer(4);
		vec4Color.put(c.r).put(c.g).put(c.b).put(c.a);
		vec4Color.rewind();

		GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, vec4Color);

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.color(c.r, c.g, c.b, c.a);
		GlStateManager.disableCull();

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x, y, 0.0f);
			GlStateManager.scale(scale, -scale, 1.0f);

			ModelManager.renderModel(iconKey);
		}
		GlStateManager.popMatrix();

		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
	}

	public static class Stencil
	{
		private static final int MAX_STACK_SIZE = 128;
		private static final List<StencilState> STATES_STACK = new ArrayList<>();

		static
		{
			STATES_STACK.add(new StencilState());
		}

		public static void enableTest()
		{
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			getLast().testEnabled = true;
		}

		public static void disableTest()
		{
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			getLast().testEnabled = false;
		}

		public static boolean isTestEnabled()
		{
			return getLast().testEnabled;
		}

		public static void enableWrite()
		{
			StencilState last = getLast();
			GL11.glStencilMask(last.mask);
			last.writeEnabled = true;
		}

		public static void disableWrite()
		{
			GL11.glStencilMask(0x00);
			getLast().writeEnabled = false;
		}

		public static void mask(int mask)
		{
			getLast().mask = mask;
		}

		public static int getMask()
		{
			return getLast().mask;
		}

		public static boolean isWritingEnabled()
		{
			return getLast().writeEnabled;
		}

		public static void clear()
		{
			GL11.glStencilMask(0xFF);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glStencilMask(getLast().mask);
		}

		public static void setFunction(int function, int reference)
		{
			StencilState last = getLast();
			GL11.glStencilFunc(function, reference, last.mask);
			last.function = function;
			last.referenceValue = reference;
		}

		public static int getFunction()
		{
			return getLast().function;
		}

		public static int getReferenceValue()
		{
			return getLast().referenceValue;
		}

		public static void setOperation(int fail, int zFail, int pass)
		{
			GL11.glStencilOp(fail, zFail, pass);

			StencilState last = getLast();
			last.stencilFailOperation = fail;
			last.zFailOperation = zFail;
			last.passOperation = pass;
		}

		public static int getFailOperation()
		{
			return getLast().stencilFailOperation;
		}

		public static int getZFailOperation()
		{
			return getLast().zFailOperation;
		}

		public static int getPassOperation()
		{
			return getLast().passOperation;
		}

		public static void pushStencilState()
		{
			if (STATES_STACK.size() == MAX_STACK_SIZE - 1)
				throw new RuntimeException("Exceeded max stencil stack size");

			STATES_STACK.add(new StencilState(getLast()));
			getLast().apply();
		}

		public static void popStencilState()
		{
			if (STATES_STACK.size() == 1)
				throw new RuntimeException("Popped stencil states too much");
			STATES_STACK.remove(getLast());

			getLast().apply();
		}

		public static void resetStencilState()
		{
			STATES_STACK.clear();
			STATES_STACK.add(new StencilState());

			getLast().apply();
		}

		private static StencilState getLast()
		{
			return STATES_STACK.get(STATES_STACK.size() - 1);
		}

		private static class StencilState
		{
			boolean testEnabled = false;
			boolean writeEnabled = false;
			int mask = 0xFF;

			int function = GL11.GL_ALWAYS;
			int referenceValue = 1;

			int stencilFailOperation = GL11.GL_KEEP;
			int zFailOperation = GL11.GL_KEEP;
			int passOperation = GL11.GL_KEEP;

			public StencilState()
			{
			}

			public StencilState(StencilState copy)
			{
				testEnabled = copy.testEnabled;
				writeEnabled = copy.writeEnabled;
				mask = copy.mask;
				function = copy.function;
				referenceValue = copy.referenceValue;
				stencilFailOperation = copy.stencilFailOperation;
				zFailOperation = copy.zFailOperation;
				passOperation = copy.passOperation;
			}

			void apply()
			{
				if (testEnabled)
					GL11.glEnable(GL11.GL_STENCIL_TEST);
				else
					GL11.glDisable(GL11.GL_STENCIL_TEST);

				if (writeEnabled)
					GL11.glStencilMask(mask);
				else
					GL11.glStencilMask(0x00);

				GL11.glStencilFunc(function, referenceValue, mask);
				GL11.glStencilOp(stencilFailOperation, zFailOperation, passOperation);
			}
		}
	}

	public static enum Direction
	{
		TO_LEFT, TO_RIGHT, TO_BOTTOM, TO_TOP;

		// Orient from the default TO_BOTTOM orientation
		private void orient(float left, float top, float right, float bottom, float verticies[])
		{
			switch (this)
			{
			case TO_BOTTOM:
				verticies[0] = left;
				verticies[1] = bottom;

				verticies[2] = right;
				verticies[3] = bottom;

				verticies[4] = right;
				verticies[5] = top;

				verticies[6] = left;
				verticies[7] = top;
				return;
			case TO_LEFT:
				verticies[0] = left;
				verticies[1] = top;

				verticies[2] = left;
				verticies[3] = bottom;

				verticies[4] = right;
				verticies[5] = bottom;

				verticies[6] = right;
				verticies[7] = top;
				return;
			case TO_RIGHT:
				verticies[0] = right;
				verticies[1] = bottom;

				verticies[2] = right;
				verticies[3] = top;

				verticies[4] = left;
				verticies[5] = top;

				verticies[6] = left;
				verticies[7] = bottom;
				return;
			case TO_TOP:
				verticies[0] = right;
				verticies[1] = top;

				verticies[2] = left;
				verticies[3] = top;

				verticies[4] = left;
				verticies[5] = bottom;

				verticies[6] = right;
				verticies[7] = bottom;
				return;
			}
		}
	}

	public static enum Anchor
	{
		TOP_LEFT, TOP_CENTER, TOP_RIGHT, MID_LEFT, CENTER, MID_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

		private void anchor(String text, float x, float y, float newPosition[])
		{
			int stringWidth = getStringWidth(text);
			int stringHeight = getStringHeight(text);

			switch (this)
			{
			case MID_LEFT:
			case CENTER:
			case MID_RIGHT:
				newPosition[1] = y - stringHeight / 2;
				break;
			case BOTTOM_LEFT:
			case BOTTOM_CENTER:
			case BOTTOM_RIGHT:
				newPosition[1] = y - stringHeight;
				break;
			default:
				newPosition[1] = y;
				break;
			}

			switch (this)
			{
			case TOP_CENTER:
			case CENTER:
			case BOTTOM_CENTER:
				newPosition[0] = x - stringWidth / 2;
				break;
			case TOP_RIGHT:
			case MID_RIGHT:
			case BOTTOM_RIGHT:
				newPosition[0] = x - stringWidth;
				break;
			default:
				newPosition[0] = x;
				break;
			}
		}
	}

	private static class Color
	{
		float r, g, b, a;

		public Color(int intColor)
		{
			a = (float) (intColor >> 24 & 255) / 255.0F;
			r = (float) (intColor >> 16 & 255) / 255.0F;
			g = (float) (intColor >> 8 & 255) / 255.0F;
			b = (float) (intColor & 255) / 255.0F;
		}
	}
}
