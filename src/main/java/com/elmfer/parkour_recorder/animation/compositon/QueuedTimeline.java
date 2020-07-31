package com.elmfer.parkour_recorder.animation.compositon;

import java.util.ArrayList;
import java.util.List;

import com.elmfer.parkour_recorder.animation.Timeline;

public class QueuedTimeline implements IAct{
	
	public final Timeline timeline;
	
	private final List<Byte> actions = new ArrayList<Byte>();
	
	protected QueuedTimeline(Timeline value) {
		
		timeline = value;
	}
	
	@Override
	public void addAction(byte action) {
		
		actions.add(action);
	}
	
	@Override
	public void applyActions() {
		
		for(Byte action : actions) {
			
			switch(action) {
				case 1:
					timeline.play();
					break;
				case 2:
					timeline.rewind();
					break;
				case 3:
					timeline.resume();
					break;
				case 4:
					timeline.pause();
					break;
				case 5:
					timeline.stop();
					break;
			}
		}
	}
}
