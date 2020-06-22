package com.elmfer.parkourhelper.gui;

import static com.elmfer.parkourhelper.render.GraphicsHelper.getIntColor;

import java.io.IOException;

import com.elmfer.parkourhelper.EventHandler;
import com.elmfer.parkourhelper.parkour.PlaybackSession;
import com.elmfer.parkourhelper.parkour.Recording;
import com.elmfer.parkourhelper.parkour.RecordingSession;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiSaveSession extends GuiScreen
{
	private GuiButtonList theList = new GuiButtonList();
	private GuiAlertBox alertBox = null;
	private Recording currentSelection = null;
	
	@Override
	public void initGui()
	{
		int buttonMargin = 5;
		int buttonHeight = 20;
		theList.buttonList.clear();
		buttonList.clear();
		for(int i = 0; i < EventHandler.recordHistory.size(); i++)
			theList.buttonList.add(new GuiButton(i, 0, 0, EventHandler.recordHistory.get(i).getName()));
		
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.save_session.save_last")));
		addButton(new GuiButton(1, 0, 0, I18n.format("gui.save_session.erase_history")));
		addButton(new GuiButton(2, 0, 0, I18n.format("gui.save_session.save")));
		addButton(new GuiButton(3, 0, 0, I18n.format("gui.save_session.remove")));
		addButton(new GuiButton(4, 0, 0, I18n.format("gui.save_session.open")));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		try {
			super.keyTyped(typedChar, keyCode);
		} catch (IOException e1) {}
		if(alertBox != null)
		{
			try {
				alertBox.keyTyped(typedChar, keyCode);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException 
	{
		if(alertBox != null)
		{
			alertBox.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
		theList.mouseClicked(mouseX, mouseY, mouseButton);
		
		GuiButton button = theList.getButton();
		if(button != null)
			currentSelection = EventHandler.recordHistory.get(button.id);
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		if(button.id == 4)
		{
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(currentSelection);
			mc.displayGuiScreen(null);
		}
		if(button.id == 1)
		{
			GuiAlertBox clearBox = new GuiConfirmationBox("Should Clear History?", this::clearHistory);
			alertBox = clearBox;
			alertBox.initGui();
		}
		if(button.id == 3)
		{
			GuiAlertBox removeBox = new GuiConfirmationBox("Should Remove?", this::remove);
			alertBox = removeBox;
			alertBox.initGui();
		}
		if(button.id == 0 || button.id == 2)
		{
			if(button.id == 0) currentSelection = EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1);
			GuiAlertBox nameBox = new GuiAlertBox("Name Recording")
			{
				GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 80, 20);
				String prevText = currentSelection.getName();
				
				@Override
				public void initGui()
				{
					super.initGui();
					int margins = 20 / (new ScaledResolution(mc)).getScaleFactor();
					textField.setMaxStringLength(128);
					if(textField.getText().isEmpty()) textField.setText(currentSelection.getName());
					textField.setCursorPositionZero();
					addButton(new GuiButton(0, 0, 0, "Save"));
					addButton(new GuiButton(-1, 0, 0, "Cancel"));
					height = 40 + margins;
				}
				@Override
				public void keyTyped(char typedChar, int keyCode)
				{
					textField.textboxKeyTyped(typedChar, keyCode);
					//If enter is pressed
					if(keyCode == 28 && save(textField.getText()))
					{
						mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
						setShouldClose(true);
					}
				}
				@Override
				public void mouseClicked(int mouseX, int mouseY, int mouseButton)
				{
					try {
						super.mouseClicked(mouseX, mouseY, mouseButton);
					} catch (IOException e) {}
					textField.mouseClicked(mouseX, mouseY, mouseButton);
				}
				@Override
				protected void doDrawScreen(int mouseX, int mouseY, float partialTicks)
				{
					viewport.pushMatrix(false);
					{
						int margins = 20 / (new ScaledResolution(mc)).getScaleFactor();
						textField.width = viewport.getWidth();
						textField.drawTextBox();
						GuiButton rename = (GuiButton) buttonList.get(1);
						GuiButton cancel = (GuiButton) buttonList.get(2);
						rename.width = cancel.width = viewport.getWidth() / 2 - margins;
						rename.y = cancel.y = textField.height + margins;
						cancel.x = viewport.getWidth() - cancel.width;
						rename.drawButton(mc, mouseX, mouseY, partialTicks);
						cancel.drawButton(mc, mouseX, mouseY, partialTicks);
					}
					viewport.popMatrix();
				}
				@Override
				public void actionPerformed(net.minecraft.client.gui.GuiButton button)
				{
					super.actionPerformed(button);
					if(button.id == 0 && save(textField.getText()))
						setShouldClose(true);
				}
			};
			alertBox = nameBox;
			alertBox.initGui();
		}
	}
	
	@Override
	 public void drawScreen(int mouseX, int mouseY, float partialTicks)
	 {
		ScaledResolution res = new ScaledResolution(mc);
		int bodyMargin = 80 / res.getScaleFactor();
		int listMargin = 20 / res.getScaleFactor();
		int asideMargin = 10;
		int fontHeight_2 = fontRenderer.FONT_HEIGHT / 2;
		float listWidth = 0.7f;
		String worldName = " - " + Recording.getCurrentWorldName(mc);
		int buttonMargin = 5;
		int buttonHeight = 14;
		
		int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
		int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport body = new GuiViewport(all);
		body.left = body.top = bodyMargin; body.right -= bodyMargin; body.bottom -= bodyMargin;
		GuiViewport listBody = new GuiViewport(body);
		listBody.left = listBody.top = listMargin; 
		listBody.right = (int) ((listBody.getParent().getWidth() - listMargin * 2) * listWidth);
		listBody.bottom -= listMargin;
		GuiViewport list = new GuiViewport(listBody);
		list.left = listMargin;
		list.right -= listMargin;
		list.top = listMargin + fontRenderer.FONT_HEIGHT + listMargin;
		list.bottom -= listMargin;
		GuiViewport actionsBody = new GuiViewport(body);
		actionsBody.left = listBody.right + listMargin; actionsBody.top = listMargin;
		actionsBody.right -= listMargin;
		GuiViewport actions = new GuiViewport(actionsBody);
		actions.left = listMargin; actions.top = listMargin + fontRenderer.FONT_HEIGHT + listMargin;
		actions.right -= listMargin;
		actions.bottom = actions.top + 2 * (buttonMargin + buttonHeight);
		actionsBody.bottom = actions.bottom + listMargin;
		GuiViewport asideBody = new GuiViewport(body);
		asideBody.left = listBody.right + listMargin; asideBody.top = actionsBody.bottom + listMargin;
		asideBody.right -= listMargin;
		asideBody.bottom -= listMargin;
		GuiViewport aside = new GuiViewport(asideBody);
		aside.left = listMargin; aside.top = listMargin + fontRenderer.FONT_HEIGHT + listMargin;
		aside.right -= listMargin;
		aside.bottom = aside.top + (buttonMargin + buttonHeight) * 3;
		GuiViewport desc = new GuiViewport(asideBody);
		desc.left = listMargin;
		desc.right -= listMargin;
		desc.top = aside.bottom;
		desc.bottom -= listMargin;
		
		GlStateManager.pushMatrix();
		{
			all.pushMatrix(false);
			{
				drawDefaultBackground();
				drawCenteredString(fontRenderer, I18n.format("gui.save_session.title"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				drawRect(0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(false);
			{	
				drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				fontRenderer.drawString(I18n.format("gui.save_session.record_history") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			theList.drawScreen(mouseX, mouseY, partialTicks, list);
			
			actionsBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				fontRenderer.drawString(I18n.format("gui.save_session.actions"), listMargin, listMargin, 0xFFFFFFFF);
			}
			actionsBody.popMatrix();
			
			actions.pushMatrix(false);
			{
				for(int i = 0; i < 2; i++)
				{
					buttonList.get(i).width = actions.getWidth();
					buttonList.get(i).height = buttonHeight;
					buttonList.get(i).y = (buttonHeight + buttonMargin) * i;
					buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTicks);
				}
			}
			actions.popMatrix();
			
			asideBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, asideBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				fontRenderer.drawString(I18n.format("gui.save_session.information"), listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{
				for(int i = 2; i < buttonList.size(); i++)
				{
					buttonList.get(i).width = aside.getWidth();
					buttonList.get(i).height = buttonHeight;
					buttonList.get(i).y = (buttonHeight + buttonMargin) * (i - 2);
					buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTicks);
					buttonList.get(i).enabled = currentSelection != null;
				}
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttonList.get(4);
				boolean flag = EventHandler.session.isSessionActive();
				open.enabled = !flag && open.enabled;
				
				String warning = I18n.format("gui.save_session.warn.cannot_open_while_recording_or_playing");
				if(open.isMouseOver() && flag && currentSelection != null) drawHoveringText(warning, mouseX, mouseY);
				RenderHelper.disableStandardItemLighting();
			}
			all.popMatrix();
			
			if(currentSelection != null && desc.getHeight() > 0)
			{
				desc.pushMatrix(true);
				{
					String[] lines = currentSelection.toString().split("\n");
					for(int i = 0; i < lines.length; i++)
						drawString(fontRenderer, lines[i], 0, fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
				}
				desc.popMatrix();
			}
		}
		GlStateManager.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.drawScreen(mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	 }
	
	private boolean save(String newName)
	{
		if(newName.length() > 0)
		{
			currentSelection.rename(newName);
			currentSelection.save();
			remove();
			return true;
		}
		else return false;
	}
	
	private void remove()
	{
		EventHandler.recordHistory.remove(currentSelection);
		currentSelection = null;
		initGui();
	}
	
	private void clearHistory()
	{
		EventHandler.recordHistory.clear();
		EventHandler.session.cleanUp();
		currentSelection = null;
		if(!EventHandler.session.isSessionActive())
			EventHandler.session = new RecordingSession();
		initGui();
	}
}
