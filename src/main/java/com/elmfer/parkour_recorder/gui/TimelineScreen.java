package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

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
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class TimelineScreen extends Screen
{
	private TimelineViewport timelineViewport = new TimelineViewport(this);
	protected SessionType session = SessionType.NONE;
	private State state = State.PAUSED;
	protected Timeline timeline;
	protected PlaybackViewerEntity viewer = null;
	
	protected GuiSettingsButton settingsButton = new GuiSettingsButton(0, 0, this::actionPerformed);
	protected GuiSlider speedSlider = new GuiSlider(0, 0, I18n.format("gui.timeline.speed") + ": ", "x", 0.01, 4.0, 1.0, true, true, this::actionPerformed);
	
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
			minecraft.player.prevRenderArmYaw = minecraft.player.renderArmYaw = minecraft.player.prevRotationYaw;
			minecraft.player.prevRenderArmPitch = minecraft.player.renderArmPitch = minecraft.player.prevRotationPitch;
		}
		
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.start_here"), this::actionPerformed));
		addButton(new GuiButton(0, 0, I18n.format("gui.timeline.load"), this::actionPerformed));
		
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "rewind_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "play_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "pause_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "start_button"));
		addButton(new GuiModeledButton(0, 0, this::actionPerformed, "end_button"));
		addButton(settingsButton);
		addButton(speedSlider);
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
			break;
		case 7:
			GuiSettingsButton b = (GuiSettingsButton) button;
			if(b.gear.getState() == Timeline.State.REVERSE) {b.gear.play(); b.highlighed = true; }
			else {b.gear.rewind(); b.highlighed = false; }
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
		
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int gradientHeight = 15;
		int buttonHeight = GuiStyle.Gui.buttonHeight();
		int backroundColor = getIntColor(GuiStyle.Gui.backroundColor());
		
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		
		GuiSettingsButton settingsButton = (GuiSettingsButton) buttons.get(7);
		
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
			fillGradient(0, 0, taskBar.getWidth(), -gradientHeight, fade1, fade2);
			fill(0, 0, timelineBar.getWidth(), timelineBar.getHeight(), getIntColor(0.0f, 0.0f, 0.0f, 0.7f));
			
			renderControls(controls, mouseX, mouseY, partialTicks);
			timelineViewport.drawScreen(mouseX, mouseY, partialTicks, timeline);
			renderTaskbar(title, taskBar, mouseX, mouseY, partialTicks);
		}
		timelineBar.popMatrix();
		
		if(settingsBody.getHeight() > smallMargin * 2)
		{
			settingsBody.pushMatrix(false);
			{
				fill(0, 0, settingsBody.getWidth(), settingsBody.getHeight(), backroundColor);
				settings.pushMatrix(true);
				{
					Slider slider = (Slider) buttons.get(8);
					slider.setWidth(settings.getWidth());
					slider.setHeight(20);
					slider.renderButton(mouseX, mouseY, partialTicks);
					this.timeline.setSpeed(slider.getValue());
				}
				settings.popMatrix();
			}
			settingsBody.popMatrix();
		}
		
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
			load.visible = false;
			
			start.renderButton(mouseX, mouseY, partialTicks);
			load.renderButton(mouseX, mouseY, partialTicks);
		}
		taskBar.popMatrix();
	}
	
	private void renderControls(GuiViewport controls, int mouseX, int mouseY, float partialTicks)
	{	
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		
		controls.pushMatrix(false);
		{
			GuiButton rewind = (GuiButton) buttons.get(2);
			GuiButton play = (GuiButton) buttons.get(3);
			GuiButton pause = (GuiButton) buttons.get(4);
			GuiButton atBegginning = (GuiButton) buttons.get(5);
			GuiButton atEnd = (GuiButton) buttons.get(6);
			GuiButton settings = (GuiButton) buttons.get(7);
			GuiButton[] buttonControls = {atBegginning, rewind, play, atEnd, settings, pause};
			
			int size = controls.getHeight() - smallMargin * 2;
			
			int i = 0;
			for(GuiButton control : buttonControls)
			{
				control.setWidth(size);
				control.setHeight(size);
				control.y = smallMargin;
				control.x = i * (size + smallMargin) + smallMargin;
				i++;
			}
			settings.setWidth(size); settings.setHeight(size);
			
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
				for(GuiButton control : buttonControls) control.active = false;
			
			fill(0, 0, controls.getWidth(), controls.getHeight(), getIntColor(GuiStyle.Gui.backroundColor()));
			
			for(GuiButton control : buttonControls)
				control.renderButton(mouseX, mouseY, partialTicks);
			
			settings.renderButton(mouseX, mouseY, partialTicks);
		}
		controls.popMatrix();
	}
	
	protected static class GuiSettingsButton extends GuiModeledButton
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
			if(visible)
			{
				animation.tick();
				gear.tick();
				if(isHovered) { animation.queue("hovered"); animation.play(); animation.apply();}
				else { animation.queue("hovered"); animation.rewind(); animation.apply();}	
				if(highlighed) { animation.queue("highlight"); animation.play(); animation.apply();}
				else { animation.queue("highlight"); animation.rewind(); animation.apply();}
				if(!active) {animation.queue("hovered", "highlight"); animation.rewind(); animation.apply();}
				
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
				if (!active)
	                j = 10526880;
	            else if (isHovered)
	                j = 16777120;
				
				fill(x, y, x + width, y + height, color);
				
				int shader = ShaderManager.getGUIShader();
				int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
				float smallMargin = GuiStyle.Gui.smallMargin() * 2;
				float modelSize = height - smallMargin * 2;
				Vector4f color1 = GraphicsHelper.getFloatColor(j);
				color1.setW(1.0f);
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				RenderSystem.color3f(color1.getX(), color1.getY(), color1.getZ());
				
				RenderSystem.pushMatrix();
				{
					RenderSystem.translatef(x + width / 2, y + height / 2, 0.0f);
					RenderSystem.scalef(modelSize, -modelSize, 1.0f);
					RenderSystem.rotatef((float) gear.getProperty("rotation").getValue(), 0, 0, 1);
					
					GL20.glUseProgram(shader);
					GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color1.getX(), color1.getY(), color1.getZ(), color1.getW());
					ModelManager.renderModel(modelName);
					GL20.glUseProgram(prevShader);
				}
				RenderSystem.popMatrix();
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
