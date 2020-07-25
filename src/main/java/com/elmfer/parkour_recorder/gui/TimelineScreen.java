package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import com.elmfer.alasen.util.Property;
import com.elmfer.alasen.util.Timeline;
import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.parkour.ParkourFrame;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.PlaybackViewerEntity;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class TimelineScreen extends Screen
{
	private TimelineViewport timelineViewport = new TimelineViewport(this);
	protected SessionType session = SessionType.NONE;
	private State state = State.PAUSED;
	protected Timeline timeline;
	protected PlaybackViewerEntity viewer = null;
	
	public TimelineScreen()
	{
		super(new TranslationTextComponent("gui.timeline"));
		
		if(EventHandler.session instanceof PlaybackSession)
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			
			this.session = session.isPlaying() ? SessionType.PLAYBACK : SessionType.REPLAY;
		}
		
		if(session.isActive())
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			
			double duration = (session.recording.size() - 1) / 20.0;
			timeline = new Timeline(duration);
			Property framePos = new Property("framePos", 0.0, session.recording.size() - 1);
			timeline.addProperties(framePos);
			timeline.setTimePos(session.recording.startingFrame / 20.0);
		}
		else
		{
			timeline = new Timeline(1.0);
			Property framePos = new Property("framePos", 0.0, 1.0);
			timeline.addProperties(framePos);
		}
	}
	
	@Override
	public void init()
	{
		timelineViewport.init();
		if(session == SessionType.REPLAY)
		{
			viewer = new PlaybackViewerEntity();
			minecraft.setRenderViewEntity(viewer);
		}
		
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.start_here"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.load"), this::actionPerformed));
		
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "rewind_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "play_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "pause_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "start_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "end_button"));
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonID = buttons.indexOf(button);
		
		switch (buttonID) {
		case 0:
			onClose();
			if(session == SessionType.REPLAY)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				
				session.startAt((int) timeline.getProperty("framePos").getValue());
			}
			break;
		case 1:
			onClose();
			minecraft.displayGuiScreen(new LoadRecordingScreen());
			break;
		case 2:
			timeline.rewind();
			break;
		case 3:
			timeline.play();
			break;
		case 4:
			timeline.pause();
			break;
		case 5:
			timeline.stop();
			timeline.setFracTime(0.0);
			break;
		case 6:
			timeline.stop();
			timeline.setFracTime(1.0);
		}
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		timeline.tick();
		
		if(session == SessionType.PLAYBACK)
			timeline.setTimePos((((PlaybackSession) EventHandler.session).getFrameNumber() + mc.getRenderPartialTicks()) / 20.0);
		if(session == SessionType.REPLAY)
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			
			int currentFrame = (int) timeline.getProperty("framePos").getValue();
			float partialFrame = (float) (timeline.getProperty("framePos").getValue() - currentFrame);
			ParkourFrame prevFrame = session.recording.get(currentFrame);
			ParkourFrame frame = session.recording.get(Math.min(session.recording.size() - 1, currentFrame + 1));
			
			
			viewer.setState(frame, prevFrame, partialFrame);
		}
		
		int smallMargin = GuiStyle.Gui.smallMargin();
		int margin = (int) GuiStyle.Gui.margin();
		int gradientHeight = 15;
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport timelineBar = new GuiViewport(all);
		timelineBar.top = all.bottom - (smallMargin * 2 + buttonHeight * 2);
		GuiViewport taskBar = new GuiViewport(all);
		taskBar.bottom = smallMargin * 2 + buttonHeight;
		GuiViewport title = new GuiViewport(taskBar);
		title.top = title.left = smallMargin;
		title.bottom -= smallMargin;
		title.right = title.left + mc.fontRenderer.getStringWidth(I18n.format("gui.timeline")) + title.getHeight() - mc.fontRenderer.FONT_HEIGHT;
		GuiViewport controls = new GuiViewport(timelineBar);
		controls.left = controls.top = smallMargin;
		controls.bottom -= smallMargin;
		controls.right = controls.left + controls.getHeight();
		GuiViewport timeline = new GuiViewport(timelineBar);
		timeline.top = smallMargin;
		timeline.left = controls.right + margin;
		timeline.bottom -= smallMargin;
		timeline.right -= smallMargin;
		
		timelineBar.pushMatrix(false);
		{
			fillGradient(0, 0, taskBar.getWidth(), -gradientHeight, fade1, fade2);
			fill(0, 0, timelineBar.getWidth(), timelineBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			renderControls(controls, mouseX, mouseY, partialTicks);
			timelineViewport.drawScreen(mouseX, mouseY, partialTicks, timeline);
			renderTaskbar(title, taskBar, mouseX, mouseY, partialTicks);
		}
		timelineBar.popMatrix();
		
		title.pushMatrix(false);
		{
			fill(0, 0, title.getWidth(), title.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.5f));
			drawCenteredString(font, I18n.format("gui.timeline"), title.getWidth() / 2, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
		title.popMatrix();
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		minecraft.setRenderViewEntity(minecraft.player);
	}
	
	@Override
	public boolean isPauseScreen()
	{
	      return false;
	}
	
	private void renderTaskbar(GuiViewport title, GuiViewport taskBar, int mouseX, int mouseY, float partialTicks)
	{
		int buttonWidth = 100;
		boolean recordingIsLoaded = EventHandler.session instanceof PlaybackSession;
		
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		int fadeHeight = GuiStyle.Gui.fadeHeight();
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		
		taskBar.pushMatrix(false);
		{
			fillGradient(0, taskBar.getHeight(), taskBar.getWidth(), taskBar.getHeight() + fadeHeight, fade1, fade2);
			fill(0, 0, taskBar.getWidth(), taskBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			vLine(title.right + margin, smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			String recordingName = recordingIsLoaded ? ((PlaybackSession) EventHandler.session).recording.getName() : I18n.format("gui.timeline.no_recording_is_loaded");
			int nameColor = recordingIsLoaded ? 0xFFFFFFFF : getIntColor(0.5f, 0.5f, 0.5f, 1.0f);
			
			drawString(font, recordingName, title.right + margin * 2, taskBar.getHeight() / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, nameColor);
			
			vLine(title.right + margin * 3 + font.getStringWidth(recordingName), smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			GuiButton start = (GuiButton) buttons.get(0);
			GuiButton load = (GuiButton) buttons.get(1);
			
			start.setHeight(buttonHeight);
			load.setHeight(buttonHeight);
			start.setWidth(buttonWidth);
			load.setWidth(buttonWidth);
			start.x = title.right + margin * 4 + font.getStringWidth(recordingName);
			load.x = start.x + buttonWidth + margin;
			start.y = load.y = smallMargin;
			start.active = session == SessionType.REPLAY;
			load.active = !EventHandler.session.isSessionActive();
			
			start.renderButton(mouseX, mouseY, partialTicks);
			load.renderButton(mouseX, mouseY, partialTicks);
		}
		taskBar.popMatrix();
	}
	
	private void renderControls(GuiViewport controls, int mouseX, int mouseY, float partialTicks)
	{	
		int smallMargin = GuiStyle.Gui.smallMargin();
		
		controls.pushMatrix(false);
		{
			GuiButton rewind = (GuiButton) buttons.get(2);
			GuiButton play = (GuiButton) buttons.get(3);
			GuiButton pause = (GuiButton) buttons.get(4);
			GuiButton atBegginning = (GuiButton) buttons.get(5);
			GuiButton atEnd = (GuiButton) buttons.get(6);
			GuiButton[] buttonControls = {rewind, play, pause, atBegginning, atEnd};
			
			int size = controls.getWidth() / 2 - smallMargin / 2;
			
			for(GuiButton control : buttonControls)
			{
				control.setWidth(size);
				control.setHeight(size);
			}
			
			if(!(timeline.isPaused() || timeline.hasStopped()))
			{
				switch(timeline.getState())
				{
				case FORWARD:
					state = State.PLAYING;
					break;
				case REVERSE:
					state = State.REWINDING;
					break;
				}
			}
			else state = State.PAUSED;
			
			pause.setWidth(controls.getWidth());
			if(state.isActive()) { pause.visible = true; play.visible = rewind.visible = false; }
			else { pause.visible = false; play.visible = rewind.visible = true; }
			atBegginning.y = atEnd.y = controls.getHeight() - size;
			play.x = atEnd.x = controls.getWidth() - size;
			
			if(session == SessionType.PLAYBACK || session == SessionType.NONE)
				for(GuiButton control : buttonControls) control.active = false;
			
			for(GuiButton control : buttonControls)
				control.renderButton(mouseX, mouseY, partialTicks);
		}
		controls.popMatrix();
	}
	
	protected static enum SessionType
	{
		REPLAY, PLAYBACK, NONE;
		
		public boolean isActive()
		{
			return this == REPLAY || this == PLAYBACK;
		}
	}
	
	private static enum State
	{
		PLAYING, REWINDING, PAUSED;
		
		public boolean isActive()
		{
			return this == PLAYING || this == REWINDING;
		}
	}
}
