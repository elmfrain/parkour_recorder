package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.TimelineView.SessionType;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.UIrender.Direction;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Vector4f;

public class NumberLineView extends Widget
{
	/**Predicted width of a single digit.**/
	private static final int NUM_CHAR_WIDTH = UIrender.getStringWidth("5");
	
	public static double scrollAmount = 0.0;
	
	private TimelineView parentWidget;
	private boolean pointerIsDragging = false;
	private boolean leftButtonIsPressed = false;
	private Viewport mainViewport = null;
	private Viewport numberLineViewport = null;
	
	float start = 0.0f;
	float end = 0.0f;
	
	public NumberLineView(TimelineView parent)
	{
		super();
		parentWidget = parent;
	}
	
	public void setViewport(Viewport viewport)
	{
		this.mainViewport = viewport;
	}
	
	@Override
	public void draw()
	{
		Minecraft mc = Minecraft.getInstance();
		FontRenderer font = mc.fontRenderer;
		
		numberLineViewport = new Viewport(mainViewport);
		numberLineViewport.bottom = (int) (mainViewport.getHeight() / 3.0f);
		Viewport pointer = new Viewport(numberLineViewport);
		
		end = (float) parentWidget.timeline.getDuration();
		
		//Get timeline values
		double duration = parentWidget.timeline.getDuration() * 20;
		double framePos = parentWidget.timeline.getProperty("framePos").getValue();
		
		//Y pos for numbers on number line
		int stringCenterY = (int) numberLineViewport.getHeight() / 2 - font.FONT_HEIGHT / 2;
		
		//Styling constants
		final int FADE_HEIGHT = GuiStyle.Gui.fadeHeight();
		final int FADE_1 = getIntColor(GuiStyle.Gui.fade1());
		final int FADE_2 = getIntColor(GuiStyle.Gui.fade2());
		final int GRAD_1 = -450484698;
		final int GRAD_2 = -451997937;
		final int BLUE_1 = -16741121;
		final int BLUE_2 = -16764084;
		final int SMALL_MARGIN = GuiStyle.Gui.smallMargin();
		final int MARGIN = GuiStyle.Gui.margin();
		
		mainViewport.pushMatrix(true);
		{	
			//Render timeline backround and numberline backround
			UIrender.drawGradientRect(0, 0, mainViewport.getWidth(), mainViewport.getHeight(), FADE_1, FADE_2);
			UIrender.drawGradientRect(0, 0, numberLineViewport.getWidth(), numberLineViewport.getHeight(), GRAD_1, GRAD_2);
			
			//Determine spacing of time markers
			int nbTimeMarkers = Math.min((int)mainViewport.getWidth() / (TimelineView.timeStampFormat.AVERAGE_LENGTH * NUM_CHAR_WIDTH + MARGIN * 2), 10);
			int delta = (int) (Math.round((duration / (nbTimeMarkers * nbTimeMarkers))) * nbTimeMarkers);
			
			//Fractional progress of timeline
			float amount = (float) ((framePos) / duration);
			
			//Render time markers
			if(delta == 0) delta = 5;
			{
				for(float i = 0.0f; i < duration; i += delta)
				{
					float numberX = (float) ((i / duration) * numberLineViewport.getWidth());
					UIrender.drawString(Anchor.TOP_CENTER, TimelineView.timeStampFormat.getTimeStamp((int) i), (int) numberX, stringCenterY, 0xFFFFFFFF);
					UIrender.drawVerticalLine((int) numberX, numberLineViewport.bottom, mainViewport.bottom, 2137417318);
				}
			}
			
			//Render checkpoint markers of recording
			if(EventHandler.session instanceof PlaybackSession)
				for(Checkpoint c : ((PlaybackSession) EventHandler.session).recording.checkpoints)
					renderCheckpointMarker(c, (int)numberLineViewport.getHeight(), (int)mainViewport.getWidth(), (int)mainViewport.getHeight());
			
			//Position pointer
			String timeStamp = TimelineView.timeStampFormat.getTimeStamp(framePos);
			float pointerWidth = (font.getStringWidth(timeStamp)) / 2.0f + SMALL_MARGIN;
			float alignmentRatio = ((numberLineViewport.getWidth() - 1.0f) / numberLineViewport.getWidth());
			float pointerPos = amount * numberLineViewport.getWidth() * alignmentRatio;
			float pointerHeadPos = Math.max(pointerWidth, Math.min(pointerPos, numberLineViewport.getWidth() - pointerWidth));
			pointer.left = (int) (pointerHeadPos - pointerWidth);
			pointer.right = (int) (pointerHeadPos + pointerWidth);
			
			//Render pointer
			UIrender.drawGradientRect(Direction.TO_RIGHT, (int) pointerPos - FADE_HEIGHT / 4, pointer.getHeight(), (int) pointerPos, mainViewport.getHeight(), FADE_2, FADE_1);
			UIrender.drawGradientRect(Direction.TO_RIGHT, (int) pointerPos + 1, pointer.getHeight(), (int) pointerPos + FADE_HEIGHT / 4 + 1, mainViewport.getHeight(), FADE_1, FADE_2);
			UIrender.drawVerticalLine((int) pointerPos, pointer.getHeight() - 1, mainViewport.getHeight(), BLUE_1);
			pointer.pushMatrix(false);
			{
				UIrender.drawGradientRect(Direction.TO_LEFT, -FADE_HEIGHT, 0, 0, pointer.getHeight(), FADE_1, FADE_2);
				UIrender.drawGradientRect(Direction.TO_RIGHT, pointer.getWidth(), 0, pointer.getWidth() + FADE_HEIGHT / 2, pointer.getHeight(), FADE_1, FADE_2);
				UIrender.drawGradientRect(0, 0, pointer.getWidth(), pointer.getHeight(), BLUE_1, BLUE_2);
				UIrender.drawString(Anchor.TOP_CENTER, timeStamp, pointer.getWidth() / 2, stringCenterY, 0xFFFFFFFF);
			}
			pointer.popMatrix();
			
			//Darken timeline if not active
			if(!parentWidget.session.isActive()) UIrender.drawRect(0, 0, mainViewport.getWidth(), mainViewport.getHeight(), GraphicsHelper.getIntColor(GuiStyle.Gui.backroundColor()));
		}
		mainViewport.popMatrix();
	}
	
