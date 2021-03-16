package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Stack;

import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.alertbox.GuiAlertBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiConfirmationBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiNamerBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiOverrideBox;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SaveRecordingScreen extends GuiScreen
{
	private ButtonListViewport listViewport = new ButtonListViewport(this);
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
			if(selections.size() > 1) buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove_selected"));
			else buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove"));
		addButton(new GuiButton(0, 0, I18n.format("gui.save_recording.open"), this::actionPerformed));
		
		if(alertBox != null)
			alertBox.init();
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonId = buttons.indexOf(button);
		switch(buttonId)
		{
		case 1: //Clear History
			GuiAlertBox clearBox = new GuiConfirmationBox(I18n.format("gui.save_recording.clear_history_?"), this::clearHistory, this);
			alertBox = clearBox;
			alertBox.init();
			break;
		case 3: //Remove from history
			String title = 1 < selections.size() ? I18n.format("gui.save_recording.remove_selected_?") : I18n.format("gui.save_recording.should_remove_?");
			GuiAlertBox removeBox = new GuiConfirmationBox(title, this::remove, this);
			alertBox = removeBox;
			alertBox.init();
			break;
		case 4: //Exit screen
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getInstance().displayGuiScreen(null);
			break;
		default: //Save recording
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
				
				//If recording was originally opened from a file, prompt overriding alert box
				if(selections.lastElement().getOriginalFile() != null)
				{
					//Create Override box
					String boxMessage = I18n.format("gui.save_recording.recording_was_loaded_from") + ":\n" + selections.lastElement().getOriginalFile().getName();
					GuiOverrideBox overrideBox = new GuiOverrideBox(I18n.format("gui.save_recording.override_recording_?"), this, boxMessage, this::override, this::saveNew);
					alertBox = overrideBox;
					alertBox.init();
				}
				else
				{
					//Create namer box
					GuiNamerBox namerBox = new GuiNamerBox(I18n.format("gui.save_recording.name_recording"), this, (String s) -> { return s.length() > 0; } , this::save);
					alertBox = namerBox;
					alertBox.init();
					namerBox.textField.setText(selections.lastElement().getName());
					namerBox.textField.setCursorPositionZero();
				}
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
	
	/** Screen should not close when an alertbox is open. **/
	@Override
	public boolean func_231178_ax__()
	{
		return alertBox == null;
	}
	
	@Override
	public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	 {
		Minecraft mc = Minecraft.getInstance();
		FontRenderer fontRenderer = mc.fontRenderer;
		MainWindow res = mc.getMainWindow();
		
		//Styling
		int bodyMargin = GuiStyle.Gui.bodyMargin();
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		float listWidth = 0.7f;
		
		String worldName = " - " + Recording.getCurrentWorldName(mc);
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport body = new GuiViewport(all);
		body.left = body.top = bodyMargin; body.right -= bodyMargin; body.bottom -= bodyMargin;
		GuiViewport listBody = new GuiViewport(body);
		listBody.left = listBody.top = margin; 
		listBody.right = (int) ((listBody.getParent().getWidth() - margin * 2) * listWidth);
		listBody.bottom -= margin;
		GuiViewport list = new GuiViewport(listBody);
		list.left = margin;
		list.right -= margin;
		list.top = margin + mc.fontRenderer.FONT_HEIGHT + margin;
		list.bottom -= margin;
		GuiViewport actionsBody = new GuiViewport(body);
		actionsBody.left = listBody.right + margin; actionsBody.top = margin;
		actionsBody.right -= margin;
		GuiViewport actions = new GuiViewport(actionsBody);
		actions.left = margin; actions.top = margin + mc.fontRenderer.FONT_HEIGHT + margin;
		actions.right -= margin;
		actions.bottom = actions.top + 2 * (smallMargin + shortButtonHeight);
		actionsBody.bottom = actions.bottom + margin;
		GuiViewport asideBody = new GuiViewport(body);
		asideBody.left = listBody.right + margin; asideBody.top = actionsBody.bottom + margin;
		asideBody.right -= margin;
		asideBody.bottom -= margin;
		GuiViewport aside = new GuiViewport(asideBody);
		aside.left = margin; aside.top = margin + mc.fontRenderer.FONT_HEIGHT + margin;
		aside.right -= margin;
		aside.bottom = aside.top + (smallMargin + shortButtonHeight) * 3;
		GuiViewport desc = new GuiViewport(asideBody);
		desc.left = margin;
		desc.right -= margin;
		desc.top = aside.bottom;
		desc.bottom -= margin;
		
		GL11.glPushMatrix();
		{
			all.pushMatrix(false);
			{
				GraphicsHelper.renderBackground();
				GraphicsHelper.drawCenteredString(mc.fontRenderer, I18n.format("gui.save_recording"), all.getWidth() / 2, bodyMargin / 2 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				GraphicsHelper.fill(0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(false);
			{	
				GraphicsHelper.fillGradient(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				GraphicsHelper.drawString(mc.fontRenderer, I18n.format("gui.save_recording.record_history") + worldName, margin, margin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			listViewport.drawScreen(mouseX, mouseY, partialTicks, list);
			
			actionsBody.pushMatrix(true);
			{
				GraphicsHelper.fillGradient(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				GraphicsHelper.drawString(mc.fontRenderer, I18n.format("gui.save_recording.actions"), margin, margin, 0xFFFFFFFF);
			}
			actionsBody.popMatrix();
			
			actions.pushMatrix(false);
			{
				for(int i = 0; i < 2; i++)
				{
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(shortButtonHeight);
					button.setY((shortButtonHeight + smallMargin) * i);
					button.renderButton(mouseX, mouseY, partialTicks);
					button.setActive(!EventHandler.recordHistory.isEmpty());
				}
			}
			actions.popMatrix();
			
			asideBody.pushMatrix(true);
			{
				GraphicsHelper.fillGradient(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				
				String subTitle = I18n.format("gui.load_recording.information");
				subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
				GraphicsHelper.drawString(mc.fontRenderer, subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{
				for(int i = 2; i < buttons.size(); i++)
				{
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(shortButtonHeight);
					button.setY((shortButtonHeight + smallMargin) * (i - 2));
					button.renderButton( mouseX, mouseY, partialTicks);
					button.setActive(!selections.isEmpty());
				}
				if(selections.size() > 1) buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove_selected"));
				else buttons.get(3).func_238482_a_(new TranslationTextComponent("gui.save_recording.remove"));
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttons.get(4);
				boolean flag = EventHandler.session.isSessionActive();
				open.setActive(!flag && open.active());
				
				if(open.isHovered() && flag && !selections.isEmpty()) 
				{	
					String warning = I18n.format("gui.save_recording.warn.cannot_open_while_recording_or_playing");
					GraphicsHelper.renderToolTip(this, warning, mouseX, mouseY);
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
						GraphicsHelper.drawString(mc.fontRenderer, lines[i], 0, mc.fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
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
	
	/**When Save As New button is pressed from alert box**/
	private void saveNew()
	{
		if(alertBox instanceof GuiOverrideBox)
		{
			alertBox.setShouldClose(true);
			//Create namer box
			GuiNamerBox namerBox = new GuiNamerBox(I18n.format("gui.save_recording.name_recording"), this, (String s) -> { return s.length() > 0; } , this::save);
			alertBox = namerBox;
			alertBox.init();
			namerBox.textField.setText(selections.lastElement().getName());
			namerBox.textField.setCursorPositionZero();	
		}
	}
	
	private void save(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save(false, true, false);
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
		init();
	}
	
	private void override()
	{
		selections.lastElement().save(false, true, true);
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
		init();
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
