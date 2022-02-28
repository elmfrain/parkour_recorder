package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleArrowLoop extends ParticleArrow
{
	public ParticleArrowLoop(World worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		
		float x = (float) (posX - interpPosX);
		float y = (float) (posY - interpPosY);
		float z = (float) (posZ - interpPosZ);
		float ticks = particleAge + partialTicks;
		float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);

		GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		{
			double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(0, Math.sin((particleAge + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
			GL11.glScaled(scale, scale, scale);
			GL11.glRotatef(angle, 0, 1, 0);
			ModelManager.renderModel("arrow_loop_mode");
		}

		GlStateManager.popMatrix();

		GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        
	}
}
