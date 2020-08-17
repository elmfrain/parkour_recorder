package com.elmfer.parkour_recorder.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleArrow extends Particle{

	public ParticleArrow(World worldIn, double posXIn, double posYIn, double posZIn) {
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
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		boolean tex2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		GlStateManager.disableTexture2D();
		float x = (float)(posX - interpPosX);
        float y = (float)(posY - interpPosY);
        float z = (float)(posZ - interpPosZ);
        float ticks = particleAge + partialTicks;
        float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);
        
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
			GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, normalSpaceMatrix);
		}
		GL11.glPopMatrix();
		
        int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int shader = ShaderManager.getDefaultShader();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL20.glUseProgram(shader);
        GL11.glPushMatrix();
        {
        	GlStateManager.enableCull();
        	int prevFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        	GL11.glTranslated(x, y, z);
        	ShaderManager.importMatricies(ShaderManager.getDefaultShader(), worldSpaceMatrix, normalSpaceMatrix);
        	
        	GL20.glUniform1i(GL20.glGetUniformLocation(shader, "enableWhiteScreen"), 1);
        	GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), 1.0f, 1.0f, 1.0f, 2.0f);
        	GlStateManager.depthFunc(GL11.GL_LEQUAL);
    		ModelManager.renderModel("box");
    		
    		GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), 1.0f, 1.0f, 1.0f, 1.0f);
        	GlStateManager.depthFunc(GL11.GL_GREATER);
    		ModelManager.renderModel("box");
    		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "enableWhiteScreen"), 0);
    		
    		GlStateManager.depthFunc(prevFunc);
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        {
        	double distance = (new Vec3d(posX + 0.5, posY, posZ + 0.5)).distanceTo(Minecraft.getMinecraft().getRenderViewEntity().getPositionVector());
        	distance *= Math.min(ticks / 20.0f, 1);
        	double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
        	GL11.glTranslated(x, y, z);
        	GL11.glTranslated(0, Math.sin((particleAge + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
        	GL11.glScaled(scale, scale, scale);
        	GL11.glRotatef(angle, 0, 1, 0);
        	GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), 1.0f, 1.0f, 1.0f, (float) ((distance - 0.5) / 3.0));
        	ShaderManager.importMatricies(ShaderManager.getDefaultShader(), worldSpaceMatrix, normalSpaceMatrix);
    		ModelManager.renderModel("arrow");
        }
        GL11.glPopMatrix();
        
        GlStateManager.enableAlpha();
		if(tex2DEnabled)
			GlStateManager.enableTexture2D();
		GL20.glUseProgram(prevShader);
	}
}
