package com.elmfer.prmod.util;

import com.elmfer.prmod.mixin.KeyBindingMixins;

import net.minecraft.client.option.KeyBinding;

/**
 * Modifies the state of a KeyBinding when the player is in a parkour session.
 * This is used to prevent the player from using the keybindings for their
 * intended purpose when they are in a playback session and modifies its state
 * to match the state of the player's inputs during the playback of a recording.
 */
public class KeyBindingStateHandler {

    private boolean isPressed = false;
    private boolean lastIsKeyDown = false;
    private boolean isKeyDown = false;
    
    private boolean capture = false;
    
    /**
     * Called on every game tick, this method is meant receive a stream of off/on
     * states so that KeyBinding's <code>pressed</code> and <code>timesPressed</code>
     * are emulated
     * 
     * @param keyDown from a single Frame of a recording
     */
    public void tick(boolean keyDown) {
        isKeyDown = isPressed = keyDown;
    }
    
    public void modifyKeyBindingState(KeyBinding keyBinding) {
        keyBinding.setPressed(isKeyDown);
        
        if (lastIsKeyDown == isKeyDown) 
            isPressed = false;
        
        ((KeyBindingMixins) keyBinding).setTimesPressed(isPressed ? 1 : 0);
        
        lastIsKeyDown = isKeyDown;
    }
    
    public void reset() {
        isPressed = false;
        lastIsKeyDown = false;
        isKeyDown = false;
    }
    
    /**
     * Called during minecraft's handleInputEvents method to capture the state of a
     * KeyBinding at the correct time
     * @param keyBinding
     */
    public void capturePress(KeyBinding keyBinding) {
        capture = keyBinding.isPressed();
    }
    
    public boolean getCapture() {
        return capture;
    }
}
