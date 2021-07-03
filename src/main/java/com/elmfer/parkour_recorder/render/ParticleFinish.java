package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleFinish extends Particle
{

	private static final double expireDuration = 20.0;
	private int expiredAge = 0;

	public ParticleFinish(World worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		particleAge++;
		if (isExpired)
			expiredAge++;
	}

	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
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

		GL11.glPushMatrix();
		{
			double expiredAmount = 1.0 - (expiredAge + (isExpired ? partialTicks : 0)) / expireDuration;
			double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;

			GL11.glTranslated(x, y, z);
			GL11.glTranslated(0, Math.sin(ticks * Math.PI / 20.0) * 0.3 + 1.4, 0);
			GL11.glTranslated(0, Math.pow(1.0 - expiredAmount, 2.0) * 5.0, 0);
			GL11.glScaled(scale, scale, scale);
			GL11.glRotated(angle, 0, 1, 0);
			GL11.glRotated(-Math.pow(expiredAmount, 2.0) * 720.0, 0, 1, 0);
			GL11.glRotated(Math.sin(ticks * Math.PI / 20.0) * 10, 0, 0, 1);

			ModelManager.renderModel("finish");
		}
		GL11.glPopMatrix();

		GlStateManager.enableTexture2D();
	}
}