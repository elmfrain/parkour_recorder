package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class SaveRecordingScreen extends GuiScreen
{
	private GuiButtonList theList = new GuiButtonList(this);
	private GuiAlertBox alertBox = null;
	private Recording currentSelection = null;
	
	public SaveRecordingScreen()
	{
		super(new StringTextComponent(I18n.format("gui.save_session.title")));
	}
	
	@Override
	public void initGui()
	{
		int buttonMargin = 5;
		int buttonHeight = 20;
		theList.buttonList.clear();
		widgetList.clear();
		for(int i = 0; i < EventHandler.recordHistory.size(); i++)
		{
			GuiButton button = new GuiButton(0, 0, EventHandler.recordHistory.get(i).getName(), this::buttonListCallback);
			theList.addButton(button);
		}
		
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_session.save_last"), this::actionPerformed));
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_session.erase_history"), this::actionPerformed));
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_session.save"), this::actionPerformed));
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_session.remove"), this::actionPerformed));
		addWidget(new GuiButton(0, 0, I18n.format("gui.save_session.open"), this::actionPerformed));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonId = widgetList.indexOf(button);
		switch(buttonId)
		{
		case 1:
			GuiAlertBox clearBox = new GuiConfirmationBox("Should Clear History?", this::clearHistory, this);
			alertBox = clearBox;
			alertBox.initGui();
			break;
		case 3:
			GuiAlertBox removeBox = new GuiConfirmationBox("Should Remove?", this::remove, this);
			alertBox = removeBox;
			alertBox.initGui();
			break;
		case 4:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(currentSelection);
			mc.displayGuiScreen(null);
			break;
		default:
			if(buttonId == 0 || buttonId == 2) 
			{
				if(buttonId == 0) currentSelection = EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1);
				GuiNamerBox namerBox = new GuiNamerBox("Name Recording", this, (String s) -> { return s.length() > 0; } , this::save);
				namerBox.textField.setText(currentSelection.getName());
				alertBox = namerBox;
				alertBox.initGui();
			}
		}
	}
	
	protected void buttonListCallback(Button button)
	{
		currentSelection = EventHandler.recordHistory.get(theList.getIndex((GuiButton) button));
	}
	
	@Override
	 public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	 {
		MainWindow res = mc.getMainWindow();
		int bodyMargin = (int) (80 / res.getGuiScaleFactor());
		int listMargin = (int) (20 / res.getGuiScaleFactor());
		int asideMargin = 10;
		int fontHeight_2 = mc.fontRenderer.FONT_HEIGHT / 2;
		float listWidth = 0.7f;
		String worldName = " - " + Recording.getCurrentWorldName(mc);
		int buttonMargin = 5;
		int buttonHeight = 14;
		
		int fade1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
		int fade2 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
		
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
		list.top = listMargin + mc.fontRenderer.FONT_HEIGHT + listMargin;
		list.bottom -= listMargin;
		GuiViewport actionsBody = new GuiViewport(body);
		actionsBody.left = listBody.right + listMargin; actionsBody.top = listMargin;
		actionsBody.right -= listMargin;
		GuiViewport actions = new GuiViewport(actionsBody);
		actions.left = listMargin; actions.top = listMargin + mc.fontRenderer.FONT_HEIGHT + listMargin;
		actions.right -= listMargin;
		actions.bottom = actions.top + 2 * (buttonMargin + buttonHeight);
		actionsBody.bottom = actions.bottom + listMargin;
		GuiViewport asideBody = new GuiViewport(body);
		asideBody.left = listBody.right + listMargin; asideBody.top = actionsBody.bottom + listMargin;
		asideBody.right -= listMargin;
		asideBody.bottom -= listMargin;
		GuiViewport aside = new GuiViewport(asideBody);
		aside.left = listMargin; aside.top = listMargin + mc.fontRenderer.FONT_HEIGHT + listMargin;
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
				/**drawDefaultBackround(MatrixStack)**/
				func_230446_a_(stack);
				/**drawCenteredString(MatrixStack, FontRenderer, String, int x, int y, int color)**/
				func_238471_a_(stack, mc.fontRenderer, I18n.format("gui.save_session.title"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
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
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_session.record_history") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			theList.drawScreen(mouseX, mouseY, partialTicks, list);
			
			actionsBody.pushMatrix(true);
			{
				/**drawGradientRect(MatrixStack, int left, int top, int right, int bottom, int color1, int color2)**/
				func_238468_a_(stack, 0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_session.actions"), listMargin, listMargin, 0xFFFFFFFF);
			}
			actionsBody.popMatrix();
			
			actions.pushMatrix(false);
			{
				for(int i = 0; i < 2; i++)
				{
					GuiButton button = (GuiButton) widgetList.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.setY((buttonHeight + buttonMargin) * i);
					button.drawButton(stack, mouseX, mouseY, partialTicks);
				}
			}
			actions.popMatrix();
			
			asideBody.pushMatrix(true);
			{
				/**drawGradientRect(MatrixStack, int left, int top, int right, int bottom, int color1, int color2)**/
				func_238468_a_(stack, 0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_session.information"), listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{
				for(int i = 2; i < widgetList.size(); i++)
				{
					GuiButton button = (GuiButton) widgetList.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.setY((buttonHeight + buttonMargin) * (i - 2));
					button.drawButton(stack, mouseX, mouseY, partialTicks);
					button.setEnabled(currentSelection != null);
				}
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) widgetList.get(4);
				boolean flag = EventHandler.session.isSessionActive();
				open.setEnabled(!flag && open.enabled());
				
				//String warning = I18n.format("gui.save_session.warn.cannot_open_while_recording_or_playing");
				//if(open.isMouseOver() && flag && currentSelection != null) drawHoveringText(warning, mouseX, mouseY);
				//RenderHelper.disableStandardItemLighting();
			}
			all.popMatrix();
			
			if(currentSelection != null && desc.getHeight() > 0)
			{
				desc.pushMatrix(true);
				{
					String[] lines = currentSelection.toString().split("\n");
					for(int i = 0; i < lines.length; i++)
					{
						/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
						func_238476_c_(new MatrixStack(), mc.fontRenderer, lines[i], 0, mc.fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
				}
				desc.popMatrix();
			}
		}
		GlStateManager.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.drawScreen(stack, mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	 }
	
	private void save(String newName)
	{
		currentSelection.rename(newName);
		currentSelection.save();
		remove();
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
