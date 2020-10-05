package com.elmfer.parkour_recorder.gui.alertbox;

import java.io.IOException;
import java.util.function.Predicate;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.widgets.GuiButton;
import com.elmfer.parkour_recorder.gui.widgets.GuiTextField;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class GuiNamerBox extends GuiAlertBox
{
	public GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 80, 20);
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
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == Phase.START)
			textField.updateCursorCounter();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		int margin = GuiStyle.Gui.margin();
		textField.setMaxStringLength(128);
		textField.setCursorPositionZero();
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.naming_box.name")));
		addButton(new GuiButton(-1, 0, 0, I18n.format("gui.confirmation_box.cancel")));
		height = 40 + margin;
	}

	@Override
	protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
	{
		viewport.pushMatrix(false);
		{
			Minecraft mc = Minecraft.getMinecraft();
			
			int margin = GuiStyle.Gui.margin();
			textField.width = viewport.getWidth();
			textField.drawTextBox();
			GuiButton rename = (GuiButton) buttonList.get(1);
			GuiButton cancel = (GuiButton) buttonList.get(2);
			rename.setWidth(viewport.getWidth() / 2 - margin);
			cancel.width =  rename.width;
			rename.y = textField.height + margin;
			cancel.y = rename.y;
			cancel.x = viewport.getWidth() - cancel.width;
			rename.drawButton(mc, mouseX, mouseY, partialTicks);
			cancel.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		viewport.popMatrix();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void keyTyped(char charTyped, int keyCode)
	{
		//Close if escape is pressed
		if(keyCode == 1) setShouldClose(true);
		
		textField.textboxKeyTyped(charTyped, keyCode);
		if(keyCode == 28)
		{
			if(textValidator.test(textField.getText()))
			{
				callback.name(textField.getText());
				setShouldClose(true);
			}
		}
	}
	
	@Override
	public void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			if(textValidator.test(textField.getText()))
			{
				callback.name(textField.getText());
				setShouldClose(true);
			}
		}
	}
	
	@Override
	public void setShouldClose(boolean shouldClose)
	{
		super.setShouldClose(shouldClose);
		if(shouldClose)
			MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	public static interface INamedCallback
	{
		public void name(String name);
	}
}
