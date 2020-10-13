package com.elmfer.parkour_recorder.gui.alertbox;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;
import static com.elmfer.parkour_recorder.render.GraphicsHelper.gradientRectToRight;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.elmfer.parkour_recorder.gui.GuiStyle;
import com.elmfer.parkour_recorder.gui.GuiViewport;
import com.elmfer.parkour_recorder.gui.widget.GuiButton;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

abstract public class GuiAlertBox extends Screen {

	private boolean shouldClose = false;
	protected GuiViewport viewport;
	protected Screen parentScreen;
	protected int height = 40;
	
	public GuiAlertBox(String titleIn, Screen parent)
	{
		super(new StringTextComponent(titleIn));
		parentScreen = parent;
	}
	
	@Override
	public void init()
	{
		buttons.clear();
		GuiButton.currentZLevel = 1;
		GuiButton closeButton = new GuiButton(0, 0, "", this::close) 
		{
			@Override
			public void renderButton(int mouseX, int mouseY, float partialTicks)
			{
				if(visible)
				{
					preRender(mouseX, mouseY, partialTicks);
					int color = isHovered && active ? getIntColor(0.8f, 0.0f, 0.0f, 0.9f) : getIntColor(0.5f, 0.0f, 0.0f, 0.8f);
					
					fill(x, y, x + width, y + height, color);
				}
			}
		};
		addButton(closeButton);
	}
	
	@Override
	protected <T extends Widget> T addButton(T widget)
	{
		super.addButton(widget);
		@SuppressWarnings("unchecked")
		List<IGuiEventListener> pChildren = (List<IGuiEventListener>) parentScreen.children();
		pChildren.add(widget);
		return widget;
	}
	
	@Override
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		if(keyID == GLFW.GLFW_KEY_ESCAPE) { setShouldClose(true); return true; }
		return false;
	}
	
	public void setShouldClose(boolean shouldClose)
	{
		this.shouldClose = shouldClose;
		if(shouldClose)
		{
			parentScreen.children().removeAll(children);
			GuiButton.currentZLevel = 0;
		}
	}
	
	public boolean shouldClose()
	{
		return shouldClose;
	}
	
	abstract protected void doDrawScreen(int mouseX, int mouseY, float partialTicks);
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		
		int boxWidth = GuiStyle.AlertBox.boxWidth();
		int  titleHeight = GuiStyle.AlertBox.titleHeight();
		int margin = (int) GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		
		int fade1 = getIntColor(0.0f, 0.0f, 0.0f, 0.9f);
		int fade2 = getIntColor(0.0f, 0.0f, 0.0f, 0.4f);
		
		GuiViewport all = new GuiViewport(res);
		GuiViewport box = new GuiViewport(all);
		box.left = all.getWidth() / 2 - boxWidth / 2;
		box.top = (int) (all.getHeight() / 2.0f - (titleHeight + margin * 2.0f + height) / 2.0f);
		box.right = all.getWidth() / 2 + boxWidth / 2;
		box.bottom = (int) (all.getHeight() / 2.0f + (titleHeight + margin * 2.0f + height) / 2.0f);
		GuiViewport title = new GuiViewport(box);
		title.bottom = mc.fontRenderer.FONT_HEIGHT * 2;
		viewport = new GuiViewport(box);
		viewport.left = margin; viewport.top = title.bottom + margin;
		viewport.right -= margin;
		viewport.bottom -= margin;
		
		box.pushMatrix(false);
		{
			fill(-smallMargin, -smallMargin, box.getWidth() + smallMargin, box.getHeight() + smallMargin, getIntColor(0.0f, 0.0f, 0.0f, 1.0f));
			
			fill(0, 0, box.getWidth(), box.getHeight(), getIntColor(0.15f, 0.15f, 0.15f, 1.0f));
		}
		box.popMatrix();
		
		title.pushMatrix(false);
		{
			gradientRectToRight(0, 0, title.getWidth(), title.getHeight(), fade1, fade2);
			
			drawString(mc.fontRenderer, getTitle().getString(), margin, title.getHeight() / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
			
			GuiButton closeButton = (GuiButton) buttons.get(0);
			closeButton.setHeight(title.getHeight() - smallMargin * 2);
			closeButton.setWidth(closeButton.getHeight());
			closeButton.y = smallMargin;
			closeButton.x = title.getWidth() - smallMargin - closeButton.getWidth();
			closeButton.renderButton(mouseX, mouseY, partialTicks);
		}
		title.popMatrix();
		
		doDrawScreen(mouseX, mouseY, partialTicks);
	}
	
	protected void close(Button button)
	{
		setShouldClose(true);
	}
}
