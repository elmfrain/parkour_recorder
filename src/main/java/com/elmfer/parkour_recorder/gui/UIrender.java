package com.elmfer.parkour_recorder.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.render.ModelManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

//Rendering implementation for UIs.
public class UIrender
{
	public static final PoseStack identity = new PoseStack();
	private static Minecraft mc = Minecraft.getInstance();

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

		GuiComponent.fill(identity, (int) left, (int) top, (int) right, (int) bottom, color);
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

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex((double) verticies[0], (double) verticies[1], 0.0).color(c1.r, c1.g, c1.b, c1.a)
				.endVertex();
		bufferbuilder.vertex((double) verticies[2], (double) verticies[3], 0.0).color(c1.r, c1.g, c1.b, c1.a)
				.endVertex();
		bufferbuilder.vertex((double) verticies[4], (double) verticies[5], 0.0).color(c0.r, c0.g, c0.b, c0.a)
				.endVertex();
		bufferbuilder.vertex((double) verticies[6], (double) verticies[7], 0.0).color(c0.r, c0.g, c0.b, c0.a)
				.endVertex();
		tesselator.end();
		//RenderSystem.set
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	public static void drawHoveringText(String text, float x, float y)
	{
		drawHoveringText(Arrays.asList(text), x, y);
	}

	public static void drawHoveringText(List<String> lines, float x, float y)
	{
		// Not yet implemented
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
		return mc.getFrameTime();
	}

	public static int getStringWidth(String text)
	{
		return mc.font.width(text);
	}

	public static int getStringHeight()
	{
		return mc.font.lineHeight;
	}

	public static int getStringHeight(String text)
	{
		return mc.font.lineHeight;
	}

	public static int getCharWidth(int character)
	{
		String strChar = "" + (char) character;
		return mc.font.width(strChar);
	}

	public static int getUIScaleFactor()
	{
		return (int) mc.getWindow().getGuiScale();
	}

	public static int getWindowWidth()
	{
		return mc.getWindow().getWidth();
	}

	public static int getWindowHeight()
	{
		return mc.getWindow().getHeight();
	}

	public static int getUIwidth()
	{
		return mc.getWindow().getGuiScaledWidth();
	}

	public static int getUIheight()
	{
		return mc.getWindow().getGuiScaledHeight();
	}

	public static void drawString(String text, float x, float y, int color)
	{
		drawString(Anchor.TOP_LEFT, text, x, y, color);
	}

	public static void drawString(Anchor anchor, String text, float x, float y, int color)
	{
		float newPositions[] = { 0, 0 };
		anchor.anchor(text, x, y, newPositions);

		mc.font.draw(identity, text, newPositions[0], newPositions[1], color);
	}

	public static void drawIcon(String iconKey, float x, float y, float scale, int color)
	{
		Color c = new Color(color);
		
		RenderSystem.enableBlend();
		RenderSystem.disableCull();

		RenderSystem.getModelViewStack().pushPose();
		{
			RenderSystem.getModelViewStack().translate(x, y, 0.0);
			RenderSystem.getModelViewStack().scale(scale, -scale, 1.0f);
			RenderSystem.applyModelViewMatrix();

			ShaderInstance posColShader = GameRenderer.getPositionColorShader();
			posColShader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
			posColShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			posColShader.COLOR_MODULATOR.set(c.r, c.g, c.b, c.a);
			posColShader.apply();
			ModelManager.renderModel(iconKey);
			posColShader.clear();
		}
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
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
