package com.elmfer.parkour_recorder.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.button.Button;

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
		addWidget(new GuiButton(0, 0, "Yes", this::confirmed));
		addWidget(new GuiButton(0, 0, "Cancel", this::close));
		height = 20;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		MainWindow res = mc.getMainWindow();
		viewport.pushMatrix(false);
		{
			int margins = (int) (20 / res.getGuiScaleFactor());
			
			GuiButton yes = (GuiButton) widgetList.get(1);
			GuiButton cancel = (GuiButton) widgetList.get(2);
			yes.setWidth(viewport.getWidth() / 2 - margins);
			cancel.setWidth(yes.width());
			yes.setY(viewport.getHeight() / 2 - yes.height() / 2);
			cancel.setY(yes.y());
			cancel.setX(viewport.getWidth() - cancel.width());
			MatrixStack stack = new MatrixStack();
			yes.drawButton(stack, mouseX, mouseY, partialTicks);
			cancel.drawButton(stack, mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	private void confirmed(Button button)
	{
		setShouldClose(true);
		callback.callBack();
	}
}
