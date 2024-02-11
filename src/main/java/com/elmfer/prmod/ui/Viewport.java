package com.elmfer.prmod.ui;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

public class Viewport {
    public float left = 0;
    public float right = 0;
    public float top = 0;
    public float bottom = 0;
    private List<Viewport> children = new ArrayList<Viewport>();
    private List<Viewport> parents = new ArrayList<Viewport>();
    FloatBuffer guiMatrix = null;
    IntBuffer prevViewport = null;

    public Viewport() {
        right = UIRender.getUIwidth();
        bottom = UIRender.getUIheight();
        guiMatrix = BufferUtils.createFloatBuffer(16);
        RenderSystem.getModelViewMatrix().get(guiMatrix);
    }

    public Viewport(Viewport parent) {
        parent.children.add(this);
        this.parents.addAll(parent.parents);
        this.parents.add(parent);
        right = parent.getWidth();
        bottom = parent.getHeight();
    }

    public boolean isHovered(float mouseX, float mouseY) {
        float left = getAbsoluteLeft();
        float top = getAbsoluteTop();
        float right = left + getWidth();
        float bottom = top + getHeight();
        return mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;
    }

    public float getWidth() {
        return right - left;
    }

    public float getHeight() {
        return bottom - top;
    }

    public float getAbsoluteLeft() {
        if (parents.isEmpty())
            return left;
        else {
            float totalLeft = left;
            for (Viewport parent : parents)
                totalLeft += parent.left;
            return totalLeft;
        }
    }

    public float getAbsoluteTop() {
        if (parents.isEmpty())
            return top;
        else {
            float totalTop = top;
            for (Viewport parent : parents)
                totalTop += parent.top;
            return totalTop;
        }
    }

    protected Viewport getParent() {
        if (!parents.isEmpty())
            return parents.get(parents.size() - 1);
        else
            return null;
    }

    private FloatBuffer getGuiMatrix() {
        if (guiMatrix != null)
            return guiMatrix;
        else
            return parents.get(0).guiMatrix;
    }

    public void pushMatrix(boolean clipping) {
        RenderSystem.getModelViewStack().push();
        if (clipping) {
            prevViewport = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);

            int left = (int) getAbsoluteLeft();
            int top = (int) getAbsoluteTop();
            int bottom = (int) (top + getHeight());
            double factor = UIRender.getUIScaleFactor();
            int x = (int) (left * factor);
            int y = (int) (UIRender.getWindowHeight() - bottom * factor);
            int width = (int) (getWidth() * factor);
            int height = (int) (getHeight() * factor);
            GL11.glViewport(x, y, width, height);
//			RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0f, getWidth(), 0.0f, getHeight(), 1000.0f, 3000.0f));
            Matrix4f ortho = new Matrix4f().setOrtho(0.0f, getWidth(), getHeight(), 0.0f, 1000.0f, 21000.0f);
            RenderSystem.setProjectionMatrix(ortho, VertexSorter.BY_DISTANCE);

            RenderSystem.getModelViewStack().peek().getPositionMatrix().set(getGuiMatrix());
            RenderSystem.applyModelViewMatrix();
        } else {
            RenderSystem.getModelViewStack().peek().getPositionMatrix().set(getGuiMatrix());
            RenderSystem.getModelViewStack().translate(left, top, 0.0f);

            for (int i = parents.size() - 1; i >= 0; i--) {
                Viewport v = parents.get(i);
                if (v.prevViewport == null)
                    RenderSystem.getModelViewStack().translate(v.left, v.top, 0.0f);
                else
                    break;
            }

            RenderSystem.applyModelViewMatrix();
        }
    }

    public void popMatrix() {
        if (prevViewport != null) {
            GL11.glViewport(prevViewport.get(0), prevViewport.get(1), prevViewport.get(2), prevViewport.get(3));
            setupOverlayRendering();
            prevViewport = null;
        }
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

    private void setupOverlayRendering() {
        Matrix4f ortho = new Matrix4f().setOrtho(0.0f, UIRender.getUIwidth(), UIRender.getUIheight(), 0.0f, 1000.0f,
                21000.0f);
        RenderSystem.setProjectionMatrix(ortho, VertexSorter.BY_Z);
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.getModelViewStack().translate(0.0f, 0.0f, -2000.0f);
        RenderSystem.applyModelViewMatrix();
    }
}