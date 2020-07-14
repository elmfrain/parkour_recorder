package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Stack;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SaveRecordingScreen extends GuiScreen
{
	private GuiButtonList listViewport = new GuiButtonList(this);
	private GuiAlertBox alertBox = null;
	private Stack<Recording> selections = new Stack<Recording>();
	
	public SaveRecordingScreen()
	{
		super(new StringTextComponent(I18n.format("gui.save_recording")));
	}
	
	@Override
	public void init()
	{
		GuiButton.currentZLevel = 0;
		buttons.clear();
		children.clear();
		listViewport.clearButtons();
		for(int i = 0; i < EventHandler.recordHistory.size(); i++)
		{
			GuiButton button = new GuiButton(0, 0, EventHandler.recordHistory.get(i).getName(), this::buttonListCallback);
			button.highlighed = selections.contains(EventHandler.recordHistory.get(i)); 
			button.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
			listViewport.addButton(button);
		}
		if(!selections.isEmpty())
		{
			int latestSelection = EventHandler.recordHistory.indexOf(selections.lastElement());
			listViewport.buttonList.get(latestSelection).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}
		
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.save_last"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.erase_history"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.save"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.remove"), this::actionPerformed));
												  /**setMessage()**/
			if(selections.size() > 1) buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove_selected"));
			else buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.load_recording.remove"));
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.open"), this::actionPerformed));
		
		if(alertBox != null)
			alertBox.init();
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonId = buttons.indexOf(button);
		switch(buttonId)
		{
		case 1:
			GuiAlertBox clearBox = new GuiConfirmationBox(I18n.format("gui.save_recording.clear_history_?"), this::clearHistory, this);
			alertBox = clearBox;
			alertBox.init();
			break;
		case 3:
			String title = 1 < selections.size() ? I18n.format("gui.save_recording.remove_selected_?") : I18n.format("gui.save_recording.should_remove_?");
			GuiAlertBox removeBox = new GuiConfirmationBox(title, this::remove, this);
			alertBox = removeBox;
			alertBox.init();
			break;
		case 4:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getInstance().displayGuiScreen(null);
			break;
		default:
			if(buttonId == 0 || buttonId == 2) 
			{
				if(buttonId == 0) 
				{
					selections.clear();
					for(int i = 0; i < listViewport.buttonList.size(); i++)
					{
						GuiButton b = listViewport.buttonList.get(i);
						b.highlighed = selections.contains(EventHandler.recordHistory.get(i)); 
						b.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
					}
					
					selections.push(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
					GuiButton guiButton = listViewport.buttonList.get(EventHandler.recordHistory.indexOf(selections.lastElement()));
					guiButton.highlighed = true;
					guiButton.highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
				}
				GuiNamerBox namerBox = new GuiNamerBox(I18n.format("gui.save_recording.name_recording"), this, (String s) -> { return s.length() > 0; } , this::save);
				namerBox.textField.setText(selections.lastElement().getName());
				alertBox = namerBox;
				alertBox.init();
			}
		}
	}
	
	protected void buttonListCallback(Button button)
	{
		GuiButton guiButton = (GuiButton) button;
		
		/** hasControlDown() **/
		if(!func_231172_r_()) selections.clear();
		
		for(int i = 0; i < listViewport.buttonList.size(); i++)
		{
			GuiButton b = listViewport.buttonList.get(i);
			b.highlighed = selections.contains(EventHandler.recordHistory.get(i)); 
			b.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
		}
		
		if(selections.contains(EventHandler.recordHistory.get(listViewport.getIndex(guiButton))))
		{
			selections.remove(EventHandler.recordHistory.get(listViewport.getIndex(guiButton)));
			guiButton.highlighed = false;
			if(!selections.isEmpty())
				listViewport.buttonList.get(EventHandler.recordHistory.indexOf(selections.lastElement())).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}
		else
		{
			guiButton.highlighed = true;
			guiButton.highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
			selections.push(EventHandler.recordHistory.get(listViewport.getIndex(guiButton)));
		}
	}
	
	@Override
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		if(alertBox != null) alertBox.keyPressed(keyID, scancode, mods);
		return super.keyPressed(keyID, scancode, mods);
	}
	
	@Override
	 public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	 {
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		int bodyMargin = (int) (80 / res.getGuiScaleFactor());
		int listMargin = (int) (20 / res.getGuiScaleFactor());
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
		
		GL11.glPushMatrix();
		{
			all.pushMatrix(false);
			{
				/**drawDefaultBackround(MatrixStack)**/
				func_230446_a_(stack);
				/**drawCenteredString(MatrixStack, FontRenderer, String, int x, int y, int color)**/
				func_238471_a_(stack, mc.fontRenderer, I18n.format("gui.save_recording"), all.getWidth() / 2, bodyMargin / 2 - fontHeight_2, 0xFFFFFFFF);
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
				func_238476_c_(new MatrixStack(), mc.fontRenderer, I18n.format("gui.save_recording.record_history") + worldName, listMargin, listMargin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			listViewport.drawScreen(mouseX, mouseY, partialTicks, list);
			
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
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.setY((buttonHeight + buttonMargin) * i);
					button.renderButton(stack, mouseX, mouseY, partialTicks);
					button.setEnabled(!EventHandler.recordHistory.isEmpty());
				}
			}
			actions.popMatrix();
			
			asideBody.pushMatrix(true);
			{
				/**drawGradientRect(MatrixStack, int left, int top, int right, int bottom, int color1, int color2)**/
				func_238468_a_(stack, 0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				
				String subTitle = I18n.format("gui.load_recording.information");
				subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
				/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
				func_238476_c_(new MatrixStack(), mc.fontRenderer, subTitle, listMargin, listMargin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{
				for(int i = 2; i < buttons.size(); i++)
				{
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(buttonHeight);
					button.setY((buttonHeight + buttonMargin) * (i - 2));
					button.renderButton(stack, mouseX, mouseY, partialTicks);
					button.setEnabled(!selections.isEmpty());
				}
														/**setMessage()**/
				if(selections.size() > 1) buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove_selected"));
				else buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove"));
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttons.get(4);
				boolean flag = EventHandler.session.isSessionActive();
				open.setEnabled(!flag && open.enabled());;
				
				if(open.hovered() && flag && !selections.isEmpty()) 
				{	
					String warning = I18n.format("gui.save_recording.warn.cannot_open_while_recording_or_playing");
					func_238652_a_(stack, ITextProperties.func_240652_a_(warning), mouseX, mouseY);
				}
			}
			all.popMatrix();
			
			if(!selections.isEmpty() && desc.getHeight() > 0)
			{
				desc.pushMatrix(true);
				{
					String[] lines = selections.lastElement().toString().split("\n");
					for(int i = 0; i < lines.length; i++)
					{
						/**drawString(MatrixStack, FontRenderer, int x, int, y, int color)**/
						func_238476_c_(new MatrixStack(), mc.fontRenderer, lines[i], 0, mc.fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
				}
				desc.popMatrix();
			}
			
			if(alertBox != null)
			{
				alertBox.drawScreen(stack, mouseX, mouseY, partialTicks);
				if(alertBox.shouldClose()) alertBox = null;
			}
		}
		GL11.glPopMatrix();
		
		all.pushMatrix(false);
	 }
	
	private void save(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save();
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
	}
	
	private void remove()
	{
		for(int i = 0; i < selections.size(); i++)
			EventHandler.recordHistory.remove(selections.get(i));
		selections.clear();
		init();
	}
	
	private void clearHistory()
	{
		EventHandler.recordHistory.clear();
		EventHandler.session.cleanUp();
		selections.clear();
		if(!EventHandler.session.isSessionActive())
			EventHandler.session = new RecordingSession();
		init();
	}
}
