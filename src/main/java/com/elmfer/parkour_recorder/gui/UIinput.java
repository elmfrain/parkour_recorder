package com.elmfer.parkour_recorder.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;

public class UIinput
{
	private static int previousScroll = 0;
	private static int previousMouseX = 0;
	private static int previousMouseY = 0;
	
	private static final List<Consumer<Integer>> SCHEDULED_OPERATIONS = new ArrayList<>();
	private static final List<Listener> INPUT_LISTENERS = new ArrayList<Listener>();
	
	public static void addListener(Listener listener)
	{
		SCHEDULED_OPERATIONS.add((i) -> {INPUT_LISTENERS.add(listener);});
	}
	
	public static void removeListener(Listener listener)
	{
		SCHEDULED_OPERATIONS.add((i) -> {INPUT_LISTENERS.remove(listener);});
	}
	
	public static void clearListeners()
	{
		SCHEDULED_OPERATIONS.add((i) -> {INPUT_LISTENERS.clear();});
	}
	
	/** Returns true if there any listeners listening to inputs. Used to determine if any GUIscreen from this mod is active to clear the stencil buffer.*/
	public static boolean pollInputs()
	{
		SCHEDULED_OPERATIONS.forEach((o) -> { o.accept(0); });
		SCHEDULED_OPERATIONS.clear();
		
		if(!INPUT_LISTENERS.isEmpty())
		{
			handleCursorUpdates();
			handleMouseUpdates();
			handleKeyboardUpdates();
			
			return true;
		}
		
		return false;
	}
	
	public static int getUICursorX()
	{
		return Mouse.getX() / UIrender.getUIScaleFactor();
	}
	
	public static int getUICursorY()
	{
		return (UIrender.getWindowHeight() - Mouse.getY()) / UIrender.getUIScaleFactor();
	}
	
	public static boolean isCtrlPressed()
	{
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
	}
	
	public static boolean isShiftPressed()
	{
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}
	
	public static boolean isAltPressed()
	{
		return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
	}
	
	private static void handleCursorUpdates()
	{
		int uiScale = UIrender.getUIScaleFactor();
		
		int currentMouseX = Mouse.getX();
		int currentMouseY = UIrender.getWindowHeight() - Mouse.getY();
		
		final float cursorX = ((float)(currentMouseX)) / uiScale;
		final float cursorY = ((float)(currentMouseY)) / uiScale;
		
		if(currentMouseX != previousMouseX || currentMouseY != previousMouseY)
		{			
			INPUT_LISTENERS.forEach((w) -> {w.onCursorMove(cursorX, cursorY);});
		}
		
		previousMouseX = currentMouseX;
		previousMouseY = currentMouseY;
	}
	
	private static void handleMouseUpdates()
	{
		if(Mouse.isCreated())
		{
			while(Mouse.next())
			{
				int buttonID = Mouse.getEventButton();
				
				if (Mouse.getEventButtonState())
		        {
		            INPUT_LISTENERS.forEach((w) -> {w.onMouseClicked(buttonID);});
		        }
		        else if (buttonID != -1)
		        {
		        	INPUT_LISTENERS.forEach((w) -> {w.onMouseReleased(buttonID);});
		        }
			}
			
			int currentScroll = Mouse.getDWheel();
			
			if(currentScroll != previousScroll)
			{
				INPUT_LISTENERS.forEach((w) -> {w.onMouseScroll(currentScroll);});
			}
			
			previousScroll = currentScroll;
		}
	}
	
	private static void handleKeyboardUpdates()
	{
		if(Keyboard.isCreated())
		{
			while(Keyboard.next())
			{
				int keyCode = Keyboard.getEventKey();
				char charTyped = Keyboard.getEventCharacter();
				
				if(charTyped != Keyboard.CHAR_NONE && charTyped >= ' ')
				{
					INPUT_LISTENERS.forEach((w) -> {w.onCharTyped(charTyped);});
				}
				if(keyCode != Keyboard.KEY_NONE && Keyboard.getEventKeyState())
				{
					INPUT_LISTENERS.forEach((w) -> {w.onKeyPressed(keyCode);});
				}
				
				Minecraft.getMinecraft().dispatchKeypresses();
			}
		}
	}
	
	public static interface Listener
	{
		public void onCursorMove(float mouseX, float mouseY);

		public void onMouseClicked(int button);
		
		public void onMouseReleased(int button);
		
		public void onKeyPressed(int keyCode);
		
		public void onCharTyped(int charTyped);
		
		public void onMouseScroll(int scrollAmount);
		
		default public void close()
		{
			UIinput.removeListener(this);
		}
	}
}
