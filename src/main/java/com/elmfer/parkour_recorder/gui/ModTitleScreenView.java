package com.elmfer.parkour_recorder.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.ParkourRecorderMod;
import com.elmfer.parkour_recorder.animation.Smoother;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.UIrender.Direction;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import com.elmfer.parkour_recorder.util.HTTPSfetcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class ModTitleScreenView extends Widget implements IMenuTabView
{
	private static HTTPSfetcher changelog = null;
	private static String offlineChangelog = null;
	private Viewport changelogViewport = new Viewport();
	private Smoother changelogScrool = new Smoother();
	
	private Smoother fovTransition = new Smoother();
	private Smoother logoAngleSpeed = new Smoother();
	private float prevLogoRotation = -90.0f;
	private float logoRotation = -90.0f;
	private int logoOpacityCounter = 0;
	/**
	 * The state for which the user controls the rotation of the mod's logo
	 * 
	 * At -2: Logo rotates on its own, but waits for user to move the cursor before giving control of the rotation to user
	 * At -1: User has control of the rotation
	 * At 0 -> 39: No rotation input, but counts up to 40 in this phase
	 * At 40 <=: The logo rotates on its own
	 */
	private int userControlState = 40;
	private float prevMouseX = 0.0f;
	private float cursorSpeed = 0.0f;
	
	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		cursorSpeed = mouseX - prevMouseX;
		prevMouseX = mouseX;
		
		if(userControlState == -2) userControlState = -1;
	}

	@Override
	public void onMouseClicked(int button)
	{
		// TODO Auto-generated method stub
		if(button == 0 && UIinput.getUICursorY() > 15 && !changelogViewport.isHovered(UIinput.getUICursorX(), UIinput.getUICursorY()))
		{
			userControlState = -2;
		}
	}

	@Override
	public void onMouseReleased(int button)
	{
		// TODO Auto-generated method stub
		if(button == 0)
		{
			if(userControlState == -2) userControlState = 40;
			else if(userControlState == -1) userControlState = 0;
		}
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
		if(changelogViewport.isHovered(UIinput.getUICursorX(), UIinput.getUICursorY()))
		{
			changelogScrool.grab(changelogScrool.grabbingTo() + scrollAmount * 0.2);
		}
		else
		{
			logoRotation += scrollAmount * 0.15f;
			userControlState = 15;
		}
	}

	@Override
	public void refresh()
	{
		// TODO Auto-generated method stub
		if(!ModLogoRenderer.isLoaded())
		{
			ModLogoRenderer.load();
			prevLogoRotation = -90.0f;  
			logoRotation = -90.0f;      
		}
		
		logoAngleSpeed.setValueAndGrab(1.8);
		logoAngleSpeed.setSpeed(5.0);
		
		fovTransition.setSpeed(1.8);
		if(logoOpacityCounter < 4) fovTransition.setValue(10.0);
		
		if(offlineChangelog == null)
			loadOfflineChangelog();
		if(changelog == null)
			changelog = new HTTPSfetcher("https://prmod.elmfer.com/changelog.txt");
	}

	@Override
	public void update(SidedUpdate side)
	{
		if(side == SidedUpdate.CLIENT)
		{
			if(logoOpacityCounter < 25 && isVisible()) logoOpacityCounter++;
			
			prevLogoRotation = logoRotation;
			logoRotation += logoAngleSpeed.getValue();
			
			if(userControlState >= 0) userControlState++;
			fovTransition.grab(38.0f);
			
			if(userControlState >= 40 || userControlState == -2) logoAngleSpeed.grab(1.8);
			else if(userControlState == -1) logoAngleSpeed.grab(cursorSpeed * 3);
			else logoAngleSpeed.grab(0.0);
			
			cursorSpeed = 0.0f;
		}
	}

	@Override
	public void draw()
	{
		if (!isVisible())
			return;
		
		float uiWidth = UIrender.getUIwidth();
		float uiHeight = UIrender.getUIheight();
		float yCursor = UIrender.getStringHeight() * 4 + 19;
		
		//Draw main body
		UIrender.drawGradientRect(uiWidth / 4 - 10, 15, uiWidth * 3 / 4 + 10, uiHeight * 5 / 6, 1711276032, 0);
		UIrender.drawRect(uiWidth / 3, 18, uiWidth * 2 / 3, yCursor, 1275068416);
		UIrender.drawGradientRect(Direction.TO_RIGHT, uiWidth / 4 - 10, 18, uiWidth / 3, yCursor, 0, 1275068416);
		UIrender.drawGradientRect(Direction.TO_LEFT, uiWidth * 2 / 3, 18, uiWidth * 3 / 4 + 10, yCursor, 0, 1275068416);
		
		//Render Title
		String title = I18n.format("com.elmfer.parkour_recorder");
		float titleWidth = UIrender.getStringWidth(title);
		GL11.glPushMatrix();
		{
			GL11.glScalef(2.0f, 2.0f, 1.0f);
			UIrender.drawString(Anchor.TOP_CENTER, title, uiWidth / 4, 13.5f, -10066330);
			UIrender.drawString(Anchor.TOP_CENTER, title, uiWidth / 4, 12.5f, 0xFFFFFFFF);
		}
		GL11.glPopMatrix();
		
		//Render tagline
		String tagline = "by elmfer - v" + ParkourRecorderMod.MOD_VERSION;
		UIrender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 12, -10066330);
		UIrender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 13, 0xFFFFFFFF);
		
		//Render slogan
		yCursor += 8;
		UIrender.drawString(Anchor.CENTER, I18n.format("com.elmfer.record_and_playback_your_parkour_sessions"), uiWidth / 2, yCursor + 1, -10066330);
		UIrender.drawString(Anchor.CENTER, I18n.format("com.elmfer.record_and_playback_your_parkour_sessions"), uiWidth / 2, yCursor, 0xFFFFFFFF);
		
		//Render changelog title and body
		changelogViewport = new Viewport();
		changelogViewport.left = (int) uiWidth / 4 - 5;
		changelogViewport.top = uiHeight * 0.75f;
		changelogViewport.right = uiWidth * 3 / 4 + 5;
		changelogViewport.bottom = uiHeight;
		
		changelogViewport.pushMatrix(false);
		{
			UIrender.drawRect(0, 0, changelogViewport.getWidth(), changelogViewport.getHeight(), 2130706432);
			UIrender.drawRect(0, 0, changelogViewport.getWidth(), 12, 1711276032);
			UIrender.drawString(Anchor.MID_LEFT, I18n.format("com.elmfer.changelog"), 6, 6, 0xFFFFFFFF);
		}
		changelogViewport.popMatrix();
		
		//Render the changelog
		changelogViewport.top += 17;
		changelogViewport.left += 5;
		changelogViewport.right -= 5;
		changelogViewport.bottom -= 5;
		changelogViewport.pushMatrix(true);
		{
			GL11.glPushMatrix();
			{
				GL11.glTranslated(0.0, -changelogScrool.getValue(), 0.0);
				
				yCursor = 0.0f;
				//Process changelog line per line
				String changelogText = changelog.hasFailed() ? changelog.stringContent() + "\n" + offlineChangelog : changelog.stringContent();
				for(String line : changelogText.split("\n"))
				{	
					String formats = "";
					int linePos = 0;
					int lineLen = line.length();
					
					//Determine indentation
					float xCursor = 0.0f;
					int i = 0;
					for(; i < lineLen; i++)
					{
						if(line.charAt(i) != ' ') break;
						xCursor += 5.0f;
						lineLen--;
					}
					line = line.substring(i);
					
					//Render the line with as much space as it needs
					//Wraps the line around when it reaches the width of this viewport
					//All text formatting is preserved for each line
					do
					{
						String subline = UIrender.splitStringToFit(line.substring(linePos), changelogViewport.getWidth() - xCursor, " ");
						UIrender.drawString(formats + subline, xCursor, yCursor, 0xFFFFFFFF);
						linePos += subline.length();
						formats += UIrender.getTextFormats(subline);
						
						yCursor += UIrender.getStringHeight() + 2;
					} while(linePos < lineLen);
				}
				
				final float MIN_SCROLL = 0;
				final float MAX_SCROLL = yCursor - changelogViewport.getHeight() / 2;
				if(changelogScrool.getValue() < 0) changelogScrool.grab(MIN_SCROLL);
				else if(changelogScrool.getValue() > MAX_SCROLL) changelogScrool.grab(MAX_SCROLL);
			}
			GL11.glPopMatrix();
		}
		changelogViewport.popMatrix();
		
		//Draw the 3D logo
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		Matrix4f perspectiveMatrix = Matrix4f.perspective((float) fovTransition.getValue(), (float) UIrender.getWindowWidth() / (float) UIrender.getWindowHeight(), 0.05f, 50.0f);
		FloatBuffer perspective = BufferUtils.createFloatBuffer(16);
		perspectiveMatrix.write(perspective);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0f, 0.04f, 0.0f);
		GL11.glMultMatrixf(perspective);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		{
			GL11.glLoadIdentity();
			GL11.glRotatef(5.2f, 1.0f, 0.0f, 0.0f);
			GL11.glTranslatef(0.0f, -1.51f, -10.0f);
			GL11.glRotatef(prevLogoRotation + (logoRotation - prevLogoRotation) * UIrender.getPartialTicks(), 0.0f, 1.0f, 0.0f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, logoOpacityCounter / 25.0f);
			ModLogoRenderer.render();
		}
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		//End draw 3D logo
	}
	
	public void onExit()
	{
		ModLogoRenderer.unload();
		changelog = null;
		offlineChangelog = null;
	}

	private static void loadOfflineChangelog()
	{
		ResourceLocation loc = new ResourceLocation(ParkourRecorderMod.MOD_ID, "changelog.txt");
		
		try
		{
			InputStream file = Minecraft.getInstance().getResourceManager().getResource(loc).getInputStream();
			Scanner scanner = new Scanner(file, "utf-8");
			
			offlineChangelog = "";
			while(scanner.hasNextLine())
			{
				offlineChangelog += scanner.nextLine() + '\n';
			}
			
			scanner.close();
			file.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			offlineChangelog = "Failed to load changelog!";
		}
	}

}
