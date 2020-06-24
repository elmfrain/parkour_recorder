package com.elmfer.parkourhelper.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleFinish extends Particle{
	
	private static final double expireDuration = 20.0;
	private int expiredAge = 0;

	public ParticleFinish(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        particleAge++;
        if(isExpired) expiredAge++;
    }
	
	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		boolean tex2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		GlStateManager.disableTexture2D();
		float x = (float)(posX - interpPosX);
        float y = (float)(posY - interpPosY);
        float z = (float)(posZ - interpPosZ);
        float ticks = particleAge + partialTicks;
        float angle = (float) Math.sin(ticks * Math.PI / 20.0) * 10.0f;
        
        FloatBuffer worldSpaceMatrix = BufferUtils.createFloatBuffer(16);
		FloatBuffer normalSpaceMatrix = BufferUtils.createFloatBuffer(16);
        
		//WorldSpace Matrix Calculation
		GL11.glPushMatrix();
		{
		    GL11.glLoadIdentity();
		    GL11.glTranslated(x, y, z);
		    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, worldSpaceMatrix);
		}
		GL11.glPopMatrix();
		//NormalSpace Matrix Calculation
		GL11.glPushMatrix();
		{
			GL11.glLoadIdentity();
			GL11.glRotatef(angle, 0, 0, 1);
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, normalSpaceMatrix);
		}
		GL11.glPopMatrix();
		
        int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int shader = ShaderManager.getDefaultShader();
        OpenGlHelper.glUseProgram(shader);
        GL11.glPushMatrix();
        {
        	double distance = (new Vec3d(posX, posY, posZ)).distanceTo(Minecraft.getMinecraft().player.getPositionVector());
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
			GlStateManager.enableTexture2D();
		OpenGlHelper.glUseProgram(prevShader);
	}
}