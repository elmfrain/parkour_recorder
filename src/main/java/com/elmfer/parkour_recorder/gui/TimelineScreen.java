package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.animation.Easing;
import com.elmfer.parkour_recorder.animation.Property;
import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.parkour.ParkourFrame;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.parkour.PlaybackViewerEntity;
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
	
	protected GuiSettingsButton settingsButton = new GuiSettingsButton(-1, 0, 0);
	protected GuiSlider speedSlider = new GuiSlider(-2, 0, 0, I18n.format("gui.timeline.speed") + ": ", "x", 0.01f, 4.0f, 1.0f);
	
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
		addButton(settingsButton);
		addButton(speedSlider);
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
		
		GuiSettingsButton settingsButton = (GuiSettingsButton) buttonList.get(7);
		
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
		int controlsWidth = (controlsSize - 2 * smallMargin) * 5 + 5 * smallMargin + margin;
		controls.top = timelineBar.top - controlsSize - smallMargin;
		controls.bottom = controls.top + controlsSize;
		controls.left = all.getWidth() / 2 - controlsWidth / 2;
		controls.right = controls.left + controlsWidth;
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
			drawGradientRect(0, 0, taskBar.getWidth(), -gradientHeight, fade1, fade2);
			drawRect(0, 0, timelineBar.getWidth(), timelineBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			renderControls(controls, mouseX, mouseY, partialTicks);
			timelineViewport.drawScreen(mouseX, mouseY, partialTicks, timeline);
			renderTaskbar(title, taskBar, mouseX, mouseY, partialTicks);
		}
		timelineBar.popMatrix();
		
		if(settingsBody.getHeight() > smallMargin * 2)
		{
			settingsBody.pushMatrix(false);
			{
				drawRect(0, 0, settingsBody.getWidth(), settingsBody.getHeight(), backroundColor);
				settings.pushMatrix(true);
				{
					GuiSlider slider = (GuiSlider) buttonList.get(8);
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
	}
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		mc.setRenderViewEntity(mc.player);
		if(viewer != null) { mc.world.removeEntityDangerously(viewer); }
	}
	
	@Override
	public boolean doesGuiPauseGame()
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
			GuiButton settings = (GuiButton) buttonList.get(7);
			GuiButton[] buttonControls = {atBegginning, rewind, play, atEnd, settings, pause};
			
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
			settings.setWidth(size); settings.height = size;
			
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
			if(state.isActive()) { pause.visible = true; play.visible = rewind.visible = false; }
			else { pause.visible = false; play.visible = rewind.visible = true; }
			settings.x = atEnd.x + size + margin;
			pause.x = atBegginning.x + size + smallMargin;
			
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
	                j = 10526880;
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
