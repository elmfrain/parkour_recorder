package com.elmfer.parkour_recorder.gui;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector4f;

import com.elmfer.parkour_recorder.EventHandler;
import com.elmfer.parkour_recorder.gui.TimelineScreen.SessionType;
import com.elmfer.parkour_recorder.parkour.Checkpoint;
import com.elmfer.parkour_recorder.parkour.PlaybackSession;
import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class TimelineViewport extends Gui
{
	public static double scrollAmount = 0.0;
	
	protected List<GuiButton> buttonList = new ArrayList<GuiButton>();
	private TimelineScreen parentScreen;
	private boolean pointerIsDragging = false;
	
	float start = 0.0f;
	float end = 0.0f;
	
	public TimelineViewport(TimelineScreen parent)
	{
		parentScreen = parent;
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks, GuiViewport viewport)
	{
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		
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
			int sMargin = (int) (4.0f / (new ScaledResolution(mc).getScaleFactor()));
			
			int grad1 = GraphicsHelper.getIntColor(0.15f, 0.15f, 0.15f, 0.9f);
			int grad2 = GraphicsHelper.getIntColor(0.06f, 0.06f, 0.06f, 0.9f);
			
			drawGradientRect(0, 0, viewport.getWidth(), viewport.getHeight(), fade1, fade2);
			drawGradientRect(0, 0, numberLine.getWidth(), numberLine.getHeight(), grad1, grad2);
			
			int nbTimeMarkers = 10;
			int delta = (int) (Math.round((duration / (nbTimeMarkers * nbTimeMarkers))) * nbTimeMarkers);
			float amount = (float) ((framePos) / duration);
			
			if(delta == 0) delta = 5;
			{
				for(float i = 0.0f; i < duration; i += delta)
				{
					float numberX = (float) ((i / duration) * numberLine.getWidth());
					drawCenteredString(font, Integer.toString((int) i), (int) numberX, stringCenterY, 0xFFFFFFFF);
					drawVerticalLine((int) numberX, numberLine.bottom, viewport.bottom, getIntColor(0.4f, 0.4f, 0.4f, 0.5f));
				}
			}
			
			if(EventHandler.session instanceof PlaybackSession)
				for(Checkpoint c : ((PlaybackSession) EventHandler.session).recording.checkpoints)
					renderCheckpointMarker(c, numberLine.getHeight(), viewport.getWidth(), viewport.getHeight());
			
			float pointerWidth = (font.getStringWidth(Integer.toString((int) framePos))) / 2.0f + sMargin;
			float alignmentRatio = ((numberLine.getWidth() - 1.0f) / numberLine.getWidth());
			float pointerPos = amount * numberLine.getWidth() * alignmentRatio;
			float pointerHeadPos = Math.max(pointerWidth, Math.min(pointerPos, numberLine.getWidth() - pointerWidth));
			pointer.left = (int) (pointerHeadPos - pointerWidth);
			pointer.right = (int) (pointerHeadPos + pointerWidth);
			
			int blue1 = GraphicsHelper.getIntColor(0.0f, 0.55f, 1.0f, 1.0f);
			int blue2 = GraphicsHelper.getIntColor(0.0f, 0.2f, 0.3f, 1.0f);
			
			GraphicsHelper.gradientRectToRight((int) pointerPos - fadeHeight / 4, pointer.getHeight(), (int) pointerPos, viewport.getHeight(), fade2, fade1);
			GraphicsHelper.gradientRectToRight((int) pointerPos + 1, pointer.getHeight(), (int) pointerPos + fadeHeight / 4 + 1, viewport.getHeight(), fade1, fade2);
			drawVerticalLine((int) pointerPos, pointer.getHeight() - 1, viewport.getHeight(), blue1);
			pointer.pushMatrix(false);
			{
				GraphicsHelper.gradientRectToRight(0, 0, -fadeHeight / 2, pointer.getHeight(), fade1, fade2);
				GraphicsHelper.gradientRectToRight(pointer.getWidth(), 0, pointer.getWidth() + fadeHeight / 2, pointer.getHeight(), fade1, fade2);
				drawGradientRect(0, 0, pointer.getWidth(), pointer.getHeight(), blue1, blue2);
				drawCenteredString(font, Integer.toString((int)framePos), pointer.getWidth() / 2, stringCenterY, 0xFFFFFFFF);
			}
			pointer.popMatrix();
			
			if(viewport.isHovered(mouseX, mouseY) && Mouse.isButtonDown(0) && parentScreen.session == SessionType.REPLAY && (parentScreen.timeline.isPaused() || parentScreen.timeline.hasStopped()))
				pointerIsDragging = true;
			else if(!Mouse.isButtonDown(0) && pointerIsDragging) pointerIsDragging = false;
			
			if(pointerIsDragging) parentScreen.timeline.setFracTime((mouseX - numberLine.getAbsoluteLeft() * 1.0f) / numberLine.getWidth());
			
			if(!parentScreen.session.isActive()) drawRect(0, 0, viewport.getWidth(), viewport.getHeight(), GraphicsHelper.getIntColor(GuiStyle.Gui.backroundColor()));
		}
		viewport.popMatrix();
	}
	
	private void renderCheckpointMarker(Checkpoint c, int numberLineHeight, int timelineWidth, int timelineHeight)
	{
		double duration = parentScreen.timeline.getDuration() * 20;
		double framePos = c.frameNumber;
		float amount = (float) ((framePos) / duration);
		int x = (int) (amount * timelineWidth);
		Vector4f color = GraphicsHelper.getFloatColor(c.color);
		if(parentScreen.currentCheckpoint != null && parentScreen.currentCheckpoint == c)
			color.translate(0.25f, 0.25f, 0.25f, 0.0f);
		
		int shader = ShaderManager.getGUIShader();
		int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.color(color.getX(), color.getY(), color.getZ());
		GlStateManager.pushMatrix();
		{
			float scale = timelineHeight - numberLineHeight;
			GlStateManager.translate(x, numberLineHeight, 0.0f);
			GlStateManager.scale(scale, -scale, 1.0);
			
			GL20.glUseProgram(shader);
			GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), 1.0f);
			ModelManager.renderModel("checkpoint");
			GL20.glUseProgram(prevShader);
		}
		GlStateManager.popMatrix();
	}
}
