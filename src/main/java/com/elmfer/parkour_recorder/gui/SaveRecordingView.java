package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Stack;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.ConfirmationWindow;
import com.elmfer.parkour_recorder.gui.window.NamingWindow;
import com.elmfer.parkour_recorder.gui.window.OverrideWindow;
import com.elmfer.parkour_recorder.gui.window.OverrideWindow.SaveToNew;
import com.elmfer.parkour_recorder.gui.window.Window;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.RecordingSession;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public class SaveRecordingView extends Widget implements IMenuTabView
{
	private ButtonListView buttonsView = new ButtonListView();
	private Stack<Recording> selections = new Stack<Recording>();
	
	private Button saveLastButton = new Button(I18n.get("com.elmfer.save_last"));
	private Button clearHistoryButton = new Button(I18n.get("com.elmfer.clear_history"));
	private Button saveButton = new Button(I18n.get("com.elmfer.save"));
	private Button removeButton = new Button(I18n.get("com.elmfer.remove_selected"));
	private Button openButton = new Button(I18n.get("com.elmfer.open"));
	
	public SaveRecordingView()
	{
		super();
		
		addWidgets(saveLastButton, clearHistoryButton, saveButton, removeButton, openButton, buttonsView);
		
		clearHistoryButton.setAction((b) ->
		{
			String title = I18n.get("com.elmfer.clear_history_?");
			Window.createWindow(v -> {return new ConfirmationWindow(title, this::clearHistory);});
		});
		removeButton.setAction(b ->
		{
			String title = 1 < selections.size() ? I18n.get("com.elmfer.remove_selected_?") : I18n.get("com.elmfer.should_remove_?");
			Window.createWindow(v -> {return new ConfirmationWindow(title, this::remove);});
		});
		openButton.setAction(b ->
		{
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			EventHandler.hud.fadedness = 200;
			Minecraft.getInstance().setScreen(null);
		});
		saveLastButton.setAction(b ->
		{
			selectLast();
			onSave(b);
		});
		saveButton.setAction(this::onSave);
		
		updateRecordList();
	}
	
	protected void buttonListCallback(Button button)
	{
		if(!UIinput.isCtrlPressed()) selections.clear();
		
		int i = 0;
		for(Widget w : buttonsView.getChildrenWidgets())
		{
			if(!(w instanceof Button)) continue;
			Button b = (Button) w;
			
			b.setHighlighted(selections.contains(EventHandler.recordHistory.get(i++)));
			b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
		}
		
		int buttonIndex = buttonsView.getChildrenWidgets().indexOf(button);
		if(selections.contains(EventHandler.recordHistory.get(buttonIndex)))
		{
			selections.remove(EventHandler.recordHistory.get(buttonIndex));
			button.setHighlighted(false);
			if(!selections.isEmpty())
			{
				Button b = (Button) buttonsView.getChildrenWidgets().get(EventHandler.recordHistory.indexOf(selections.lastElement()));
				b.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			}
		}
		else
		{
			button.setHighlighted(true);
			button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			selections.push(EventHandler.recordHistory.get(buttonIndex));
		}
	}
	
	@Override
	 public void draw()
	 {
		if(!isVisible()) return;
		
		//Styling
		int bodyMargin = GuiStyle.Gui.bodyMargin();
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		float listWidth = 0.7f;
		
		String worldName = " - " + Recording.getCurrentWorldName(Minecraft.getInstance());
		
		Viewport all = new Viewport();
		Viewport body = new Viewport(all);
		body.left = body.top = bodyMargin; body.right -= bodyMargin; body.bottom -= bodyMargin;
		Viewport listBody = new Viewport(body);
		listBody.left = listBody.top = margin; 
		listBody.right = (int) ((listBody.getParent().getWidth() - margin * 2) * listWidth);
		listBody.bottom -= margin;
		Viewport list = new Viewport(listBody);
		list.left = margin;
		list.right -= margin;
		list.top = margin + UIrender.getStringHeight() + margin;
		list.bottom -= margin;
		Viewport actionsBody = new Viewport(body);
		actionsBody.left = listBody.right + margin; actionsBody.top = margin;
		actionsBody.right -= margin;
		Viewport actions = new Viewport(actionsBody);
		actions.left = margin; actions.top = margin + UIrender.getStringHeight() + margin;
		actions.right -= margin;
		actions.bottom = actions.top + 2 * (smallMargin + shortButtonHeight);
		actionsBody.bottom = actions.bottom + margin;
		Viewport asideBody = new Viewport(body);
		asideBody.left = listBody.right + margin; asideBody.top = actionsBody.bottom + margin;
		asideBody.right -= margin;
		asideBody.bottom -= margin;
		Viewport aside = new Viewport(asideBody);
		aside.left = margin; aside.top = UIrender.getStringHeight() + margin * 2;
		aside.right -= margin;
		aside.bottom = aside.top + (smallMargin + shortButtonHeight) * 3;
		Viewport desc = new Viewport(asideBody);
		desc.left = margin;
		desc.right -= margin;
		desc.top = aside.bottom;
		desc.bottom -= margin;
		
		body.pushMatrix(false);
		{
			UIrender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
		}
		body.popMatrix();
		
		listBody.pushMatrix(false);
		{	
			UIrender.drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
			UIrender.drawString(I18n.get("com.elmfer.record_history") + worldName, margin, margin, 0xFFFFFFFF);
		}
		listBody.popMatrix();
		
		buttonsView.setViewport(list);
		buttonsView.draw();
		
		actionsBody.pushMatrix(true);
		{
			UIrender.drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
			UIrender.drawString(I18n.get("com.elmfer.actions"), margin, margin, 0xFFFFFFFF);
		}
		actionsBody.popMatrix();
		
		actions.pushMatrix(false);
		{
			Button[] buttons = { saveLastButton, clearHistoryButton };
			int i = 0;
			for(Button button : buttons)
			{
				button.width = aside.getWidth();
				button.height = shortButtonHeight;
				button.y = (shortButtonHeight + smallMargin) * i++;
				button.draw();
				button.setEnabled(!EventHandler.recordHistory.isEmpty());
			}
		}
		actions.popMatrix();
		
		asideBody.pushMatrix(true);
		{
			UIrender.drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
			
			String subTitle = I18n.get("com.elmfer.information");
			subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
			UIrender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
		}
		asideBody.popMatrix();
		
		aside.pushMatrix(false);
		{
			Button[] buttons = { saveButton, removeButton, openButton };
			int i = 2;
			for(Button button : buttons)
			{
				button.width = aside.getWidth();
				button.height = shortButtonHeight;
				button.y = (shortButtonHeight + smallMargin) * (i++ - 2);
				button.draw();
				button.setEnabled(!selections.isEmpty());
			}
			
			if(selections.size() > 1) removeButton.setText(I18n.get("com.elmfer.remove_selected"));
			else removeButton.setText(I18n.get("com.elmfer.remove"));
		}
		aside.popMatrix();
		
		all.pushMatrix(false);
		{
			boolean flag = EventHandler.session.isSessionActive();
			openButton.setEnabled(!flag && openButton.isEnabled());
			
			if(openButton.isHovered() && flag && !selections.isEmpty()) 
			{	
				String warning = I18n.get("com.elmfer.cannot_open_while_recording_or_playing");
				UIrender.drawHoveringText(warning, UIinput.getUICursorX(), UIinput.getUICursorY());
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
					UIrender.drawString(lines[i], 0, UIrender.getStringHeight() * i, 0xFFFFFFFF);
				}
			}
			desc.popMatrix();
		}
	 }
	
	private void override()
	{
		selections.lastElement().save(false, true, true);
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
		updateRecordList();
	}
	
	private void selectLast()
	{
		selections.clear();
		for(int i = 0; i < buttonsView.getChildrenWidgets().size(); i++)
		{
			Button b = (Button) buttonsView.getChildrenWidgets().get(i);
			b.setHighlighted(selections.contains(EventHandler.recordHistory.get(i)));; 
			b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));;
		}
		
		selections.push(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
		Button button = (Button) buttonsView.getChildrenWidgets().get(EventHandler.recordHistory.indexOf(selections.lastElement()));
		button.setHighlighted(true);
		button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
	}
	
	private void onSave(Button button)
	{
		//If recording was originally opened from a file, prompt overriding alert box
		if(selections.lastElement().getOriginalFile() != null)
		{
			//When Save As New button is pressed from alert box
			SaveToNew saveNewCallback = () ->
			{
				NamingWindow namerBox = (NamingWindow) Window.createWindow(v ->
				{
					return new NamingWindow(I18n.get("com.elmfer.name_recording"), (String s) -> { return s.length() > 0; } , this::save);
				});
				namerBox.getTextField().setText(selections.lastElement().getName());
				namerBox.getTextField().setCursorAtEnd(false);
				namerBox.getTextField().setFocused(true);
			};
			
			//Create Override box
			String boxMessage = I18n.get("com.elmfer.recording_was_loaded_from") + ":\n" + selections.lastElement().getOriginalFile().getName();
			Window.createWindow(v -> 
			{
				return new OverrideWindow(I18n.get("com.elmfer.override_recording_?"), boxMessage, this::override, saveNewCallback);
			});
		}
		else
		{
			//Create namer box
			NamingWindow namerBox = (NamingWindow) Window.createWindow(v ->
			{
				return new NamingWindow(I18n.get("com.elmfer.name_recording"), (String s) -> { return s.length() > 0; } , this::save);
			});
			namerBox.getTextField().setText(selections.lastElement().getName());
			namerBox.getTextField().setCursorAtEnd(false);
			namerBox.getTextField().setFocused(true);
		}
	}
	
	public void refresh()
	{
		updateRecordList();
	}
	
	private void save(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save(false, true, false);
		EventHandler.recordHistory.remove(selections.lastElement());
		selections.pop();
		Window.closeWindows();
		updateRecordList();
	}
	
	private void remove()
	{
		for(int i = 0; i < selections.size(); i++)
			EventHandler.recordHistory.remove(selections.get(i));
		selections.clear();
		updateRecordList();
	}
	
	private void clearHistory()
	{
		EventHandler.recordHistory.clear();
		EventHandler.session.cleanUp();
		selections.clear();
		if(!EventHandler.session.isSessionActive())
			EventHandler.session = new RecordingSession();
		updateRecordList();
	}
	
	private void updateRecordList()
	{
		buttonsView.getChildrenWidgets().forEach(b -> b.close());
		buttonsView.getChildrenWidgets().clear();
		for(int i = 0; i < EventHandler.recordHistory.size(); i++)
		{
			Button button = new Button(EventHandler.recordHistory.get(i).getName());
			button.setHighlighted(selections.contains(EventHandler.recordHistory.get(i)));
			button.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
			button.setAction(this::buttonListCallback);
			button.setzLevel(0);
			buttonsView.addWidgets(button);
		}
		
		if(!selections.isEmpty())
		{
			int latestSelection = EventHandler.recordHistory.indexOf(selections.lastElement());
			if(latestSelection > -1)
			{
				Button button = (Button) buttonsView.getChildrenWidgets().get(latestSelection);
				button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			}
		}
		
		if(selections.size() > 1) removeButton.setText(I18n.get("com.elmfer.remove_selected"));
		else removeButton.setText(I18n.get("com.elmfer.remove"));
	}

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseClicked(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseReleased(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyPressed(int keyCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCharTyped(int charTyped)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseScroll(int scrollAmount)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(SidedUpdate side)
	{
		// TODO Auto-generated method stub
		
	}
}
