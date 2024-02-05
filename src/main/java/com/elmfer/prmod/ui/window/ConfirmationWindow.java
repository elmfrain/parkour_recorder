package com.elmfer.prmod.ui.window;

import org.lwjgl.glfw.GLFW;

import com.elmfer.prmod.ui.GuiStyle;
import com.elmfer.prmod.ui.widgets.Button;

import net.minecraft.client.resource.language.I18n;

public class ConfirmationWindow extends Window {
	
	private Callback callback;
	private Button yesButton = new Button(I18n.translate("com.prmod.yes"));
	private Button cancelButton = new Button(I18n.translate("com.prmod.cancel"));
	
	public ConfirmationWindow(String title, Callback func)
	{
		super(title);
		callback = func;
		
		cancelButton.setAction((b) -> {setShouldClose(true);});
		yesButton.setAction((b) -> 
		{
			callback.callBack();
			setShouldClose(true);
		});
		
		addWidgets(cancelButton, yesButton);
	}
	
	public static interface Callback
	{
		public void callBack();
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
			if(keyCode == GLFW.GLFW_KEY_ENTER)
			{
				callback.callBack();
				setShouldClose(true);
			}
			else if(keyCode == GLFW.GLFW_KEY_ESCAPE)
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

	@Override
	protected void doDrawScreen()
	{
		viewport.pushMatrix(false);
		{
			int margin = GuiStyle.Gui.margin();
			
			//Position the buttons
			yesButton.width = (int) (viewport.getWidth() / 2 - margin);
			cancelButton.width = yesButton.width;
			yesButton.y = (int) (viewport.getHeight() / 2 - yesButton.height / 2);
			cancelButton.y = yesButton.y;
			cancelButton.x = (int) (viewport.getWidth() - cancelButton.width);
			
			//Render buttons
			yesButton.draw();
			cancelButton.draw();
		}
		viewport.popMatrix();
	}
}
