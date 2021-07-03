package com.elmfer.parkour_recorder.gui.window;

import org.lwjgl.input.Keyboard;

import com.elmfer.parkour_recorder.gui.ButtonListView;
import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.TimelineView;
import com.elmfer.parkour_recorder.gui.NumberLineView.TimeStampFormat;
import com.elmfer.parkour_recorder.gui.widgets.Button;

import net.minecraft.client.resources.I18n;

public class TimeFormatSelectionWindow extends Window
{
	private static final int BUTTON_LIST_HEIGHT = GuiStyle.Gui.buttonHeight() * 6 + GuiStyle.Gui.smallMargin() * 8;
	
	/**A scrollable list of buttons to select time format.**/
	protected ButtonListView selections = new ButtonListView();
	
	public TimeFormatSelectionWindow()
	{
		super(I18n.format("com.elmfer.change_time_format"));
		
		//Setup button list
		for(TimeStampFormat format : TimeStampFormat.values())
		{
			Button button = new Button(format.NAME);
			button.setHighlighted(TimelineView.timeStampFormat == format);
			button.setAction((b) -> selectionsPressed(b));
			selections.addWidgets(button);
		}
		
		height = BUTTON_LIST_HEIGHT;
		addWidgets(selections);
	}
	
	@Override
	protected void doDrawScreen()
	{
		selections.setViewport(viewport);
		selections.draw();
	}
	
	private void selectionsPressed(Button button)
	{
		TimelineView.timeStampFormat = TimeStampFormat.values()[selections.getChildrenWidgets().indexOf(button)];
		selections.getChildrenWidgets().forEach(b -> 
		{
			if(b instanceof Button) ((Button) b).setHighlighted(false);
		});
		
		button.setHighlighted(true);
	}

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
	}

	@Override
	public void onMouseClicked(int button)
	{
	}

	@Override
	public void onMouseReleased(int button)
	{
	}

	@Override
	public void onKeyPressed(int keyCode)
	{
		if(onCurrentZlevel())
		{
			if(keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE)
			{
				setShouldClose(true);
			}
		}
	}

	@Override
	public void onCharTyped(int charTyped)
	{
	}

	@Override
	public void onMouseScroll(int scrollAmount)
	{
	}
}
