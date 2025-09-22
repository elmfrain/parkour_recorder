package com.elmfer.prmod.mesh;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import com.elmfer.prmod.ParkourRecorder;
import com.elmfer.prmod.mesh.VertexFormat.VertexAttribute;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;

public class Mesh {
    public final String name;
    public final ArrayList<Float> positions = new ArrayList<Float>();
    public final ArrayList<Float> normals = new ArrayList<Float>();
    public final ArrayList<Integer> indices = new ArrayList<Integer>();

    public Optional<ArrayList<Float>> uvs = Optional.empty();
    public Optional<ArrayList<Float>> colors = Optional.empty();

    private boolean isRenderable = false;
    private boolean hasWarnedAboutNotBeingRenderable = false;

    private int glVBO;
    private int glEBO;
    private int glVAO;

    private int numVerticies = -1;
    private int numIndicies = -1;
    
    private GpuBuffer gpuVertexBuffer;
    private GpuBuffer gpuIndexBuffer;
    
    public Mesh(String name) {
        this.name = name;
    }

    public int numVertices() {
        if (numVerticies == -1)
            return positions.size() / 3;
        
        return numVerticies;
    }
    
    public int numIndicies() {
        if (numIndicies == -1)
            return positions.size();
        
        return numIndicies;
    }

    public boolean hasUvs() {
        return uvs.isPresent();
    }

    public boolean hasColors() {
        return colors.isPresent();
    }

    public boolean isRenderable() {
        return isRenderable;
    }
    
    public GpuBuffer getVertexBuffer() {
        return gpuVertexBuffer;
    }
    
    public GpuBuffer getIndexBuffer() {
        return gpuIndexBuffer;
    }

    public void putMeshArrays(MeshBuilder builder) {
        VertexFormat format = builder.getVertexFormat();
        int indexCount = indices.size();

        if (positions.isEmpty() || indexCount == 0)
            return;

        for (int i = 0; i < indexCount; i++) {
            int index = indices.get(i);

            for (VertexAttribute attribute : format.getAttributes()) {
                switch (attribute.getUsage()) {
                case POSITION:
                    putPosition(builder, index);
                    break;
                case UV:
                    putUV(builder, index);
                    break;
                case NORMAL:
                    putNormal(builder, index);
                    break;
                case COLOR:
                    putColor(builder, index);
                    break;
                default:
                    break;
                }
            }
        }
    }

    public void putMeshElements(MeshBuilder builder) {
        VertexFormat format = builder.getVertexFormat();

        if (positions.isEmpty() || indices.isEmpty())
            return;

        int[] indexArray = indices.stream().mapToInt(i -> i).toArray();
        builder.index(indexArray);

        int numVertices = numVertices();

        for (int i = 0; i < numVertices; i++) {
            for (VertexAttribute attribute : format.getAttributes()) {
                switch (attribute.getUsage()) {
                case POSITION:
                    putPosition(builder, i);
                    break;
                case UV:
                    putUV(builder, i);
                    break;
                case NORMAL:
                    putNormal(builder, i);
                    break;
                case COLOR:
                    putColor(builder, i);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    public void uploadToGPU(com.mojang.blaze3d.vertex.VertexFormat format) {
        int numVerticies = numVertices();
        int initialSize = numVertices() * format.getVertexSize();
        
        ByteBuffer indexBackingBuffer = BufferUtils.createByteBuffer(indices.size() * 4);
        indices.forEach(i -> indexBackingBuffer.putInt(i));
        indexBackingBuffer.flip();
        gpuIndexBuffer = RenderSystem.getDevice().createBuffer(() -> name, GpuBuffer.USAGE_INDEX, indexBackingBuffer);
        
        
        try(BufferAllocator bufferAllocator = BufferAllocator.method_72201(initialSize)) {
            BufferBuilder bufferBuilder = new BufferBuilder(bufferAllocator, com.mojang.blaze3d.vertex.VertexFormat.DrawMode.TRIANGLES, format);
            
            for (int i = 0; i < numVerticies; i++)
            for (VertexFormatElement element : format.getElements()) {
                switch(element.usage()) {
                case POSITION:
                    putPosition(bufferBuilder, i);
                    break;
                case UV:
                    putUV(bufferBuilder, i);
                    break;
                case NORMAL:
                    putNormal(bufferBuilder, i);
                    break;
                case COLOR:
                    putColor(bufferBuilder, i);
                    break;
                default:
                    break;
                }
            }
            
            try(BuiltBuffer builtBuffer = bufferBuilder.end()) {
                gpuVertexBuffer = RenderSystem.getDevice().createBuffer(() -> name, GpuBuffer.USAGE_VERTEX, builtBuffer.getBuffer());
            }
        }
        
        clearOriginalData();
    }

    @Deprecated
    public void makeRenderable(VertexFormat format) {
        if (isRenderable)
            return;

        MeshBuilder builder = new MeshBuilder(format);

        putMeshElements(builder);

        ByteBuffer vertexData = builder.getVertexData();
        ByteBuffer indexData = builder.getIndexData();

        glVAO = GL30.glGenVertexArrays();
        glVBO = GL30.glGenBuffers();
        glEBO = GL30.glGenBuffers();

        GL30.glBindVertexArray(glVAO);
        {
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, glVBO);
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexData, GL30.GL_STATIC_DRAW);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, glEBO);
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexData, GL30.GL_STATIC_DRAW);

            format.apply();
        }
        GL30.glBindVertexArray(0);

        isRenderable = true;
        clearOriginalData();
    }

