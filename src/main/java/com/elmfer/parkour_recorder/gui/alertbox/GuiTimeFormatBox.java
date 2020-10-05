package com.elmfer.parkour_recorder.gui.alertbox;

import java.io.IOException;

import com.elmfer.parkour_recorder.gui.ButtonListViewport;
import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.TimelineScreen;
import com.elmfer.parkour_recorder.gui.TimelineViewport.TimeStampFormat;
import com.elmfer.parkour_recorder.gui.widgets.GuiButton;

import net.minecraft.client.resources.I18n;

public class GuiTimeFormatBox extends GuiAlertBox
{
	private static final int BUTTON_LIST_HEIGHT = GuiStyle.Gui.buttonHeight() * 6 + GuiStyle.Gui.smallMargin() * 8;
	
	/**A scrollable list of buttons to select time format.**/
	protected ButtonListViewport selections = new ButtonListViewport(this::selectionsPressed);
	
	public GuiTimeFormatBox(TimelineScreen parent)
	{
		super(I18n.format("gui.timeline.change_time_format"), parent);
		
		//Setup button list
		int i = 0;
		for(TimeStampFormat format : TimeStampFormat.values())
		{
			GuiButton button = new GuiButton(i++, 0, 0, format.NAME);
			button.highlighed = TimelineScreen.timeStampFormat == format;
			button.zLevel = 1;
			selections.buttonList.add(button);
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		
		height = BUTTON_LIST_HEIGHT;
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException 
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		selections.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		selections.drawScreen(mouseX, mouseY, partialTicks, viewport);
	}
	
	private void selectionsPressed(com.elmfer.parkour_recorder.gui.widgets.GuiButton button)
	{
		TimelineScreen.timeStampFormat = TimeStampFormat.values()[button.id];
		selections.buttonList.forEach((GuiButton b) -> { b.highlighed = false; });
		
		button.highlighed = true;
	}
}
