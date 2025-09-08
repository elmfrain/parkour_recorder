package com.elmfer.prmod.render;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.elmfer.prmod.mesh.Mesh;
import com.elmfer.prmod.mesh.Meshes;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ParticleArrow extends Particle {

    public ParticleArrow(ClientWorld worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    @Override
    public void tick() {
        lastX = x;
        lastY = y;
        lastZ = z;
        this.age++;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        Vec3d vec3d = camera.getPos();
        float x = MathHelper.lerp(partialTicks, (float) this.lastX, (float) this.x) - (float) vec3d.x;
        float y = MathHelper.lerp(partialTicks, (float) this.lastY, (float) this.y) - (float) vec3d.y;
        float z = MathHelper.lerp(partialTicks, (float) this.lastZ, (float) this.z) - (float) vec3d.z;
        float ticks = age + partialTicks;
        float angle = (float) ((60.0f * Math.log(2 * ticks + 1) + ticks) * 2);

        MinecraftClient mc = MinecraftClient.getInstance();
        double distanceFromCamera = (new Vec3d(this.x + 0.5, this.y, this.z + 0.5))
                .distanceTo(mc.cameraEntity.getLerpedPos(partialTicks));
        distanceFromCamera *= Math.min(ticks / 20.0f, 1);

        RenderSystem.getModelViewStack().pushMatrix();
        
        float scale = (float) (-Math.pow(Math.min(ticks, 25) - 25, 3) / 15625 + 0.5);

        RenderSystem.getModelViewStack().translate(x, y, z);
        RenderSystem.getModelViewStack().translate(0,
                (float) (Math.sin((age + partialTicks) * Math.PI / 20.0) * 0.3 + 1.4), 0);
        RenderSystem.getModelViewStack().scale(scale, scale, scale);
        AxisAngle4f axisAngle = new AxisAngle4f((float) Math.toRadians(angle), 0, 1, 0);
        RenderSystem.getModelViewStack().rotate(axisAngle);

        GpuBufferSlice dynamicTransformsUniform = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(),
                new Vector4f(1.0f, 1.0f, 1.0f, (float) ((distanceFromCamera - 0.5) / 3.0)), new Vector3f(), new Matrix4f(), 0.0f);
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

        Mesh arrowMesh = Meshes.get("arrow");
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "etst",
                framebuffer.getColorAttachmentView(), OptionalInt.empty(), framebuffer.getDepthAttachmentView(),
                OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.DEBUG_QUADS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransformsUniform);
            renderPass.setIndexBuffer(arrowMesh.getIndexBuffer(), IndexType.INT);
            renderPass.setVertexBuffer(0, arrowMesh.getVertexBuffer());
            renderPass.draw(0, arrowMesh.numIndicies());
        }

        RenderSystem.getModelViewStack().popMatrix();

    }
}
