package com.elmfer.parkourhelper.gui;

import net.minecraft.client.gui.ScaledResolution;

public class GuiConfirmationBox extends GuiAlertBox {
	
	private Callback callback;
	
	public GuiConfirmationBox(String title, Callback func)
	{
		super(title);
		callback = func;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		addButton(new GuiButton(0, 0, 0, "Yes"));
		addButton(new GuiButton(-1, 0, 0, "No"));
		height = 20;
	}
	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		ScaledResolution res = new ScaledResolution(mc);
		viewport.pushMatrix(false);
		{
			int margins = 20 / res.getScaleFactor();
			
			GuiButton yes = (GuiButton) buttonList.get(1);
			GuiButton cancel = (GuiButton) buttonList.get(2);
			yes.width = cancel.width = viewport.getWidth() / 2 - margins;
			yes.y = cancel.y =  viewport.getHeight() / 2 - yes.height / 2;
			cancel.x = viewport.getWidth() - cancel.width;
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

	public static interface Callback
	{
		public void callBack();
	}
}
