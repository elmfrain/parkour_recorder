package com.elmfer.parkour_recorder.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ParticleFinish extends Particle
{

	private static final double expireDuration = 20.0;
	private int expiredAge = 0;

	public ParticleFinish(ClientLevel worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void tick()
	{
		xo = x;
		yo = y;
		zo = z;
		this.age++;
		if (this.removed)
			expiredAge++;
	}

	@Override
	public boolean isAlive()
	{
		return expiredAge < expireDuration;
	}

	@Override
	public void render(VertexConsumer p_107261_, Camera renderInfo, float partialTicks)
	{
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Vec3 vec3d = renderInfo.getPosition();
		float x = (float) (Mth.lerp(partialTicks, xo, this.x) - vec3d.x);
		float y = (float) (Mth.lerp(partialTicks, yo, this.y) - vec3d.y);
		float z = (float) (Mth.lerp(partialTicks, zo, this.z) - vec3d.z);
		float ticks = age + partialTicks;
		float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);

		Minecraft mc = Minecraft.getInstance();
		double distanceFromCamera = (new Vec3(this.x + 0.5, this.y, this.z + 0.5)).distanceTo(mc.cameraEntity.getPosition(partialTicks));
		distanceFromCamera *= Math.min(ticks / 20.0f, 1);

		RenderSystem.enableBlend();
		RenderSystem.getModelViewStack().pushPose();
		{
			double expiredAmount = 1.0 - (expiredAge + (removed ? partialTicks : 0)) / expireDuration;
			float scale = (float) (-Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5);
			distanceFromCamera *= expiredAmount;

			RenderSystem.getModelViewStack().translate(x, y, z);
			RenderSystem.getModelViewStack().translate(0, Math.sin(ticks * Math.PI / 20.0) * 0.3 + 1.4, 0);
			RenderSystem.getModelViewStack().translate(0, Math.pow(1.0 - expiredAmount, 2.0) * 5.0, 0);
			RenderSystem.getModelViewStack().scale(scale, scale, scale);
			RenderSystem.getModelViewStack().mulPose(Quaternion.fromXYZ(0.0f, (float) Math.toRadians(angle), 0.0f));
			RenderSystem.getModelViewStack().mulPose(Quaternion.fromXYZ(0.0f, (float) (Math.toRadians(-Math.pow(expiredAmount, 2.0) * 720.0)), 0.0f));
			RenderSystem.getModelViewStack().mulPose(Quaternion.fromXYZ(0.0f, 0.0f, (float) Math.toRadians(Math.sin(ticks * Math.PI / 20.0) * 10)));
			RenderSystem.applyModelViewMatrix();

			ShaderInstance posColorShader = GameRenderer.getPositionColorShader();
			posColorShader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
			posColorShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			posColorShader.COLOR_MODULATOR.set(1.0f, 1.0f, 1.0f, (float) ((distanceFromCamera - 0.5) / 3.0));
			
			posColorShader.apply();
			ModelManager.renderModel("finish");
			posColorShader.clear();
		}
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
	}

	@Override
	public boolean shouldCull()
	{
        return false;
    }
	
	@Override
	public ParticleRenderType getRenderType()
	{
		return ParticleRenderType.CUSTOM;
	}
}