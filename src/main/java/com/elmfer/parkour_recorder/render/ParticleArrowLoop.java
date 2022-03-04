package com.elmfer.parkour_recorder.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ParticleArrowLoop extends ParticleArrow
{

	public ParticleArrowLoop(ClientLevel worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
	}

	@Override
	public void render(VertexConsumer p_107261_, Camera renderInfo, float partialTicks)
	{
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
			float scale = (float) (-Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5);
			
			RenderSystem.getModelViewStack().translate(x, y, z);
			RenderSystem.getModelViewStack().translate(0, Math.sin((age + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4, 0);
			RenderSystem.getModelViewStack().scale(scale, scale, scale);
			RenderSystem.getModelViewStack().mulPose(Quaternion.fromXYZ(0.0f, (float) Math.toRadians(angle), 0.0f));
			RenderSystem.applyModelViewMatrix();
			
			ShaderInstance posColorShader = GameRenderer.getPositionColorShader();
			posColorShader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
			posColorShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			posColorShader.COLOR_MODULATOR.set(1.0f, 1.0f, 1.0f, (float) ((distanceFromCamera - 0.5) / 3.0));
			
			posColorShader.apply();
			ModelManager.renderModel("arrow_loop_mode");
			posColorShader.clear();
		}
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
	}
}
