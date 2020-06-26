package com.elmfer.parkour_recorder.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParticleArrow extends Particle{

	public ParticleArrow(ClientWorld worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        age++;
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
        float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);
        
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
			GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, normalSpaceMatrix);
		}
		GL11.glPopMatrix();
		
        int prevShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int shader = ShaderManager.getDefaultShader();
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
        	double distance = (new Vector3d(posX + 0.5, posY, posZ + 0.5)).distanceTo(Minecraft.getInstance().player.getPositionVec());
        	distance *= Math.min(ticks / 20.0f, 1);
        	double scale = -Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5;
        	GL11.glTranslated(x, y, z);
        	GL11.glTranslated(0, Math.sin((age + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
        	GL11.glScaled(scale, scale, scale);
        	GL11.glRotatef(angle, 0, 1, 0);
        	GL20.glUniform4f(GL20.glGetUniformLocation(shader, "masterColor"), 1.0f, 1.0f, 1.0f, (float) ((distance - 0.5) / 3.0));
        	ShaderManager.importMatricies(ShaderManager.getDefaultShader(), worldSpaceMatrix, normalSpaceMatrix);
    		ModelManager.renderModel("arrow");
        }
        GL11.glPopMatrix();
		if(tex2DEnabled)
			RenderSystem.enableTexture();
		GL30.glUseProgram(prevShader);
	}

	@Override
	public IParticleRenderType getRenderType() {
		// TODO Auto-generated method stub
		return null;
	}
}
