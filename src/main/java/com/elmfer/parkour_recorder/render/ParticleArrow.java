package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleArrow extends Particle
{

	public ParticleArrow(World worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.particleAge++;
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

		GlStateManager.disableTexture2D();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		GlStateManager.pushMatrix();
		{
			double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(0, Math.sin((particleAge + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
			GL11.glScaled(scale, scale, scale);
			GL11.glRotatef(angle, 0, 1, 0);
			ModelManager.renderModel("arrow");
		}

		GlStateManager.popMatrix();

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
	}
}
