package com.elmfer.parkour_recorder.gui.widgets;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.elmfer.parkour_recorder.gui.UIinput;
import com.elmfer.parkour_recorder.gui.UIrender;

public abstract class Widget implements UIinput.Listener
{
	public static final float DEFAULT_WIDTH = 150.0f;
	public static final float DEFAULT_HEIGHT = 20.0f;
	
	private static final List<Consumer<Integer>> SCHEDULED_OPERATIONS = new ArrayList<>();
	private static final List<Widget> LOADED_WIDGETS = new ArrayList<Widget>();
	
	private static int currentZLevel = 0;
	
	public float x = 0.0f, y = 0.0f, width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT;
	
	protected ModelviewAndViewportState mvVpState = new ModelviewAndViewportState();
	
	private final List<Widget> children = new ArrayList<Widget>();
	private boolean hovered = false;
	private boolean visible = true;
	private boolean enabled = true;
	private int zLevel = 0;
	
	public Widget()
	{
		SCHEDULED_OPERATIONS.add(o -> LOADED_WIDGETS.add(this));
		UIinput.addListener(this);
		
		zLevel = currentZLevel;
	}
	
	@Override
	public void close()
	{
		SCHEDULED_OPERATIONS.add(o -> LOADED_WIDGETS.remove(this));
		UIinput.removeListener(this);
		children.forEach(w -> w.close());
	}
	
	abstract public void update(SidedUpdate side);
	
	abstract public void draw();
	
	public boolean isHovered()
	{
		return hovered;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		children.forEach(w -> w.setEnabled(enabled));
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
		children.forEach(w -> w.setVisible(visible));
	}

	public static void updateWidgetsOnClientTick()
	{
		performScheduledOperations();
		//System.out.println("Widget count: " + LOADED_WIDGETS.size());
		LOADED_WIDGETS.forEach((w) -> {w.update(SidedUpdate.CLIENT);});
	}
	
	public static void updateWidgetsOnRenderTick()
	{
		performScheduledOperations();
		//System.out.println("Widget count: " + LOADED_WIDGETS.size());
		LOADED_WIDGETS.forEach((w) -> 
		{
			w.updateHoverState(UIinput.getUICursorX(), UIinput.getUICursorY());
			w.update(SidedUpdate.RENDER);
		});
	}
	
	public static void clearWidgets()
	{
		SCHEDULED_OPERATIONS.add(o -> LOADED_WIDGETS.clear());
	}
	
	public static int getCurrentZLevel()
	{
		return currentZLevel;
	}

	public static void setCurrentZLevel(int currentZLevel)
	{
		Widget.currentZLevel = currentZLevel;
	}

	public int getzLevel()
	{
		return zLevel;
	}

	public void setzLevel(int zLevel)
	{
		this.zLevel = zLevel;
	}
	
	public boolean onCurrentZlevel()
	{
		return this.zLevel == currentZLevel;
	}
	
	public void addWidgets(Widget...widgets)
	{
		for(Widget widget : widgets) this.children.add(widget);
	}
	
	public List<Widget> getChildrenWidgets()
	{
		return children;
	}
	
	protected void updateModelviewAndViewportState()
	{
		mvVpState.updateState();
	}
	
	private static void performScheduledOperations()
	{
		if(!SCHEDULED_OPERATIONS.isEmpty())
		{
			SCHEDULED_OPERATIONS.forEach(o -> o.accept(0));
			SCHEDULED_OPERATIONS.clear();
		}
	}
	
	private void updateHoverState(float cursorX, float cursorY)
	{
		float xTranslation = mvVpState.xTranslation;
		float yTranslation = mvVpState.yTranslation;
		
		float mX = cursorX - xTranslation;
		float mY = cursorY - yTranslation;
		boolean isHoverable = cursorInViewport(cursorX, cursorY) && enabled && visible;
		
		hovered = (zLevel == currentZLevel) && (mX >= this.x && mY >= this.y && mX < this.x + this.width && mY < this.y + this.height && isHoverable);
	}
	
	private boolean cursorInViewport(float cursorX, float cursorY)
	{
		int viewportX = mvVpState.viewportX;
		int viewportY = mvVpState.viewportY;
		int viewportWidth = mvVpState.viewportWidth;
		int viewportHeight = mvVpState.viewportHeight;
		return cursorX >= viewportX && cursorY >= viewportY && cursorX < viewportX + viewportWidth && cursorY < viewportY + viewportHeight;
	}
	
	public static enum SidedUpdate
	{
		CLIENT,
		RENDER;
	}
	
	protected static class ModelviewAndViewportState
	{
		static final FloatBuffer MODELVIEW_MATRIX = BufferUtils.createFloatBuffer(16);
		static final IntBuffer   VIEWPORT_PARAMS  = BufferUtils.createIntBuffer(16);
		
		int viewportX = 0, viewportY = 0, viewportWidth = 0, viewportHeight = 0;
		float xTranslation = 0, yTranslation = 0;
		
		public void updateState()
		{
			int uiScale = UIrender.getUIScaleFactor();
			
			MODELVIEW_MATRIX.rewind();
			VIEWPORT_PARAMS.rewind();
			GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX);
	    	GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT_PARAMS);
	    	
	    	viewportX = VIEWPORT_PARAMS.get(0) / uiScale;
			viewportY = (UIrender.getWindowHeight() - VIEWPORT_PARAMS.get(1) - VIEWPORT_PARAMS.get(3)) / uiScale;
			xTranslation = MODELVIEW_MATRIX.get(12) + viewportX;
			yTranslation = MODELVIEW_MATRIX.get(13) + viewportY;
			viewportWidth = VIEWPORT_PARAMS.get(2) / uiScale;
			viewportHeight = VIEWPORT_PARAMS.get(3) / uiScale;
		}
	}
}
