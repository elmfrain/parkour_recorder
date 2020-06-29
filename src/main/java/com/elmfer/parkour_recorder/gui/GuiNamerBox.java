package com.elmfer.parkour_recorder.gui;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiNamerBox extends GuiAlertBox
{
	public GuiTextField textField = new GuiTextField(Minecraft.getInstance().fontRenderer, 0, 0, 80, 20);
	private Predicate<String> textValidator;
	private INamedCallback callback;
	
	public GuiNamerBox(String titleIn, Screen parent, Predicate<String> textPredicate, INamedCallback callback)
	{
		super(titleIn, parent);
		textValidator = textPredicate;
		this.callback = callback;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if(event.phase == Phase.START)
		textField.tick();
	}
	
	@Override
	public void init()
	{
		super.init();
		Minecraft mc = Minecraft.getInstance();
		int margins = (int) (20 / mc.getMainWindow().getGuiScaleFactor());
		textField.setMaxStringLength(128);
		textField.setCursorPositionZero();
		addButton(new GuiButton(0, 0, I18n.format("gui.naming_box.name"), this::name));
		addButton(new GuiButton(0, 0, I18n.format("gui.confirmation_box.cancel"), this::close));
		addButton(textField);
		height = 40 + margins;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		viewport.pushMatrix(false);
		{
			int margins = (int) (20 / Minecraft.getInstance().getMainWindow().getGuiScaleFactor());
			textField.setWidth(viewport.getWidth());
			textField.renderButton(mouseX, mouseY, partialTicks);
			GuiButton rename = (GuiButton) buttons.get(1);
			GuiButton cancel = (GuiButton) buttons.get(2);
			rename.setWidth(viewport.getWidth() / 2 - margins);
			cancel.setWidth(rename.getWidth());
			rename.y = textField.getHeight() + margins;
			cancel.y = rename.y;
			cancel.x = viewport.getWidth() - cancel.getWidth();
			rename.renderButton(mouseX, mouseY, partialTicks);
			cancel.renderButton(mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public void setShouldClose(boolean shouldClose)
	{
		super.setShouldClose(shouldClose);
		if(shouldClose)
			MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	private void name(Button button)
	{
		if(textValidator.test(textField.getText()))
		{
			callback.name(textField.getText());
			setShouldClose(true);
		}
	}
	
	public static interface INamedCallback
	{
		public void name(String name);
	}
}
