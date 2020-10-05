package com.elmfer.parkour_recorder.gui.alertbox;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widgets.GuiButton;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**Override alert box. Used for saving overridden recordings.**/
public class GuiOverrideBox extends GuiAlertBox
{

	private final ConfirmOverride override;
	private final SaveToNew saveNew;
	private final String message;
	
	public GuiOverrideBox(String titleIn, GuiScreen parent, String message, ConfirmOverride overrideCallback, SaveToNew saveNewCallback)
	{
		super(titleIn, parent);
		override = overrideCallback;
		saveNew = saveNewCallback;
		this.message = message;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		//Add buttons
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.override_box.override")));
		addButton(new GuiButton(1, 0, 0, I18n.format("gui.override_box.save_as_new")));
		addButton(new GuiButton(-1, 0, 0, I18n.format("gui.override_box.cancel")));
		
		//Set height
		height = 40 + GuiStyle.Gui.margin();
	}
	
	@Override
	public void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		super.actionPerformed(button);
		
		//Perform callbacks if called
		if(button.id == 0) { override.overrideRecording(); setShouldClose(true); }
		else if(button.id == 1) saveNew.saveRecording();
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		//Styling constant
		final int MARGIN = GuiStyle.Gui.margin();
		
		//Render box body
		viewport.pushMatrix(false);
		{
			//Position buttons
			GuiButton override = (GuiButton) buttonList.get(1);
			GuiButton saveAsNew = (GuiButton) buttonList.get(2);
			GuiButton cancel = (GuiButton) buttonList.get(3);
			override.width = saveAsNew.width = cancel.width = (viewport.getWidth() - MARGIN * 2) / 3;
			saveAsNew.x = override.width + MARGIN;
			cancel.x = viewport.getWidth() - cancel.width;
			override.y = saveAsNew.y = cancel.y = GuiStyle.Gui.buttonHeight() + MARGIN;
			
			//Show message
			String[] message = this.message.split("\n");
			int i = 0;
			for(String line : message) drawCenteredString(mc.fontRenderer, line, viewport.getWidth() / 2, i++ * mc.fontRenderer.FONT_HEIGHT, 0xFFFFFFFF);
			
			//Render buttons
			override.drawButton(mc, mouseX, mouseY, partialTicks);
			saveAsNew.drawButton(mc, mouseX, mouseY, partialTicks);
			cancel.drawButton(mc, mouseX, mouseY, partialTicks);
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
}
