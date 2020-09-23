package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.io.IOException;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class SaveRecordingScreen extends GuiScreen
{
	private ButtonListViewport listViewport = new ButtonListViewport(this::buttonListCallback);
	private GuiAlertBox alertBox = null;
	private Stack<Recording> selections = new Stack<Recording>();
	
	@Override
	public void initGui()
	{
		GuiButton.currentZLevel = 0;
		buttonList.clear();
		listViewport.buttonList.clear();
		for(int i = 0; i < EventHandler.recordHistory.size(); i++)
		{
			GuiButton button = new GuiButton(i, 0, 0, EventHandler.recordHistory.get(i).getName());
			button.highlighed = selections.contains(EventHandler.recordHistory.get(i)); 
			button.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
			listViewport.buttonList.add(button);
		}
		if(!selections.isEmpty())
		{
			int latestSelection = EventHandler.recordHistory.indexOf(selections.lastElement());
			listViewport.buttonList.get(latestSelection).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}
		
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.save_recording.save_last")));
		addButton(new GuiButton(1, 0, 0, I18n.format("gui.save_recording.erase_history")));
		addButton(new GuiButton(2, 0, 0, I18n.format("gui.save_recording.save")));
		addButton(new GuiButton(3, 0, 0, I18n.format("gui.save_recording.remove")));
			if(selections.size() > 3) buttonList.get(1).displayString = I18n.format("gui.save_recording.remove_selected");
			else buttonList.get(3).displayString = I18n.format("gui.save_recording.remove");
		addButton(new GuiButton(4, 0, 0, I18n.format("gui.save_recording.open")));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		int buttonId = button.id;
		switch(buttonId)
		{
		case 1:
			GuiAlertBox clearBox = new GuiConfirmationBox(I18n.format("gui.save_recording.clear_history_?"), this::clearHistory, this);
			alertBox = clearBox;
			alertBox.initGui();
			break;
		case 3:
			String title = 1 < selections.size() ? I18n.format("gui.save_recording.remove_selected_?") : I18n.format("gui.save_recording.should_remove_?");
			GuiAlertBox removeBox = new GuiConfirmationBox(title, this::remove, this);
			alertBox = removeBox;
			alertBox.initGui();
			break;
		case 4:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getMinecraft().displayGuiScreen(null);
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
				alertBox.initGui();
			}
		}
	}
	
	protected void buttonListCallback(GuiButton button)
	{
		GuiButton guiButton = (GuiButton) button;
		
		if(!isCtrlKeyDown()) selections.clear();
		
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
	public void keyTyped(char keyTyped, int keyID)
	{
		if(alertBox != null) alertBox.keyTyped(keyTyped, keyID);
		try { super.keyTyped(keyTyped, keyID); } catch (IOException e) {}
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
		listViewport.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	 public void drawScreen(int mouseX, int mouseY, float partialTicks)
	 {
		FontRenderer fontRenderer = mc.fontRenderer;
		ScaledResolution res = new ScaledResolution(mc);
		
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
		
		GlStateManager.pushMatrix();
		{
			all.pushMatrix(false);
			{
				drawDefaultBackground();
				drawCenteredString(mc.fontRenderer, I18n.format("gui.save_recording"), all.getWidth() / 2, bodyMargin / 2 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
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
				drawString(mc.fontRenderer, I18n.format("gui.save_recording.record_history") + worldName, margin, margin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			listViewport.drawScreen(mouseX, mouseY, partialTicks, list);
			
			actionsBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				drawString(mc.fontRenderer, I18n.format("gui.save_recording.actions"), margin, margin, 0xFFFFFFFF);
			}
			actionsBody.popMatrix();
			
			actions.pushMatrix(false);
			{
				for(int i = 0; i < 2; i++)
				{
					GuiButton button = (GuiButton) buttonList.get(i);
					button.setWidth(aside.getWidth());
					button.height = shortButtonHeight;
					button.y = (shortButtonHeight + smallMargin) * i;
					button.drawButton(mc, mouseX, mouseY, partialTicks);
					button.enabled = !EventHandler.recordHistory.isEmpty();
				}
			}
			actions.popMatrix();
			
			asideBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				
				String subTitle = I18n.format("gui.load_recording.information");
				subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
				drawString(mc.fontRenderer, subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(false);
			{
				for(int i = 2; i < buttonList.size(); i++)
				{
					GuiButton button = (GuiButton) buttonList.get(i);
					button.setWidth(aside.getWidth());
					button.height = shortButtonHeight;
					button.y = (shortButtonHeight + smallMargin) * (i - 2);
					button.drawButton(mc, mouseX, mouseY, partialTicks);
					button.enabled = !selections.isEmpty();
				}
				if(selections.size() > 3) buttonList.get(1).displayString = I18n.format("gui.save_recording.remove_selected");
				else buttonList.get(3).displayString = I18n.format("gui.save_recording.remove");
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttonList.get(4);
				boolean flag = EventHandler.session.isSessionActive();
				open.enabled = !flag && open.enabled;
				
				if(open.isMouseOver() && flag && !selections.isEmpty()) 
				{	
					String warning = I18n.format("gui.save_recording.warn.cannot_open_while_recording_or_playing");
					drawHoveringText(warning, mouseX, mouseY);
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
						drawString(mc.fontRenderer, lines[i], 0, mc.fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
				}
				desc.popMatrix();
			}
			
			if(alertBox != null)
			{
				alertBox.drawScreen(mouseX, mouseY, partialTicks);
				if(alertBox.shouldClose()) alertBox = null;
			}
		}
		GlStateManager.popMatrix();
		
		all.pushMatrix(false);
	 }
	
	private void save(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save();
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
		initGui();
	}
	
	private void remove()
	{
		for(int i = 0; i < selections.size(); i++)
			EventHandler.recordHistory.remove(selections.get(i));
		selections.clear();
		initGui();
	}
	
	private void clearHistory()
	{
		EventHandler.recordHistory.clear();
		EventHandler.session.cleanUp();
		selections.clear();
		if(!EventHandler.session.isSessionActive())
			EventHandler.session = new RecordingSession();
		initGui();
	}
}
