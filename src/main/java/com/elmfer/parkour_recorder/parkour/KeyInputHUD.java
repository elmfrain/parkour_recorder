package com.elmfer.parkour_recorder.parkour;

import java.util.HashMap;
import java.util.Map;

import com.elmfer.parkour_recorder.animation.Smoother;
import com.elmfer.parkour_recorder.gui.UIrender;
import com.elmfer.parkour_recorder.gui.Viewport;
import com.elmfer.parkour_recorder.parkour.ParkourFrame.Flags;

public class KeyInputHUD
{
	public static float size = 40.0f;
	public static float posX = 0.0f;
	public static float posY = 0.0f;
	
	private static final float BODY_ASPECT_RATIO = 0.77f;
	
	private static Viewport body;
	
	private static Smoother forwardKeyPos = new Smoother();
	private static Smoother leftKeyPos = new Smoother();
	private static Smoother backKeyPos = new Smoother();
	private static Smoother rightKeyPos = new Smoother();
	private static Smoother jumpKeyPos = new Smoother();
	private static Smoother sprintKeyPos = new Smoother();
	private static Smoother sneakKeyPos = new Smoother();
	
	private static final Map<Flags, Smoother> keyTransitions = new HashMap<Flags, Smoother>();
	
	static
	{
		keyTransitions.put(Flags.FORWARD, forwardKeyPos);
		keyTransitions.put(Flags.LEFT_STRAFE, leftKeyPos);
		keyTransitions.put(Flags.BACKWARD, backKeyPos);
		keyTransitions.put(Flags.RIGHT_STRAFE, rightKeyPos);
		keyTransitions.put(Flags.JUMPING, jumpKeyPos);
		keyTransitions.put(Flags.SPRINTING, sprintKeyPos);
		keyTransitions.put(Flags.SNEAKING, sneakKeyPos);
		
		for(Smoother smoother : keyTransitions.values())
			smoother.setSpeed(30);
	}
	
	private static ParkourFrame parkourFrame;
	
	public static void setParkourFrame(ParkourFrame frame)
	{
		parkourFrame = frame;
		
		if(parkourFrame == null) return;
		
		for(Map.Entry<Flags, Smoother> entry: keyTransitions.entrySet())
		{
			if(frame.getFlag(entry.getKey()))
				entry.getValue().grab(2);
			else
				entry.getValue().grab(0);
		}
	}
	
	public static float getHeight()
	{
		return size * BODY_ASPECT_RATIO;
	}
	
	public static ParkourFrame getParkourFrame()
	{
		return parkourFrame;
	}
	
	public static void render()
	{
		int lip = 2;
		
		body = new Viewport();
		body.left = posX;
		body.right = body.left + size;
		body.top = posY;
		body.bottom = body.top + size * BODY_ASPECT_RATIO;
		size = 60.0f;
		
		body.pushMatrix(false);
		{
			UIrender.drawRect(-lip, -lip, body.getWidth() + lip, body.getHeight() + lip, 0x4D000000);
			UIrender.drawIcon("up_key", 0, -forwardKeyPos.getValuef(), size, getKeyColor(Flags.FORWARD));
			UIrender.drawIcon("left_key", -leftKeyPos.getValuef(), 0, size, getKeyColor(Flags.LEFT_STRAFE));
			UIrender.drawIcon("down_key", 0, backKeyPos.getValuef(), size, getKeyColor(Flags.BACKWARD));
			UIrender.drawIcon("right_key", rightKeyPos.getValuef(), 0, size, getKeyColor(Flags.RIGHT_STRAFE));
			UIrender.drawIcon("spacebar_key", 0, jumpKeyPos.getValuef(), size, getKeyColor(Flags.JUMPING));
			UIrender.drawIcon("sprint_key", sprintKeyPos.getValuef(), 0, size, getKeyColor(Flags.SPRINTING));
			UIrender.drawIcon("sneak_key", 0, sneakKeyPos.getValuef(), size, getKeyColor(Flags.SNEAKING));
		}
		body.popMatrix();
	}

	private static int getKeyColor(Flags flag)
	{
		if(parkourFrame == null) return 0xFF888888;
		return parkourFrame.getFlag(flag) ? 0xFFEFEFEF : 0xFF888888;
	}
}