    @Deprecated
    public void render(int mode) {
        if (!isRenderable) {
            if (!hasWarnedAboutNotBeingRenderable) {
                ParkourRecorder.LOGGER.warn("Attempted to render mesh \"%s\" that is not renderable", name);
                hasWarnedAboutNotBeingRenderable = true;
            }
            return;
        }

        GL30.glBindVertexArray(glVAO);
        GL30.glDrawElements(mode, indices.size(), GL30.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    @Deprecated
    private void putPosition(MeshBuilder builder, int index) {
        index *= 3;
        builder.position(positions.get(index), positions.get(index + 1), positions.get(index + 2));
    }

    @Deprecated
    private void putUV(MeshBuilder builder, int index) {
        index *= 2;
        ArrayList<Float> uvs = this.uvs.orElse(null);

        if (uvs != null)
            builder.uv(uvs.get(index), uvs.get(index + 1));
        else
            builder.uv();
    }

    @Deprecated
    private void putNormal(MeshBuilder builder, int index) {
        index *= 3;
        builder.normal(normals.get(index), normals.get(index + 1), normals.get(index + 2));
    }

    @Deprecated
    private void putColor(MeshBuilder builder, int index) {
        index *= 4;
        ArrayList<Float> colors = this.colors.orElse(null);

        if (colors != null)
            builder.color(colors.get(index), colors.get(index + 1), colors.get(index + 2), colors.get(index + 3));
        else
            builder.color();
    }
    
    private void putPosition(BufferBuilder builder, int index) {
        index *= 3;
        builder.vertex(positions.get(index), positions.get(index + 1), positions.get(index + 2));
    }
    
    private void putUV(BufferBuilder builder, int index) {
        index *= 2;
        index *= 2;
        ArrayList<Float> uvs = this.uvs.orElse(null);

        if (uvs != null)
            builder.texture(uvs.get(index), uvs.get(index + 1));
        else
            builder.texture(0, 0);
    }
    
    private void putNormal(BufferBuilder builder, int index) {
        index *= 3;
        builder.normal(normals.get(index), normals.get(index + 1), normals.get(index + 2));
    }
    
    private void putColor(BufferBuilder builder, int index) {
        index *= 4;
        ArrayList<Float> colors = this.colors.orElse(null);

        if (colors != null)
            builder.color(colors.get(index), colors.get(index + 1), colors.get(index + 2), colors.get(index + 3));
        else
            builder.color(-1);
    }
    
    private void clearOriginalData() {
        numVerticies = positions.size() / 3;
        numIndicies = indices.size();
                
        positions.clear();
        normals.clear();
        indices.clear();
        uvs.ifPresent(list -> list.clear());
        colors.ifPresent(list -> list.clear());
    }
}
