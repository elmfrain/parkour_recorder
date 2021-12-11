package com.elmfer.parkour_recorder.gui.widgets;

import static com.elmfer.parkour_recorder.render.GraphicsHelper.getIntColor;

import com.elmfer.parkour_recorder.animation.Timeline;
import com.elmfer.parkour_recorder.animation.compositon.Composition;
import com.elmfer.parkour_recorder.gui.UIrender;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;

import net.minecraft.util.math.vector.Vector3f;

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
			
			UIrender.drawRect(x, y, x + width, y + height, color);
			
			if(!icon.isEmpty())
			{
				UIrender.drawIcon(icon, x + width / 2, y + height / 2, DEFAULT_ICON_SCALE, j);
			}
			else
			{
				UIrender.drawString(Anchor.CENTER, text, x + width / 2, y + height / 2, j);	
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
		Vector3f highlightColor = highlightTint.copy();
		highlightColor.mul((float) (transitions.getTimeline("highlight").getFracTime() * 0.6));
		c.add(hoveredcolor);
		c.add(highlightColor);
		return getIntColor(c, 0.4f);
	}
	
	protected int getTextColor()
	{
		int j = 14737632;
		if (!isEnabled())
            j = 10526880;
        else if (isHovered())
            j = 16777120;
		
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
