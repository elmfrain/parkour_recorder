package com.elmfer.prmod.animation.compositon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.elmfer.prmod.animation.Property;
import com.elmfer.prmod.animation.Timeline;

public class Composition {
	
	private final Map<String, Timeline> timelines = new HashMap<String, Timeline>();
	private final List<QueuedTimeline> queuedTimelines = new ArrayList<QueuedTimeline>();
	private final List<GroupedTimelines> groupedTimelines = new ArrayList<GroupedTimelines>();
	
	public void queue(String... timelines) {
		
		if(!queuedTimelines.isEmpty()) apply();
		
		for(String name : timelines) {
			
			if(this.timelines.containsKey(name)) {
				
				QueuedTimeline queuedTimeline = new QueuedTimeline(this.timelines.get(name));
				queuedTimelines.add(queuedTimeline);
			}
		}
		
		final Consumer<GroupedTimelines> action = (GroupedTimelines gT) -> gT.queue(Arrays.asList(timelines));
		groupedTimelines.forEach(action);
	}
	
	public void force(String timeline) {
		
		final Consumer<GroupedTimelines> action = (GroupedTimelines gT) -> gT.force(timeline);
		groupedTimelines.forEach(action);
	}
	
	public void clearQueue() {
		
		queuedTimelines.clear();
		
		final Consumer<GroupedTimelines> action = (GroupedTimelines gT) -> gT.clearQueue();
		groupedTimelines.forEach(action);
	}
	
	public void removeFromQueue(String... strings) {
		
		final Predicate<QueuedTimeline> filter = p -> Arrays.asList(strings).contains(p.timeline.getName());
		queuedTimelines.removeIf(filter);
		
		final Consumer<GroupedTimelines> action = (GroupedTimelines gT) -> gT.removeFromQueue(Arrays.asList(strings));
		groupedTimelines.forEach(action);
	}
	
	public void play() {
		
		action((byte) 1);
	}
	
	public void rewind() {
		
		action((byte) 2);
	}
	
	public void resume() {
		
		action((byte) 3);
	}
	
	public void pause() {
		
		action((byte) 4);
	}
	
	public void stop() {
		
		action((byte) 5);
	}
	
	public void apply() {
		
		final Consumer<IAct> action = (IAct actor) -> actor.applyActions();
		
		queuedTimelines.forEach(action);
		groupedTimelines.forEach(action);
		queuedTimelines.clear();
	}
	
	public void addTimelines(Timeline... timelines) {
		
		for(Timeline timeline : timelines) {
			
			if(timeline.getName() != null) {
				
				sortTimeline(timeline);
			}else {
				
				String name = Integer.toString(this.timelines.size());
				System.out.println("[!Alasen-Warn!] : " + timeline + " doesn't have a name! Setting it to " + name + ".");
				sortTimeline(timeline);
			}
		}
		
	}
	
	public Property getProperty(String prop) {
		
		for(Timeline timeline : timelines.values()) {
			
			if(timeline.getProperties().containsKey(prop)) {
				
				return timeline.getProperty(prop);
			}
		}
		
		for(GroupedTimelines timelineGroup : groupedTimelines) {
			
			if(timelineGroup.hasProperty(prop)) {
				
				return timelineGroup.getProperty(prop);
			}
		}
		
		return null;
	}
	
	public Property getProperty(String timeline, String prop) {
		
		if(timelines.containsKey(timeline)) {
			
			if(timelines.get(timeline).hasProperty(prop)) {
				
				return timelines.get(timeline).getProperty(prop);
			}
		}
		
		for(GroupedTimelines timelineGroup : groupedTimelines) {
			
			if(timelineGroup.hasTimeline(timeline)) {
				
				if(timelineGroup.getTimeline(timeline).hasProperty(prop)) {
					
					return timelineGroup.getTimeline(timeline).getProperty(prop);
				}
			}
		}
		
		return null;
	}
	
	public void tick() {
		
		final Consumer<Timeline> action = (Timeline tL) -> tL.tick();
		timelines.values().forEach(action);
		
		final Consumer<GroupedTimelines> gAction = (GroupedTimelines gT) -> gT.tick();
		groupedTimelines.forEach(gAction);
	}
	
	public Timeline getTimeline(String name) {
		
		if(timelines.containsKey(name)) {
			
			return timelines.get(name);
		}
		
		for(GroupedTimelines timelineGroup : groupedTimelines) {
			
			if(timelineGroup.hasTimeline(name)) {
				
				return timelineGroup.getTimeline(name);
			}
		}
		
		return null;
	}
	
	private void sortTimeline(Timeline timeline) {
		
		for(Property prop : timeline.getProperties().values()) {
			
			for(Timeline timeline0 : timelines.values()) {
				
				if(timeline0.hasProperty(prop.getName())) {
					
					timelines.remove(timeline0.getName());
					GroupedTimelines group = new GroupedTimelines(timeline0);
					group.addTimeline(timeline);
					groupedTimelines.add(group);
					return;
				}
			}
		}
		
		for(GroupedTimelines timelineGroup : groupedTimelines) {
			
			if(timelineGroup.addTimeline(timeline)) {
				
				return;
			}
		}

		timelines.put(timeline.getName(), timeline);
	}
	
	private void action(byte id) {
		
		final Consumer<IAct> action = (IAct actor) -> actor.addAction(id);
		queuedTimelines.forEach(action);
		groupedTimelines.forEach(action);
	}

}
