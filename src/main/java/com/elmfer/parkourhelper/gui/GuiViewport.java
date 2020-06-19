package com.elmfer.parkourhelper.gui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor;

public class GuiViewport extends Gui {
	
	public int left = 0;
	public int right = 0;
	public int top = 0;
	public int bottom = 0;
	private List<GuiViewport> children = new ArrayList<GuiViewport>();
	private List<GuiViewport> parents = new ArrayList<GuiViewport>();
	FloatBuffer guiMatrix = null;
	IntBuffer prevViewport = null;
	
	public GuiViewport(ScaledResolution mc)
	{
		right = mc.getScaledWidth();
		bottom = mc.getScaledHeight();
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
			int right = left + getWidth();
			int bottom = top + getHeight();
			int factor = res.getScaleFactor();
			GlStateManager.viewport(left * factor, mc.displayHeight - bottom * factor, getWidth() * factor, getHeight() * factor);
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0, getWidth(), getHeight(), 0, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			
			GL11.glLoadMatrix(getGuiMatrix());
		}
		else 
		{
			prevViewport = null;
			GL11.glLoadMatrix(getGuiMatrix());
			GlStateManager.translate(left, top, 0.0f);
			parents.forEach((GuiViewport v) -> {GlStateManager.translate(v.left, v.top, 0.0f);});
		}
	}
	
	public void popMatrix()
	{
		if(prevViewport != null)
		{
			GlStateManager.viewport(prevViewport.get(), prevViewport.get(), prevViewport.get(), prevViewport.get());
			Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
			//GL11.glLoadMatrix(getGuiMatrix());
		}
	}
}
