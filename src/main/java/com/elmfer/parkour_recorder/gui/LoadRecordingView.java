package com.elmfer.parkour_recorder.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.ConfirmationWindow;
import com.elmfer.parkour_recorder.gui.window.NamingWindow;
import com.elmfer.parkour_recorder.gui.window.Window;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.SessionHUD;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public class LoadRecordingView extends Widget implements IMenuTabView
{

	private List<Recording> records = null;
	private Stack<Recording> selections = new Stack<Recording>();
	private ButtonListView buttonsView = new ButtonListView();

	private Button openButton = new Button(I18n.get("com.elmfer.open"));
	private Button deleteButton = new Button(I18n.get("com.elmfer.delete"));
	private Button renameButton = new Button(I18n.get("com.elmfer.rename"));

	public LoadRecordingView()
	{
		super();

		addWidgets(openButton, deleteButton, renameButton, buttonsView);

		openButton.setAction(b ->
		{
			EventHandler.session.cleanUp();
			EventHandler.session = new PlaybackSession(selections.lastElement());
			SessionHUD.fadedness = 200;
			Minecraft.getInstance().setScreen(null);
		});
		deleteButton.setAction(b ->
		{
			String title = selections.size() > 1 ? I18n.get("com.elmfer.delete_all_?")
					: I18n.get("com.elmfer.should_delete_?");
			Window.createWindow(v ->
			{
				return new ConfirmationWindow(title, this::delete);
			});
		});
		renameButton.setAction(b ->
		{
			String title = I18n.get("com.elmfer.rename_recording");
			NamingWindow renameBox = (NamingWindow) Window.createWindow(v ->
			{
				return new NamingWindow(title, (String s) ->
				{
					return s.length() > 0;
				}, this::rename);
			});
			renameBox.getTextField().setText(selections.lastElement().getName());
			renameBox.getTextField().setCursorAtEnd(false);
			renameBox.getTextField().setFocused(true);
		});

		updateRecordList();
	}

	protected void buttonListCallback(Button button)
	{
		if (!UIinput.isCtrlPressed())
			selections.clear();

		int i = 0;
		for (Widget w : buttonsView.getChildrenWidgets())
		{
			if (!(w instanceof Button))
				continue;
			Button b = (Button) w;

			b.setHighlighted(selections.contains(records.get(i++)));
			b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
		}

		int buttonIndex = buttonsView.getChildrenWidgets().indexOf(button);
		if (selections.contains(records.get(buttonIndex)))
		{
			selections.remove(records.get(buttonIndex));
			button.setHighlighted(false);
			if (!selections.isEmpty())
			{
				Button b = (Button) buttonsView.getChildrenWidgets().get(records.indexOf(selections.lastElement()));
				b.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			}
		} else
		{
			button.setHighlighted(true);
			button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			selections.push(records.get(buttonIndex));
		}
	}

	@Override
	public void draw()
	{
		if(!isVisible()) return;
		
		// Styling
		int bodyMargin = GuiStyle.Gui.bodyMargin();
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();
		float listWidth = 0.7f;

		String worldName = " - " + Recording.getCurrentWorldName(Minecraft.getInstance());

		Viewport all = new Viewport();
		Viewport body = new Viewport(all);
		body.left = body.top = bodyMargin;
		body.right -= bodyMargin;
		body.bottom -= bodyMargin;
		Viewport listBody = new Viewport(body);
		listBody.left = listBody.top = margin;
		listBody.right = (int) ((listBody.getParent().getWidth() - margin * 2) * listWidth);
		listBody.bottom -= margin;
		Viewport list = new Viewport(listBody);
		list.left = margin;
		list.right -= margin;
		list.top = margin + UIrender.getStringHeight() + margin;
		list.bottom -= margin;
		Viewport asideBody = new Viewport(body);
		asideBody.top = margin;
		asideBody.bottom -= margin;
		asideBody.left = listBody.right + margin;
		asideBody.right -= margin;
		Viewport aside = new Viewport(asideBody);
		aside.left = margin;
		aside.right -= margin;
		aside.top = margin + UIrender.getStringHeight() + margin;
		aside.bottom = aside.top + (shortButtonHeight + smallMargin) * 3;
		Viewport desc = new Viewport(asideBody);
		desc.left = margin;
		desc.right -= margin;
		desc.top = aside.bottom;
		desc.bottom -= margin;

		int fade1 = 1711276032;
		int fade2 = 0;

		body.pushMatrix(false);
		{
			UIrender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
		}
		body.popMatrix();

		listBody.pushMatrix(true);
		{
			UIrender.drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
			UIrender.drawString(I18n.get("com.elmfer.list") + worldName, margin, margin, 0xFFFFFFFF);
		}
		listBody.popMatrix();

		buttonsView.setViewport(list);
		buttonsView.draw();

		asideBody.pushMatrix(true);
		{
			UIrender.drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);

			String subTitle = I18n.get("com.elmfer.information");
			subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")"
					: subTitle;
			UIrender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
		}
		asideBody.popMatrix();

		aside.pushMatrix(true);
		{
			Button[] buttons = { openButton, deleteButton, renameButton };
			int i = 0;
			for(Button button : buttons)
			{
				button.width = aside.getWidth();
				button.height = shortButtonHeight;
				button.y = (shortButtonHeight + smallMargin) * i++;
				button.draw();
				button.setEnabled(!selections.isEmpty());
			}
			
			if(selections.size() > 1) deleteButton.setText(I18n.get("com.elmfer.delete_selected"));
			else deleteButton.setText(I18n.get("com.elmfer.delete"));
		}
		aside.popMatrix();

		all.pushMatrix(false);
		{
			boolean flag = EventHandler.session.isSessionActive();
			openButton.setEnabled(!flag && openButton.isEnabled());

			if (openButton.isHovered() && flag && !selections.isEmpty())
			{
				String warning = I18n.get("com.elmfer.cannot_open_while_recording_or_playing");
				UIrender.drawHoveringText(warning, UIinput.getUICursorX(), UIinput.getUICursorY());
			}
		}
		all.popMatrix();

		if (!selections.isEmpty() && desc.getHeight() > 0)
		{
			desc.pushMatrix(true);
			{
				String[] lines = selections.lastElement().toString().split("\n");
				for (int i = 0; i < lines.length; i++)
				{
					UIrender.drawString(lines[i], 0, UIrender.getStringHeight() * i, 0xFFFFFFFF);
				}
			}
			desc.popMatrix();
		}
	}

	public void refresh()
	{
		updateRecordList();
	}
	
	private void rename(String newName)
	{
		selections.lastElement().rename(newName);
		selections.lastElement().save(false, true, true);

		updateRecordList();
	}

	private void delete()
	{
		for (int i = 0; i < selections.size(); i++)
			Recording.deleteSave(selections.get(i));

		records = Arrays.asList(Recording.loadSaves());
		selections.clear();

		updateRecordList();
	}

	private void updateRecordList()
	{
		records = Arrays.asList(Recording.loadSaves());

		buttonsView.getChildrenWidgets().forEach(b -> b.close());
		buttonsView.getChildrenWidgets().clear();

		for (int i = 0; i < records.size(); i++)
		{
			Button button = new Button(records.get(i).getName());
			button.setHighlighted(selections.contains(records.get(i)));
			;
			button.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
			;
			button.setAction(this::buttonListCallback);
			button.setzLevel(0);
			buttonsView.addWidgets(button);
		}
		
		if (!selections.isEmpty())
		{
			int latestSelection = records.indexOf(selections.lastElement());
			if(latestSelection > -1)
			{
				Button button = (Button) buttonsView.getChildrenWidgets().get(latestSelection);
				button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
			}
		}
		
		if(selections.size() > 1) deleteButton.setText(I18n.get("com.elmfer.delete_selected"));
		else deleteButton.setText(I18n.get("com.elmfer.delete"));
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
