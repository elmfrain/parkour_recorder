package com.elmfer.parkour_recorder.gui.window;

import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.TextField;

import net.minecraft.client.resources.I18n;

public class NamingWindow extends Window
{
	private final TextField textField = new TextField();
	private Button nameButton = new Button(I18n.format("com.elmfer.name"));
	private Button cancelButton = new Button(I18n.format("com.elmfer.cancel"));
	private Predicate<String> textValidator;
	private NamedCallback callback;
	
	public NamingWindow(String titleIn, Predicate<String> textPredicate, NamedCallback callback)
	{
		super(titleIn);
		textValidator = textPredicate;
		this.callback = callback;
		
		textField.setTitle(I18n.format("com.elmfer.type_here"));
		
		cancelButton.setAction((b) -> {setShouldClose(true);});
		nameButton.setAction((b) -> 
		{
			onNamed();
		});
		
		height += GuiStyle.Gui.margin();
		addWidgets(nameButton, cancelButton, textField);
	}

	public TextField getTextField()
	{
		return textField;
	}

	@Override
	protected void doDrawScreen()
	{
		viewport.pushMatrix(false);
		{
			int margin = GuiStyle.Gui.margin();
			textField.width = (int) viewport.getWidth();
			textField.draw();
			
			nameButton.width = viewport.getWidth() / 2 - margin;
			cancelButton.width =  nameButton.width;
			nameButton.y = textField.height + margin;
			cancelButton.y = nameButton.y;
			cancelButton.x = (int) (viewport.getWidth() - cancelButton.width);
			
			nameButton.draw();
			cancelButton.draw();
		}
		viewport.popMatrix();
	}
	
	@Override
	public void onKeyPressed(int keyCode)
	{
		if(onCurrentZlevel())
		{
			if(keyCode == Keyboard.KEY_RETURN)
			{
				onNamed();
			}
			else if(keyCode == Keyboard.KEY_ESCAPE)
			{
				setShouldClose(true);
			}
		}
	}
	
	private void onNamed()
	{
		if(textValidator.test(textField.getText()))
		{
			callback.name(textField.getText());
			setShouldClose(true);
		}
	}
	
	public static interface NamedCallback
	{
		public void name(String name);
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
