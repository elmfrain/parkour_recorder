package com.elmfer.parkour_recorder.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleArrow extends Particle
{

	public ParticleArrow(ClientWorld worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void tick()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.age++;
	}

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
	{
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
			double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(0, Math.sin((age + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
			GL11.glScaled(scale, scale, scale);
			GL11.glRotatef(angle, 0, 1, 0);
			ModelManager.renderModel("arrow");
		}

		GL11.glPopMatrix();
		
		GL15.glActiveTexture(GL15.GL_TEXTURE2);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL15.glActiveTexture(GL15.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public IParticleRenderType getRenderType() {
		// TODO Auto-generated method stub
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
}
