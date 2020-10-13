package com.elmfer.parkour_recorder.gui.alertbox;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;

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
		viewport.pushMatrix(false);
		{
			int margin = GuiStyle.Gui.margin();
			
			GuiButton yes = (GuiButton) buttons.get(1);
			GuiButton cancel = (GuiButton) buttons.get(2);
			yes.setWidth(viewport.getWidth() / 2 - margin);
			cancel.setWidth(yes.getWidth());
			yes.y = viewport.getHeight() / 2 - yes.getHeight() / 2;
			cancel.y = yes.y;
			cancel.x = viewport.getWidth() - cancel.getWidth();
			yes.renderButton(mouseX, mouseY, partialTicks);
			cancel.renderButton(mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		if(keyID == GLFW.GLFW_KEY_ENTER)
			confirmed(null);
		
		return super.keyPressed(keyID, scancode, mods);
	}
	
	private void confirmed(@Nullable Button button)
	{
		callback.callBack();
		setShouldClose(true);
	}
}
