package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;
import static com.elmfer.parkour_recorder.render.GraphicsHelper.gradientRectToRight;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

abstract public class GuiAlertBox extends GuiScreen {

	String title;
	private boolean shouldClose = false;
	protected GuiViewport viewport;
	protected GuiScreen parentScreen;
	protected int height = 40;
	
	public GuiAlertBox(String titleIn, GuiScreen parent)
	{
		title = titleIn;
		parentScreen = parent;
		mc = Minecraft.getMinecraft();
	}
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		GuiButton.currentZLevel = 1;
		GuiButton closeButton = new GuiButton(-1, 0, 0, "") 
		{
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
			{
				if(visible)
				{
					preRender(mouseX, mouseY, partialTicks);
					int color = hovered && enabled ? getIntColor(0.8f, 0.0f, 0.0f, 0.9f) : getIntColor(0.5f, 0.0f, 0.0f, 0.8f);
					
					drawRect(x, y, x + width, y + height, color);
				}
			}
		};
		addButton(closeButton);
	}
	
	public void setShouldClose(boolean shouldClose)
	{
		this.shouldClose = shouldClose;
		if(shouldClose)
		{
			GuiButton.currentZLevel = 0;
		}
	}
	
	public boolean shouldClose()
	{
		return shouldClose;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		// TODO Auto-generated method stub
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void keyTyped(char charTyped, int keyCode)
	{
		try {
			super.keyTyped(charTyped, keyCode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	abstract protected void doDrawScreen(int mouseX, int mouseY, float partialTicks);
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		ScaledResolution res = new ScaledResolution(mc);
		
		int boxWidth = GuiStyle.AlertBox.boxWidth();
		int  titleHeight = GuiStyle.AlertBox.titleHeight();
		int margin = (int) GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		
		int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.9f);
		int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport box = new GuiViewport(all);
		box.left = all.getWidth() / 2 - boxWidth / 2;
		box.top = (int) (all.getHeight() / 2.0f - (titleHeight + margin * 2.0f + height) / 2.0f);
		box.right = all.getWidth() / 2 + boxWidth / 2;
		box.bottom = (int) (all.getHeight() / 2.0f + (titleHeight + margin * 2.0f + height) / 2.0f);
		GuiViewport title = new GuiViewport(box);
		title.bottom = mc.fontRenderer.FONT_HEIGHT * 2;
		viewport = new GuiViewport(box);
		viewport.left = margin; viewport.top = title.bottom + margin;
		viewport.right -= margin;
		viewport.bottom -= margin;
		
		box.pushMatrix(false);
		{
			drawRect(-smallMargin, -smallMargin, box.getWidth() + smallMargin, box.getHeight() + smallMargin, getIntColor(0.0f, 0.0f, 0.0f, 1.0f));
			
			drawRect(0, 0, box.getWidth(), box.getHeight(), getIntColor(0.15f, 0.15f, 0.15f, 1.0f));
		}
		box.popMatrix();
		
		title.pushMatrix(false);
		{
			gradientRectToRight(0, 0, title.getWidth(), title.getHeight(), fade1, fade2);
			
			GuiButton closeButton = (GuiButton) buttonList.get(0);
			
			closeButton.height = title.getHeight() - smallMargin * 2;
			closeButton.width = closeButton.height;
			closeButton.y = smallMargin;
			closeButton.x = title.getWidth() - smallMargin - closeButton.width;
			closeButton.drawButton(mc, mouseX, mouseY, partialTicks);
			
			GuiViewport message = new GuiViewport(title);
			message.right -= closeButton.width + smallMargin * 2;
			
			message.pushMatrix(true);
				mc.fontRenderer.drawString(this.title, margin, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			message.popMatrix();
		}
		title.popMatrix();
		
		doDrawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		if(button.id == -1)
			setShouldClose(true);
	}
}
