package com.elmfer.parkour_recorder.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleFinish extends Particle{
	
	private static final double expireDuration = 20.0;
	private int expiredAge = 0;

	public ParticleFinish(ClientWorld worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        age++;
        if(isExpired) expiredAge++;
    }
	
	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
	}
	
	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks)
	{
		boolean tex2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		RenderSystem.disableTexture();
		Vector3d vector3d = renderInfo.getProjectedView();
		float x = (float)(MathHelper.lerp((double)partialTicks, this.prevPosX, this.posX) - vector3d.getX());
	    float y = (float)(MathHelper.lerp((double)partialTicks, this.prevPosY, this.posY) - vector3d.getY());
	    float z = (float)(MathHelper.lerp((double)partialTicks, this.prevPosZ, this.posZ) - vector3d.getZ());
        float ticks = age + partialTicks;
        float angle = (float) Math.sin(ticks * Math.PI / 20.0) * 10.0f;
        
        FloatBuffer worldSpaceMatrix = BufferUtils.createFloatBuffer(16);
		FloatBuffer normalSpaceMatrix = BufferUtils.createFloatBuffer(16);
        
		//WorldSpace Matrix Calculation
		GL11.glPushMatrix();
		{
		    GL11.glLoadIdentity();
		    GL11.glTranslated(x, y, z);
		    GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, worldSpaceMatrix);
		}
		GL11.glPopMatrix();
		//NormalSpace Matrix Calculation
		GL11.glPushMatrix();
		{
			GL11.glLoadIdentity();
			GL11.glRotatef(angle, 0, 0, 1);
			GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, normalSpaceMatrix);
		}
		GL11.glPopMatrix();
		
        int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int shader = ShaderManager.getDefaultShader();
        GL30.glUseProgram(shader);
        GL11.glPushMatrix();
        {
        	double distance = (new Vector3d(posX, posY, posZ)).distanceTo(Minecraft.getInstance().player.getPositionVec());
        	double expiredAmount = 1.0 - (expiredAge + (isExpired ? partialTicks : 0)) / expireDuration;
        	double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
        	distance *= expiredAmount;
        	distance *= Math.min(ticks / 20.0f, 1);
        	GL11.glTranslated(x, y, z);
        	GL11.glTranslated(0, Math.sin(ticks * Math.PI / 20.0) * 0.3 + 1.4, 0);
        	GL11.glTranslated(0, Math.pow(1.0 - expiredAmount, 2.0) * 5.0, 0);
        	GL11.glScaled(scale, scale, scale);
        	GL11.glRotated((60.0 * Math.log(2 * ticks + 1) + ticks / 3) * 2, 0, 1, 0);
        	GL11.glRotated(Math.pow(expiredAmount, 2.0) * 720.0, 0, 1, 0);
        	GL11.glRotatef(angle, 0, 0, 1);
        	GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), 1.0f, 1.0f, 1.0f, (float) ((distance - 0.5) / 3.0));
        	ShaderManager.importMatricies(ShaderManager.getDefaultShader(), worldSpaceMatrix, normalSpaceMatrix);
    		ModelManager.renderModel("finish");
        }
        GL11.glPopMatrix();
		if(tex2DEnabled)
			RenderSystem.enableTexture();
		GL30.glUseProgram(prevShader);
	}

	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.CUSTOM;
	}
}