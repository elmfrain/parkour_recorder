package com.elmfer.parkourhelper.gui;

import static com.elmfer.parkourhelper.render.GraphicsHelper.getIntColor;

import java.io.IOException;

import com.elmfer.parkourhelper.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

abstract public class GuiAlertBox extends GuiScreen {
	
	public String title;
	private boolean shouldClose = false;
	protected GuiViewport viewport;
	protected int height = 40;
	
	public GuiAlertBox(String title)
	{
		this.title = title;
	}
	
	@Override
	public void initGui()
	{
		this.mc = Minecraft.getMinecraft();
		GuiButton closeButton = new GuiButton(-1, 0, 0, "") 
		{
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
			{
				if(visible)
				{
					preRender(mc, mouseX, mouseY, partialTicks);
					int color = hovered && enabled ? GraphicsHelper.getIntColor(0.8f, 0.0f, 0.0f, 0.9f) : GraphicsHelper.getIntColor(0.5f, 0.0f, 0.0f, 0.8f);
					drawRect(this.x, this.y, this.x + width, this.y + height, color);
					float scale = (height * 1.0f) / mc.fontRenderer.FONT_HEIGHT;
				}
			}
		};
		addButton(closeButton);
	}
	
	public void setShouldClose(boolean shouldClose)
	{
		this.shouldClose = shouldClose;
	}
	
	public boolean shouldClose()
	{
		return shouldClose;
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void actionPerformed(net.minecraft.client.gui.GuiButton button) 
	{
		if(button.id == -1)
			shouldClose = true;
	}
	
	abstract protected void doDrawScreen(int mouseX, int mouseY, float partialTicks);
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc);
		
		int boxSize = res.getScaledWidth() / 2;
		int  titleHeight = mc.fontRenderer.FONT_HEIGHT * 2;
		int margins = 20 / res.getScaleFactor();
		int closeMargin = (int) (4.0f / res.getScaleFactor());
		
		int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.9f);
		int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport box = new GuiViewport(all);
		box.left = all.getWidth() / 2 - boxSize / 2;
		box.top = (int) (all.getHeight() / 2.0f - (titleHeight + margins * 2.0f + height) / 2.0f);
		box.right = all.getWidth() / 2 + boxSize / 2;
		box.bottom = (int) (all.getHeight() / 2.0f + (titleHeight + margins * 2.0f + height) / 2.0f);
		GuiViewport title = new GuiViewport(box);
		title.bottom = mc.fontRenderer.FONT_HEIGHT * 2;
		viewport = new GuiViewport(box);
		viewport.left = margins; viewport.top = title.bottom + margins;
		viewport.right -= margins;
		viewport.bottom -= margins;
		
		box.pushMatrix(false);
		{
			drawRect(-closeMargin, -closeMargin, box.getWidth() + closeMargin, box.getHeight() + closeMargin, getIntColor(0.0f, 0.0f, 0.0f, 1.0f));
			drawRect(0, 0, box.getWidth(), box.getHeight(), getIntColor(0.15f, 0.15f, 0.15f, 1.0f));
		}
		box.popMatrix();
		
		title.pushMatrix(false);
		{
			GraphicsHelper.gradientRectToRight(0, 0, title.getWidth(), title.getHeight(), fade1, fade2);
			mc.fontRenderer.drawString(this.title, margins, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			
			GuiButton closeButton = (GuiButton) buttonList.get(0);
			closeButton.height = title.getHeight() - closeMargin * 2;
			closeButton.width = closeButton.height;
			closeButton.y = closeMargin;
			closeButton.x = title.getWidth() - closeMargin - closeButton.width;
			closeButton.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		title.popMatrix();
		
		doDrawScreen(mouseX, mouseY, partialTicks);
	}
}
