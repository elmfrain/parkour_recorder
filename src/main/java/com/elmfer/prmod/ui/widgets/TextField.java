package com.elmfer.prmod.ui.widgets;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.elmfer.prmod.animation.Property;
import com.elmfer.prmod.animation.Smoother;
import com.elmfer.prmod.animation.Timeline;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.ui.UIInput;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.UIRender.Stencil;
import com.mojang.blaze3d.systems.RenderSystem;

public class TextField extends Button
{
	private static final float SPACING = 4.0f;
	
	private static long lastTimeCursorMoved = System.currentTimeMillis();
	
	private String title = "";
	private boolean focused = false;
	private int cursorPosition = 0;
	private int secondCursorPosition = 0;
	private int lastCursorPosition = 0;
	private Timeline focusTransition = new Timeline(0.08);
	private Smoother textScroll = new Smoother();
	private Smoother titlePosition = new Smoother();
	private Smoother titleOpacity = new Smoother();
	
	public TextField()
	{
		focusTransition.addProperties(new Property("focus", 0.0, 1.0));
		lastTimeCursorMoved = System.currentTimeMillis();
	}
	
	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		moveCursorWithMouse();
	}

	@Override
	public void onMouseClicked(int button)
	{
		super.onMouseClicked(button);
		
		focused = justPressed();
		
		positionTitle();
		moveCursorWithMouse();
	}

	@Override
	public void onMouseReleased(int button)
	{
		super.onMouseReleased(button);
	}

	@Override
	public void onKeyPressed(int keyCode)
	{
		if(focused)
		{
			if(keyCode == GLFW.GLFW_KEY_BACKSPACE)
			{
				handleBackspace();
			}
			//handleLeftArrow()
			if(keyCode == GLFW.GLFW_KEY_LEFT && cursorPosition != 0)
			{
				cursorPosition--;
				if(!UIInput.isShiftPressed())
					secondCursorPosition = cursorPosition;
				autoScroll(1.0f);
			}
			//handleRightArrow()
			if(keyCode == GLFW.GLFW_KEY_RIGHT)
			{
				cursorPosition = Math.min(++cursorPosition, getText().length());
				if(!UIInput.isShiftPressed())
					secondCursorPosition = cursorPosition;
				autoScroll(1.0f);
			}
			//handleSelectAll
			if(keyCode == GLFW.GLFW_KEY_A && UIInput.isCtrlPressed())
			{
				setCursorAtEnd();
				secondCursorPosition = 0;
			}
		}
	}
	
	@Override
	public void onCharTyped(int charTyped)
	{
		if(focused)
		{
			if (secondCursorPosition == cursorPosition)
			{
				setText(getText().substring(0, cursorPosition) + (char) charTyped + getText().substring(cursorPosition));
				cursorPosition++;
			}
			else
			{
				int firstChar = Math.min(secondCursorPosition, cursorPosition);
				int lastChar = Math.max(secondCursorPosition, cursorPosition);
				setText(getText().substring(0, firstChar) + (char) charTyped + getText().substring(lastChar));
				cursorPosition = firstChar + 1;
			}
			secondCursorPosition = cursorPosition;
			lastTimeCursorMoved = System.currentTimeMillis();
			autoScroll(5.5f);
		}
	}
	
	@Override
	public void onMouseScroll(int scrollAmount)
	{
		if(focused && isHovered())
		{
			double maxScroll = -UIRender.getStringWidth(getText()) + width * 0.6;
			textScroll.grab(Math.min(0.0, Math.max(textScroll.grabbingTo() + scrollAmount * 0.2, maxScroll)));
		}
	}
	
	@Override
	public void update(SidedUpdate side)
	{
		super.update(side);
		
		if(side == SidedUpdate.CLIENT)
		{
			if(cursorPosition != lastCursorPosition)
			{
				lastCursorPosition = cursorPosition;
				lastTimeCursorMoved = System.currentTimeMillis();
			}
			if(getText().length() < 4) autoScroll(5.0f);
			positionTitle();
		}
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public boolean isFocused()
	{
		return focused;
	}

	public void setFocused(boolean focused)
	{
		this.focused = focused;
	}

	public void setCursorAtEnd()
	{
		setCursorAtEnd(true);
	}
	
	public void setCursorAtStart()
	{
		setCursorAtStart(true);
	}
	
	public void setCursorAtEnd(boolean autoScroll)
	{
		cursorPosition = getText().length();
		secondCursorPosition = cursorPosition;
		lastTimeCursorMoved = System.currentTimeMillis();
		
		if(autoScroll)
		{
			float stringWidth = UIRender.getStringWidth(getText());
			double scroll = -(stringWidth - Math.min(stringWidth, width * 0.66));
			textScroll.grab(scroll);
		}
	}
	
	public void setCursorAtStart(boolean autoScroll)
	{
		cursorPosition = 0;
		secondCursorPosition = 0;
		lastTimeCursorMoved = System.currentTimeMillis();

		if(autoScroll) textScroll.grab(0.0);
	}
	
	@Override
	public void draw()
	{
		if(!isVisible()) return;
		
		updateModelviewAndViewportState();
		
		updateTransitions();
		int textColor = getTextColor();
		
		UIRender.drawRect(x, y, x + width, y + height, 1275068416);
		
		Stencil.pushStencilState();
		{
			Stencil.enableTest();
			Stencil.enableWrite();
			Stencil.setOperation(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			Stencil.setFunction(GL11.GL_ALWAYS, 1);
			GL11.glColorMask(false, false, false, false);
			UIRender.drawRect(x + SPACING, y + SPACING, x + width - SPACING, y + height - SPACING, 0XFFFFFFFF);
			GL11.glColorMask(true, true, true, true);
			
			Stencil.disableWrite();
			Stencil.setFunction(GL11.GL_EQUAL, 1);
			
			if(!getIcon().isEmpty())
			{
				float iconOffset = (float) titlePosition.getValue();
				float iconTransparency = (float) titleOpacity.getValue();
				float iconX = x + SPACING + DEFAULT_ICON_SCALE / 2 - iconOffset;
				
				UIRender.drawIcon(getIcon(), iconX, y + height / 2, DEFAULT_ICON_SCALE, GraphicsHelper.getIntColor(0.7f, 0.7f, 0.7f, iconTransparency));
			}
			else
			{
				float titleOffset = (float) titlePosition.getValue();
				float titleTransparency = (float) titleOpacity.getValue();
				
				UIRender.drawString(Anchor.MID_LEFT, title, x + SPACING - titleOffset, y + height / 2, GraphicsHelper.getIntColor(0.7f, 0.7f, 0.7f, titleTransparency));
			}
			
			RenderSystem.getModelViewStack().push();
			{
				float scroll = (float) textScroll.getValue();
				RenderSystem.getModelViewStack().translate(scroll, 0.0, 0.0);
				RenderSystem.applyModelViewMatrix();
				
				if(focused)
				{			
					boolean showCursor = ((System.currentTimeMillis() - lastTimeCursorMoved) / 600) % 2 == 0;
					float cursorXCoord = UIRender.getStringWidth(getText().substring(0, cursorPosition)) + SPACING;
					
					if (secondCursorPosition != cursorPosition)
					{
						float secondCursorXCoord = UIRender.getStringWidth(getText().substring(0, secondCursorPosition)) + SPACING;
						float blueBoxXCoord = cursorXCoord;
						float blueBoxWidth = secondCursorXCoord - cursorXCoord;

						UIRender.drawRect(blueBoxXCoord + x, y + SPACING, blueBoxXCoord + x + blueBoxWidth, y + height - SPACING, -1776726785);
					}
					if (showCursor)
					{
						UIRender.drawRect(cursorXCoord + x, y + SPACING, cursorXCoord + x + 1, y + height - SPACING, 0xFFFFFFFF);
					}
				}
				
				UIRender.drawString(Anchor.MID_LEFT, getText(), x + SPACING, y + height / 2, textColor);
			}
			RenderSystem.getModelViewStack().pop();
			RenderSystem.applyModelViewMatrix();
		}
		Stencil.popStencilState();
	}
	
	private void handleBackspace()
	{
		if(cursorPosition > 0)
		{
			if(secondCursorPosition == cursorPosition)
			{
				setText(getText().substring(0, cursorPosition - 1) + getText().substring(cursorPosition));
				cursorPosition--;
			}
			else
			{
				int firstCharIndex = Math.min(secondCursorPosition, cursorPosition);
				int lastCharIndex = Math.max(secondCursorPosition, cursorPosition);
				
				setText(getText().substring(0, firstCharIndex) + getText().substring(lastCharIndex));
				cursorPosition = firstCharIndex; 
			}
			secondCursorPosition = cursorPosition;
			autoScroll(2.5f);
		}
	}
	
	private void positionTitle()
	{
		if(getText().isEmpty())
		{
			titlePosition.grab(0.0);
			titleOpacity.grab(1.0);
		}
		else
		{
			titlePosition.grab(getIcon().isEmpty() ? UIRender.getStringWidth(title) + 5 : DEFAULT_ICON_SCALE + 5);
			titleOpacity.grab(0.0);
		}
	}
	
	private void moveCursorWithMouse()
	{
		if(isPressed())
		{
			float scroll = (float) textScroll.getValue();
			float cursorX = UIInput.getUICursorX() - mvVpState.xTranslation - x - SPACING - scroll;
			float stringWidth = UIRender.getStringWidth(getText());
			
			if(stringWidth < cursorX)
			{
				cursorPosition = getText().length();
			}
			else
			{
				float xCoord = 0.0f;
				for(int i = 0; i < getText().length(); i++)
				{
					float charWidth = UIRender.getCharWidth(getText().charAt(i));
					xCoord += charWidth;
					
					if(cursorX < xCoord - charWidth / 2.0f)
					{
						cursorPosition = i;
						break;
					}
				}
			}
			
			if(justPressed())
			{
				secondCursorPosition = cursorPosition;
			}
			autoScroll(0.8f);
		}
	}
	
	private void autoScroll(float scrollFactor)
	{
		float cursorXCoord = UIRender.getStringWidth(getText().substring(0, cursorPosition));
		float localCursorXCoord = (float) (cursorXCoord + SPACING + textScroll.grabbingTo());
		float scrollAmount = UIRender.getStringHeight("") * scrollFactor;
		
		if(localCursorXCoord < SPACING)
		{
			textScroll.grab(Math.min(0.0, textScroll.grabbingTo() + scrollAmount));
		}
		else if(width - SPACING < localCursorXCoord)
		{
			textScroll.grab(textScroll.grabbingTo() - scrollAmount);
		}
	}

}
