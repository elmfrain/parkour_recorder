package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.Comparator;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.animation.Easing;
import com.elmfer.parkour_recorder.animation.Property;
import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.NumberLineView.TimeStampFormat;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.UIrender.Direction;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Slider;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.gui.window.ConfirmationWindow;
import com.elmfer.parkour_recorder.gui.window.NamingWindow;
import com.elmfer.parkour_recorder.gui.window.TimeFormatSelectionWindow;
import com.elmfer.parkour_recorder.gui.window.Window;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.KeyInputHUD;
import com.elmfer.parkour_recorder.parkour.ParkourFrame;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.parkour.ReplayViewerEntity;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class TimelineView extends Widget implements IMenuTabView
{
	public static TimeStampFormat timeStampFormat = ConfigManager.loadTimeFormat();

	private static final Minecraft mc = Minecraft.getInstance();

	private NumberLineView timelineViewport = new NumberLineView(this);
	protected SessionType session = SessionType.NONE;
	private State state = State.PAUSED;
	protected Timeline timeline;
	protected ReplayViewerEntity viewer = null;
	protected Checkpoint currentCheckpoint = null;

	protected SettingsButton settingsButton = new SettingsButton();
	protected Slider speedSlider = new Slider(I18n.format("com.elmfer.speed"));
	protected Button formatSelectButton = new Button(I18n.format("com.elmfer.time_format"));
	private Button startHereButton = new Button(I18n.format("com.elmfer.start_here"));
	private Button rewindButton = new Button();
	private Button playButton = new Button();
	private Button pauseButton = new Button();
	private Button startButton = new Button();
	private Button endButton = new Button();
	private Button previousFrameButton = new Button();
	private Button nextFrameButton = new Button();
	private Button addCheckpointButton = new Button();
	private Button removeCheckpointButton = new Button();
	private Button previousCheckpointButton = new Button();
	private Button nextCheckpointButton = new Button();

	private int frameSkipCountDown = 0;

	public TimelineView()
	{
		speedSlider.setAmount(0.248120301);
		setButtonsIcons();
		setButtonsActions();
		addWidgets(settingsButton, speedSlider, formatSelectButton, startHereButton, rewindButton, playButton,
				pauseButton, startButton, endButton, previousFrameButton, nextFrameButton, addCheckpointButton,
				removeCheckpointButton, previousCheckpointButton, nextCheckpointButton, timelineViewport);
	}

	private void setButtonsIcons()
	{
		settingsButton.setIcon("settings_button");
		rewindButton.setIcon("rewind_button");
		playButton.setIcon("play_button");
		pauseButton.setIcon("pause_button");
		startButton.setIcon("start_button");
		endButton.setIcon("end_button");
		previousFrameButton.setIcon("prev_frame_button");
		nextFrameButton.setIcon("next_frame_button");
		addCheckpointButton.setIcon("add_checkpoint_button");
		removeCheckpointButton.setIcon("remove_checkpoint_button");
		previousCheckpointButton.setIcon("prev_checkpoint_button");
		nextCheckpointButton.setIcon("next_checkpoint_button");
	}

	private void setButtonsActions()
	{
		startHereButton.setAction(b ->
		{
			if (session == SessionType.REPLAY)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				session.startAt((int) timeline.getProperty("framePos").getValue());
			}
			mc.displayGuiScreen(null);
		});
		settingsButton.setAction(b ->
		{
			Timeline gear = settingsButton.gear;
			if (gear.getState() == Timeline.State.REVERSE)
			{
				gear.play();
				b.setHighlighted(true);
			} else
			{
				gear.rewind();
				b.setHighlighted(b.isPressed());
			}
		});
		rewindButton.setAction(b -> timeline.rewind());
		playButton.setAction(b -> timeline.play());
		pauseButton.setAction(b -> timeline.pause());
		startButton.setAction(b ->
		{
			timeline.stop();
			timeline.setFracTime(0.0);
		});
		endButton.setAction(b ->
		{
			timeline.stop();
			timeline.setFracTime(1.0);
		});
		previousFrameButton.setAction(b ->
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);

			// Snap timeline pos to current frame
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + oneTick / 10.0);

			timeline.setFracTime(timeline.getFracTime() - oneTick);
			timeline.pause();
		});
		nextFrameButton.setAction(b ->
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);

			// Snap timeline pos to current frame
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + oneTick / 10.0);

			timeline.setFracTime(timeline.getFracTime() + oneTick);
			timeline.pause();
		});
		addCheckpointButton.setAction(b ->
		{
			Window.createWindow(v ->
			{
				return new NamingWindow(I18n.format("com.elmfer.add_checkpoint"), (String s) ->
				{
					return true;
				}, this::addCheckpoint);
			});
			// TODO: namerBox.setMaxTextLength(64);
		});
		removeCheckpointButton.setAction(b ->
		{
			Window.createWindow(v ->
			{
				String message = I18n.format("com.elmfer.remove_checkpoint_?");
				message += currentCheckpoint.name.isEmpty() ? "" : " - " + currentCheckpoint.name;

				return new ConfirmationWindow(message, this::removeCheckpoint);
			});
		});
		previousCheckpointButton.setAction(b ->
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			int framePos = (int) timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);

			// If the frame position is greater then the current checkpoint's position, set
			// the frame position to the checkpoint's position.
			if (currentCheckpoint.frameNumber < framePos)
				timeline.setTimePos(currentCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			else // Frame position is on the current checkpoint, thus goto previous checkpoint
			{
				int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
				Checkpoint prevCheckpoint = session.recording.checkpoints.get(currentCheckptIndex - 1);

				timeline.setTimePos(prevCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			}
			timeline.pause();
		});
		nextCheckpointButton.setAction(b ->
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);

			int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
			Checkpoint nextCheckpoint = session.recording.checkpoints.get(currentCheckptIndex + 1);

			timeline.setTimePos(nextCheckpoint.frameNumber / 20.0 + oneTick / 100.0);
			timeline.pause();
		});
		formatSelectButton.setAction(b ->
		{
			Window.createWindow(v ->
			{
				return new TimeFormatSelectionWindow();
			});
		});
	}

	@Override
	public void draw()
	{
		if (!isVisible())
			return;

		timeline.tick();

		// Set frame position as the same from playback. Only occurs while playback is
		// active
		if (session == SessionType.PLAYBACK)
		{
			timeline.setTimePos(
					(((PlaybackSession) EventHandler.session).getFrameNumber() + UIrender.getPartialTicks()) / 20.0);
		} else if (session == SessionType.REPLAY) // During Replay Mode
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;

			int currentFrame = (int) timeline.getProperty("framePos").getValue();
			float partialFrame = (float) (timeline.getProperty("framePos").getValue() - currentFrame);
			ParkourFrame prevFrame = session.recording.get(currentFrame);
			ParkourFrame frame = session.recording.get(Math.min(session.recording.size() - 1, currentFrame + 1));

			viewer.setState(frame, prevFrame, partialFrame); // Set the viewer's state.
		}

		// Styling Constants
		final int margin = GuiStyle.Gui.margin();
		final int smallMargin = GuiStyle.Gui.smallMargin();
		final int gradientHeight = 15;
		final int buttonHeight = GuiStyle.Gui.buttonHeight();
		final int backroundColor = getIntColor(GuiStyle.Gui.backroundColor());
		final int fade1 = getIntColor(GuiStyle.Gui.fade1());
		final int fade2 = getIntColor(GuiStyle.Gui.fade2());

		// Structering of the GUI
		Viewport all = new Viewport();
		Viewport timelineBar = new Viewport(all);
		timelineBar.top = all.bottom - (smallMargin * 2 + buttonHeight * 2);
		Viewport taskBar = new Viewport(all);
		taskBar.top = x;
		taskBar.bottom = 15 + x;
		Viewport timeline = new Viewport(timelineBar);
		timeline.top = smallMargin;
		timeline.left = smallMargin;
		timeline.bottom -= smallMargin;
		timeline.right -= smallMargin;
		Viewport controls = new Viewport(all); // Playback controls
		int controlsSize = (int) (timeline.getHeight() * 0.55f);
		int controlsWidth = (controlsSize - 2 * smallMargin) * 7 + 7 * smallMargin + margin;
		controls.top = timelineBar.top - controlsSize - smallMargin;
		controls.bottom = controls.top + controlsSize;
		controls.left = all.getWidth() / 2 - controlsWidth / 2;
		controls.right = controls.left + controlsWidth;
		Viewport checkpointControls = new Viewport(all);
		int checkpointControlsWidth = (controlsSize - 2 * smallMargin) * 4 + 4 * smallMargin + margin;
		checkpointControls.top = controls.top;
		checkpointControls.bottom = controls.bottom;
		checkpointControls.left = smallMargin;
		checkpointControls.right = checkpointControls.left + checkpointControlsWidth;
		Viewport settingsBody = new Viewport(all);
		settingsBody.left = controls.right + smallMargin;
		settingsBody.top = (int) (timelineBar.top - (GuiStyle.Gui.buttonHeight() * 2 + smallMargin * 4)
				* settingsButton.gear.getProperty("height_mult").getValue());
		settingsBody.right -= smallMargin;
		settingsBody.bottom = timelineBar.top - smallMargin;
		Viewport settings = new Viewport(settingsBody);
		settings.left = settings.top = smallMargin;
		settings.right -= smallMargin;

		renderTaskbar(taskBar);
		renderCheckpointStatus(taskBar);
		
		if(ConfigManager.showInputs())
		{
			if(session == SessionType.REPLAY)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				int currentFrame = (int) this.timeline.getProperty("framePos").getValue();
				KeyInputHUD.setParkourFrame(session.recording.get(currentFrame));
			}
			
			KeyInputHUD.posY = taskBar.bottom + smallMargin * 2;
			KeyInputHUD.posX = UIrender.getUIwidth() - smallMargin * 2 - KeyInputHUD.size;
			KeyInputHUD.render();
		}

		// Render the timeline bar
		timelineBar.pushMatrix(false);
		{
			UIrender.drawGradientRect(0, -gradientHeight, taskBar.getWidth(), 0, fade2, fade1);
			UIrender.drawRect(0, 0, timelineBar.getWidth(), timelineBar.getHeight(),
					getIntColor(0.0f, 0.0f, 0.0f, 0.7f));

			renderCheckpointControls(checkpointControls);
			renderControls(controls);
			timelineViewport.setViewport(timeline);
			timelineViewport.draw();
		}
		timelineBar.popMatrix();

		// Render settings if it's enabled
		if (settingsBody.getHeight() > smallMargin * 2)
		{
			settingsBody.pushMatrix(false);
			{
				UIrender.drawRect(0, 0, settingsBody.getWidth(), settingsBody.getHeight(), backroundColor);
				settings.pushMatrix(true);
				{
					speedSlider.width = formatSelectButton.width = (int) settings.getWidth();
					speedSlider.height = formatSelectButton.height = 20;
					formatSelectButton.y = speedSlider.height + smallMargin;

					double speed = 0.01 + speedSlider.getAmount() * 3.99;
					this.timeline.setSpeed(speed);
					speedSlider.setText(String.format("%s: %.2fx", I18n.format("com.elmfer.speed"), speed));
					formatSelectButton.setText(I18n.format("com.elmfer.time_format") + ": " + timeStampFormat.NAME);

					speedSlider.draw();
					formatSelectButton.draw();
				}
				settings.popMatrix();
			}
			settingsBody.popMatrix();
		}
	}

	public void refresh()
	{
		// Set session state
		if (EventHandler.session instanceof PlaybackSession)
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			this.session = session.isPlaying() ? SessionType.PLAYBACK : SessionType.REPLAY;
		}
		else session = SessionType.NONE;
	
		if (session.isActive()) // Setup timeline object if there is a recording loaded
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
	
			double duration = (session.recording.size() - 1) / 20.0;
			timeline = new Timeline(duration);
			Property framePos = new Property("framePos", 0.0, session.recording.size() - 1);
			timeline.addProperties(framePos);
			timeline.setTimePos(session.recording.startingFrame / 20.0);
		} else // Setup a dummy timline object
		{
			timeline = new Timeline(1.0);
			Property framePos = new Property("framePos", 0.0, 1.0);
			timeline.addProperties(framePos);
		}
		
		if (session == SessionType.REPLAY)
		{
			if (viewer == null)
				viewer = new ReplayViewerEntity();
			mc.setRenderViewEntity(viewer);
			mc.player.prevRenderArmYaw = mc.player.renderArmYaw = mc.player.prevRotationYaw;
			mc.player.prevRenderArmPitch = mc.player.renderArmPitch = mc.player.prevRotationPitch;
		}
		else mc.setRenderViewEntity(mc.player);
	}

	public void onExit()
	{
		mc.setRenderViewEntity(mc.player);

		// Save time format to config file
		ConfigManager.saveTimeFormat(timeStampFormat);
	}

	/**
	 * Renders the checkpoint marker and the name of the current checkpoint, if any.
	 **/
	private void renderCheckpointStatus(Viewport taskBar)
	{
		if (currentCheckpoint != null)
		{
			// Styling Constants
			final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
			final int BACKROUND_COLOR = 1711276032;
			final int FADE_2 = GraphicsHelper.getIntColor(GuiStyle.Gui.fade2());
			final int FADE_WIDTH = GuiStyle.Gui.fadeHeight();

			// String dimensions.
			final int STRING_LENGTH = currentCheckpoint.name != null ? UIrender.getStringWidth(currentCheckpoint.name)
					: 0;
			final float STRING_HEIGHT = UIrender.getStringHeight() * 2;

			// Render gradient backround
			int stringOffset = (int) (STRING_HEIGHT * 0.6f + SMALL_MARGIN);

			taskBar.pushMatrix(false);
			{
				GL11.glTranslatef(0, taskBar.getHeight(), 0);
				UIrender.drawRect(SMALL_MARGIN, SMALL_MARGIN, STRING_LENGTH + stringOffset,
						SMALL_MARGIN + STRING_HEIGHT, BACKROUND_COLOR);
				UIrender.drawGradientRect(Direction.TO_RIGHT, STRING_LENGTH + stringOffset, SMALL_MARGIN,
						SMALL_MARGIN + STRING_LENGTH + FADE_WIDTH + stringOffset, SMALL_MARGIN + STRING_HEIGHT,
						BACKROUND_COLOR, FADE_2);
				// Render the checkpoint marker
				UIrender.drawIcon("checkpoint_icon", SMALL_MARGIN, SMALL_MARGIN, STRING_HEIGHT,
						currentCheckpoint.color);

				// If checkpoint has a name, render it's name.
				if (!currentCheckpoint.name.isEmpty())
					UIrender.drawString(currentCheckpoint.name, stringOffset,
							SMALL_MARGIN + STRING_HEIGHT / 2 - UIrender.getStringHeight() / 2, 0xFFFFFFFF);
			}
			taskBar.popMatrix();
		}
	}

	/** Renders the top bar. **/
	private void renderTaskbar(Viewport taskBar)
	{
		int buttonWidth = 100;
		boolean recordingIsLoaded = EventHandler.session instanceof PlaybackSession;

		// Styling Constants
		final int MARGIN = GuiStyle.Gui.margin();
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();

		// Render Taskbar
		taskBar.pushMatrix(false);
		{
			UIrender.drawRect(0, 0, taskBar.getWidth(), taskBar.getHeight(), 1711276032);

			String recordingName = recordingIsLoaded ? ((PlaybackSession) EventHandler.session).recording.getName()
					: I18n.format("com.elmfer.no_recording_is_loaded");
			int nameColor = recordingIsLoaded ? 0xFFFFFFFF : -8421505;

			UIrender.drawString(Anchor.MID_LEFT, recordingName, MARGIN * 2, taskBar.getHeight() / 2, nameColor);

			UIrender.drawVerticalLine(MARGIN * 3 + UIrender.getStringWidth(recordingName), SMALL_MARGIN,
					taskBar.getHeight() - SMALL_MARGIN * 2, 2137417318);

			startHereButton.height = taskBar.getHeight();
			startHereButton.width = buttonWidth;
			startHereButton.x = MARGIN * 4 + UIrender.getStringWidth(recordingName);
			startHereButton.setEnabled(session == SessionType.REPLAY);

			startHereButton.draw();
		}
		taskBar.popMatrix();
	}

	/** Renders the Checkpoint toolbar **/
	private void renderCheckpointControls(Viewport checkpointControls)
	{
		int framePos = (int) timeline.getProperty("framePos").getValue();

		// Styling Constants
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
		final int MARGIN = GuiStyle.Gui.margin();

		// Render toolbar
		checkpointControls.pushMatrix(false);
		{
			Button[] buttons = { addCheckpointButton, removeCheckpointButton, previousCheckpointButton,
					nextCheckpointButton };

			currentCheckpoint = null;
			addCheckpointButton.setEnabled(this.session.isActive());
			removeCheckpointButton.setEnabled(false);
			previousCheckpointButton.setEnabled(false);
			nextCheckpointButton.setEnabled(false);

			// Get current checkpoint and update the buttons' states
			if (EventHandler.session instanceof PlaybackSession)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				Recording recording = session.recording;

				for (Checkpoint c : session.recording.checkpoints)
				{
					if (framePos < c.frameNumber)
						break;
					currentCheckpoint = c;
				}

				if (currentCheckpoint != null)
					removeCheckpointButton.setEnabled(true);

				if (!recording.checkpoints.isEmpty()
						&& framePos < recording.checkpoints.get(recording.checkpoints.size() - 1).frameNumber)
					nextCheckpointButton.setEnabled(true);
				if (!recording.checkpoints.isEmpty() && recording.checkpoints.get(0).frameNumber < framePos)
					previousCheckpointButton.setEnabled(true);
			}

			// Buttons' width and height
			int size = (int) checkpointControls.getHeight() - SMALL_MARGIN * 2;

			// Set buttons's size and position
			int i = 0;
			for (Button button : buttons)
			{
				button.width = size;
				button.height = size;
				button.y = SMALL_MARGIN;
				button.x = SMALL_MARGIN + (size + SMALL_MARGIN) * i;
				i++;
			}
			previousCheckpointButton.x = removeCheckpointButton.x + removeCheckpointButton.width + MARGIN;
			nextCheckpointButton.x = previousCheckpointButton.x + previousCheckpointButton.width + SMALL_MARGIN;

			// Draw backround and buttons
			UIrender.drawRect(0, 0, checkpointControls.getWidth(), checkpointControls.getHeight(),
					getIntColor(GuiStyle.Gui.backroundColor()));
			addCheckpointButton.draw();
			removeCheckpointButton.draw();
			previousCheckpointButton.draw();
			nextCheckpointButton.draw();
		}
		checkpointControls.popMatrix();
	}

	/** Renders the replay toolbar **/
	private void renderControls(Viewport controls)
	{
		// Style constants
		final int MARGIN = GuiStyle.Gui.margin();
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();

		// Render toolbar
		controls.pushMatrix(false);
		{
			Button[] buttons = { startButton, previousFrameButton, rewindButton, playButton, nextFrameButton, endButton,
					settingsButton, pauseButton };

			// Buttons' width and height.
			int size = (int) controls.getHeight() - SMALL_MARGIN * 2;

			// Set buttons's size and position
			int i = 0;
			for (Button button : buttons)
			{
				button.width = size;
				button.height = size;
				button.y = SMALL_MARGIN;
				button.x = i * (size + SMALL_MARGIN) + SMALL_MARGIN;
				i++;
			}

			// Update current replay state
			if (!(timeline.isPaused() || timeline.hasStopped()))
			{
				switch (timeline.getState())
				{
				case FORWARD:
					state = State.PLAYING;
					break;
				case REVERSE:
					state = State.REWINDING;
					break;
				}
			} else
				state = State.PAUSED;

			// Set the pause and settings button specific sizes and positions
			pauseButton.width = size * 2 + SMALL_MARGIN;
			if (state.isActive())
			{
				pauseButton.setVisible(true);
				playButton.setVisible(false);
				rewindButton.setVisible(false);
			} else
			{
				pauseButton.setVisible(false);
				playButton.setVisible(true);
				rewindButton.setVisible(true);
			}
			settingsButton.x = endButton.x + endButton.width + MARGIN;
			pauseButton.x = rewindButton.x;

			// Disable all buttons (except settings) if GUI is not in Replay Mode
			if (session == SessionType.PLAYBACK || session == SessionType.NONE)
			{
				for (Button control : buttons)
					control.setEnabled(false);
			}
			settingsButton.setEnabled(true);

			// Render backround and buttons
			UIrender.drawRect(0, 0, controls.getWidth(), controls.getHeight(),
					getIntColor(GuiStyle.Gui.backroundColor()));
			GlStateManager.disableCull();
			for (Button button : buttons)
				button.draw();
		}
		controls.popMatrix();
	}

	private void removeCheckpoint()
	{
		// Remove checkpoint
		PlaybackSession session = (PlaybackSession) EventHandler.session;
		session.recording.checkpoints.remove(currentCheckpoint);

		// Automatically save the recording
		session.recording.save(true, false, true);
	}

	private void addCheckpoint(String checkpointName)
	{
		final Comparator<Checkpoint> SORTER = (Checkpoint c1, Checkpoint c2) ->
		{
			if (c1.frameNumber < c2.frameNumber)
				return -1;
			else if (c2.frameNumber < c1.frameNumber)
				return 1;
			return 0;
		};

		PlaybackSession session = (PlaybackSession) EventHandler.session;
		int framePos = (int) timeline.getProperty("framePos").getValue();

		// Remove any checkpoints with the same frame number.
		session.recording.checkpoints.removeIf((Checkpoint c) ->
		{
			return c.frameNumber == framePos;
		});

		Checkpoint checkpoint = new Checkpoint(checkpointName, framePos);

		// Generate random color and assign it to new checkpoint
		Random rand = new Random();
		float red = rand.nextFloat() * 0.5f + 0.3f;
		float green = rand.nextFloat() * 0.5f + 0.3f;
		float blue = rand.nextFloat() * 0.5f + 0.3f;
		checkpoint.color = GraphicsHelper.getIntColor(red, green, blue, 1.0f);

		// Add checkpoint and sort the list
		session.recording.checkpoints.add(checkpoint);
		session.recording.checkpoints.sort(SORTER);

		// Automatically save the recording
		session.recording.save(true, false, true);
	}

	/** Specialized Iconified Button With Animations **/
	protected static class SettingsButton extends Button
	{
		Timeline gear = new Timeline(0.2);

		public SettingsButton()
		{
			super();
			setIcon("settings_button");

			// Setup animation properties
			gear.addProperties(new Property("rotation", 0.0, -180.0, Easing.INOUT_QUAD));
			gear.addProperties(new Property("height_mult", 0.0, 1.0, Easing.INOUT_QUAD));
			gear.rewind();
			gear.stop();
		}

		@Override
		public void update(SidedUpdate side)
		{
			super.update(side);
		}

		@Override
		public void draw()
		{
			if (isVisible())
			{
				updateModelviewAndViewportState();

				// Styling Constats
				float SMALL_MARGIN = GuiStyle.Gui.smallMargin() * 2;
				float MODEL_SCALE = height - SMALL_MARGIN * 2;

				// Set Animation State
				updateTransitions();

				gear.tick();

				// Calculate Button's color
				int backgroundColor = getBackgroundColor();
				int iconColor = getTextColor();

				// Draw backround
				UIrender.drawRect(x, y, x + width, y + height, backgroundColor);

				// Render Gear Icon
				GL11.glPushMatrix();
				{
					GL11.glTranslatef(x + width / 2, y + height / 2, 0.0f);
					GL11.glRotatef((float) gear.getProperty("rotation").getValue(), 0.0f, 0.0f, 1.0f);

					UIrender.drawIcon(getIcon(), 0, 0, MODEL_SCALE, iconColor);
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
		if (onCurrentZlevel() && isVisible())
		{
			if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_K)
			{
				if (!timeline.isPaused())
					timeline.pause();
				else
					timeline.resume();
			} else if (keyCode == GLFW.GLFW_KEY_J)
			{
				timeline.rewind();
			} else if (keyCode == GLFW.GLFW_KEY_L)
			{
				timeline.play();
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

	@Override
	public void update(SidedUpdate side)
	{
		if (side == SidedUpdate.CLIENT)
		{
			if (previousFrameButton.isPressed() || nextFrameButton.isPressed())
				frameSkipCountDown++;
			else
				frameSkipCountDown = 0;

			if (frameSkipCountDown > 12 && frameSkipCountDown % 2 == 0)
			{
				if (previousFrameButton.isPressed())
					previousFrameButton.getAction().onAction(previousFrameButton);
				else if (nextFrameButton.isPressed())
					nextFrameButton.getAction().onAction(nextFrameButton);
			}
		}
	}
}
