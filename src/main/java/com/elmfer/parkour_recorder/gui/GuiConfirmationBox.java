package com.elmfer.parkour_recorder.gui;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

public class GuiConfirmationBox extends GuiAlertBox {
	
	private ICallback callback;
	
	public GuiConfirmationBox(String title, ICallback func, Screen parent)
	{
		super(title, parent);
		callback = func;
	}
	
	public static interface ICallback
	{
		public void callBack();
	}
	
	@Override
	public void init()
	{
		super.init();
		addButton(new GuiButton(0, 0, I18n.format("gui.confirmation_box.yes"), this::confirmed));
		addButton(new GuiButton(0, 0, I18n.format("gui.confirmation_box.cancel"), this::close));
		height = 20;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		MainWindow res = Minecraft.getInstance().getMainWindow();
		viewport.pushMatrix(false);
		{
			int margins = (int) (20 / res.getGuiScaleFactor());
			
			GuiButton yes = (GuiButton) buttons.get(1);
			GuiButton cancel = (GuiButton) buttons.get(2);
			yes.setWidth(viewport.getWidth() / 2 - margins);
			cancel.setWidth(yes.getWidth());
			yes.y = viewport.getHeight() / 2 - yes.getHeight() / 2;
			cancel.y = yes.y;
			cancel.x = viewport.getWidth() - cancel.getWidth();
			yes.renderButton(mouseX, mouseY, partialTicks);
			cancel.renderButton(mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	private void confirmed(Button button)
	{
		setShouldClose(true);
		callback.callBack();
	}
}
