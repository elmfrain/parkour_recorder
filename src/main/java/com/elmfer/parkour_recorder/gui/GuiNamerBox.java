package com.elmfer.parkour_recorder.gui;

import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
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
	
	public GuiNamerBox(String titleIn, GuiScreen parent, Predicate<String> textPredicate, INamedCallback callback)
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
	public void initGui()
	{
		super.initGui();
		int margins = (int) (20 / mc.getMainWindow().getGuiScaleFactor());
		textField.setMaxStringLength(128);
		textField.setCursorPositionZero();
		addWidget(new GuiButton(0, 0, I18n.format("Name"), this::name));
		addWidget(new GuiButton(0, 0, "Cancel", this::close));
		addWidget(textField);
		height = 40 + margins;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		viewport.pushMatrix(false);
		{
			MatrixStack stack = new MatrixStack();
			int margins = (int) (20 / Minecraft.getInstance().getMainWindow().getGuiScaleFactor());
			textField.setWidth(viewport.getWidth());
			textField.drawTextBox(stack, mouseX, mouseY, partialTicks);
			GuiButton rename = (GuiButton) widgetList.get(1);
			GuiButton cancel = (GuiButton) widgetList.get(2);
			rename.setWidth(viewport.getWidth() / 2 - margins);
			cancel.setWidth(rename.width());
			rename.setY(textField.height() + margins);
			cancel.setY(rename.y());
			cancel.setX(viewport.getWidth() - cancel.width());
			rename.drawButton(stack, mouseX, mouseY, partialTicks);
			cancel.drawButton(stack, mouseX, mouseY, partialTicks);
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
