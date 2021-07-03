package com.elmfer.parkour_recorder.gui;

import org.lwjgl.input.Keyboard;

import com.elmfer.parkour_recorder.animation.Smoother;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.TimeFormatSelectionWindow;
import com.elmfer.parkour_recorder.gui.window.Window;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class MenuScreen extends UIscreen
{
	private static final float NAV_BAR_HEIGHT = 15.0f;
	private static final float MARGIN = 3.0f;
	
	private Tab loadTab = new Tab();
	private Tab saveTab = new Tab();
	private Tab timelineTab = new Tab();
	private LoadRecordingView loadView = new LoadRecordingView();
	private SaveRecordingView saveView = new SaveRecordingView();
	private TimelineView timelineView  = new TimelineView();
	
	private Smoother tabSelectorX1 = new Smoother();
	private Smoother tabSelectorX2 = new Smoother();
	
	private static int pageSelected = 0; //0 = load, 1 = save, 2 = timeline
	
	public MenuScreen()
	{
		super();
		
		loadTab.setText(I18n.format("com.elmfer.load"));
		saveTab.setText(I18n.format("com.elmfer.save"));
		timelineTab.setText(I18n.format("com.elmfer.timeline"));
		
		loadTab.width = UIrender.getStringWidth(loadTab.getText()) + 10;
		saveTab.width = UIrender.getStringWidth(saveTab.getText()) + 10;
		timelineTab.width = UIrender.getStringWidth(timelineTab.getText()) + 10;
		
		loadTab.setAction((t) -> 
		{
			loadView.setVisible(true);
			saveView.setVisible(false);
			timelineView.setVisible(false);
			loadView.refresh();
			
			tabSelectorX1.grab(t.x - MARGIN);
			tabSelectorX2.grab(t.x + t.width + MARGIN);
			pageSelected = 0;
		});
		saveTab.setAction((t) ->
		{
			loadView.setVisible(false);
			saveView.setVisible(true);
			timelineView.setVisible(false);
			saveView.refresh();
			
			tabSelectorX1.grab(t.x - MARGIN);
			tabSelectorX2.grab(t.x + t.width + MARGIN);
			pageSelected = 1;
		});
		timelineTab.setAction((t) ->
		{
			loadView.setVisible(false);
			saveView.setVisible(false);
			timelineView.setVisible(true);
			timelineView.refresh();
			
			tabSelectorX1.grab(t.x - MARGIN);
			tabSelectorX2.grab(t.x + t.width + MARGIN);
			pageSelected = 2;
		});
		timelineView.x = NAV_BAR_HEIGHT + MARGIN;
		
		tabSelectorX1.setSpeed(20);
		tabSelectorX2.setSpeed(20);
		tabSelectorX1.setValueAndGrab(0);
		tabSelectorX2.setValueAndGrab(20);
		initFirstTab();
	}
	
	
	@Override
	public void initGui()
	{
		//drawTabs();
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		timelineView.onExit();
	}
	
	private void initFirstTab()
	{
		GlStateManager.colorMask(false, false, false, false);
		drawTabs();
		GlStateManager.colorMask(true, true, true, true);
		
		switch (pageSelected)
		{
		case 0:
			loadTab.getAction().onAction(loadTab);
			break;
		case 1:
			saveTab.getAction().onAction(saveTab);
			break;
		case 2:
			timelineTab.getAction().onAction(timelineTab);
			break;
		default:
			loadTab.getAction().onAction(loadTab);
			break;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(0, 0, 0);
			loadView.draw();
			saveView.draw();
			timelineView.draw();
		}
		GlStateManager.popMatrix();
		
		drawNavigationBar();
		Window.drawWindows();
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
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
		super.onKeyPressed(keyCode);
		
		if(Widget.getCurrentZLevel() == 0)
		{
			if(keyCode == Keyboard.KEY_T)
			{
				Window.createWindow((v) -> { return new TimeFormatSelectionWindow(); });
			}
		}
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
	
	private void drawNavigationBar()
	{
		float uiWidth = UIrender.getUIwidth();
		
		UIrender.drawRect(0, 0, uiWidth, NAV_BAR_HEIGHT, -1308622848);
		UIrender.drawRect(0, NAV_BAR_HEIGHT, uiWidth, NAV_BAR_HEIGHT + MARGIN, -16749608);
		UIrender.drawGradientRect(0, NAV_BAR_HEIGHT + MARGIN, uiWidth, NAV_BAR_HEIGHT * 2 + MARGIN, 1275068416, 0);
		
		UIrender.drawString(Anchor.MID_RIGHT, I18n.format("com.elmfer.parkour_recorder"), uiWidth - MARGIN, NAV_BAR_HEIGHT / 2, 0xFFFFFFFF);
		
		drawTabs();
	}
	
	private void drawTabs()
	{
		UIrender.drawGradientRect((float) tabSelectorX1.getValue(), 0, (float) tabSelectorX2.getValue(), NAV_BAR_HEIGHT, -16741121, -16749608);
		
		Tab[] tabs = {loadTab, saveTab, timelineTab};
		float tabPosition = NAV_BAR_HEIGHT;
		for(Tab tab : tabs)
		{
			tab.height = NAV_BAR_HEIGHT;
			tab.x = tabPosition;
			tab.draw();
			
			tabPosition += tab.width + NAV_BAR_HEIGHT;
			if(tab.justPressed())
			{
				tabSelectorX1.grab(tab.x - MARGIN);
				tabSelectorX2.grab(tab.x + tab.width + MARGIN);
			}
		}
		
		if(tabSelectorX1.getValue() == 0.0) //If selector has defaulted to the Load tab yet
		{
			tabSelectorX1.grab(loadTab.x - MARGIN);
			tabSelectorX2.grab(loadTab.x + loadTab.width + MARGIN);
		}
	}
	
	private static class Tab extends Button
	{
		@Override
		public void draw()
		{
			updateModelviewAndViewportState();
			
			if(isVisible())
			{
				int j = getTextColor();
				
				UIrender.drawRect(x, y, x + width, y + height, 1275068416);
				
				UIrender.drawString(Anchor.CENTER, getText(), x + width / 2, y + height / 2, j);	
			}
		}
	}
}
