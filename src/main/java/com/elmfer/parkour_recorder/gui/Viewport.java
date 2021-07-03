package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class Viewport
{
	public float left = 0;
	public float right = 0;
	public float top = 0;
	public float bottom = 0;
	private List<Viewport> children = new ArrayList<Viewport>();
	private List<Viewport> parents = new ArrayList<Viewport>();
	FloatBuffer guiMatrix = null;
	IntBuffer prevViewport = null;
	
	@Deprecated
	public Viewport(ScaledResolution window)
	{
		right = window.getScaledWidth();
		bottom = window.getScaledHeight();
		guiMatrix = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, guiMatrix);
	}
	
	public Viewport()
	{
		right = UIrender.getUIwidth();
		bottom = UIrender.getUIheight();
		guiMatrix = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, guiMatrix);
	}
	
	public Viewport(Viewport parent)
	{
		parent.children.add(this);
		this.parents.addAll(parent.parents);
		this.parents.add(parent);
		right = parent.getWidth();
		bottom = parent.getHeight();
	}
	
	public boolean isHovered(float mouseX, float mouseY)
	{
		float left = getAbsoluteLeft();
		float top = getAbsoluteTop();
		float right = left + getWidth();
		float bottom = top + getHeight();
		return mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;
	}
	
	public float getWidth()
	{
		return right - left;
	}
	
	public float getHeight()
	{
		return bottom - top;
	}
	
	public float getAbsoluteLeft()
	{
		if(parents.isEmpty())
			return left;
		else
		{
			float totalLeft = left;
			for(Viewport parent : parents)
				totalLeft += parent.left;
			return totalLeft;
		}
	}
	
	public float getAbsoluteTop()
	{
		if(parents.isEmpty())
			return top;
		else
		{
			float totalTop = top;
			for(Viewport parent : parents)
				totalTop += parent.top;
			return totalTop;
		}
	}
	
	protected Viewport getParent()
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
		GlStateManager.pushMatrix();
		if(setViewport)
		{
			prevViewport = BufferUtils.createIntBuffer(16);
			GL11.glGetInteger(GL11.GL_VIEWPORT, prevViewport);
			
			int left = (int) getAbsoluteLeft();
			int top = (int) getAbsoluteTop();
			int bottom = (int) (top + getHeight());
			double factor = UIrender.getUIScaleFactor();
			int x = (int) (left * factor);
			int	y = (int) (UIrender.getWindowHeight() - bottom * factor);
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
				Viewport v = parents.get(i);
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
		GlStateManager.popMatrix();
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
