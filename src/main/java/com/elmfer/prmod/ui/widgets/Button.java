package com.elmfer.prmod.ui.widgets;

import org.joml.Vector3f;

import com.elmfer.prmod.animation.Timeline;
import com.elmfer.prmod.animation.compositon.Composition;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.UIRender.Anchor;

public class Button extends Widget
{
	protected static final float DEFAULT_ICON_SCALE = Widget.DEFAULT_HEIGHT * 0.55f;
	
	protected Composition transitions = new Composition();
	
	private Vector3f highlightTint = new Vector3f(0.0f, 0.45f, 0.0f);
	private boolean justPressed = false;
	private boolean pressed = false;
	private boolean released = false;
	private boolean highlighted = false;
	private String text = "";
	private String icon = "";
	private ActionPerformed action = null;

	public Button()
	{
		super();
		transitions.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	public Button(String text)
	{
		super();
		this.text = text;
		transitions.addTimelines(new Timeline("hovered", 0.04), new Timeline("highlight", 0.04));
	}
	
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String iconName)
	{
		this.icon = iconName;
	}

	public ActionPerformed getAction()
	{
		return action;
	}

	public void setAction(ActionPerformed action)
	{
		this.action = action;
	}

	public boolean justPressed()
	{
		return justPressed;
	}
	
	public boolean isPressed()
	{
		return pressed;
	}
	
	public boolean justReleased()
	{
		return released;
	}
	
	@Override
	public void update(SidedUpdate side)
	{
		if(side == SidedUpdate.RENDER)
		{
			justPressed = false;
			released = false;
		}
	}

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
	}

	@Override
	public void onMouseClicked(int button)
	{
		if(button == 0 && isHovered())
		{
			justPressed = true;
			pressed = true;
			if(action != null) action.onAction(this);
		}
	}

	@Override
	public void onMouseReleased(int button)
	{
		if(button == 0)
		{
			pressed = false;
			released = true;
		}
	}

	@Override
	public void onKeyPressed(int keyCode)
	{
	}

	@Override
	public void onCharTyped(int charTyped)
	{
	}

	@Override
	public void onMouseScroll(int scrollAmount)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void draw()
	{
		updateModelviewAndViewportState();
		
		if(isVisible())
		{
			updateTransitions();
			int color = getBackgroundColor();
			int j = getTextColor();
			
			UIRender.drawRect(x, y, x + width, y + height, color);
			
			if(!icon.isEmpty())
			{
				UIRender.drawIcon(icon, x + width / 2, y + height / 2, DEFAULT_ICON_SCALE, j);
			}
			else
			{
				UIRender.drawString(Anchor.CENTER, text, x + width / 2, y + height / 2, j);	
			}
		}
	}

	public Vector3f getHighlightTint()
	{
		return highlightTint;
	}

	public void setHighlightTint(Vector3f highlightTint)
	{
		this.highlightTint = highlightTint;
	}

	public boolean isHighlighted()
	{
		return highlighted;
	}

	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}
	
	protected int getBackgroundColor()
	{
		Vector3f c = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f hoveredcolor = new Vector3f(0.3f, 0.3f, 0.3f);
		hoveredcolor.mul((float) transitions.getTimeline("hovered").getFracTime());
		Vector3f highlightColor = new Vector3f(highlightTint);
		highlightColor.mul((float) (transitions.getTimeline("highlight").getFracTime() * 0.6));
		c.add(hoveredcolor);
		c.add(highlightColor);
		return GraphicsHelper.getIntColor(c, 0.3f);
	}
	
	protected int getTextColor()
	{
		int j = -2039584;
		if (!isEnabled())
            j = -6250336;
        else if (isHovered())
            j = -96;
		
		return j;
	}
	
	protected void updateTransitions()
	{
		transitions.tick();
		if(isHovered()) { transitions.queue("hovered"); transitions.play(); transitions.apply();}
		else { transitions.queue("hovered"); transitions.rewind(); transitions.apply();}	
		if(highlighted) { transitions.queue("highlight"); transitions.play(); transitions.apply();}
		else { transitions.queue("highlight"); transitions.rewind(); transitions.apply();}
		if(!isEnabled()) {transitions.queue("hovered", "highlight"); transitions.rewind(); transitions.apply();}
	}
	
	public static interface ActionPerformed
	{
		public void onAction(Button buttonClicked);
	}
}
