package com.elmfer.parkour_recorder.gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector4f;

import com.elmfer.parkour_recorder.render.GraphicsHelper;
import com.elmfer.parkour_recorder.render.ModelManager;
import com.elmfer.parkour_recorder.render.ShaderManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiModeledButton extends GuiButton
{
	public String modelName;
	
	public GuiModeledButton(int id, int x, int y, String modelName)
	{
		super(id, x, y, "");
		this.modelName = modelName;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		
		if(visible)
		{
			int j = 14737632;
			if (!enabled)
	            j = GraphicsHelper.getIntColor(0.45f, 0.45f, 0.45f, 1.0f);
	        else if (hovered)
	            j = 16777120;
			
			int shader = ShaderManager.getGUIShader();
			int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			float smallMargin = GuiStyle.Gui.smallMargin() * 2;
			float modelSize = height - smallMargin * 2;
			Vector4f color = GraphicsHelper.getFloatColor(j);
			color.setW(1.0f);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.color(color.getX(), color.getY(), color.getZ());
			
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(x + width / 2, y + height / 2, 0.0f);
				GlStateManager.scale(modelSize, -modelSize, 1.0f);
				
				GL20.glUseProgram(shader);
				GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), color.getX(), color.getY(), color.getZ(), color.getW());
				ModelManager.renderModel(modelName);
				GL20.glUseProgram(prevShader);
			}
			GlStateManager.popMatrix();
		}
	}
}
