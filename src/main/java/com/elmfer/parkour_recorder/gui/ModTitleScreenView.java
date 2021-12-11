package com.elmfer.parkour_recorder.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.ParkourRecorderMod;
import com.elmfer.parkour_recorder.animation.Smoother;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.UIrender.Direction;
import com.elmfer.parkour_recorder.gui.widgets.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class ModTitleScreenView extends Widget implements IMenuTabView
{
	private static String changelog = null;
	private Viewport changelogViewport = new Viewport();
	private Smoother changelogScrool = new Smoother();
	
	private Smoother fovTransition = new Smoother();
	private Smoother logoAngleSpeed = new Smoother();
	private float prevLogoRotation = -90.0f;
	private float logoRotation = -90.0f;
	private int logoOpacityCounter = 0;
	private int userControlCooldown = 40;
	private float prevMouseX = 0.0f;
	private float cursorSpeed = 0.0f;
	
	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		cursorSpeed = mouseX - prevMouseX;
		prevMouseX = mouseX;
		
		if(userControlCooldown == -2) userControlCooldown = -1;
	}

	@Override
	public void onMouseClicked(int button)
	{
		// TODO Auto-generated method stub
		if(button == 0 && UIinput.getUICursorY() > 15 && !changelogViewport.isHovered(UIinput.getUICursorX(), UIinput.getUICursorY()))
		{
			userControlCooldown = -2;
		}
	}

	@Override
	public void onMouseReleased(int button)
	{
		// TODO Auto-generated method stub
		if(button == 0)
		{
			if(userControlCooldown == -2) userControlCooldown = 40;
			else if(userControlCooldown == -1) userControlCooldown = 0;
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
			userControlCooldown = 15;
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
		
		if(changelog == null)
		{
			loadChangelog();
		}
	}

	@Override
	public void update(SidedUpdate side)
	{
		if(side == SidedUpdate.CLIENT)
		{
			if(logoOpacityCounter < 25 && isVisible()) logoOpacityCounter++;
			
			prevLogoRotation = logoRotation;
			logoRotation += logoAngleSpeed.getValue();
			
			if(userControlCooldown >= 0) userControlCooldown++;
			fovTransition.grab(38.0f);
			
			if(userControlCooldown >= 40 || userControlCooldown == -2) logoAngleSpeed.grab(1.8);
			else if(userControlCooldown == -1) logoAngleSpeed.grab(cursorSpeed * 3);
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
		
		String tagline = "by elmfer - v" + ParkourRecorderMod.MOD_VERSION;
		
		UIrender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 12, -10066330);
		UIrender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 13, 0xFFFFFFFF);
		
		yCursor += 8;
		UIrender.drawString(Anchor.CENTER, I18n.format("com.elmfer.record_and_playback_your_parkour_sessions"),uiWidth / 2, yCursor + 1, -10066330);
		UIrender.drawString(Anchor.CENTER, I18n.format("com.elmfer.record_and_playback_your_parkour_sessions"),uiWidth / 2, yCursor, 0xFFFFFFFF);
		
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
		if(changelog != null)
		{
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
					for(String line : changelog.split("\n"))
					{
						if(UIrender.getStringWidth(line) > changelogViewport.getWidth())
						{
							List<String> sublines = splitStringToFit(changelogViewport.getWidth(), line);
							for(String subline : sublines)
							{
								UIrender.drawString(subline, 0, yCursor, 0xFFFFFFFF);
								yCursor += UIrender.getStringHeight() + 2;
							}
						}
						else
						{
							UIrender.drawString(line, 0, yCursor, 0xFFFFFFFF);
							yCursor += UIrender.getStringHeight() + 2;
						}
					}
					final float MIN_SCROLL = 0;
					final float MAX_SCROLL = yCursor - changelogViewport.getHeight() / 2;
					if(changelogScrool.getValue() < 0) changelogScrool.grab(MIN_SCROLL);
					else if(changelogScrool.getValue() > MAX_SCROLL) changelogScrool.grab(MAX_SCROLL);
				}
				GL11.glPopMatrix();
			}
			changelogViewport.popMatrix();
		}
		
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
	}
	
	public void onExit()
	{
		ModLogoRenderer.unload();
		changelog = null;
	}
	
	private static List<String> splitStringToFit(float maxWidth, String str)
	{
		List<String> sublines = new ArrayList<>();
		
		//If width is too small to work with, give up and return the original string. (Prevents an infinite loop that may occur
		//if a 
		if(maxWidth < 40.0f)
		{
			sublines.add(str);
			return sublines;
		}
		
		//Create indents
		String indents = "";
		for(int i = 0; i < str.length(); i++)
		{
			if(str.charAt(i) == ' ') indents += str.charAt(i);
			else break;
		}
		
		//Split lines while preserving thier formatting
		float xCoord = 0.0f;
		String prevTextFormatting = "";
		String textFormatting = "";
		for(int i = 0, s = 0; i < str.length(); i++)
		{
			float charWidth = UIrender.getCharWidth(str.charAt(i));
			xCoord += charWidth;
			
			if(str.charAt(i) == '§' && i + 1 < str.length())
			{
				prevTextFormatting += str.substring(i, i + 2);
				i++;
				continue;
			}
			
			if(i + 1 == str.length()) sublines.add(textFormatting + str.substring(s, i + 1));
			else if(maxWidth - 30 < xCoord)
			{
				sublines.add(textFormatting + str.substring(s, i - 1) + '-');
				str = str.substring(0, i - 1) + indents + str.substring(i - 1);
				s = i - 1;
				xCoord = 0.0f;
				textFormatting = prevTextFormatting;
			}
		}
		
		return sublines;
	}
	
	private static void loadChangelog()
	{
		ResourceLocation loc = new ResourceLocation(ParkourRecorderMod.MOD_ID, "changelog.txt");
		
		try
		{
			InputStream file = Minecraft.getInstance().getResourceManager().getResource(loc).getInputStream();
			Scanner scanner = new Scanner(file, "utf-8");
			
			changelog = "";
			while(scanner.hasNextLine())
			{
				changelog += scanner.nextLine() + '\n';
			}
			
			scanner.close();
			file.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			changelog = "Failed to load changelog!";
		}
	}
}