	private void positionPointer(int cursorX, int cursorY)
	{
		if(numberLineViewport == null || mainViewport == null) return;
		//Begin dragging pointer if mouse is pressed on it
		if(mainViewport.isHovered(cursorX, cursorY) && leftButtonIsPressed && parentWidget.session == SessionType.REPLAY && (parentWidget.timeline.isPaused() || parentWidget.timeline.hasStopped()) && onCurrentZlevel())
			pointerIsDragging = true;
		else if(pointerIsDragging) pointerIsDragging = false;
		
		//Set timeline position equals to cursor if dragging
		if(pointerIsDragging) parentWidget.timeline.setFracTime((cursorX - numberLineViewport.getAbsoluteLeft() * 1.0f) / numberLineViewport.getWidth());
	}
	
	/**Renders a checkpoint marker on the timeline.**/
	private void renderCheckpointMarker(Checkpoint c, int numberLineHeight, int timelineWidth, int timelineHeight)
	{
		//Determine position of marker
		double duration = parentWidget.timeline.getDuration() * 20;
		double framePos = c.frameNumber;
		float amount = (float) ((framePos) / duration);
		int x = (int) (amount * timelineWidth);
		
		//Slightly make the marker brighter if it's the one currently selected
		Vector4f color = GraphicsHelper.getFloatColor(c.color);
		if(parentWidget.currentCheckpoint != null && parentWidget.currentCheckpoint == c)
			color.set(color.getX() + 0.25f, color.getY() + 0.25F, color.getZ() + 0.25f, color.getW());

		
		//Render Marker
		float scale = timelineHeight - numberLineHeight;
		
		UIrender.drawIcon("checkpoint", x, numberLineHeight, scale, c.color);
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

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		positionPointer(UIinput.getUICursorX(), UIinput.getUICursorY());
	}

	@Override
	public void onMouseClicked(int button)
	{
		if(button == GLFW.GLFW_MOUSE_BUTTON_1) leftButtonIsPressed = true;
		positionPointer(UIinput.getUICursorX(), UIinput.getUICursorY());
	}

	@Override
	public void onMouseReleased(int button)
	{
		if(button == GLFW.GLFW_MOUSE_BUTTON_1) leftButtonIsPressed = false;
		
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
		int mouseX = UIinput.getUICursorX();
		int mouseY = UIinput.getUICursorY();
		
		//Scroll timeline with mouse wheel
		if(mainViewport != null && mainViewport.isHovered(mouseX, mouseY) && (parentWidget.timeline.isPaused() || parentWidget.timeline.hasStopped()) && onCurrentZlevel())
		{
			int currentFrame = (int) parentWidget.timeline.getProperty("framePos").getValue();
			double oneTick = 1.0 / (parentWidget.timeline.getDuration() * 20.0);
			
			if(0 < scrollAmount) //Goto next frame
			{
				//Snap timeline pos to current frame
				parentWidget.timeline.setFracTime(currentFrame / (parentWidget.timeline.getDuration() * 20.0) + oneTick / 10.0);
				
				parentWidget.timeline.setFracTime(parentWidget.timeline.getFracTime() + oneTick);
			}
			else if(scrollAmount < 0) //Goto previous frame
			{
				//Snap timeline pos to current frame
				parentWidget.timeline.setFracTime(currentFrame / (parentWidget.timeline.getDuration() * 20.0) + oneTick / 10.0);
				
				parentWidget.timeline.setFracTime(parentWidget.timeline.getFracTime() - oneTick);
			}
		}
	}

	@Override
	public void update(SidedUpdate side)
	{
		// TODO Auto-generated method stub
		
	}
}
