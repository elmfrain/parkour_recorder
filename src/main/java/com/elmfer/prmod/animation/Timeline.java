package com.elmfer.prmod.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Timeline {
	
	private final HashMap<String, Property> propertyList = new HashMap<String, Property>();
	private final double duration;
	private final String name;
	
	private State timelineState;
	private double currentFracTime;
	private double speed;
	private long currentTickTime;
	private long prevTickTime;
	private boolean paused;
	private boolean stopped;
	
	public Timeline(String name, double duration) {
		
		timelineState = State.FORWARD;
		paused = false;
		stopped = true;
		
		currentFracTime = 0.0D;
		
		speed = 1.0D;
		
		currentTickTime = System.nanoTime();
		prevTickTime = currentTickTime;
		
		this.duration = Math.abs(duration);
		
		this.name = name;
	}
	
	public Timeline(double duration) {
		
		timelineState = State.FORWARD;
		paused = false;
		stopped = true;
		
		currentFracTime = 0.0D;
		
		speed = 1.0D;
		
		currentTickTime = System.nanoTime();
		prevTickTime = currentTickTime;
		
		this.duration = Math.abs(duration);
		
		this.name = null;
	}
	
	public Timeline(Timeline value) {
		
		timelineState = value.timelineState;
		paused = value.paused;
		stopped = value.stopped;
		
		currentFracTime = value.currentFracTime;
		
		speed = value.speed;
		
		currentTickTime = value.currentTickTime;
		prevTickTime = value.currentTickTime;
		
		duration = value.duration;
		
		name = value.name;	
	}
	
	public void tick() {
		
		currentTickTime = System.nanoTime();
		
		//Tick fraction-time calculation
		double passedFracTime = (currentTickTime - prevTickTime) * 10e-10;
		double fracTimeTick = passedFracTime * speed / duration;
		
		//Play forward are reversed
		if(!paused && !stopped) {
			
			switch(timelineState) {
				case FORWARD:
					currentFracTime += fracTimeTick;
					break;
				case REVERSE:
					currentFracTime -= fracTimeTick;
					break;
				default:
					break;
			}
		}
		
		//Maintain the time between or equal to 0 or 1
		if(currentFracTime <= 0.0D) {
			
			stopped = true;
			paused = false;
			currentFracTime = 0.0D;
		}else if(currentFracTime >= 1.0D) {
			
			stopped = true;
			paused = false;
			currentFracTime = 1.0D;
		}
		
		prevTickTime = currentTickTime;
	}
	
	public double getDuration() {
		
		return duration;
	}
	
	public Property getProperty(String propName) {
		
		return propertyList.get(propName);
	}
	
	public boolean hasProperty(String propName) {
		
		return propertyList.containsKey(propName);
	}
	
	public String getName() {
		
		return name;
	}
	
	public State getState() {
		
		return timelineState;
	}
	
	public double getFracTime() {
		
		return currentFracTime;
	}
	
	public void setFracTime(double fracTime) {
		
		currentFracTime = Math.min(1.0, Math.max(fracTime, 0.0));
	}
	
	public void setTimePos(double timePos) {
		
		double d = timePos / duration;
		currentFracTime = Math.min(1.0, Math.max(d, 0.0));
	}
	
	public double getTimePos() {
		
		return currentFracTime * duration;
	}
	
	public double getSpeed() {
		
		return speed;
	}
	
	public boolean isPaused() {
		
		return paused;
	}
	
	public boolean hasStopped() {
		
		return stopped;
	}
	
	public Timeline addProperties(Property... properties) {
		
		for(Property prop : properties) {
			
			prop.host = this;
			propertyList.put(prop.getName() , prop);
		}
		return this;
	}
	
	public Timeline addProperties(Collection<Property> properties) {
		
		for(Property prop : properties) {
			
			prop.host = this;
			propertyList.put(prop.getName() , prop);
		}		
		return this;
	}
	
	public void setSpeed(double fracValue) {
		
		speed = fracValue;
	}
	
	public void play() {
		
		paused = false;
		stopped = false;
		
		timelineState = State.FORWARD;
	}
	
	public void rewind() {
		
		paused = false;
		stopped = false;
		
		timelineState = State.REVERSE;
	}
	
	public void pause() {
		
		if(!stopped) {
			paused = true;
		}
	}
	
	public void resume() {
		
		paused = false;
	}
	
	public void stop() {
		
		stopped = true;
		currentFracTime = 0.0D;
	}
	
	public HashMap<String, Property> getProperties(){
		
		return propertyList;
	}
	
	@Override
	protected Timeline clone() {
		
		Timeline timeline = new Timeline(this);
		Map<String, Property> propertyListClone = new HashMap<String, Property>(propertyList);
		
		timeline.addProperties(propertyListClone.values());
		
		return timeline;
		
	}
	
	public static enum State
	{
		FORWARD, REVERSE;
	}
}