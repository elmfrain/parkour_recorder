package com.elmfer.parkour_recorder.gui;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ButtonListViewport extends AbstractGui
{
	protected List<GuiButton> buttonList = new ArrayList<GuiButton>();
	private Screen parentScreen;
	private float scrollPos = 0.0f;
	private float scrollSpeed = 0.0f;
	private static double scrollAmount = 0.0;
	
	public ButtonListViewport(Screen parent)
	{
		parentScreen = parent;
	}
	
	@SubscribeEvent
	public static void onScrollCallback(GuiScreenEvent.MouseScrollEvent event)
	{
		scrollAmount = event.getScrollDelta();
	}
	
	public int getIndex(GuiButton button)
	{
		return buttonList.indexOf(button);
	}
	
	public void addButton(GuiButton button)
	{
		buttonList.add(button);
		@SuppressWarnings("unchecked")
		List<IGuiEventListener> pChildren = (List<IGuiEventListener>) parentScreen.children();
		pChildren.add(button);
	}
	
	public void clearButtons()
	{
		buttonList.forEach((GuiButton b) -> {parentScreen.children().remove(b);});
		buttonList.clear();
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks, GuiViewport viewport)
	{
		int scrollerWidth = 3;
		int smallMargin = GuiStyle.Gui.smallMargin();
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		int listHeight = (buttonHeight + smallMargin) * buttonList.size() + 80;
		int scrollMovement = Math.max(0, listHeight - viewport.getHeight());
		
		if(viewport.isHovered(mouseX, mouseY) && scrollPos <= 0) scrollSpeed += scrollAmount * 2.0;
		scrollSpeed *= 0.95f;
		scrollPos += scrollSpeed;
		if(scrollPos > 0) {scrollPos = 0.0f; scrollSpeed = 0.0f;}
		if(scrollPos < -scrollMovement) {scrollPos = -scrollMovement; scrollSpeed = 0.0f;}
		
		viewport.pushMatrix(true);
		{
			RenderSystem.pushMatrix();
			{
				RenderSystem.translatef(0, scrollPos, 0);
				for(int i = 0; i < buttonList.size(); i++)
				{
					buttonList.get(i).setWidth(viewport.getWidth() - scrollerWidth);;
					buttonList.get(i).setHeight(buttonHeight);
					buttonList.get(i).y = (buttonHeight + smallMargin) * i;;
					buttonList.get(i).renderButton(mouseX, mouseY, partialTicks);
				}
			}
			RenderSystem.popMatrix();
			
			int tabHeight = (int) (((float) viewport.getHeight() / listHeight) * viewport.getHeight());
			int tabTravel = (int) (((float) -scrollPos / scrollMovement) * (viewport.getHeight() - tabHeight));

			fill(viewport.getWidth() - scrollerWidth, 0, viewport.getWidth(), viewport.getHeight(), 
					GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.5f));

			fill(viewport.getWidth() - scrollerWidth, tabTravel, viewport.getWidth(), 
					tabHeight + tabTravel, 
					GraphicsHelper.getIntColor(0.4f, 0.4f, 0.4f, 0.3f));
		}
		viewport.popMatrix();
		
		scrollAmount = 0;
	}
}
