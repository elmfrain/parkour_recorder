package com.elmfer.parkour_recorder.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class GuiViewport extends AbstractGui
{
	public int left = 0;
	public int right = 0;
	public int top = 0;
	public int bottom = 0;
	private List<GuiViewport> children = new ArrayList<GuiViewport>();
	private List<GuiViewport> parents = new ArrayList<GuiViewport>();
	float[] guiMatrix = null;
	int[] prevViewport = null;
	
	public GuiViewport(MainWindow window)
	{
		right = window.getScaledWidth();
		bottom = window.getScaledHeight();
		guiMatrix = new float[16];
		GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, guiMatrix);
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
	
	private float[] getGuiMatrix()
	{
		if(guiMatrix != null) return guiMatrix;
		else return parents.get(0).guiMatrix;
	}
	
	public void pushMatrix(boolean setViewport)
	{
		if(setViewport)
		{
			prevViewport = new int[16];
			GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
			
			Minecraft mc = Minecraft.getInstance();
			MainWindow res = mc.getMainWindow();
			
			int left = getAbsoluteLeft();
			int top = getAbsoluteTop();
			int bottom = top + getHeight();
			double factor = res.getGuiScaleFactor();
			int x = (int) (left * factor);
			int	y = (int) (res.getHeight() - bottom * factor);
			int	width = (int) (getWidth() * factor);
			int	height = (int) (getHeight() * factor);
			GlStateManager.viewport(x, y, width, height);
			RenderSystem.matrixMode(GL11.GL_PROJECTION);
			RenderSystem.loadIdentity();
			RenderSystem.ortho(0, getWidth(), getHeight(), 0, 1000.0D, 3000.0D);
			RenderSystem.matrixMode(GL11.GL_MODELVIEW);
			
			GL11.glLoadMatrixf(getGuiMatrix());
		}
		else 
		{
			prevViewport = null;
			GL11.glLoadMatrixf(getGuiMatrix());
			RenderSystem.translatef(left, top, 0.0f);
			parents.forEach((GuiViewport v) -> {RenderSystem.translatef(v.left, v.top, 0.0f);});
		}
	}
	
	public void popMatrix()
	{
		if(prevViewport != null)
		{
			GlStateManager.viewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
			setupOverlayRendering();
			//GL11.glLoadMatrix(getGuiMatrix());
		}
	}
	
	private void setupOverlayRendering()
	{
		MainWindow mainwindow = Minecraft.getInstance().getMainWindow();
        RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, (double)mainwindow.getFramebufferWidth() / mainwindow.getGuiScaleFactor(), (double)mainwindow.getFramebufferHeight() / mainwindow.getGuiScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
	}
}
