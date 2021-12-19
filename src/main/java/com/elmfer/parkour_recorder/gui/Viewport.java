package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

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
	
	public Viewport()
	{
		right = UIrender.getUIwidth();
		bottom = UIrender.getUIheight();
		guiMatrix = BufferUtils.createFloatBuffer(16);
		RenderSystem.getModelViewMatrix().store(guiMatrix);
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
		RenderSystem.getModelViewStack().pushPose();
		if(setViewport)
		{
			prevViewport = BufferUtils.createIntBuffer(16);
			GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
			
			int left = (int) getAbsoluteLeft();
			int top = (int) getAbsoluteTop();
			int bottom = (int) (top + getHeight());
			double factor = UIrender.getUIScaleFactor();
			int x = (int) (left * factor);
			int	y = (int) (UIrender.getWindowHeight() - bottom * factor);
			int	width = (int) (getWidth() * factor);
			int	height = (int) (getHeight() * factor);
			GL11.glViewport(x, y, width, height);
			RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0f, getWidth(), 0.0f, getHeight(), 1000.0f, 3000.0f));
		
			RenderSystem.getModelViewStack().last().pose().load(getGuiMatrix());
			RenderSystem.applyModelViewMatrix();
		}
		else 
		{
			RenderSystem.getModelViewStack().last().pose().load(getGuiMatrix());
			RenderSystem.getModelViewStack().translate(left, top, 0.0f);
			
			for(int i = parents.size() - 1; i >= 0; i--)
			{
				Viewport v = parents.get(i);
				if(v.prevViewport == null)
					RenderSystem.getModelViewStack().translate(v.left, v.top, 0.0f);
				else
					break;
			}
			
			RenderSystem.applyModelViewMatrix();
		}
	}
	
	public void popMatrix()
	{
		if(prevViewport != null)
		{
			GL11.glViewport(prevViewport.get(0), prevViewport.get(1), prevViewport.get(2), prevViewport.get(3));
			setupOverlayRendering();
			prevViewport = null;
		}
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
	}
	
	private void setupOverlayRendering()
	{
        RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0f, UIrender.getUIwidth(), 0.0f, UIrender.getUIheight(), 1000.0f, 3000.0f));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();
	}
}
