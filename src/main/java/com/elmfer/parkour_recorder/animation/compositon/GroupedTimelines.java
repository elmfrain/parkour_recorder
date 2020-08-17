package com.elmfer.parkour_recorder.animation.compositon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.elmfer.parkour_recorder.animation.Property;
import com.elmfer.parkour_recorder.animation.Timeline;

public class GroupedTimelines implements IAct {

	private final Map<String, Timeline> timelines = new HashMap<String, Timeline>();
	private final List<QueuedTimeline> queuedTimelines = new ArrayList<QueuedTimeline>();
	private final List<Timeline> lastestTimelines = new ArrayList<Timeline>();
	
	private QueuedTimeline forcedTimeline = null;
	
	private int currentQueueBuffer = 0;
	private boolean currentIsDone = false;
	private boolean forcedIsActive = false;
	private boolean forcedIsDone = true;
	
	protected GroupedTimelines(Timeline firstTimeline) {
		
		timelines.put(firstTimeline.getName(), firstTimeline);
	}
	
	public boolean addTimeline(Timeline timeline) {
		
		for(Property prop : timeline.getProperties().values()) {
			
			for(Timeline timeline0 : timelines.values()) {
				
				if(timeline0.hasProperty(prop.getName())) {
					
					timelines.put(timeline.getName(), timeline);
					return true;
				}
			}
		}
		return false;
	}
	
	public void queue(Collection<String> names) {
		
		for(String name : names) {
			
			if(timelines.containsKey(name)) {
				
				queuedTimelines.add(new QueuedTimeline(timelines.get(name)));
				currentQueueBuffer++;
			}
		}
	}
	
	public void force(String timeline) {
		
		if(timelines.containsKey(timeline)) {
			
			forcedTimeline = new QueuedTimeline(timelines.get(timeline));
			forcedIsActive = true;
			forcedIsDone = false;
		}
	}
	
	public void clearQueue() {
		
		queuedTimelines.clear();
	}
	
	public void removeFromQueue(Collection<String> timelines) {
		
		final Predicate<QueuedTimeline> filter = p -> timelines.contains(p.timeline.getName());
		queuedTimelines.removeIf(filter);
	}
	
	public void addAction(byte action) {
		
		if(forcedIsActive) {
			
			forcedTimeline.addAction(action);
		}else if(currentQueueBuffer > 0){
			
			for(int i = queuedTimelines.size() - 1 ; i >= queuedTimelines.size() - currentQueueBuffer ; i--) {
				
				queuedTimelines.get(i).addAction(action);
			}
		}
	}
	
	public void applyActions() {
		
		currentQueueBuffer = 0;
		
		if(forcedIsActive) {
			
			forcedTimeline.applyActions();
			sortLastestTimelines(forcedTimeline.timeline);
			queuedTimelines.clear();
			forcedIsActive = false;
		}
	}
	
	public Property getProperty(String prop) {
		
		for(Timeline timeline : lastestTimelines) {
			
			if(timeline.hasProperty(prop)) {
				
				return timeline.getProperty(prop);
			}
		}
		
		for(Timeline timeline : timelines.values()) {
			
			if(timeline.hasProperty(prop)) {
				
				return timeline.getProperty(prop);
			}
		}
		
		return null;
	}
	
	public boolean hasProperty(String prop) {
		
		for(Timeline timeline : timelines.values()) {
			
			if(timeline.hasProperty(prop)) {
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasTimeline(String name) {
		
		if(timelines.containsKey(name)) {
			
			return true;
		}else {
			
			return false;
		}
	}
	
	public Timeline getTimeline(String name) {
		
		if(timelines.containsKey(name)) {
			
			return timelines.get(name);
		}
		
		return null;
	}
	
	public void tick() {
		
		update();
		
		final Consumer<Timeline> action = (Timeline tL) -> tL.tick();
		timelines.values().forEach(action);
	}
	
	private void sortLastestTimelines(Timeline timeline) {
		
		final Predicate<Timeline> filter = p -> p == timeline;
		lastestTimelines.removeIf(filter);
		lastestTimelines.add(0, timeline);
	}
	
	private void update() {
		
		if(forcedTimeline != null) {
			
			if(forcedTimeline.timeline.hasStopped()) {
				
				forcedIsDone = true;
			}
		}
		
		if(!queuedTimelines.isEmpty() && forcedIsDone) {
			
			QueuedTimeline currentTimeline = queuedTimelines.get(0);
			
			if(currentTimeline.timeline.hasStopped() && !currentIsDone) {
				
				currentTimeline.applyActions();
				sortLastestTimelines(currentTimeline.timeline);
				
				currentIsDone = true;
			}
			
			if(currentTimeline.timeline.hasStopped() && currentIsDone) {
				
				queuedTimelines.remove(0);
				currentIsDone = false;
			}
			
			if(!queuedTimelines.isEmpty()) {
				
				currentTimeline = queuedTimelines.get(0);
				
				if(currentTimeline.timeline.hasStopped() && !currentIsDone) {
					currentTimeline.applyActions();
					
					sortLastestTimelines(currentTimeline.timeline);
					
					currentIsDone = true;
				}
			}else {
				
				currentIsDone = false;
			}
		}else {
			
			currentIsDone = false;
		}
		
	}
	
}

