package com.elmfer.parkour_recorder.gui;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

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
	public void init()
	{
		super.init();
		
		int margin = GuiStyle.Gui.margin();
		textField.setMaxStringLength(128);
		textField.setCursorPositionZero();
		addButton(new GuiButton(0, 0, I18n.format("gui.naming_box.name"), this::name));
		addButton(new GuiButton(0, 0, I18n.format("gui.confirmation_box.cancel"), this::close));
		addButton(textField);
		height = 40 + margin;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		viewport.pushMatrix(false);
		{
			int margin = GuiStyle.Gui.margin();
			textField.setWidth(viewport.getWidth());
			textField.renderTextBox(GraphicsHelper.identity, mouseX, mouseY, partialTicks);
			GuiButton rename = (GuiButton) buttons.get(1);
			GuiButton cancel = (GuiButton) buttons.get(2);
			rename.setWidth(viewport.getWidth() / 2 - margin);
			cancel.setWidth(rename.getWidth());
			rename.setY(textField.getHeight() + margin);
			cancel.setY(rename.y());
			cancel.setX(viewport.getWidth() - cancel.getWidth());
			rename.renderButton(mouseX, mouseY, partialTicks);
			cancel.renderButton(mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		if(keyID == GLFW.GLFW_KEY_ENTER)
			name(null);
		return false;
	}
	
	@Override
	public void setShouldClose(boolean shouldClose)
	{
		super.setShouldClose(shouldClose);
		if(shouldClose)
			MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	private void name(@Nullable Button button)
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
