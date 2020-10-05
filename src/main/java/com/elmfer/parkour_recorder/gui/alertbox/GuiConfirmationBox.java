package com.elmfer.parkour_recorder.gui.alertbox;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widgets.GuiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiConfirmationBox extends GuiAlertBox {
	
	private ICallback callback;
	
	public GuiConfirmationBox(String title, ICallback func, GuiScreen parent)
	{
		super(title, parent);
		callback = func;
	}
	
	public static interface ICallback
	{
		public void callBack();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.confirmation_box.yes")));
		addButton(new GuiButton(-1, 0, 0, I18n.format("gui.confirmation_box.cancel")));
		height = 20;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		viewport.pushMatrix(false);
		{
			Minecraft mc = Minecraft.getMinecraft();
			int margin = GuiStyle.Gui.margin();
			
			//Position the buttons
			GuiButton yes = (GuiButton) buttonList.get(1);
			GuiButton cancel = (GuiButton) buttonList.get(2);
			yes.setWidth(viewport.getWidth() / 2 - margin);
			cancel.width = yes.width;
			yes.y = viewport.getHeight() / 2 - yes.height / 2;
			cancel.y = yes.y;
			cancel.x = viewport.getWidth() - cancel.width;
			
			//Render buttons
			yes.drawButton(mc, mouseX, mouseY, partialTicks);
			cancel.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			callback.callBack();
			setShouldClose(true);
		}
	}
	
	@Override
	public void keyTyped(char c, int keycode)
	{
		super.keyTyped(c, keycode);
		
		if(keycode == 28)
			callback.callBack();
			setShouldClose(true);
	}
}
