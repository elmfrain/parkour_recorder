package com.elmfer.prmod.render;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;

import com.elmfer.prmod.mesh.Meshes;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		this.age++;
		if (this.dead)
			expiredAge++;
	}

	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
	}

	@Override
	public void buildGeometry(VertexConsumer p_107261_, Camera renderInfo, float partialTicks)
	{
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Vec3d vec3d = renderInfo.getPos();
		float x = MathHelper.lerp(partialTicks, (float) this.prevPosX, (float) this.x) - (float) vec3d.x;
		float y = MathHelper.lerp(partialTicks, (float) this.prevPosY, (float) this.y) - (float) vec3d.y;
		float z = MathHelper.lerp(partialTicks, (float) this.prevPosZ, (float) this.z) - (float) vec3d.z;
		float ticks = age + partialTicks;
		float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);

		MinecraftClient mc = MinecraftClient.getInstance();
		double distanceFromCamera = (new Vec3d(this.x + 0.5, this.y, this.z + 0.5)).distanceTo(mc.cameraEntity.getLerpedPos(partialTicks));
		distanceFromCamera *= Math.min(ticks / 20.0f, 1);

		RenderSystem.enableBlend();
		RenderSystem.getModelViewStack().push();
		{
			double expiredAmount = 1.0 - (expiredAge + (dead ? partialTicks : 0)) / expireDuration;
			float scale = (float) (-Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5);
			distanceFromCamera *= expiredAmount;

			RenderSystem.getModelViewStack().translate(x, y, z);
			RenderSystem.getModelViewStack().translate(0, Math.sin(ticks * Math.PI / 20.0) * 0.3 + 1.4, 0);
			RenderSystem.getModelViewStack().translate(0, Math.pow(1.0 - expiredAmount, 2.0) * 5.0, 0);
			RenderSystem.getModelViewStack().scale(scale, scale, scale);
			AxisAngle4f yAngle = new AxisAngle4f((float) Math.toRadians(angle) + (float) (Math.toRadians(-Math.pow(expiredAmount, 2.0) * 720.0)), 0, 1, 0);
			AxisAngle4f zAngle = new AxisAngle4f((float) Math.toRadians(Math.sin(ticks * Math.PI / 20.0) * 10), 0, 0, 1);
			RenderSystem.getModelViewStack().multiply(new Quaternionf(yAngle));
			RenderSystem.getModelViewStack().multiply(new Quaternionf(zAngle));
			RenderSystem.applyModelViewMatrix();

			ShaderProgram posColorShader = GameRenderer.getPositionColorProgram();
			posColorShader.modelViewMat.set(RenderSystem.getModelViewMatrix());
			posColorShader.projectionMat.set(RenderSystem.getProjectionMatrix());
			posColorShader.colorModulator.set(1.0f, 1.0f, 1.0f, (float) ((distanceFromCamera - 0.5) / 3.0));
			
			posColorShader.bind();
			Meshes.get("finish").render(GL30.GL_TRIANGLES);
			posColorShader.unbind();
		}
		RenderSystem.getModelViewStack().pop();
		RenderSystem.applyModelViewMatrix();
	}
	
	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.CUSTOM;
	}
}
