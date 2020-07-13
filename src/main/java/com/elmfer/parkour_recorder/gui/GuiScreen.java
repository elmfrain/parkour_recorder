package com.elmfer.parkour_recorder.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

/** A class that wraps MC's screen gui **/
public class GuiScreen extends Screen{

	protected final Minecraft mc;
	protected final List<Widget> buttons;
	protected final List<IGuiEventListener> children;
	protected GuiAlertBox alertBox = null;
	
	protected GuiScreen(ITextComponent titleIn)
	{
		super(titleIn);
		buttons = this.field_230710_m_;
		children = this.field_230705_e_;
		mc = Minecraft.getInstance();
	}
	@Override
	public boolean func_231043_a_(double mouseX, double mouseY, double mouseW)
	{
		return onScrollCallback(mouseX, mouseY, mouseW);
	}
	
	@Override
	public void func_230430_a_(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_)
	{
		drawScreen(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}
	
	@Override
	protected void func_231160_c_()
	{
		init();
	}
	
	@Override
	public boolean func_231178_ax__()
	{
		return doesGamePause();
	}
	
	@Override
	protected <T extends IGuiEventListener> T func_230481_d_(T p_230481_1_)
	{
		return addChild(p_230481_1_);
	}
	
	@Override
	protected <T extends Widget> T func_230480_a_(T p_230480_1_)
	{
		return addButton(p_230480_1_);
	}
	
	@Override
	public boolean func_231046_a_(int p_231046_1_, int p_231046_2_, int p_231046_3_)
	{
		return keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}
	
	public boolean keyPressed(int keyID, int scancode, int mods)
	{
		return super.func_231046_a_(keyID, scancode, mods);
	}
	
	public List<IGuiEventListener> children()
	{
		return children;
	}
	
	public boolean onScrollCallback(double mouseX, double mouseY, double mouseW)
	{
		return false;
	}
	
	public void drawScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		for(int i = 0; i < buttons.size(); i++)
		{
			buttons.get(i).func_230430_a_(stack, mouseX, mouseY, partialTicks);
		}
	}
	
	public boolean doesGamePause()
	{
		return true;
	}
	
	protected void init()
	{
		GuiButton.currentZLevel = 0;
	}
	
	protected <T extends Widget> T addButton(T widget)
	{
		buttons.add(widget);
		return addChild(widget);
	}
	
	protected <T extends IGuiEventListener> T addChild(T widget)
	{
		children.add(widget);
		return widget;
	}
}
