package com.elmfer.prmod.ui;

import org.lwjgl.glfw.GLFW;

import com.elmfer.prmod.ui.widgets.Widget;
import com.elmfer.prmod.ui.window.Window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;

public abstract class UIScreen extends Screen implements UIInput.Listener {
	public UIScreen() {
		super(MutableText
				.of(new TranslatableTextContent("com.prmod.parkour_recorder", "Parkour Recorder", new Object[0])));
		UIInput.addListener(this);
	}

	@Override
	public void finalize() {
		close();
	}

	/** When gui closes. **/
	@Override
	public void removed() {
		Window.closeWindows();
		UIInput.clearListeners();
		Widget.clearWidgets();
	}

	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		return false;
	}

	@Override
	public void onKeyPressed(int keyCode) {
		if (!Window.areWindowsOpen() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.setScreen(null);
		}
	}
}
