package com.elmfer.parkourhelper.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import com.elmfer.parkourhelper.gui.GuiButton;
import com.elmfer.parkourhelper.render.GraphicsHelper;

import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonList extends Gui {
	
	public List<GuiButton> buttonList = new ArrayList<GuiButton>();
	private float scrollPos = 0.0f;
	private float scrollSpeed = 0.0f;
	private GuiButton selectedButton = null;
	
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
		Minecraft mc = Minecraft.getMinecraft();
        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttonList.size(); ++i)
            {
                GuiButton guibutton = this.buttonList.get(i);

                if (guibutton.mousePressed(mc, mouseX, mouseY))
                {
                    guibutton.playPressSound(mc.getSoundHandler());
                    selectedButton = guibutton;
                }
            }
        }
    }
	
	public GuiButton getButton()
	{
		GuiButton button = selectedButton;
		selectedButton = null;
		return button;
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks, GuiViewport viewport)
	{
		Minecraft mc = Minecraft.getMinecraft();
		int scrollerWidth = 3;
		int buttonMargin = 5;
		int buttonHeight = 20;
		int listHeight = (buttonHeight + buttonMargin) * buttonList.size() + 80;
		int scrollMovement = Math.max(0, listHeight - viewport.getHeight());
		
		if(viewport.isHovered(mouseX, mouseY) && scrollPos <= 0) scrollSpeed += Mouse.getDWheel() / 20.0f;
		Mouse.getDWheel();
		scrollSpeed *= 0.93f;
		scrollPos += scrollSpeed;
		if(scrollPos > 0) {scrollPos = 0.0f; scrollSpeed = 0.0f;}
		if(scrollPos < -scrollMovement) {scrollPos = -scrollMovement; scrollSpeed = 0.0f;}
		
		viewport.pushMatrix(true);
		{
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(0, scrollPos, 0);
				for(int i = 0; i < buttonList.size(); i++)
				{
					buttonList.get(i).width = viewport.getWidth() - scrollerWidth;
					buttonList.get(i).height = buttonHeight;
					buttonList.get(i).y = (buttonHeight + buttonMargin) * i;
					buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTicks);
				}
			}
			GlStateManager.popMatrix();
			
			int tabHeight = (int) (((float) viewport.getHeight() / listHeight) * viewport.getHeight());
			int tabTravel = (int) (((float) -scrollPos / scrollMovement) * (viewport.getHeight() - tabHeight));
			drawRect(viewport.getWidth() - scrollerWidth, 0, viewport.getWidth(), viewport.getHeight(), 
					GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.5f));
			drawRect(viewport.getWidth() - scrollerWidth, tabTravel, viewport.getWidth(), 
					tabHeight + tabTravel, 
					GraphicsHelper.getIntColor(0.4f, 0.4f, 0.4f, 0.3f));
		}
		viewport.popMatrix();
	}
}
