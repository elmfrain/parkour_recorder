package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleFinish extends Particle
{

	private static final double expireDuration = 20.0;
	private int expiredAge = 0;

	public ParticleFinish(ClientWorld worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void tick()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		age++;
		if (isExpired)
			expiredAge++;
	}

	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
	}

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
	{
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Vector3d vec3d = renderInfo.getProjectedView();
		float x = (float)(MathHelper.lerp((double)partialTicks, this.prevPosX, this.posX) - vec3d.getX());
	    float y = (float)(MathHelper.lerp((double)partialTicks, this.prevPosY, this.posY) - vec3d.getY());
	    float z = (float)(MathHelper.lerp((double)partialTicks, this.prevPosZ, this.posZ) - vec3d.getZ());
        float ticks = age + partialTicks;
        float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);

        GL15.glActiveTexture(GL15.GL_TEXTURE2);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL15.glActiveTexture(GL15.GL_TEXTURE0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
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

		GL15.glActiveTexture(GL15.GL_TEXTURE2);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL15.glActiveTexture(GL15.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
}