package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.io.IOException;
import java.util.Comparator;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.animation.Easing;
import com.elmfer.parkour_recorder.animation.Property;
import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.ParkourFrame;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.PlaybackViewerEntity;
import com.elmfer.parkour_recorder.parkour.Recording;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class TimelineScreen extends GuiScreen
{
	private TimelineViewport timelineViewport = new TimelineViewport(this);
	protected SessionType session = SessionType.NONE;
	private State state = State.PAUSED;
	protected Timeline timeline;
	protected PlaybackViewerEntity viewer = null;
	protected Checkpoint currentCheckpoint = null;
	
	protected GuiSettingsButton settingsButton = new GuiSettingsButton(-1, 0, 0);
	protected GuiSlider speedSlider = new GuiSlider(-2, 0, 0, I18n.format("gui.timeline.speed") + ": ", "x", 0.01f, 4.0f, 1.0f);
	private GuiAlertBox alertBox = null;
	
	public TimelineScreen()
	{	
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
	public void initGui()
	{
		if(session == SessionType.REPLAY)
		{
			viewer = new PlaybackViewerEntity();
			mc.setRenderViewEntity(viewer);
			mc.player.prevRenderArmYaw = mc.player.renderArmYaw = mc.player.prevRotationYaw;
			mc.player.prevRenderArmPitch = mc.player.renderArmPitch = mc.player.prevRotationPitch;
		}
		
		addButton(new GuiButton(0, 0, 0, I18n.format("gui.timeline.start_here")));
		addButton(new GuiButton(1, 0, 0, I18n.format("gui.timeline.load")));
		
		addButton(new GuiModeledButton(2, 0, 0, "rewind_button"));
		addButton(new GuiModeledButton(3, 0, 0, "play_button"));
		addButton(new GuiModeledButton(4, 0, 0, "pause_button"));
		addButton(new GuiModeledButton(5, 0, 0, "start_button"));
		addButton(new GuiModeledButton(6, 0, 0, "end_button"));
		addButton(new GuiModeledButton(7, 0, 0, "prev_frame_button"));
		addButton(new GuiModeledButton(8, 0, 0, "next_frame_button"));
		
		addButton(settingsButton);
		addButton(speedSlider);
		
		addButton(new GuiModeledButton(9, 0, 0, "add_checkpoint_button"));
		addButton(new GuiModeledButton(10, 0, 0, "remove_checkpoint_button"));
		addButton(new GuiModeledButton(11, 0, 0, "prev_checkpoint_button"));
		addButton(new GuiModeledButton(12, 0, 0, "next_checkpoint_button"));
		
		if(alertBox != null)
			alertBox.initGui();
	}
	
	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button)
	{
		int buttonID = button.id;
		
		switch (buttonID) {
		case 0:
			onGuiClosed();
			mc.displayGuiScreen(null);
			if(session == SessionType.REPLAY)
			{
				PlaybackSession session = (PlaybackSession) EventHandler.session;
				
				session.startAt((int) timeline.getProperty("framePos").getValue());
			}
			break;
		case 1:
			onGuiClosed();
			mc.displayGuiScreen(new LoadRecordingScreen());
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
			break;
		case 7:
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + 0.001);
			
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			timeline.setFracTime(timeline.getFracTime() - oneTick);
			timeline.pause();
			break;
		}
		case 8:
		{
			int framePos = (int) timeline.getProperty("framePos").getValue();
			timeline.setFracTime(framePos / (timeline.getDuration() * 20.0) + 0.001);
			
			double oneTick = 1.0 / (timeline.getDuration() * 20.0);
			timeline.setFracTime(timeline.getFracTime() + oneTick);
			timeline.pause();
			break;
		}
		case 9:
			GuiNamerBox namerBox = new GuiNamerBox(I18n.format("gui.timeline.add_checkpoint"), this, (String s) -> { return true; } , this::addCheckpoint);
			namerBox.initGui();
			namerBox.textField.setMaxStringLength(64);
			alertBox = namerBox;
			break;
		case 10:
			String message = I18n.format("gui.timeline.remove_checkpoint_?");
			message += currentCheckpoint.name.isEmpty() ? "" : " - " + currentCheckpoint.name;
			GuiConfirmationBox confirmBox = new GuiConfirmationBox(message, this::removeCheckpoint, this);
			confirmBox.initGui();
			alertBox = confirmBox;
			break;
		case 11:
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			int framePos = (int) timeline.getProperty("framePos").getValue();
			
			if(currentCheckpoint.frameNumber < framePos) timeline.setTimePos(currentCheckpoint.frameNumber / 20.0 + 0.001);
			else
			{
				int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
				Checkpoint prevCheckpoint = session.recording.checkpoints.get(currentCheckptIndex - 1);
				
				timeline.setTimePos(prevCheckpoint.frameNumber / 20.0 + 0.001);
			}
			timeline.pause();
			break;
		}
		case 12:
		{
			PlaybackSession session = (PlaybackSession) EventHandler.session;
			
			int currentCheckptIndex = session.recording.checkpoints.indexOf(currentCheckpoint);
			Checkpoint nextCheckpoint = session.recording.checkpoints.get(currentCheckptIndex + 1);
			
			timeline.setTimePos(nextCheckpoint.frameNumber / 20.0 + 0.001);
			timeline.pause();
			break;
		}
		case -1:
			GuiSettingsButton b = (GuiSettingsButton) button;
			if(b.gear.getState() == Timeline.State.REVERSE) {b.gear.play(); b.highlighed = true; }
			else {b.gear.rewind(); b.highlighed = false; }
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		ScaledResolution res = new ScaledResolution(mc);
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
		
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int gradientHeight = 15;
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		int backroundColor = getIntColor(GuiStyle.Gui.backroundColor());
		
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		
		GuiSettingsButton settingsButton = (GuiSettingsButton) buttonList.get(9);
		
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
		GuiViewport controls = new GuiViewport(all);
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
		settingsBody.top = (int) (timelineBar.top - (GuiStyle.Gui.buttonHeight() + smallMargin * 3) * settingsButton.gear.getProperty("height_mult").getValue());
		settingsBody.right -= smallMargin;
		settingsBody.bottom = timelineBar.top - smallMargin;
		GuiViewport settings = new GuiViewport(settingsBody);
		settings.left = settings.top = smallMargin;
		settings.right -= smallMargin;
		
		timelineBar.pushMatrix(false);
		{
			drawGradientRect(0, -gradientHeight, taskBar.getWidth(), 0, fade2, fade1);
			drawRect(0, 0, timelineBar.getWidth(), timelineBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			renderCheckpointControls(checkpointControls, mouseX, mouseY, partialTicks);
			renderControls(controls, mouseX, mouseY, partialTicks);
			timelineViewport.drawScreen(mouseX, mouseY, partialTicks, timeline);
			renderTaskbar(title, taskBar, mouseX, mouseY, partialTicks);
			renderCheckpointStatus(taskBar, mouseX, mouseY, partialTicks);
		}
		timelineBar.popMatrix();
		
		if(settingsBody.getHeight() > smallMargin * 2)
		{
			settingsBody.pushMatrix(false);
			{
				drawRect(0, 0, settingsBody.getWidth(), settingsBody.getHeight(), backroundColor);
				settings.pushMatrix(true);
				{
					GuiSlider slider = (GuiSlider) buttonList.get(10);
					slider.setWidth(settings.getWidth());
					slider.height = 20;
					slider.drawButton(mc, mouseX, mouseY, partialTicks);
					this.timeline.setSpeed(slider.getSliderValue());
				}
				settings.popMatrix();
			}
			settingsBody.popMatrix();
		}
		
		title.pushMatrix(false);
		{
			drawRect(0, 0, title.getWidth(), title.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.5f));
			drawCenteredString(mc.fontRenderer, I18n.format("gui.timeline"), title.getWidth() / 2, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
		title.popMatrix();
		
		if(alertBox != null)
		{
			alertBox.drawScreen(mouseX, mouseY, partialTicks);
			if(alertBox.shouldClose()) alertBox = null;
		}
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		mc.setRenderViewEntity(mc.player);
		if(viewer != null) { mc.world.removeEntityDangerously(viewer); }
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
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
	      return false;
	}
	
	private void renderCheckpointStatus(GuiViewport taskBar, int mouseX, int mouseY, float partialTicks)
	{
		if(currentCheckpoint != null)
		{
			int smallMargin = GuiStyle.Gui.smallMargin();
			int backroundColor = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
			int fade2 = GraphicsHelper.getIntColor(GuiStyle.Gui.fade2());
			int fadeWidth = GuiStyle.Gui.fadeHeight();
			int nameLength = currentCheckpoint.name != null ? fontRenderer.getStringWidth(currentCheckpoint.name) : 0;
			float height = mc.fontRenderer.FONT_HEIGHT * 2;
			
			Vector4f color = GraphicsHelper.getFloatColor(currentCheckpoint.color);

			int stringOffset = (int) (height * 0.6f + smallMargin);
			drawRect(smallMargin, taskBar.bottom + smallMargin, nameLength + stringOffset, (int) (taskBar.bottom + smallMargin + height), backroundColor);
			GraphicsHelper.gradientRectToRight(nameLength + stringOffset, taskBar.bottom + smallMargin, smallMargin + nameLength + fadeWidth + stringOffset, (int) (taskBar.bottom + smallMargin + height), backroundColor, fade2);
			
			int shader = ShaderManager.getGUIShader();
			int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			GlStateManager.enableBlend();
			GlStateManager.color(color.getX(), color.getY(), color.getZ());
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(smallMargin, taskBar.bottom + smallMargin, 0.0f);
				GlStateManager.scale(height, -height, 1.0);
				
				GL20.glUseProgram(shader);
				GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), 1.0f);
				ModelManager.renderModel("checkpoint_icon");
				GL20.glUseProgram(prevShader);
			}
			GlStateManager.popMatrix();
			
			if(!currentCheckpoint.name.isEmpty())
				drawString(fontRenderer, currentCheckpoint.name, stringOffset, (int) (taskBar.bottom + smallMargin + height / 2 - fontRenderer.FONT_HEIGHT / 2), 0xFFFFFFFF);
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
			drawGradientRect(0, taskBar.getHeight(), taskBar.getWidth(), taskBar.getHeight() + fadeHeight, fade1, fade2);
			drawRect(0, 0, taskBar.getWidth(), taskBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			drawVerticalLine(title.right + margin, smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			String recordingName = recordingIsLoaded ? ((PlaybackSession) EventHandler.session).recording.getName() : I18n.format("gui.timeline.no_recording_is_loaded");
			int nameColor = recordingIsLoaded ? 0xFFFFFFFF : getIntColor(0.5f, 0.5f, 0.5f, 1.0f);
			
			drawString(mc.fontRenderer, recordingName, title.right + margin * 2, taskBar.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, nameColor);
			
			drawVerticalLine(title.right + margin * 3 + mc.fontRenderer.getStringWidth(recordingName), smallMargin, taskBar.getHeight() - smallMargin * 2, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
			
			GuiButton start = (GuiButton) buttonList.get(0);
			GuiButton load = (GuiButton) buttonList.get(1);
			
			start.height = buttonHeight;
			load.height = buttonHeight;
			start.setWidth(buttonWidth);
			load.setWidth(buttonWidth);
			start.x = title.right + margin * 4 + mc.fontRenderer.getStringWidth(recordingName);
			load.x = start.x + buttonWidth + margin;
			start.y = load.y = smallMargin;
			start.enabled = session == SessionType.REPLAY;
			load.visible = false;
			
			start.drawButton(mc, mouseX, mouseY, partialTicks);
			load.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		taskBar.popMatrix();
	}
	
	private void renderCheckpointControls(GuiViewport checkpointControls, int mouseX, int mouseY, float partialTicks)
	{
		int framePos = (int) timeline.getProperty("framePos").getValue();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int margin = GuiStyle.Gui.margin();
		
		checkpointControls.pushMatrix(false);
		{
			GuiButton addCheckpoint = (GuiButton) buttonList.get(11);
			GuiButton removeCheckpoint = (GuiButton) buttonList.get(12);
			GuiButton prevCheckpoint = (GuiButton) buttonList.get(13);
			GuiButton nextCheckpoint = (GuiButton) buttonList.get(14);
			GuiButton[] buttonControls = {addCheckpoint, removeCheckpoint, prevCheckpoint, nextCheckpoint};
			
			addCheckpoint.enabled = this.session.isActive();
			removeCheckpoint.enabled = prevCheckpoint.enabled = nextCheckpoint.enabled = false;
			currentCheckpoint = null;
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
					removeCheckpoint.enabled = true;
				
				if(!recording.checkpoints.isEmpty() && framePos < recording.checkpoints.get(recording.checkpoints.size() - 1).frameNumber)
					nextCheckpoint.enabled = true;
				if(!recording.checkpoints.isEmpty() && recording.checkpoints.get(0).frameNumber < framePos)
					prevCheckpoint.enabled = true;
			}
			
			int size = checkpointControls.getHeight() - smallMargin * 2;
			
			int i = 0;
			for(GuiButton button : buttonControls)
			{
				button.setWidth(size);
				button.height = size;
				button.y = smallMargin;
				button.x = smallMargin + (size + smallMargin) * i;
				i++;
			}
			prevCheckpoint.x = removeCheckpoint.x + removeCheckpoint.width + margin;
			nextCheckpoint.x = prevCheckpoint.x + prevCheckpoint.width + smallMargin;
			
			drawRect(0, 0, checkpointControls.getWidth(), checkpointControls.getHeight(), getIntColor(GuiStyle.Gui.backroundColor()));
			addCheckpoint.drawButton(mc, mouseX, mouseY, partialTicks);
			removeCheckpoint.drawButton(mc, mouseX, mouseY, partialTicks);
			prevCheckpoint.drawButton(mc, mouseX, mouseY, partialTicks);
			nextCheckpoint.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		checkpointControls.popMatrix();
	}
	
	private void renderControls(GuiViewport controls, int mouseX, int mouseY, float partialTicks)
	{	
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		
		controls.pushMatrix(false);
		{
			GuiButton rewind = (GuiButton) buttonList.get(2);
			GuiButton play = (GuiButton) buttonList.get(3);
			GuiButton pause = (GuiButton) buttonList.get(4);
			GuiButton atBegginning = (GuiButton) buttonList.get(5);
			GuiButton atEnd = (GuiButton) buttonList.get(6);
			GuiButton settings = (GuiButton) buttonList.get(9);
			GuiButton prevFrame = (GuiButton) buttonList.get(7);
			GuiButton nextFrame = (GuiButton) buttonList.get(8);
			GuiButton[] buttonControls = {atBegginning, prevFrame, rewind, play, nextFrame, atEnd, settings, pause};
			
			int size = controls.getHeight() - smallMargin * 2;
			
			int i = 0;
			for(GuiButton control : buttonControls)
			{
				control.setWidth(size);
				control.height = size;
				control.y = smallMargin;
				control.x = i * (size + smallMargin) + smallMargin;
				i++;
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
			
			pause.setWidth(size * 2 + smallMargin);
			if(state.isActive()) { pause.visible = true; play.visible = rewind.visible = false;}
			else { pause.visible = false; play.visible = rewind.visible = true; }
			settings.x = atEnd.x + atEnd.width + margin;
			pause.x = rewind.x;
			
			if(session == SessionType.PLAYBACK || session == SessionType.NONE)
				for(GuiButton control : buttonControls) control.enabled = false;
			
			drawRect(0, 0, controls.getWidth(), controls.getHeight(), getIntColor(GuiStyle.Gui.backroundColor()));
			
			GlStateManager.disableCull();
			for(GuiButton control : buttonControls)
				control.drawButton(mc, mouseX, mouseY, partialTicks);
			
			settings.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		controls.popMatrix();
	}

	private void removeCheckpoint()
	{
		final Comparator<Checkpoint> SORTER = (Checkpoint c1, Checkpoint c2) -> 
		{
			if(c1.frameNumber < c2.frameNumber) return -1;
			else if(c2.frameNumber < c1.frameNumber) return 1;
			return 0;
		};
		
		PlaybackSession session = (PlaybackSession) EventHandler.session;
		session.recording.checkpoints.remove(currentCheckpoint);
		session.recording.checkpoints.sort(SORTER);
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
		
		session.recording.checkpoints.removeIf((Checkpoint c) -> { return c.frameNumber == framePos; });
		
		Checkpoint checkpoint = new Checkpoint(checkpointName, framePos);
		
		Random rand = new Random();
		float red = rand.nextFloat() * 0.5f + 0.3f;
		float green = rand.nextFloat() * 0.5f + 0.3f;
		float blue = rand.nextFloat() * 0.5f + 0.3f;
		
		checkpoint.color = GraphicsHelper.getIntColor(red, green, blue, 1.0f);
		
		session.recording.checkpoints.add(checkpoint);
		session.recording.checkpoints.sort(SORTER);
	}
	
	protected static class GuiSettingsButton extends GuiModeledButton
	{
		Timeline gear = new Timeline(0.2);
		
		public GuiSettingsButton(int id, int x, int y) {
			super(id, x, y, "settings_button");
			gear.addProperties(new Property("rotation", 0.0, -180.0, Easing.INOUT_QUAD));
			gear.addProperties(new Property("height_mult", 0.0, 1.0, Easing.INOUT_QUAD));
			gear.rewind();
			gear.stop();
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if(visible)
			{
				animation.tick();
				gear.tick();
				if(hovered) { animation.queue("hovered"); animation.play(); animation.apply();}
				else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
				if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
				else { animation.queue("highlight"); animation.rewind(); animation.apply();}
				if(!enabled) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
				
				preRender(mouseX, mouseY, partialTicks);
				
				Vector3f hoveredcolor = new Vector3f(0.3f, 0.3f, 0.3f);
				hoveredcolor.scale((float) animation.getTimeline("hovered").getFracTime());
				Vector3f highlightColor = new Vector3f(highlightTint);
				highlightColor.scale((float) (animation.getTimeline("highlight").getFracTime() * 0.6));
				Vector3f c = hoveredcolor;
				c = Vector3f.add(c, highlightColor, null);
				int color = getIntColor(c, 0.2f);
				
				int j = 14737632;
				if (!enabled)
		            j = GraphicsHelper.getIntColor(0.45f, 0.45f, 0.45f, 1.0f);
		        else if (hovered)
		            j = 16777120;
				
				drawRect(x, y, x + width, y + height, color);
				
				int shader = ShaderManager.getGUIShader();
				int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
				float smallMargin = GuiStyle.Gui.smallMargin() * 2;
				float modelSize = height - smallMargin * 2;
				Vector4f color1 = GraphicsHelper.getFloatColor(j);
				color1.setW(1.0f);
				GlStateManager.disableTexture2D();
				GlStateManager.enableBlend();
				GlStateManager.color(color1.getX(), color1.getY(), color1.getZ());
				
				GlStateManager.pushMatrix();
				{
					GlStateManager.translate(x + width / 2, y + height / 2, 0.0f);
					GlStateManager.scale(modelSize, -modelSize, 1.0f);
					GlStateManager.rotate((float) gear.getProperty("rotation").getValue(), 0, 0, 1);
					
					GL20.glUseProgram(shader);
					GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color1.getX(), color1.getY(), color1.getZ(), color1.getW());
					ModelManager.renderModel(modelName);
					GL20.glUseProgram(prevShader);
				}
				GlStateManager.popMatrix();
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
