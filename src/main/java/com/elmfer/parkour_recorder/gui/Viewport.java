package com.elmfer.parkour_recorder.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

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
		GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, guiMatrix);
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
		GL11.glPushMatrix();
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
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, getWidth(), getHeight(), 0, 1000.0D, 3000.0D);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
			GL11.glLoadMatrixf(getGuiMatrix());
		}
		else 
		{
			GL11.glLoadMatrixf(getGuiMatrix());
			GL11.glTranslatef(left, top, 0.0f);
			for(int i = parents.size() - 1; i >= 0; i--)
			{
				Viewport v = parents.get(i);
				if(v.prevViewport == null)
					GL11.glTranslatef(v.left, v.top, 0.0f);
				else
					break;
			}
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
		GL11.glPopMatrix();
	}
	
	private void setupOverlayRendering()
	{
		MainWindow mainwindow = Minecraft.getInstance().getMainWindow();
        RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double)mainwindow.getFramebufferWidth() / mainwindow.getGuiScaleFactor(), (double)mainwindow.getFramebufferHeight() / mainwindow.getGuiScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}
}
