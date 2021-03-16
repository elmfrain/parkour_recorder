package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Comparator;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.animation.Easing;
import com.elmfer.parkour_recorder.animation.Property;
import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.TimelineViewport.TimeStampFormat;
import com.elmfer.parkour_recorder.gui.alertbox.GuiAlertBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiConfirmationBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiNamerBox;
import com.elmfer.parkour_recorder.gui.alertbox.GuiTimeFormatBox;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;
import com.elmfer.parkour_recorder.gui.widget.GuiIconifiedButton;
import com.elmfer.parkour_recorder.gui.widget.GuiSlider;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.ParkourFrame;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.ReplayViewerEntity;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TimelineScreen extends GuiScreen
{
	public static TimeStampFormat timeStampFormat = ConfigManager.loadTimeFormat();
	
	private TimelineViewport timelineViewport = new TimelineViewport(this);
	protected SessionType session = SessionType.NONE;
	private State state = State.PAUSED;
	protected Timeline timeline;
	protected ReplayViewerEntity viewer = null;
	protected Checkpoint currentCheckpoint = null;
	
	//Settings widgets
	protected GuiSettingsButton settingsButton = new GuiSettingsButton(0, 0, this::actionPerformed);
	protected GuiSlider speedSlider = new GuiSlider(0, 0, I18n.format("gui.timeline.speed") + ": ", "x", 0.01, 4.0, 1.0, true, true, this::actionPerformed);
	protected GuiButton formatSelect = new GuiButton(0, 0, I18n.format("gui.timeline.time_format"), this::actionPerformed);
	
	private GuiAlertBox alertBox = null;
	
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
		//Create viewer entity and reset the player's arm
		if(session == SessionType.REPLAY)
		{
			viewer = new ReplayViewerEntity();
			mc.setRenderViewEntity(viewer);
			mc.player.prevRenderArmYaw = mc.player.renderArmYaw = mc.player.prevRotationYaw;
			mc.player.prevRenderArmPitch = mc.player.renderArmPitch = mc.player.prevRotationPitch;
		}
		
		//Taskbar Buttons
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.start_here"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.load"), this::actionPerformed));
		
		//Replay Control Buttons
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "rewind_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "play_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "pause_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "start_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "end_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "prev_frame_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "next_frame_button"));
		
		//Settings Menu's Widget
		addButton(settingsButton);
		addButton(speedSlider);
		addButton(formatSelect);
		
		//Checkpoint Toolbar Buttons
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "add_checkpoint_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "remove_checkpoint_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "prev_checkpoint_button"));
		addButton(new GuiIconifiedButton(0, 0, this::actionPerformed, "next_checkpoint_button"));
	}
	
	protected void actionPerformed(Button button)
	{
		int buttonID = buttons.indexOf(button);
		
		switch (buttonID) {
		case 0: //Start Here
			func_231175_as__();
			if(session == SessionType.REPLAY)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				
				session.startAt((int) timeline.getProperty("framePos").getValue());
			}
			break;
		case 1: //Load Recording; Unused
			func_231175_as__();
			mc.displayGuiScreen(new LoadRecordingScreen());
			break;
		case 2: //Rewind
			timeline.rewind();
			break;
		case 3: //Play
			timeline.play();
			break;
		case 4: //Pause
			timeline.pause();
			break;
		case 5: //At beginning
			timeline.stop();
			timeline.setFracTime(0.0);
			break;
		case 6: //At end
			timeline.stop();
			timeline.setFracTime(1.0);
			break;
		case 7: //Previous Frame
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			
			//Snap timeline pos to current frame
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + oneTick / 10.0);
			
			timeline.setFracTime(timeline.getFracTime() - oneTick);
			timeline.pause();
			break;
		}
		case 8: //Next Frame
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			
			//Snap timeline pos to current frame
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + oneTick / 10.0);
			
			timeline.setFracTime(timeline.getFracTime() + oneTick);
			timeline.pause();
			break;
		}
		case 9: //Show settings menu
			GuiSettingsButton b = (GuiSettingsButton) button;
			if(b.gear.getState() == Timeline.State.REVERSE) {b.gear.play(); b.highlighed = true; }
			else {b.gear.rewind(); b.highlighed = false; }
			break;
		case 11: //Change time format
			GuiTimeFormatBox formattingBox = new GuiTimeFormatBox(this);
			formattingBox.init();
			alertBox = formattingBox;
			break;
		case 12: //Add checkpoint
			GuiNamerBox namerBox = new GuiNamerBox(I18n.format("gui.timeline.add_checkpoint"), this, (String s) -> { return true; } , this::addCheckpoint);
			namerBox.init();
			namerBox.textField.setMaxStringLength(64);
			alertBox = namerBox;
			break;
		case 13: //Remove checkpoint
			String message = I18n.format("gui.timeline.remove_checkpoint_?");
			message += currentCheckpoint.name.isEmpty() ? "" : " - " + currentCheckpoint.name;
			GuiConfirmationBox confirmBox = new GuiConfirmationBox(message, this::removeCheckpoint, this);
			confirmBox.init();
			alertBox = confirmBox;
			break;
		case 14: //Prev checkpoint
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			
			//If the frame position is greater then the current checkpoint's position, set the frame position to the checkpoint's position.
			if(currentCheckpoint.frameNumber < framePos) timeline.setTimePos(currentCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			else //Frame position is on the current checkpoint, thus goto previous checkpoint
			{
				int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
				Checkpoint prevCheckpoint = session.recording.checkpoints.get(currentCheckptIndex - 1);
				
				timeline.setTimePos(prevCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			}
			timeline.pause();
			break;
		}
		case 15: //Next checkpoint
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			
			int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
			Checkpoint nextCheckpoint = session.recording.checkpoints.get(currentCheckptIndex + 1);
			
			timeline.setTimePos(nextCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			timeline.pause();
			break;
		}
		}
	}
	
	@Override
	public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		timeline.tick();
		
		//Set frame position as the same from playback. Only occurs while playback is active
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
		
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int gradientHeight = 15;
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		int backroundColor = getIntColor(GuiStyle.Gui.backroundColor());
		
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		
		//Structering of the GUI
		GuiViewport all = new GuiViewport(res);
		GuiViewport timelineBar = new GuiViewport(all);
		timelineBar.top = all.bottom - (smallMargin * 2 + buttonHeight * 2);
		GuiViewport taskBar = new GuiViewport(all);
		taskBar.bottom = smallMargin * 2 + buttonHeight;
		GuiViewport title = new GuiViewport(taskBar);
		title.top = title.left = smallMargin;
		title.bottom -= smallMargin;
		title.right = title.left + mc.fontRenderer.getStringWidth(I18n.format("gui.timeline")) + title.getHeight() - mc.fontRenderer.FONT_HEIGHT;
		GuiViewport timeline = new GuiViewport(timelineBar);
		timeline.top = smallMargin;
		timeline.left = smallMargin;
		timeline.bottom -= smallMargin;
		timeline.right -= smallMargin;
		GuiViewport controls = new GuiViewport(all); //Playback controls
		int controlsSize = (int) (timeline.getHeight() * 0.55f); 
		int controlsWidth = (controlsSize - 2 * smallMargin) * 7 + 7 * smallMargin + margin;
		controls.top = timelineBar.top - controlsSize - smallMargin;
		controls.bottom = controls.top + controlsSize;
		controls.left = all.getWidth() / 2 - controlsWidth / 2;
		controls.right = controls.left + controlsWidth;
		GuiViewport checkpointControls = new GuiViewport(all);
		int checkpointControlsWidth = (controlsSize - 2 * smallMargin) * 4 + 4 * smallMargin + margin;
		checkpointControls.top = controls.top;
		checkpointControls.bottom = controls.bottom;
		checkpointControls.left = smallMargin;
		checkpointControls.right = checkpointControls.left + checkpointControlsWidth;
		GuiViewport settingsBody = new GuiViewport(all);
		settingsBody.left = controls.right + smallMargin;
		settingsBody.top = (int) (timelineBar.top - (GuiStyle.Gui.buttonHeight() * 2 + smallMargin * 4) * settingsButton.gear.getProperty("height_mult").getValue());
		settingsBody.right -= smallMargin;
		settingsBody.bottom = timelineBar.top - smallMargin;
		GuiViewport settings = new GuiViewport(settingsBody);
		settings.left = settings.top = smallMargin;
		settings.right -= smallMargin;
		
		//Render the timeline bar
		timelineBar.pushMatrix(false);
		{
			GraphicsHelper.fillGradient(0, 0, taskBar.getWidth(), -gradientHeight, fade1, fade2);
			GraphicsHelper.fill(0, 0, timelineBar.getWidth(), timelineBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			renderCheckpointControls(checkpointControls, mouseX, mouseY, partialTicks);
			renderControls(controls, mouseX, mouseY, partialTicks);
			timelineViewport.drawScreen(mouseX, mouseY, partialTicks, timeline);
			renderTaskbar(title, taskBar, mouseX, mouseY, partialTicks);
			renderCheckpointStatus(taskBar, mouseX, mouseY, partialTicks);
		}
		timelineBar.popMatrix();
		
		//Render settings if it's enabled
		if(settingsBody.getHeight() > smallMargin * 2)
		{
			settingsBody.pushMatrix(false);
			{
				GraphicsHelper.fill(0, 0, settingsBody.getWidth(), settingsBody.getHeight(), backroundColor);
				settings.pushMatrix(true);
				{
					final int BUTTON_HEIGHT = 20;
					speedSlider.setWidth(settings.getWidth());
					formatSelect.setWidth(settings.getWidth());
					speedSlider.setHeight(BUTTON_HEIGHT);
					formatSelect.setHeight(BUTTON_HEIGHT);
					formatSelect.setY(BUTTON_HEIGHT + smallMargin);
					
					formatSelect.func_238482_a_(new StringTextComponent(I18n.format("gui.timeline.time_format") + ": " + timeStampFormat.NAME));
					
					speedSlider.renderButton(mouseX, mouseY, partialTicks);
					formatSelect.renderButton(mouseX, mouseY, partialTicks);
					
					this.timeline.setSpeed(speedSlider.getValue());
				}
				settings.popMatrix();
			}
			settingsBody.popMatrix();
		}
		
		//Render the title
		title.pushMatrix(false);
		{
			GraphicsHelper.fill(0, 0, title.getWidth(), title.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.5f));
			GraphicsHelper.drawCenteredString(mc.fontRenderer, I18n.format("gui.timeline"), title.getWidth() / 2, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
		title.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.drawScreen(stack, mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	}
	
	/**When gui closes.**/
	@Override
	public void func_231175_as__()
	{
		super.func_231175_as__();
		mc.setRenderViewEntity(mc.player);
		ConfigManager.saveTimeFormat(timeStampFormat);
	}
	
	@Override
	public boolean doesGamePause()
	{
	      return false;
	}
	
	/**Renders the checkpoint marker and the name of the current checkpoint, if any.**/
	private void renderCheckpointStatus(GuiViewport taskBar, int mouseX, int mouseY, float partialTicks)
	{
		if(currentCheckpoint != null)
		{
			FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
			
			//Styling Constants
			final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
			final int BACKROUND_COLOR = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			final int FADE_2 = GraphicsHelper.getIntColor(GuiStyle.Gui.fade2());
			final int FADE_WIDTH = GuiStyle.Gui.fadeHeight();
			
			//String dimensions.
			final int STRING_LENGTH = currentCheckpoint.name != null ? fontRenderer.getStringWidth(currentCheckpoint.name) : 0;
			final float STRING_HEIGHT = fontRenderer.FONT_HEIGHT * 2;

			//Render gradient backround
			int stringOffset = (int) (STRING_HEIGHT * 0.6f + SMALL_MARGIN);
			GraphicsHelper.fill(SMALL_MARGIN, taskBar.bottom + SMALL_MARGIN, STRING_LENGTH + stringOffset, (int) (taskBar.bottom + SMALL_MARGIN + STRING_HEIGHT), BACKROUND_COLOR);
			GraphicsHelper.gradientRectToRight(STRING_LENGTH + stringOffset, taskBar.bottom + SMALL_MARGIN, SMALL_MARGIN + STRING_LENGTH + FADE_WIDTH + stringOffset, (int) (taskBar.bottom + SMALL_MARGIN + STRING_HEIGHT), BACKROUND_COLOR, FADE_2);
			
			//Checkpoint color
			Vector4f color = GraphicsHelper.getFloatColor(currentCheckpoint.color);
			
			//Render the checkpoint marker
			final int shader = ShaderManager.getGUIShader();
			final int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(SMALL_MARGIN, taskBar.bottom + SMALL_MARGIN, 0.0f);
				GL11.glScalef(STRING_HEIGHT, -STRING_HEIGHT, 1.0f);
				
				GL20.glUseProgram(shader);
				GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), 1.0f);
				ModelManager.renderModel("checkpoint_icon");
				GL20.glUseProgram(prevShader);
			}
			GL11.glPopMatrix();
			
			//If checkpoint has a name, render it's name.
			if(!currentCheckpoint.name.isEmpty())
				GraphicsHelper.drawString(fontRenderer, currentCheckpoint.name, stringOffset, (int) (taskBar.bottom + SMALL_MARGIN + STRING_HEIGHT / 2 - fontRenderer.FONT_HEIGHT / 2), 0xFFFFFFFF);
		}
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
			GraphicsHelper.fillGradient(0, taskBar.getHeight(), taskBar.getWidth(), taskBar.getHeight() + fadeHeight, fade1, fade2);
			GraphicsHelper.fill(0, 0, taskBar.getWidth(), taskBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			GraphicsHelper.vLine(title.right + margin, smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			String recordingName = recordingIsLoaded ? ((PlaybackSession) EventHandler.session).recording.getName() : I18n.format("gui.timeline.no_recording_is_loaded");
			int nameColor = recordingIsLoaded ? 0xFFFFFFFF : getIntColor(0.5f, 0.5f, 0.5f, 1.0f);
			
			GraphicsHelper.drawString(mc.fontRenderer, recordingName, title.right + margin * 2, taskBar.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, nameColor);
			
			GraphicsHelper.vLine(title.right + margin * 3 + mc.fontRenderer.getStringWidth(recordingName), smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			GuiButton start = (GuiButton) buttons.get(0);
			GuiButton load = (GuiButton) buttons.get(1);
			
			start.setHeight(buttonHeight);
			load.setHeight(buttonHeight);
			start.setWidth(buttonWidth);
			load.setWidth(buttonWidth);
			start.setX(title.right + margin * 4 + mc.fontRenderer.getStringWidth(recordingName));
			load.setX(start.x() + buttonWidth + margin);
			start.setY(smallMargin); load.setY(smallMargin);
			start.setActive(session == SessionType.REPLAY);
			load.setVisible(false);
			
			start.renderButton(mouseX, mouseY, partialTicks);
			load.renderButton(mouseX, mouseY, partialTicks);
		}
		taskBar.popMatrix();
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
	
	/**Renders the Checkpoint toolbar**/
	private void renderCheckpointControls(GuiViewport checkpointControls, int mouseX, int mouseY, float partialTicks)
	{
		int framePos = (int) timeline.getProperty("framePos").getValue();
		
		//Styling Constants
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
		final int MARGIN = GuiStyle.Gui.margin();
		
		//Render toolbar
		checkpointControls.pushMatrix(false);
		{
			GuiButton addCheckpoint = (GuiButton) buttons.get(12);
			GuiButton removeCheckpoint = (GuiButton) buttons.get(13);
			GuiButton prevCheckpoint = (GuiButton) buttons.get(14);
			GuiButton nextCheckpoint = (GuiButton) buttons.get(15);
			GuiButton[] buttonControls = {addCheckpoint, removeCheckpoint, prevCheckpoint, nextCheckpoint};
			
			currentCheckpoint = null;
			addCheckpoint.setActive(this.session.isActive());
			removeCheckpoint.setActive(false);
			prevCheckpoint.setActive(false);
			nextCheckpoint.setActive(false);
			
			//Get current checkpoint and update the buttons' states
			if(EventHandler.session instanceof PlaybackSession)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				Recording recording = session.recording;
				
				for(Checkpoint c : session.recording.checkpoints)
				{
					if(framePos < c.frameNumber) break;
					currentCheckpoint = c;
				}
				
				if(currentCheckpoint != null) 
					removeCheckpoint.setActive(true);
				
				if(!recording.checkpoints.isEmpty() && framePos < recording.checkpoints.get(recording.checkpoints.size() - 1).frameNumber)
					nextCheckpoint.setActive(true);
				if(!recording.checkpoints.isEmpty() && recording.checkpoints.get(0).frameNumber < framePos)
					prevCheckpoint.setActive(true);
			}
			
			//Buttons' width and height
			int size = checkpointControls.getHeight() - SMALL_MARGIN * 2;
			
			//Set buttons's size and position
			int i = 0;
			for(GuiButton button : buttonControls)
			{
				button.setWidth(size);
				button.setHeight(size);
				button.setY(SMALL_MARGIN);
				button.setX(SMALL_MARGIN + (size + SMALL_MARGIN) * i);
				i++;
			}
			prevCheckpoint.setX(removeCheckpoint.x() + removeCheckpoint.getWidth() + MARGIN);
			nextCheckpoint.setX(prevCheckpoint.x() + prevCheckpoint.getWidth() + SMALL_MARGIN);
			
			//Draw backround and buttons
			GraphicsHelper.fill(0, 0, checkpointControls.getWidth(), checkpointControls.getHeight(), getIntColor(GuiStyle.Gui.backroundColor()));
			addCheckpoint.renderButton(mouseX, mouseY, partialTicks);
			removeCheckpoint.renderButton(mouseX, mouseY, partialTicks);
			prevCheckpoint.renderButton(mouseX, mouseY, partialTicks);
			nextCheckpoint.renderButton(mouseX, mouseY, partialTicks);
		}
		checkpointControls.popMatrix();
	}
	
	private void renderControls(GuiViewport controls, int mouseX, int mouseY, float partialTicks)
	{	
		//Style constants
		final int MARGIN = GuiStyle.Gui.margin();
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
		
		//Render toolbar
		controls.pushMatrix(false);
		{
			GuiButton rewind = (GuiButton) buttons.get(2);
			GuiButton play = (GuiButton) buttons.get(3);
			GuiButton pause = (GuiButton) buttons.get(4);
			GuiButton atBegginning = (GuiButton) buttons.get(5);
			GuiButton atEnd = (GuiButton) buttons.get(6);
			GuiButton settings = (GuiButton) buttons.get(9);
			GuiButton prevFrame = (GuiButton) buttons.get(7);
			GuiButton nextFrame = (GuiButton) buttons.get(8);
			GuiButton[] buttonControls = {atBegginning, prevFrame, rewind, play, nextFrame, atEnd, settings, pause};
			
			//Buttons' width and height.
			int size = controls.getHeight() - SMALL_MARGIN * 2;
			
			//Set buttons's size and position
			int i = 0;
			for(GuiButton control : buttonControls)
			{
				control.setWidth(size);
				control.setHeight(size);
				control.setY(SMALL_MARGIN);
				control.setX(i * (size + SMALL_MARGIN) + SMALL_MARGIN);
				i++;
			}
			
			//Update current replay state
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
			
			//Set the pause and settings button specific sizes and positions
			pause.setWidth(size * 2 + SMALL_MARGIN);
			if(state.isActive()) { pause.setVisible(true); play.setVisible(false); rewind.setVisible(false); }
			else { pause.setVisible(false);; play.setVisible(true); rewind.setVisible(true); }
			settings.setX(atEnd.x() + atEnd.getWidth() + MARGIN);
			pause.setX(rewind.x());
			
			//Disable all buttons (except settings) if GUI is not in Replay Mode
			if(session == SessionType.PLAYBACK || session == SessionType.NONE)
				for(GuiButton control : buttonControls) control.setActive(false);
			settings.setActive(true);
			
			//Render backround and buttons
			GraphicsHelper.fill(0, 0, controls.getWidth(), controls.getHeight(), getIntColor(GuiStyle.Gui.backroundColor()));
			GL11.glDisable(GL11.GL_CULL_FACE);
			for(GuiButton control : buttonControls)
				control.renderButton(mouseX, mouseY, partialTicks);	
			settings.renderButton(mouseX, mouseY, partialTicks);
		}
		controls.popMatrix();
	}
	
	private void removeCheckpoint()
	{
		//Remove checkpoint
		PlaybackSession session = (PlaybackSession) EventHandler.session;
		session.recording.checkpoints.remove(currentCheckpoint);
		
		//Automatically save the recording
		session.recording.save(true, false, true);
	}
	
	private void addCheckpoint(String checkpointName)
	{
		final Comparator<Checkpoint> SORTER = (Checkpoint c1, Checkpoint c2) -> 
		{
			if(c1.frameNumber < c2.frameNumber) return -1;
			else if(c2.frameNumber < c1.frameNumber) return 1;
			return 0;
		};
		
		PlaybackSession session = (PlaybackSession) EventHandler.session;
		int framePos = (int) timeline.getProperty("framePos").getValue();
		
		//Remove any checkpoints with the same frame number.
		session.recording.checkpoints.removeIf((Checkpoint c) -> { return c.frameNumber == framePos; });
		
		Checkpoint checkpoint = new Checkpoint(checkpointName, framePos);
		
		//Generate random color and assign it to new checkpoint
		Random rand = new Random();
		float red = rand.nextFloat() * 0.5f + 0.3f;
		float green = rand.nextFloat() * 0.5f + 0.3f;
		float blue = rand.nextFloat() * 0.5f + 0.3f;
		checkpoint.color = GraphicsHelper.getIntColor(red, green, blue, 1.0f);
		
		//Add checkpoint and sort the list
		session.recording.checkpoints.add(checkpoint);
		session.recording.checkpoints.sort(SORTER);
		
		//Automatically save the recording
		session.recording.save(true, false, true);
	}
	
	protected static class GuiSettingsButton extends GuiIconifiedButton
	{
		Timeline gear = new Timeline(0.2);
		
		public GuiSettingsButton(int x, int y, IPressable pressedCallback) {
			super(x, y, pressedCallback, "settings_button");
			gear.addProperties(new Property("rotation", 0.0, -180.0, Easing.INOUT_QUAD));
			gear.addProperties(new Property("height_mult", 0.0, 1.0, Easing.INOUT_QUAD));
			gear.rewind();
			gear.stop();
		}
		
		@Override
		public void renderButton(int mouseX, int mouseY, float partialTicks)
		{
			if(visible())
			{
				animation.tick();
				gear.tick();
				if(isHovered()) { animation.queue("hovered"); animation.play(); animation.apply();}
				else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
				if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
				else { animation.queue("highlight"); animation.rewind(); animation.apply();}
				if(!active()) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
				
				preRender(mouseX, mouseY, partialTicks);
				
				Vector3f c = new Vector3f(0.0f, 0.0f, 0.0f);
				Vector3f hoveredcolor = new Vector3f(0.3f, 0.3f, 0.3f);
				hoveredcolor.mul((float) animation.getTimeline("hovered").getFracTime());
				Vector3f highlightColor = highlightTint.copy();
				highlightColor.mul((float) (animation.getTimeline("highlight").getFracTime() * 0.6));
				c.add(hoveredcolor);
				c.add(highlightColor);
				int color = getIntColor(c, 0.2f);
				
				int j = 14737632;
				if (!active())
	                j = 10526880;
	            else if (isHovered())
	                j = 16777120;
				
				GraphicsHelper.fill(x(), y(), x() + getWidth(), y() + height(), color);
				
				int shader = ShaderManager.getGUIShader();
				int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
				float smallMargin = GuiStyle.Gui.smallMargin() * 2;
				float modelSize = height() - smallMargin * 2;
				Vector4f color1 = GraphicsHelper.getFloatColor(j);
				color1.setW(1.0f);
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				GL11.glColor3f(color1.getX(), color1.getY(), color1.getZ());
				
				GL11.glPushMatrix();
				{
					GL11.glTranslatef(x() + getWidth() / 2, y() + height() / 2, 0.0f);
					GL11.glScalef(modelSize, -modelSize, 1.0f);
					GL11.glRotatef((float) gear.getProperty("rotation").getValue(), 0, 0, 1);
					
					GL20.glUseProgram(shader);
					GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color1.getX(), color1.getY(), color1.getZ(), color1.getW());
					ModelManager.renderModel(modelName);
					GL20.glUseProgram(prevShader);
				}
				GL11.glPopMatrix();
			}
		}
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
