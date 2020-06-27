package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class LoadRecordingScreen extends GuiScreen {

	private Recording[] records = null;
	private Recording currentSelection = null;
	private GuiButtonList recordList = new GuiButtonList(this);
	
	public LoadRecordingScreen()
	{
		super(new TranslationTextComponent("gui.save_selection.title"));
	}

	@Override
	protected void initGui()
	{
		super.initGui();
		records = Recording.loadSaves();
		int buttonMargin = 5;
		int buttonHeight = 20;
		recordList.buttonList.clear();
		for(int i = 0; i < records.length; i++)
			recordList.addButton(new GuiButton(0, 0, records[i].getName(), this::buttonListCallback));
		
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_selection.open"), this::actionPerformed));
		addWidget(new GuiButton(0, (buttonHeight + buttonMargin), I18n.format("gui.save_selection.delete"), this::actionPerformed));
		addWidget(new GuiButton(0, (buttonHeight + buttonMargin) * 2, I18n.format("gui.save_selection.rename"), this::actionPerformed));
		addWidget(new GuiTextField(mc.fontRenderer, 0, 0));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonId = widgetList.indexOf(button);
		switch(buttonId)
		{
		case 0:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(currentSelection);
			mc.displayGuiScreen(null);
			break;
		case 1:
			GuiAlertBox deleteBox = new GuiConfirmationBox("Should Delete?", this::delete, this);
			alertBox = deleteBox;
			alertBox.initGui();
			break;
		case 2:
			GuiNamerBox renameBox = new GuiNamerBox("Rename Recording", this, (String s) -> { return s.length() > 0; } , this::rename);
			renameBox.textField.setText(currentSelection.getName());
			alertBox = renameBox;
			alertBox.initGui();
			break;
		}
	}
	
	protected void buttonListCallback(Button button)
	{
		currentSelection = records[recordList.getIndex((GuiButton) button)];
	}
	
	@Override
	public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		MainWindow res = mc.getMainWindow();
		FontRenderer fontRenderer = mc.fontRenderer;
		int bodyMargin = (int) (80 / res.getGuiScaleFactor());
		int listMargin = (int) (20 / res.getGuiScaleFactor());
		int asideMargin = 10;
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
				/**drawDefaultBackround(MatrixStack)**/
				func_230446_a_(stack);
				/**drawCenteredString(MatrixStack, FontRenderer, String, int x, int y, int color)**/
				func_238471_a_(stack, fontRenderer, I18n.format("gui.save_selection.title"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				/**drawRect(MatrixStack, int left, int top, int right, int bottom)**/
				func_238467_a_(new MatrixStack(), 0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(false);
			{
				/**drawGradientRect(MatrixStack, int left, int top, int right, int bottom, int color1, int color2)**/
				func_238468_a_(stack, 0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_selection.list") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			recordList.drawScreen(mouseX, mouseY, partialTicks, list);
			
			asideBody.pushMatrix(true);
			{
				/**drawGradientRect(MatrixStack, int left, int top, int right, int bottom, int color1, int color2)**/
				func_238468_a_(stack, 0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);
				/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_selection.aside"), listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(true);
			{				
				for(int i = 0; i < 3; i++)
				{
					GuiButton button = (GuiButton) widgetList.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.setY((buttonHeight + buttonMargin) * i);
					button.drawButton(stack, mouseX, mouseY, partialTicks);
					button.setEnabled(currentSelection != null);
				}
			}
			aside.popMatrix();
			
			if(currentSelection != null)
			{
				desc.pushMatrix(true);
				{
					String[] lines = currentSelection.toString().split("\n");
					for(int i = 0; i < lines.length; i++)
					{
						/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
						func_238476_c_(new MatrixStack(), mc.fontRenderer, lines[i], 0, fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
				}
				desc.popMatrix();
			}
		}
		RenderSystem.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.drawScreen(stack, mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	}
	
	private void rename(String newName)
	{
		alertBox.setShouldClose(true);
		alertBox = null;
		currentSelection.rename(newName);
		currentSelection.save();
		widgetList.clear();
		eventListeners.clear();
		initGui();
	}
	
	private void delete()
	{
		alertBox.setShouldClose(true);
		alertBox = null;
		Recording.deleteSave(currentSelection);
		widgetList.clear();
		eventListeners.clear();
		currentSelection = null;
		initGui();
	}
}
