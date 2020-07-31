package com.elmfer.parkour_recorder.gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.Vector4f;

public class GuiModeledButton extends GuiButton
{
	public String modelName;
	
	public GuiModeledButton(int x, int y, IPressable pressedCallback, String modelName)
	{
		super(x, y, "", pressedCallback);
		this.modelName = modelName;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		super.renderButton(mouseX, mouseY, partialTicks);
		
		if(visible)
		{
			int j = 14737632;
			if (!active)
	            j = 10526880;
	        else if (isHovered)
	            j = 16777120;
			
			int shader = ShaderManager.getGUIShader();
			int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			float smallMargin = GuiStyle.Gui.smallMargin() * 2;
			float modelSize = height - smallMargin * 2;
			Vector4f color = GraphicsHelper.getFloatColor(j);
			color.setW(1.0f);
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.color3f(color.getX(), color.getY(), color.getZ());
			
			RenderSystem.pushMatrix();
			{
				RenderSystem.translatef(x + width / 2, y + height / 2, 0.0f);
				RenderSystem.scalef(modelSize, -modelSize, 1.0f);
				
				GL20.glUseProgram(shader);
				GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), color.getW());
				ModelManager.renderModel(modelName);
				GL20.glUseProgram(prevShader);
			}
			RenderSystem.popMatrix();
		}
	}
}
