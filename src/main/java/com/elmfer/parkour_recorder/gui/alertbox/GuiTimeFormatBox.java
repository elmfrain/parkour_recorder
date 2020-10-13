package com.elmfer.parkour_recorder.gui.alertbox;

import com.elmfer.parkour_recorder.gui.ButtonListViewport;
import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.TimelineScreen;
import com.elmfer.parkour_recorder.gui.TimelineViewport.TimeStampFormat;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

public class GuiTimeFormatBox extends GuiAlertBox
{
private static final int BUTTON_LIST_HEIGHT = GuiStyle.Gui.buttonHeight() * 6 + GuiStyle.Gui.smallMargin() * 8;
	
	/**A scrollable list of buttons to select time format.**/
	protected ButtonListViewport selections;
	
	public GuiTimeFormatBox(TimelineScreen parent)
	{
		super(I18n.format("gui.timeline.change_time_format"), parent);
		selections = new ButtonListViewport(parentScreen);
	}

	@Override
	public void init()
	{
		super.init();
		
		selections.clearButtons();
		
		//Setup button list
		for(TimeStampFormat format : TimeStampFormat.values())
		{
			GuiButton button = new GuiButton(0, 0, format.NAME, this::selectionsPressed);
			button.highlighed = TimelineScreen.timeStampFormat == format;
			button.zLevel = 1;
			selections.addButton(button);
		}
		
		height = BUTTON_LIST_HEIGHT;
	}
	
	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		selections.drawScreen(mouseX, mouseY, partialTicks, viewport);
	}
	
	private void selectionsPressed(Button button)
	{
		TimelineScreen.timeStampFormat = TimeStampFormat.values()[selections.buttonList.indexOf(button)];
		selections.buttonList.forEach((GuiButton b) -> { b.highlighed = false; });
		
		((GuiButton) button).highlighed = true;
	}
}
