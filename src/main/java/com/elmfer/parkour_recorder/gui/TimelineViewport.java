package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.gui.TimelineScreen.SessionType;
import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TimelineViewport extends AbstractGui implements IGuiEventListener
{
	public static double scrollAmount = 0.0;
	
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
	
	public void init()
	{
		@SuppressWarnings("unchecked")
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
				mouseIsPressed = false;
				pointerIsDragging = false;
			}
		}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks, GuiViewport viewport)
	{
		Minecraft mc = Minecraft.getInstance();
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		GuiViewport numberLine = new GuiViewport(viewport);
		numberLine.bottom = (int) (viewport.getHeight() / 3.0f);
		GuiViewport pointer = new GuiViewport(numberLine);
		
		end = (float) parentScreen.timeline.getDuration();
		double duration = parentScreen.timeline.getDuration() * 20;
		double framePos = parentScreen.timeline.getProperty("framePos").getValue();
		
		int stringCenterY = numberLine.getHeight() / 2 - font.FONT_HEIGHT / 2;
		int fadeHeight = GuiStyle.Gui.fadeHeight();
		
		int fade1 = getIntColor(GuiStyle.Gui.fade1());
		int fade2 = getIntColor(GuiStyle.Gui.fade2());
		
		viewport.pushMatrix(true);
		{
			int sMargin = (int) (4.0f / mc.getMainWindow().getGuiScaleFactor());
			
			int grad1 = GraphicsHelper.getIntColor(0.15f, 0.15f, 0.15f, 0.9f);
			int grad2 = GraphicsHelper.getIntColor(0.06f, 0.06f, 0.06f, 0.9f);
			
			fillGradient(0, 0, viewport.getWidth(), viewport.getHeight(), fade1, fade2);
			fillGradient(0, 0, numberLine.getWidth(), numberLine.getHeight(), grad1, grad2);
			
			int nbTimeMarkers = 10;
			int delta = (int) (Math.round((duration / (nbTimeMarkers * nbTimeMarkers))) * nbTimeMarkers);
			float amount = (float) ((framePos) / duration);
			
			if(delta == 0) delta = 5;
			{
				for(float i = 0.0f; i < duration; i += delta)
				{
					float numberX = (float) ((i / duration) * numberLine.getWidth());
					drawCenteredString(font, Integer.toString((int) i), (int) numberX, stringCenterY, 0xFFFFFFFF);
					vLine((int) numberX, numberLine.bottom, viewport.bottom, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
				}
			}
			
			float pointerWidth = (font.getStringWidth(Integer.toString((int) framePos))) / 2.0f + sMargin;
			pointer.left = (int) (amount * numberLine.getWidth() - pointerWidth);
			pointer.right = (int) (amount * numberLine.getWidth() + pointerWidth);
			
			int blue1 = GraphicsHelper.getIntColor(0.0f, 0.55f, 1.0f, 1.0f);
			int blue2 = GraphicsHelper.getIntColor(0.0f, 0.2f, 0.3f, 1.0f);
			
			pointer.pushMatrix(false);
			{
				GraphicsHelper.gradientRectToRight(0, 0, -fadeHeight / 2, pointer.getHeight(), fade1, fade2);
				GraphicsHelper.gradientRectToRight(pointer.getWidth(), 0, pointer.getWidth() + fadeHeight / 2, pointer.getHeight(), fade1, fade2);
				fillGradient(0, 0, pointer.getWidth(), pointer.getHeight(), blue1, blue2);
				drawCenteredString(font, Integer.toString((int)framePos), pointer.getWidth() / 2, stringCenterY, 0xFFFFFFFF);
				vLine(pointer.getWidth() / 2, pointer.getHeight(), viewport.getHeight(), blue1);
			}
			pointer.popMatrix();
			
			if(viewport.isHovered(mouseX, mouseY) && mouseIsPressed && parentScreen.session == SessionType.REPLAY && (parentScreen.timeline.isPaused() || parentScreen.timeline.hasStopped()))
				pointerIsDragging = true;
			
			if(pointerIsDragging) parentScreen.timeline.setFracTime((mouseX - numberLine.getAbsoluteLeft() * 1.0f) / numberLine.getWidth());
			
			if(!parentScreen.session.isActive()) fill(0, 0, viewport.getWidth(), viewport.getHeight(), GraphicsHelper.getIntColor(GuiStyle.Gui.backroundColor()));
		}
		viewport.popMatrix();
	}
	
	
}
