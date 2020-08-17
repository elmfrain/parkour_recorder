package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class GuiViewport extends Gui
{
	public int left = 0;
	public int right = 0;
	public int top = 0;
	public int bottom = 0;
	private List<GuiViewport> children = new ArrayList<GuiViewport>();
	private List<GuiViewport> parents = new ArrayList<GuiViewport>();
	FloatBuffer guiMatrix = null;
	IntBuffer prevViewport = null;
	
	public GuiViewport(ScaledResolution window)
	{
		right = window.getScaledWidth();
		bottom = window.getScaledHeight();
		guiMatrix = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, guiMatrix);
	}
	
	public GuiViewport(GuiViewport parent)
	{
		parent.children.add(this);
		this.parents.addAll(parent.parents);
		this.parents.add(parent);
		right = parent.getWidth();
		bottom = parent.getHeight();
	}
	
	public boolean isHovered(int mouseX, int mouseY)
	{
		int left = getAbsoluteLeft();
		int top = getAbsoluteTop();
		int right = left + getWidth();
		int bottom = top + getHeight();
		return mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;
	}
	
	public int getWidth()
	{
		return right - left;
	}
	
	public int getHeight()
	{
		return bottom - top;
	}
	
	public int getAbsoluteLeft()
	{
		if(parents.isEmpty())
			return left;
		else
		{
			int totalLeft = left;
			for(GuiViewport parent : parents)
				totalLeft += parent.left;
			return totalLeft;
		}
	}
	
	public int getAbsoluteTop()
	{
		if(parents.isEmpty())
			return top;
		else
		{
			int totalTop = top;
			for(GuiViewport parent : parents)
				totalTop += parent.top;
			return totalTop;
		}
	}
	
	protected GuiViewport getParent()
	{
		if(!parents.isEmpty()) return parents.get(parents.size() - 1);
		else return null;
	}
	
	private FloatBuffer getGuiMatrix()
	{
		if(guiMatrix != null) return guiMatrix;
		else return parents.get(0).guiMatrix;
	}
	
	public void pushMatrix(boolean setViewport)
	{
		if(setViewport)
		{
			prevViewport = BufferUtils.createIntBuffer(16);
			GL11.glGetInteger(GL11.GL_VIEWPORT, prevViewport);
			
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution res = new ScaledResolution(mc);
			
			int left = getAbsoluteLeft();
			int top = getAbsoluteTop();
			int bottom = top + getHeight();
			double factor = res.getScaleFactor();
			int x = (int) (left * factor);
			int	y = (int) (mc.displayHeight - bottom * factor);
			int	width = (int) (getWidth() * factor);
			int	height = (int) (getHeight() * factor);
			GlStateManager.viewport(x, y, width, height);
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0, getWidth(), getHeight(), 0, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			
			GL11.glLoadMatrix(getGuiMatrix());
		}
		else 
		{
			GL11.glLoadMatrix(getGuiMatrix());
			GlStateManager.translate(left, top, 0.0f);
			for(int i = parents.size() - 1; i >= 0; i--)
			{
				GuiViewport v = parents.get(i);
				if(v.prevViewport == null)
					GlStateManager.translate(v.left, v.top, 0.0f);
				else
					break;
			}
		}
	}
	
	public void popMatrix()
	{
		if(prevViewport != null)
		{
			GlStateManager.viewport(prevViewport.get(0), prevViewport.get(1), prevViewport.get(2), prevViewport.get(3));
			setupOverlayRendering();
			prevViewport = null;
		}
	}
	
	private void setupOverlayRendering()
	{
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
	}
}
