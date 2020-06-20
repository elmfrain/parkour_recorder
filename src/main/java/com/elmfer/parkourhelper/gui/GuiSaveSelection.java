package com.elmfer.parkourhelper.gui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.elmfer.parkourhelper.EventHandler;
import com.elmfer.parkourhelper.Recording;

import static com.elmfer.parkourhelper.render.GraphicsHelper.*;

import java.awt.Graphics;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import com.elmfer.parkourhelper.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import com.elmfer.parkourhelper.gui.GuiTextField;
import com.elmfer.parkourhelper.parkour.PlaybackSession;
import com.elmfer.parkourhelper.parkour.RecordingSession;
import com.elmfer.parkourhelper.render.GraphicsHelper;
import com.elmfer.parkourhelper.render.ShaderManager;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;

public class GuiSaveSelection extends GuiScreen
{
	private Recording[] records;
	private Recording currentSelection = null;
	private GuiButtonList theList = new GuiButtonList();
	private GuiAlertBox alertBox = null;
	
	@Override
	public void initGui()
	{
		records = Recording.loadSaves();
		int buttonMargin = 5;
		int buttonHeight = 20;
		theList.buttonList.clear();
		for(int i = 0; i < records.length; i++)
		{
			if(records[i].getName() != null)
				theList.buttonList.add(new GuiButton(i, 0, 0, records[i].getName()));
			else
				theList.buttonList.add(new GuiButton(i, 0, 0, "Save " + i));
		}
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.save_selection.open")));
		addButton(new GuiButton(1, 0, (buttonHeight + buttonMargin), I18n.format("gui.save_selection.delete")));
		addButton(new GuiButton(2, 0, (buttonHeight + buttonMargin) * 2, I18n.format("gui.save_selection.rename")));
		
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
			currentSelection = records[button.id];
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		if(button.id == 0)
		{
			EventHandler.session = new PlaybackSession(currentSelection);
			mc.displayGuiScreen(null);
		}
		if(button.id == 1)
		{
			GuiAlertBox deleteBox = new GuiAlertBox("Should Delete?") {
				
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
						delete();
						setShouldClose(true);
					}
				}
			};
			alertBox = deleteBox;
			alertBox.initGui();
		}
		if(button.id == 2)
		{
			GuiAlertBox renameBox = new GuiAlertBox("Rename Recording")
			{
				GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 80, 20);
				String prevText = currentSelection.getName();
				
				@Override
				public void initGui()
				{
					super.initGui();
					int margins = 20 / (new ScaledResolution(mc)).getScaleFactor();
					textField.setMaxStringLength(128);
					textField.setText(currentSelection.getName());
					textField.setCursorPositionZero();
					addButton(new GuiButton(0, 0, 0, I18n.format("gui.save_selection.rename")));
					addButton(new GuiButton(-1, 0, 0, "Cancel"));
					height = 40 + margins;
				}
				@Override
				public void keyTyped(char typedChar, int keyCode)
				{
					textField.textboxKeyTyped(typedChar, keyCode);
					//If enter is pressed
					if(keyCode == 28 && rename(textField.getText()))
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
					if(button.id == 0 && rename(textField.getText()))
						setShouldClose(true);
				}
			};
			alertBox = renameBox;
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
		int buttonHeight = 20;
		
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
		GuiViewport asideBody = new GuiViewport(body);
		asideBody.top = listMargin; asideBody.bottom -= listMargin;
		asideBody.left = listBody.right + listMargin;
		asideBody.right -= listMargin;
		GuiViewport aside = new GuiViewport(asideBody);
		aside.left = listMargin;
		aside.right -= listMargin;
		aside.top = listMargin + fontRenderer.FONT_HEIGHT + listMargin;
		aside.bottom = aside.top + (buttonHeight + buttonMargin) * buttonList.size();
		GuiViewport desc = new GuiViewport(asideBody);
		desc.left = listMargin;
		desc.right -= listMargin;
		desc.top = aside.bottom;
		desc.bottom -= listMargin;
		
		GlStateManager.pushMatrix();
		{
			int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
			int grad1 = getIntColor(0.0f, 0.0f, 0.0f, 1.0f);
			int grad2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
			
			all.pushMatrix(false);
			{
				drawDefaultBackground();
				drawCenteredString(fontRenderer, I18n.format("gui.save_selection.title"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
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
				fontRenderer.drawString(I18n.format("gui.save_selection.list") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			theList.drawScreen(mouseX, mouseY, partialTicks, list);
			
			asideBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);
				fontRenderer.drawString(I18n.format("gui.save_selection.aside"), listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{				
				for(int i = 0; i < buttonList.size(); i++)
				{
					buttonList.get(i).width = aside.getWidth();
					buttonList.get(i).drawButton(mc, mouseX, mouseY, partialTicks);
					buttonList.get(i).enabled = currentSelection != null;
				}
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttonList.get(0);
				boolean flag = EventHandler.session.isSessionActive();
				open.enabled = !flag && open.enabled;
				
				String warning = I18n.format("gui.save_selection.warn.cannot_open_while_recording_or_playing");
				if(open.isMouseOver() && flag && currentSelection != null) drawHoveringText(warning, mouseX, mouseY);
				RenderHelper.disableStandardItemLighting();
			}
			all.popMatrix();
			
			if(currentSelection != null)
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
	
	private boolean rename(String newName)
	{
		if(newName.length() > 0)
		{
			currentSelection.rename(newName);
			currentSelection.save();
			buttonList.clear();
			initGui();
			return true;
		}
		else return false;
	}
	
	private void delete()
	{
		Recording.deleteSave(currentSelection);
		buttonList.clear();
		initGui();
		currentSelection = null;
	}
}
