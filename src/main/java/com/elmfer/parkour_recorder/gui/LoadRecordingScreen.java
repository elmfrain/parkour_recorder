package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class LoadRecordingScreen extends Screen {

	private List<Recording> records = null;
	private Stack<Recording> selections = new Stack<Recording>();
	private ButtonListViewport listViewport = new ButtonListViewport(this);
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
		if(records == null) records = Arrays.asList(Recording.loadSaves());
		
		int smallMargin = GuiStyle.Gui.smallMargin();
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		buttons.clear();
		children.clear();
		listViewport.clearButtons();
		
		
		for(int i = 0; i < records.size(); i++)
		{
			GuiButton b = new GuiButton(0, 0, records.get(i).getName(), this::buttonListCallback);
			b.highlighed = selections.contains(records.get(i)); 
			b.highlightTint = new Vector3f(0.0f, 0.3f, 0.0f);
			listViewport.addButton(b);
		}
		if(!selections.isEmpty())
		{
			int latestSelection = records.indexOf(selections.lastElement());
			listViewport.buttonList.get(latestSelection).highlightTint = new Vector3f(0.0f, 0.5f, 0.0f);
		}

		addButton(new GuiButton(0, 0, I18n.format("gui.load_recording.open"), this::actionPerformed));
		addButton(new GuiButton(0, (buttonHeight + smallMargin), I18n.format("gui.load_recording.delete"), this::actionPerformed));
			if(selections.size() > 1) buttons.get(1).setMessage(I18n.format("gui.load_recording.delete_all"));
			else buttons.get(1).setMessage(I18n.format("gui.load_recording.delete"));
		addButton(new GuiButton(0, (buttonHeight + smallMargin) * 2, I18n.format("gui.load_recording.rename"), this::actionPerformed));
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
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getInstance().displayGuiScreen(null);
			break;
		case 1:
			String title = selections.size() > 1 ? I18n.format("gui.load_recording.delete_all_?") : I18n.format("gui.load_recording.should_delete_?");
			GuiAlertBox deleteBox = new GuiConfirmationBox(title, this::delete, this);
			alertBox = deleteBox;
			alertBox.init();
			break;
		case 2:
			GuiNamerBox renameBox = new GuiNamerBox(I18n.format("gui.load_recording.rename_recording"), this, (String s) -> { return s.length() > 0; } , this::rename);
			renameBox.textField.setText(selections.lastElement().getName());
			alertBox = renameBox;
			alertBox.init();
			break;
		}
	}
	
	protected void buttonListCallback(Button button)
	{
		GuiButton guiButton = (GuiButton) button;
		
		if(!hasControlDown()) selections.clear();
		
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
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		if(alertBox != null) alertBox.keyPressed(keyID, scancode, mods);
		return super.keyPressed(keyID, scancode, mods);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
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
		
		RenderSystem.pushMatrix();
		{
			int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.0f);
			
			all.pushMatrix(false);
			{
				renderBackground();
				drawCenteredString(fontRenderer, I18n.format("gui.load_recording"), all.getWidth() / 2, bodyMargin / 2 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			}
			all.popMatrix();
			
			body.pushMatrix(false);
			{
				fill(0, 0, body.getWidth(), body.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.3f));
			}
			body.popMatrix();
			
			listBody.pushMatrix(true);
			{
				fillGradient(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
				drawString(mc.fontRenderer, I18n.format("gui.load_recording.list") + worldName, margin, margin, 0xFFFFFFFF);
			}
			listBody.popMatrix();
			
			listViewport.drawScreen(mouseX, mouseY, partialTicks, list);
			
			asideBody.pushMatrix(true);
			{
				fillGradient(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);
				
				String subTitle = I18n.format("gui.load_recording.information");
				subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
				drawString(mc.fontRenderer, subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();
			
			aside.pushMatrix(true);
			{				
				for(int i = 0; i < 3; i++)
				{
					GuiButton button = (GuiButton) buttons.get(i);
					button.setWidth(aside.getWidth());
					button.setHeight(shortButtonHeight);
					button.y = (shortButtonHeight + smallMargin) * i;
					button.renderButton(mouseX, mouseY, partialTicks);
					button.active = !selections.isEmpty();
				}
				if(selections.size() > 1) buttons.get(1).setMessage(I18n.format("gui.load_recording.delete_all"));
				else buttons.get(1).setMessage(I18n.format("gui.load_recording.delete"));
			}
			aside.popMatrix();
			
			all.pushMatrix(false);
			{
				GuiButton open = (GuiButton) buttons.get(0);
				boolean flag = EventHandler.session.isSessionActive();
				open.active = !flag && open.active;
				
				if(open.isHovered() && flag && !selections.isEmpty()) 
				{	
					String warning = I18n.format("gui.load_recording.warn.cannot_open_while_recording_or_playing");
					renderTooltip(warning, mouseX, mouseY);
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
		RenderSystem.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.render(mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
		
		all.pushMatrix(false);
	}
	
	private void rename(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save();
		buttons.clear();
		children.clear();
		init();
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
		buttons.clear();
		children.clear();
		init();
	}
}
