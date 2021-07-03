package com.elmfer.parkour_recorder.gui.window;

import org.lwjgl.input.Keyboard;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.UIrender;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.widgets.Button;

import net.minecraft.client.resources.I18n;

/**Override alert box. Used for saving overridden recordings.**/
public class OverrideWindow extends Window
{

	private final ConfirmOverride override;
	private final SaveToNew saveNew;
	private final String message;
	private Button overrideButton = new Button(I18n.format("com.elmfer.override"));
	private Button saveButton = new Button(I18n.format("com.elmfer.save_as_new"));
	private Button cancelButton = new Button(I18n.format("com.elmfer.cancel"));
	
	public OverrideWindow(String titleIn, String message, ConfirmOverride overrideCallback, SaveToNew saveNewCallback)
	{
		super(titleIn);
		override = overrideCallback;
		saveNew = saveNewCallback;
		this.message = message;
		height = 40 + GuiStyle.Gui.margin();
		
		overrideButton.setAction((b) -> 
		{
			override.overrideRecording();
			setShouldClose(true);
		});
		saveButton.setAction((b) ->
		{
			saveNew.saveRecording();
		});
		cancelButton.setAction(b -> setShouldClose(true));
		
		addWidgets(overrideButton, saveButton, cancelButton);
	}
	
	@Override
	public void onKeyPressed(int keyCode)
	{
		if(onCurrentZlevel())
		{
			if(keyCode == Keyboard.KEY_ESCAPE)
			{
				setShouldClose(true);
			}
		}
	}

	@Override
	protected void doDrawScreen()
	{
		//Styling constant
		final int MARGIN = GuiStyle.Gui.margin();
		
		//Render box body
		viewport.pushMatrix(false);
		{
			//Position buttons
			overrideButton.width = saveButton.width = cancelButton.width = (int) ((viewport.getWidth() - MARGIN * 2) / 3);
			saveButton.x = overrideButton.width + MARGIN;
			cancelButton.x = (int) (viewport.getWidth() - cancelButton.width);
			overrideButton.y = saveButton.y = cancelButton.y = GuiStyle.Gui.buttonHeight() + MARGIN;
			
			//Show message
			String[] message = this.message.split("\n");
			int i = 0;
			for(String line : message) UIrender.drawString(Anchor.TOP_CENTER, line, viewport.getWidth() / 2, i++ * UIrender.getStringHeight(), 0xFFFFFFFF);
			
			//Render buttons
			overrideButton.draw();
			saveButton.draw();
			cancelButton.draw();
		}
		viewport.popMatrix();
	}
	
	/**Callback function that is called when user wants to override recording file.**/
	public static interface ConfirmOverride
	{
		public void overrideRecording();
	}
	
	/**Callback function that is called when user wants to save to new file.**/
	public static interface SaveToNew
	{
		public void saveRecording();
	}

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseClicked(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseReleased(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCharTyped(int charTyped)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseScroll(int scrollAmount)
	{
		// TODO Auto-generated method stub
		
	}
}
