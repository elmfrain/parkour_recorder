package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.TimelineScreen.SessionType;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TimelineViewport extends AbstractGui implements IGuiEventListener
{
	public static double scrollAmount = 0.0;
	
	/**Predicted width of a single digit.**/
	private static final int NUM_CHAR_WIDTH = Minecraft.getInstance().fontRenderer.getStringWidth("5");
	
	protected List<GuiButton> buttonList = new ArrayList<GuiButton>();
	private TimelineScreen parentScreen;
	private boolean mouseIsPressed = false;
	private boolean pointerIsDragging = false;
	
	float start = 0.0f;
	float end = 0.0f;
	
	public TimelineViewport(TimelineScreen parent)
	{
		parentScreen = parent;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public static void onScrollCallback(GuiScreenEvent.MouseScrollEvent event)
	{
		scrollAmount = event.getScrollDelta();
	}
	
	public void init()
	{
		List<IGuiEventListener> pChildren = (List<IGuiEventListener>) parentScreen.children();
		pChildren.add(this);
	}
	
	@SubscribeEvent
	public void onMouseEvent(InputEvent.MouseInputEvent event)
	{
		if(Minecraft.getInstance().currentScreen != parentScreen) MinecraftForge.EVENT_BUS.unregister(this);
		
		if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1)
		{
			switch(event.getAction())
			{
			case GLFW.GLFW_PRESS:
				mouseIsPressed = true;
				break;
			case GLFW.GLFW_RELEASE:
				pointerIsDragging = false;
			}
		}
	}
	
	/**Render timeline.**/
	public void drawScreen(int mouseX, int mouseY, float partialTicks, GuiViewport viewport)
	{
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		
		GuiViewport numberLine = new GuiViewport(viewport);
		numberLine.bottom = (int) (viewport.getHeight() / 3.0f);
		GuiViewport pointer = new GuiViewport(numberLine);
		
		end = (float) parentScreen.timeline.getDuration();
		
		//Get timeline values
		double duration = parentScreen.timeline.getDuration() * 20;
		double framePos = parentScreen.timeline.getProperty("framePos").getValue();
		
		//Y pos for numbers on number line
		int stringCenterY = numberLine.getHeight() / 2 - font.FONT_HEIGHT / 2;
		
		//Styling constants
		final int FADE_HEIGHT = GuiStyle.Gui.fadeHeight();
		final int FADE_1 = getIntColor(GuiStyle.Gui.fade1());
		final int FADE_2 = getIntColor(GuiStyle.Gui.fade2());
		final int GRAD_1 = GraphicsHelper.getIntColor(0.15f, 0.15f, 0.15f, 0.9f);
		final int GRAD_2 = GraphicsHelper.getIntColor(0.06f, 0.06f, 0.06f, 0.9f);
		final int BLUE_1 = GraphicsHelper.getIntColor(0.0f, 0.55f, 1.0f, 1.0f);
		final int BLUE_2 = GraphicsHelper.getIntColor(0.0f, 0.2f, 0.3f, 1.0f);
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
		final int MARGIN = GuiStyle.Gui.margin();
		
		viewport.pushMatrix(true);
		{
			//Render timeline backround and numberline backround
			GraphicsHelper.fillGradient(0, 0, viewport.getWidth(), viewport.getHeight(), FADE_1, FADE_2);
			GraphicsHelper.fillGradient(0, 0, numberLine.getWidth(), numberLine.getHeight(), GRAD_1, GRAD_2);
			
			//Determine spacing of time markers
			int nbTimeMarkers = Math.min(viewport.getWidth() / (TimelineScreen.timeStampFormat.AVERAGE_LENGTH * NUM_CHAR_WIDTH + MARGIN * 2), 10);
			int delta = (int) (Math.round((duration / (nbTimeMarkers * nbTimeMarkers))) * nbTimeMarkers);
			
			//Fractional progress of timeline
			float amount = (float) ((framePos) / duration);
			
			//Render time markers
			if(delta == 0) delta = 5;
			{
				for(float i = 0.0f; i < duration; i += delta)
				{
					float numberX = (float) ((i / duration) * numberLine.getWidth());
					GraphicsHelper.drawCenteredString(font, TimelineScreen.timeStampFormat.getTimeStamp((int) i), (int) numberX, stringCenterY, 0xFFFFFFFF);
					GraphicsHelper.vLine((int) numberX, numberLine.bottom, viewport.bottom, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
				}
			}
			
			//Render checkpoint markers of recording
			if(EventHandler.session instanceof PlaybackSession)
				for(Checkpoint c : ((PlaybackSession) EventHandler.session).recording.checkpoints)
					renderCheckpointMarker(c, numberLine.getHeight(), viewport.getWidth(), viewport.getHeight());
			
			//Position pointer
			String timeStamp = TimelineScreen.timeStampFormat.getTimeStamp(framePos);
			float pointerWidth = (font.getStringWidth(timeStamp)) / 2.0f + SMALL_MARGIN;
			float alignmentRatio = ((numberLine.getWidth() - 1.0f) / numberLine.getWidth());
			float pointerPos = amount * numberLine.getWidth() * alignmentRatio;
			float pointerHeadPos = Math.max(pointerWidth, Math.min(pointerPos, numberLine.getWidth() - pointerWidth));
			pointer.left = (int) (pointerHeadPos - pointerWidth);
			pointer.right = (int) (pointerHeadPos + pointerWidth);
			
			//Render pointer
			GraphicsHelper.gradientRectToRight((int) pointerPos - FADE_HEIGHT / 4, pointer.getHeight(), (int) pointerPos, viewport.getHeight(), FADE_2, FADE_1);
			GraphicsHelper.gradientRectToRight((int) pointerPos + 1, pointer.getHeight(), (int) pointerPos + FADE_HEIGHT / 4 + 1, viewport.getHeight(), FADE_1, FADE_2);
			GraphicsHelper.vLine((int) pointerPos, pointer.getHeight() - 1, viewport.getHeight(), BLUE_1);
			pointer.pushMatrix(false);
			{
				GraphicsHelper.gradientRectToRight(0, 0, -FADE_HEIGHT / 2, pointer.getHeight(), FADE_1, FADE_2);
				GraphicsHelper.gradientRectToRight(pointer.getWidth(), 0, pointer.getWidth() + FADE_HEIGHT / 2, pointer.getHeight(), FADE_1, FADE_2);
				GraphicsHelper.fillGradient(0, 0, pointer.getWidth(), pointer.getHeight(), BLUE_1, BLUE_2);
				GraphicsHelper.drawCenteredString(font, timeStamp, pointer.getWidth() / 2, stringCenterY, 0xFFFFFFFF);
			}
			pointer.popMatrix();
			
			//Begin dragging pointer if mouse is pressed on it
			if(viewport.isHovered(mouseX, mouseY) && mouseIsPressed && parentScreen.session == SessionType.REPLAY && (parentScreen.timeline.isPaused() || parentScreen.timeline.hasStopped()) && GuiButton.currentZLevel == 0)
				pointerIsDragging = true;
			
			//Set timeline position equals to cursor if dragging
			if(pointerIsDragging) parentScreen.timeline.setFracTime((mouseX - numberLine.getAbsoluteLeft() * 1.0f) / numberLine.getWidth());
			
			//Scroll timeline with mouse wheel
			if(viewport.isHovered(mouseX, mouseY) && (parentScreen.timeline.isPaused() || parentScreen.timeline.hasStopped()) && GuiButton.currentZLevel == 0)
			{
				int currentFrame = (int) parentScreen.timeline.getProperty("framePos").getValue();
				double oneTick = 1.0 / (parentScreen.timeline.getDuration() * 20.0);
				
				if(0 < scrollAmount) //Goto next frame
				{
					//Snap timeline pos to current frame
					parentScreen.timeline.setFracTime(currentFrame / (parentScreen.timeline.getDuration() * 20.0) + oneTick / 10.0);
					
					parentScreen.timeline.setFracTime(parentScreen.timeline.getFracTime() + oneTick);
				}
				else if(scrollAmount < 0) //Goto previous frame
				{
					//Snap timeline pos to current frame
					parentScreen.timeline.setFracTime(currentFrame / (parentScreen.timeline.getDuration() * 20.0) + oneTick / 10.0);
					
					parentScreen.timeline.setFracTime(parentScreen.timeline.getFracTime() - oneTick);
				}
			}
			
			//Darken timeline if not active
			if(!parentScreen.session.isActive()) GraphicsHelper.fill(0, 0, viewport.getWidth(), viewport.getHeight(), GraphicsHelper.getIntColor(GuiStyle.Gui.backroundColor()));
		}
		viewport.popMatrix();
		
		mouseIsPressed = false;
		scrollAmount = 0;
	}
	
	/**Renders a checkpoint marker on the timeline.**/
	private void renderCheckpointMarker(Checkpoint c, int numberLineHeight, int timelineWidth, int timelineHeight)
	{
		//Determine position of marker
		double duration = parentScreen.timeline.getDuration() * 20;
		double framePos = c.frameNumber;
		float amount = (float) ((framePos) / duration);
		int x = (int) (amount * timelineWidth);
		
		//Slightly make the marker brighter if it's the one currently selected
		Vector4f color = GraphicsHelper.getFloatColor(c.color);
		if(parentScreen.currentCheckpoint != null && parentScreen.currentCheckpoint == c)
		{
			color.set(color.getX() + 0.25f, color.getY() + 0.25F, color.getZ() + 0.25f, color.getW());
		}
		
		//Render Marker
		int shader = ShaderManager.getGUIShader();
		int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GlStateManager.enableBlend();
		GL11.glPushMatrix();
		{
			float scale = timelineHeight - numberLineHeight;
			GL11.glTranslatef(x, numberLineHeight, 0.0f);
			GL11.glScalef(scale, -scale, 1.0f);
			
			GL20.glUseProgram(shader);
			GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), 1.0f);
			ModelManager.renderModel("checkpoint");
			GL20.glUseProgram(prevShader);
		}
		GL11.glPopMatrix();
	}
	
	/**Enum to determine the format to display time**/
	public static enum TimeStampFormat
	{
		GAME_TICKS("ticks", 4),
		SECONDS("seconds", 3),
		SECONDS_TENTHS("seconds.tenths", 4),
		SECONDS_HUNDREDTHS("seconds.hundredths", 5),
		SECONDS_TICKS("ss:ticks", 5),
		HH_MM_SS("hh:mm:ss", 8),
		HH_MM_SS_TENTHS("hh:mm:ss.tenths", 10),
		HH_MM_SS_HUNDREDTHS("hh:mm:ss.hundredths", 11),
		HH_MM_SS_TICKS("hh:mm:ss:ticks", 11);
		
		public static final TimeStampFormat DEFAULT = GAME_TICKS;
		
		/**The precidcted string length for the format.**/
		public final int AVERAGE_LENGTH;
		
		/**Name of the format. The name gives an idea on how it will format time.**/
		public final String NAME;
		
		TimeStampFormat(String name, int length)
		{
			AVERAGE_LENGTH = length;
			NAME = name;
		}
		
		/**
		 * Get time format from name. If given name does not equal to any formats, it will
		 * return {@code GAME_TICKS} by default.
		 */
		public static TimeStampFormat getFormatFromName(String name)
		{
			for(TimeStampFormat format : TimeStampFormat.values())
				if(name.equals(format.NAME)) return format;
			return GAME_TICKS;
		}
		
 		public String getTimeStamp(double gameTicks)
		{
			//Hour, minute, second formatting
			int ticks = (int) gameTicks;
			int hours = ticks / 72000; ticks %= 72000;  //72000 ticks per hour
			int minutes = ticks / 1200; ticks %= 1200;  //1200 ticks per minute
			double seconds = (gameTicks - 72000.0 * hours - 1200.0 * minutes) / 20.0; ticks %= 20; //20 ticks per second
			double fullSeconds = gameTicks / 20.0; //Has total amount of seconds
			int frame = ticks; //Remaining frames
			
			switch(this)
			{
			case SECONDS:
				return String.format("%1$01ds", (int)fullSeconds);
			case SECONDS_TENTHS:
				return String.format("%1$.1fs", fullSeconds);
			case SECONDS_HUNDREDTHS:
				return String.format("%1$.2fs", fullSeconds);
			case SECONDS_TICKS:
				return String.format("%1$02d:%2$02d", (int)seconds, frame);
			case HH_MM_SS:
				return String.format("%1$02d:%2$02d:%3$02d", hours, minutes, (int)seconds);
			case HH_MM_SS_TENTHS:
				return String.format("%1$02d:%2$02d:%3$.1f", hours, minutes, seconds);
			case HH_MM_SS_HUNDREDTHS:
				return String.format("%1$02d:%2$02d:%3$.2f", hours, minutes, seconds);
			case HH_MM_SS_TICKS:
				return String.format("%1$02d:%2$02d:%3$02d:%4$02d", hours, minutes, (int)seconds, frame);
			default:
				return Integer.toString((int)gameTicks);
			}
		}
	}
}
