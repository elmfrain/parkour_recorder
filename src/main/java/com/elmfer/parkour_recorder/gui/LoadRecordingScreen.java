package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class LoadRecordingScreen extends Screen {

	private Recording[] records = null;
	private Recording currentSelection = null;
	private GuiButtonList recordList = new GuiButtonList(this);
	private GuiAlertBox alertBox = null;
	
	public LoadRecordingScreen()
	{
		super(new TranslationTextComponent("gui.load_recording"));
	}

	@Override
	protected void init()
	{
		GuiButton.currentZLevel = 0;
		Minecraft mc = Minecraft.getInstance();
		records = Recording.loadSaves();
		int buttonMargin = 5;
		int buttonHeight = 20;
		buttons.clear();
		children.clear();
		recordList.clearButtons();
		for(int i = 0; i < records.length; i++)
			recordList.addButton(new GuiButton(0, 0, records[i].getName(), this::buttonListCallback));

		addButton(new GuiButton(0, 0, I18n.format("gui.load_recording.open"), this::actionPerformed));
		addButton(new GuiButton(0, (buttonHeight + buttonMargin), I18n.format("gui.load_recording.delete"), this::actionPerformed));
		addButton(new GuiButton(0, (buttonHeight + buttonMargin) * 2, I18n.format("gui.load_recording.rename"), this::actionPerformed));
		addButton(new GuiTextField(mc.fontRenderer, 0, 0));
		
		if(alertBox != null)
			alertBox.init();
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonId = buttons.indexOf(button);
		switch(buttonId)
		{
		case 0:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(currentSelection);
			EventHandler.hud.fadedness = 200;
			Minecraft.getInstance().displayGuiScreen(null);
			break;
		case 1:
			GuiAlertBox deleteBox = new GuiConfirmationBox(I18n.format("gui.load_recording.should_delete_?"), this::delete, this);
			alertBox = deleteBox;
			alertBox.init();
			break;
		case 2:
			GuiNamerBox renameBox = new GuiNamerBox(I18n.format("gui.load_recording.rename_recording"), this, (String s) -> { return s.length() > 0; } , this::rename);
			renameBox.textField.setText(currentSelection.getName());
			alertBox = renameBox;
			alertBox.init();
			break;
		}
	}
	
	protected void buttonListCallback(Button button)
	{
		currentSelection = records[recordList.getIndex((GuiButton) button)];
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		FontRenderer fontRenderer = mc.fontRenderer;
		int bodyMargin = (int) (80 / res.getGuiScaleFactor());
		int listMargin = (int) (20 / res.getGuiScaleFactor());
		int fontHeight_2 = fontRenderer.FONT_HEIGHT / 2;
		float listWidth = 0.7f;
		String worldName = " - " + Recording.getCurrentWorldName(mc);
		int buttonMargin = 5;
		int buttonHeight = 14;
		
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
		aside.bottom = aside.top + (buttonHeight + buttonMargin) * 3;
		GuiViewport desc = new GuiViewport(asideBody);
		desc.left = listMargin;
		desc.right -= listMargin;
		desc.top = aside.bottom;
		desc.bottom -= listMargin;
		
		RenderSystem.pushMatrix();
		{
			int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
			
			all.pushMatrix(false);
			{
				renderBackground();
				drawCenteredString(fontRenderer, I18n.format("gui.load_recording"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				fill(0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(false);
			{
				fillGradient(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				drawString(mc.fontRenderer, I18n.format("gui.load_recording.list") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			recordList.drawScreen(mouseX, mouseY, partialTicks, list);
			
			asideBody.pushMatrix(true);
			{
				fillGradient(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);
				drawString(mc.fontRenderer, I18n.format("gui.load_recording.information"), listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(true);
			{				
				for(int i = 0; i < 3; i++)
				{
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.y = (buttonHeight + buttonMargin) * i;
					button.renderButton(mouseX, mouseY, partialTicks);
					button.active = currentSelection != null;
				}
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttons.get(0);
				boolean flag = EventHandler.session.isSessionActive();
				open.active = !flag && open.active;
				
				if(open.isHovered() && flag && currentSelection != null) 
				{	
					String warning = I18n.format("gui.save_session.warn.cannot_open_while_recording_or_playing");
					renderTooltip(warning, mouseX, mouseY);
				}
			}
			all.popMatrix();
			
			if(currentSelection != null)
			{
				desc.pushMatrix(true);
				{
					String[] lines = currentSelection.toString().split("\n");
					for(int i = 0; i < lines.length; i++)
					{
						drawString(mc.fontRenderer, lines[i], 0, fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
				}
				desc.popMatrix();
			}
		}
		RenderSystem.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.render(mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	}
	
	private void rename(String newName)
	{
		alertBox.setShouldClose(true);
		alertBox = null;
		currentSelection.rename(newName);
		currentSelection.save();
		buttons.clear();
		children.clear();
		init();
	}
	
	private void delete()
	{
		alertBox.setShouldClose(true);
		alertBox = null;
		Recording.deleteSave(currentSelection);
		buttons.clear();
		children.clear();
		currentSelection = null;
		init();
	}
}
