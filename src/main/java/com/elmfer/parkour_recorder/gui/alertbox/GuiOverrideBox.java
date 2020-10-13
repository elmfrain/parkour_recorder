package com.elmfer.parkour_recorder.gui.alertbox;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

/**Override alert box. Used for saving overridden recordings.**/
public class GuiOverrideBox extends GuiAlertBox 
{
	private final ConfirmOverride override;
	private final SaveToNew saveNew;
	private final String message;
	
	public GuiOverrideBox(String titleIn, Screen parent, String message, ConfirmOverride overrideCallback, SaveToNew saveNewCallback)
	{
		super(titleIn, parent);
		override = overrideCallback;
		saveNew = saveNewCallback;
		this.message = message;
	}
	
	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		//Styling constant
		final int MARGIN = GuiStyle.Gui.margin();
		
		//Render box body
		viewport.pushMatrix(false);
		{
			//Position buttons
			GuiButton override = (GuiButton) buttons.get(1);
			GuiButton saveAsNew = (GuiButton) buttons.get(2);
			GuiButton cancel = (GuiButton) buttons.get(3);
			final int BUTTON_WIDTH = (viewport.getWidth() - MARGIN * 2) / 3;
			override.setWidth(BUTTON_WIDTH); saveAsNew.setWidth(BUTTON_WIDTH); cancel.setWidth(BUTTON_WIDTH);
			override.x = 0;
			saveAsNew.x = BUTTON_WIDTH + MARGIN;
			cancel.x = viewport.getWidth() - BUTTON_WIDTH;
			override.y = saveAsNew.y = cancel.y = GuiStyle.Gui.buttonHeight() + MARGIN;
			
			//Show message
			String[] message = this.message.split("\n");
			int i = 0;
			for(String line : message) drawCenteredString(mc.fontRenderer, line, viewport.getWidth() / 2, i++ * mc.fontRenderer.FONT_HEIGHT, 0xFFFFFFFF);
			
			//Render buttons
			override.renderButton(mouseX, mouseY, partialTicks);
			saveAsNew.renderButton(mouseX, mouseY, partialTicks);
			cancel.renderButton(mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public void init()
	{
		super.init();
		
		//Add buttons
		addButton(new GuiButton(0, 0, I18n.format("gui.override_box.override"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.override_box.save_as_new"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.override_box.cancel"), this::close));
		
		//Set height
		height = 40 + GuiStyle.Gui.margin();
	}
	
	public void actionPerformed(Button button)
	{
		int buttonID = buttons.indexOf(button);
		
		switch(buttonID)
		{
		case 1: //Override
			override.overrideRecording();
			setShouldClose(true);
			break;
		case 2: //Save As New
			saveNew.saveRecording();
		}
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
}
