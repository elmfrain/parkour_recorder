package com.elmfer.prmod.parkour;

public interface ParkourSession {

    /**
     * Called when a keybind or other event triggers the 'Record' action of this session.
     * 
     * @return This or new different session depending on the current state
     */
    public ParkourSession onRecord();

    /**
     * Called when a keybind or other event triggers the 'Play' action of this session.
     * 
     * @return This or new different session depending on the current state
     */
    public ParkourSession onPlay();

    /**
     * Called when a keybind or other event triggers the 'Override' action of this session.
     * 
     * @return This or new different session depending on the current state
     */
    public ParkourSession onOverride();

    /**
     * Session handler for a game tick
     */
    public void onClientTick();

    /**
     * Session handler for each rendering frame.
     */
    public void onRenderTick();

    public boolean isActive();

    public void cleanUp();
}
