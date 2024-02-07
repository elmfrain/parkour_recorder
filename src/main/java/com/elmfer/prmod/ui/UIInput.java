package com.elmfer.prmod.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import net.minecraft.client.MinecraftClient;

public class UIInput {
	private static float scroll = 0.0f;
	private static float previousMouseX = 0;
	private static float previousMouseY = 0;
	private static char mousePressedStates = 0;
	private static char mouseReleasedStates = 0;

	private static int charTyped = -1;
	private static int keyPressed = -1;

	private static final List<Consumer<Integer>> SCHEDULED_OPERATIONS = new ArrayList<>();
	private static final List<Listener> INPUT_LISTENERS = new ArrayList<>();

	private static MinecraftClient mc = MinecraftClient.getInstance();
	private static long glfwWindow = mc.getWindow().getHandle();

	/**
	 * The previous input callbacks are chained to the new ones to allow for
	 * multiple listeners.
	 **/
	private static GLFWKeyCallback prevKeyCallback = GLFW.glfwSetKeyCallback(glfwWindow, UIInput::onKeyPressed);
	private static GLFWCharCallback prevCharCallback = GLFW.glfwSetCharCallback(glfwWindow, UIInput::onCharTyped);
	private static GLFWMouseButtonCallback prevMouseButtonCallback = GLFW.glfwSetMouseButtonCallback(glfwWindow,
			UIInput::onMouseButton);
	private static GLFWScrollCallback prevScrollCallback = GLFW.glfwSetScrollCallback(glfwWindow,
			UIInput::onMouseScroll);

	public static void addListener(Listener listener) {
		SCHEDULED_OPERATIONS.add((i) -> {
			INPUT_LISTENERS.add(listener);
		});
	}

	public static void removeListener(Listener listener) {
		SCHEDULED_OPERATIONS.add((i) -> {
			INPUT_LISTENERS.remove(listener);
		});
	}

	public static void clearListeners() {
		SCHEDULED_OPERATIONS.add((i) -> {
			INPUT_LISTENERS.clear();
		});
	}

	/**
	 * Returns true if there any listeners listening to inputs. Used to determine if
	 * any GUIscreen from this mod is active to clear the stencil buffer.
	 */
	public static boolean pollInputs() {
		SCHEDULED_OPERATIONS.forEach((o) -> {
			o.accept(0);
		});
		SCHEDULED_OPERATIONS.clear();

		handleCursorUpdates();
		handleMouseUpdates();
		handleKeyboardUpdates();

		// System.out.println(INPUT_LISTENERS.size());

		return !INPUT_LISTENERS.isEmpty();
	}

	public static int getUICursorX() {
		return (int) (mc.mouse.getX() / UIRender.getUIScaleFactor());
	}

	public static int getUICursorY() {
		return (int) (mc.mouse.getY() / UIRender.getUIScaleFactor());
	}

	public static boolean isCtrlPressed() {
		return GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
	}

	public static boolean isShiftPressed() {
		return GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
	}

	public static boolean isAltPressed() {
		return GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(glfwWindow, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
	}

	private static void handleCursorUpdates() {
		int uiScale = UIRender.getUIScaleFactor();

		float currentMouseX = (float) mc.mouse.getX();
		float currentMouseY = (float) mc.mouse.getY();

		final float cursorX = currentMouseX / uiScale;
		final float cursorY = currentMouseY / uiScale;

		if (currentMouseX != previousMouseX || currentMouseY != previousMouseY) {
			INPUT_LISTENERS.forEach((w) -> {
				w.onCursorMove(cursorX, cursorY);
			});
		}

		previousMouseX = currentMouseX;
		previousMouseY = currentMouseY;
	}

	private static void handleMouseUpdates() {
		if (mousePressedStates != 0) {
			for (int i = 0; i < 8; i++) {
				if (((mousePressedStates >>> i) & 1) != 0)
					for (Listener w : INPUT_LISTENERS)
						w.onMouseClicked(i);
			}
			mousePressedStates = 0;
		}
		if (mouseReleasedStates != 0) {
			for (int i = 0; i < 8; i++) {
				if (((mouseReleasedStates >>> i) & 1) != 0)
					for (Listener w : INPUT_LISTENERS)
						w.onMouseReleased(i);
			}
			mouseReleasedStates = 0;
		}
		if (scroll != 0.0f) {
			INPUT_LISTENERS.forEach((w) -> {
				w.onMouseScroll((int) scroll);
			});
			scroll = 0.0f;
		}
	}

	private static void handleKeyboardUpdates() {
		if (keyPressed != -1) {
			INPUT_LISTENERS.forEach((w) -> {
				w.onKeyPressed(keyPressed);
			});
			keyPressed = -1;
		}
		if (charTyped != -1) {
			INPUT_LISTENERS.forEach((w) -> {
				w.onCharTyped(charTyped);
			});
			charTyped = -1;
		}
	}

	private static void onKeyPressed(long window, int key, int scancode, int action, int mods) {
		if(prevKeyCallback != null)
			prevKeyCallback.invoke(window, key, scancode, action, mods);

		if (action == GLFW.GLFW_PRESS) {
			keyPressed = key;
		}
	}

	private static void onCharTyped(long window, int codepoint) {
		if(prevCharCallback != null)
			prevCharCallback.invoke(window, codepoint);

		charTyped = codepoint;
	}

	private static void onMouseButton(long window, int button, int action, int mods) {
		if(prevMouseButtonCallback != null)
			prevMouseButtonCallback.invoke(window, button, action, mods);

		if (action == GLFW.GLFW_PRESS) {
			mousePressedStates |= (1 << button);
		} else if (action == GLFW.GLFW_RELEASE) {
			mouseReleasedStates |= (1 << button);
		}
	}

	private static void onMouseScroll(long window, double xoffset, double yoffset) {
		if(prevScrollCallback != null)
			prevScrollCallback.invoke(window, xoffset, yoffset);

		scroll = (float) yoffset * 100;
	}

	public static interface Listener {
		public void onCursorMove(float mouseX, float mouseY);

		public void onMouseClicked(int button);

		public void onMouseReleased(int button);

		public void onKeyPressed(int keyCode);

		public void onCharTyped(int charTyped);

		public void onMouseScroll(int scrollAmount);

		default public void close() {
			UIInput.removeListener(this);
		}
	}
}
