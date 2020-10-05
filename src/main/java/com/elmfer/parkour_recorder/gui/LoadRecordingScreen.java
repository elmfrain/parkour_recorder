package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.alertbox.GuiAlertBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiConfirmationBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiNamerBox;
import com.elmfer.parkour_recorder.gui.widgets.GuiButton;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

public class LoadRecordingScreen extends GuiScreen {

	private List<Recording> records = null;
	private Stack<Recording> selections = new Stack<Recording>();
	private ButtonListViewport listViewport = new ButtonListViewport(this::buttonListCallback);
	private GuiAlertBox alertBox = null;

	@Override
	public void initGui()
	{
		GuiButton.currentZLevel = 0;
		if(records == null) records = Arrays.asList(Recording.loadSaves());
		
		int smallMargin = GuiStyle.Gui.smallMargin();
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		buttonList.clear();
		listViewport.buttonList.clear();
		
		for(int i = 0; i < records.size(); i++)
		{
			GuiButton b = new GuiButton(i, 0, 0, records.get(i).getName());
			b.highlighed = selections.contains(records.get(i)); 
			b.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
			listViewport.buttonList.add(b);
		}
		if(!selections.isEmpty())
		{
			int latestSelection = records.indexOf(selections.lastElement());
			listViewport.buttonList.get(latestSelection).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}

		addButton(new GuiButton(0, 0, 0, I18n.format("gui.load_recording.open")));
		addButton(new GuiButton(1, 0, (buttonHeight + smallMargin), I18n.format("gui.load_recording.delete")));
			if(selections.size() > 1) buttonList.get(1).displayString = I18n.format("gui.load_recording.delete_all");
			else buttonList.get(1).displayString = I18n.format("gui.load_recording.delete");
		addButton(new GuiButton(2, 0, (buttonHeight + smallMargin) * 2, I18n.format("gui.load_recording.rename")));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		int buttonId = button.id;
		switch(buttonId)
		{
		case 0:
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getMinecraft().displayGuiScreen(null);
			break;
		case 1:
			String title = selections.size() > 1 ? I18n.format("gui.load_recording.delete_all_?") : I18n.format("gui.load_recording.should_delete_?");
			GuiAlertBox deleteBox = new GuiConfirmationBox(title, this::delete, this);
			alertBox = deleteBox;
			alertBox.initGui();
			break;
		case 2:
			GuiNamerBox renameBox = new GuiNamerBox(I18n.format("gui.load_recording.rename_recording"), this, (String s) -> { return s.length() > 0; } , this::rename);
			alertBox = renameBox;
			alertBox.initGui();
			renameBox.textField.setText(selections.lastElement().getName());
			renameBox.textField.setCursorPositionZero();
			break;
		}
	}
	
	protected void buttonListCallback(GuiButton button)
	{
		GuiButton guiButton = (GuiButton) button;
		
		if(!isCtrlKeyDown()) selections.clear();
		
		for(int i = 0; i < listViewport.buttonList.size(); i++)
		{
			GuiButton b = listViewport.buttonList.get(i);
			b.highlighed = selections.contains(records.get(i)); 
			b.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
		}
		
		if(selections.contains(records.get(listViewport.getIndex(guiButton))))
		{
			selections.remove(records.get(listViewport.getIndex(guiButton)));
			guiButton.highlighed = false;
			if(!selections.isEmpty())
				listViewport.buttonList.get(records.indexOf(selections.lastElement())).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}
		else
		{
			guiButton.highlighed = true;
			guiButton.highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
			selections.push(records.get(listViewport.getIndex(guiButton)));
		}
	}
	
	@Override
	public void keyTyped(char keyTyped, int keyID)
	{
		if(alertBox != null) { alertBox.keyTyped(keyTyped, keyID); return; }
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
		ScaledResolution res = new ScaledResolution(mc);
		FontRenderer fontRenderer = mc.fontRenderer;
		
		//Styling
		int bodyMargin = GuiStyle.Gui.bodyMargin();
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();
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
		list.top = margin + fontRenderer.FONT_HEIGHT + margin;
		list.bottom -= margin;
		GuiViewport asideBody = new GuiViewport(body);
		asideBody.top = margin; asideBody.bottom -= margin;
		asideBody.left = listBody.right + margin;
		asideBody.right -= margin;
		GuiViewport aside = new GuiViewport(asideBody);
		aside.left = margin;
		aside.right -= margin;
		aside.top = margin + fontRenderer.FONT_HEIGHT + margin;
		aside.bottom = aside.top + (shortButtonHeight + smallMargin) * 3;
		GuiViewport desc = new GuiViewport(asideBody);
		desc.left = margin;
		desc.right -= margin;
		desc.top = aside.bottom;
		desc.bottom -= margin;
		
		GlStateManager.pushMatrix();
		{
			int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
			
			all.pushMatrix(false);
			{
				drawDefaultBackground();
				drawCenteredString(fontRenderer, I18n.format("gui.load_recording"), all.getWidth() / 2, bodyMargin / 2 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				drawRect(0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				drawString(mc.fontRenderer, I18n.format("gui.load_recording.list") + worldName, margin, margin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			listViewport.drawScreen(mouseX, mouseY, partialTicks, list);
			
			asideBody.pushMatrix(true);
			{
				drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);
				
				String subTitle = I18n.format("gui.load_recording.information");
				subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
				drawString(mc.fontRenderer, subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(true);
			{				
				for(int i = 0; i < 3; i++)
				{
					GuiButton button = (GuiButton) buttonList.get(i);
					button.setWidth(aside.getWidth());
					button.height = shortButtonHeight;
					button.y = (shortButtonHeight + smallMargin) * i;
					button.drawButton(mc, mouseX, mouseY, partialTicks);
					button.enabled = !selections.isEmpty();
				}
				if(selections.size() > 1) buttonList.get(1).displayString = I18n.format("gui.load_recording.delete_all");
				else buttonList.get(1).displayString = I18n.format("gui.load_recording.delete");
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttonList.get(0);
				boolean flag = EventHandler.session.isSessionActive();
				open.enabled = !flag && open.enabled;
				
				if(open.isMouseOver() && flag && !selections.isEmpty()) 
				{	
					String warning = I18n.format("gui.load_recording.warn.cannot_open_while_recording_or_playing");
					drawHoveringText(warning, mouseX, mouseY);
					RenderHelper.disableStandardItemLighting();
				}
			}
			all.popMatrix();
			
			if(selections.size() > 0)
			{
				desc.pushMatrix(true);
				{
					String[] lines = selections.lastElement().toString().split("\n");
					for(int i = 0; i < lines.length; i++)
					{
						drawString(mc.fontRenderer, lines[i], 0, fontRenderer.FONT_HEIGHT * i, 0xFFFFFFFF);
					}
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
		
		all.pushMatrix(false);
	}
	
	private void rename(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save(false, true, true);
		buttonList.clear();
		initGui();
	}
	
	private void delete()
	{
		alertBox.setShouldClose(true);
		alertBox = null;
		
		for(int i = 0; i < selections.size(); i++)
			Recording.deleteSave(selections.get(i));
		records = Arrays.asList(Recording.loadSaves());
		selections.clear();
		
		selections.clear();
		buttonList.clear();
		initGui();
	}
}
