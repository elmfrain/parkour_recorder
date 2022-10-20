package com.elmfer.parkour_recorder.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UIinput
{
	private static float scroll = 0.0f;
	private static float previousMouseX = 0;
	private static float previousMouseY = 0;
	private static char mousePressedStates = 0;
	private static char mouseReleasedStates = 0;
	
	private static int charTyped = -1;
	private static int keyPressed = -1;
	private static boolean isAltPressed = false;
	private static boolean isCtrlPressed = false;
	private static boolean isShiftPressed = false;
	
	private static final List<Consumer<Integer>> SCHEDULED_OPERATIONS = new ArrayList<>();
	private static final List<Listener> INPUT_LISTENERS = new ArrayList<>();
	
	private static Minecraft mc = Minecraft.getInstance();
	
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
		
		handleCursorUpdates();
		handleMouseUpdates();
		handleKeyboardUpdates();
		
		//System.out.println(INPUT_LISTENERS.size());
		
		return !INPUT_LISTENERS.isEmpty();
	}
	
	public static int getUICursorX()
	{
		return (int) (mc.mouseHandler.xpos() / UIrender.getUIScaleFactor());
	}
	
	public static int getUICursorY()
	{
		return (int) (mc.mouseHandler.ypos() / UIrender.getUIScaleFactor());
	}
	
	public static boolean isCtrlPressed()
	{
		return isCtrlPressed;
	}
	
	public static boolean isShiftPressed()
	{
		return isShiftPressed;
	}
	
	public static boolean isAltPressed()
	{
		return isAltPressed;
	}
	
	private static void handleCursorUpdates()
	{
		int uiScale = UIrender.getUIScaleFactor();
		
		float currentMouseX = (float) mc.mouseHandler.xpos();
		float currentMouseY = (float) mc.mouseHandler.ypos();
		
		final float cursorX = currentMouseX / uiScale;
		final float cursorY = currentMouseY / uiScale;
		
		if(currentMouseX != previousMouseX || currentMouseY != previousMouseY)
		{			
			INPUT_LISTENERS.forEach((w) -> {w.onCursorMove(cursorX, cursorY);});
		}
		
		previousMouseX = currentMouseX;
		previousMouseY = currentMouseY;
	}
	
	private static void handleMouseUpdates()
	{
		if (mousePressedStates != 0)
        {
			for(int i = 0; i < 8; i++)
			{
				if(((mousePressedStates >>> i) & 1) != 0)
					for(Listener w : INPUT_LISTENERS) w.onMouseClicked(i);
			}
            mousePressedStates = 0;
        }
        if (mouseReleasedStates != 0)
        {
        	for(int i = 0; i < 8; i++)
        	{
        		if(((mouseReleasedStates >>> i) & 1) != 0)
        			for(Listener w : INPUT_LISTENERS) w.onMouseReleased(i);
        	}
        	mouseReleasedStates = 0;
        }
		if(scroll != 0.0f)
		{
			INPUT_LISTENERS.forEach((w) -> {w.onMouseScroll((int) scroll);});
			scroll = 0.0f;
		}
	}
	
	private static void handleKeyboardUpdates()
	{
		if(keyPressed != -1)
		{
			INPUT_LISTENERS.forEach((w) -> {w.onKeyPressed(keyPressed);});
			keyPressed = -1;
		}
		if(charTyped != -1)
		{
			INPUT_LISTENERS.forEach((w) -> {w.onCharTyped(charTyped);});
			charTyped = -1;
		}
	}
	
	@SubscribeEvent
	public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event)
	{
		keyPressed = event.getKeyCode();
		if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_ALT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_ALT)
			isAltPressed = true;
		else if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_CONTROL || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_CONTROL)
			isCtrlPressed = true;
		else if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_SHIFT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_SHIFT)
			isShiftPressed = true;
	}
	
	@SubscribeEvent
	public static void onKeyRelease(ScreenEvent.KeyReleased.Pre event)
	{
		if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_ALT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_ALT)
			isAltPressed = false;
		else if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_CONTROL || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_CONTROL)
			isCtrlPressed = false;
		else if(event.getKeyCode() == GLFW.GLFW_KEY_LEFT_SHIFT || event.getKeyCode() == GLFW.GLFW_KEY_RIGHT_SHIFT)
			isShiftPressed = false;
	}
	
	@SubscribeEvent
	public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event)
	{
		charTyped = event.getCodePoint();
	}
	
	@SubscribeEvent
	public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event)
	{
		mousePressedStates |= 1 << event.getButton();
	}
	
	@SubscribeEvent
	public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event)
	{
		mouseReleasedStates |= 1 << event.getButton();
	}
	
	@SubscribeEvent
	public static void onGuiScroll(ScreenEvent.MouseScrolled.Pre event)
	{
		scroll = (float) event.getScrollDelta() * 100;
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
