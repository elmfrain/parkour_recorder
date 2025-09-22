package com.elmfer.prmod.parkour;

import java.util.HashMap;
import java.util.Map;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.ParkourRecorder;
import com.elmfer.prmod.animation.Smoother;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.parkour.Frame.Flags;
import com.elmfer.prmod.ui.MenuScreen;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.Viewport;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class KeyInputHUD implements HudElement {

    public static final Identifier HUD_ID = Identifier.of(ParkourRecorder.MOD_ID, "keyinputs_hud");

    private static final float BODY_ASPECT_RATIO = 0.77f;

    private final Map<Flags, Smoother> keyTransitions = new HashMap<Flags, Smoother>();

    public float size = 40.0f;
    public float posX = 30000.0f;
    public float posY = 0.0f;

    private Viewport body;

    private Smoother forwardKeyPos = new Smoother();
    private Smoother leftKeyPos = new Smoother();
    private Smoother backKeyPos = new Smoother();
    private Smoother rightKeyPos = new Smoother();
    private Smoother jumpKeyPos = new Smoother();
    private Smoother sprintKeyPos = new Smoother();
    private Smoother sneakKeyPos = new Smoother();
    private Smoother smoothHUDpos = new Smoother();

    private Frame parkourFrame;
    private boolean useHUDPosition = false;

    public KeyInputHUD() {
        keyTransitions.put(Flags.FORWARD, forwardKeyPos);
        keyTransitions.put(Flags.LEFT_STRAFE, leftKeyPos);
        keyTransitions.put(Flags.BACKWARD, backKeyPos);
        keyTransitions.put(Flags.RIGHT_STRAFE, rightKeyPos);
        keyTransitions.put(Flags.JUMPING, jumpKeyPos);
        keyTransitions.put(Flags.SPRINTING, sprintKeyPos);
        keyTransitions.put(Flags.SNEAKING, sneakKeyPos);

        for (Smoother smoother : keyTransitions.values())
            smoother.setSpeed(30);

        smoothHUDpos.setValueAndGrab(posX);
    }

    public void setFrame(Frame frame) {
        parkourFrame = frame;

        if (parkourFrame == null)
            return;

        for (Map.Entry<Flags, Smoother> entry : keyTransitions.entrySet()) {
            if (frame.getFlag(entry.getKey()))
                entry.getValue().grab(2);
            else
                entry.getValue().grab(0);
        }
    }

    public float getHeight() {
        return size * BODY_ASPECT_RATIO;
    }

    public Frame getFrame() {
        return parkourFrame;
    }

    public void render() {
        final int padding = 2;

        body = new Viewport();
        body.left = useHUDPosition ? (float) smoothHUDpos.getValue() : posX;
        body.right = body.left + size;
        body.top = posY;
        body.bottom = body.top + size * BODY_ASPECT_RATIO;
        size = 60.0f;

        body.pushMatrix(false);
        {
            UIRender.drawRect(-padding, -padding, body.getWidth() + padding, body.getHeight() + padding, 0x4D000000);
            UIRender.drawIcon("up_key", 0, -forwardKeyPos.getValuef(), size, getKeyColor(Flags.FORWARD));
            UIRender.drawIcon("left_key", -leftKeyPos.getValuef(), 0, size, getKeyColor(Flags.LEFT_STRAFE));
            UIRender.drawIcon("down_key", 0, backKeyPos.getValuef(), size, getKeyColor(Flags.BACKWARD));
            UIRender.drawIcon("right_key", rightKeyPos.getValuef(), 0, size, getKeyColor(Flags.RIGHT_STRAFE));
            UIRender.drawIcon("spacebar_key", 0, jumpKeyPos.getValuef(), size, getKeyColor(Flags.JUMPING));
            UIRender.drawIcon("sprint_key", sprintKeyPos.getValuef(), 0, size, getKeyColor(Flags.SPRINTING));
            UIRender.drawIcon("sneak_key", 0, sneakKeyPos.getValuef(), size, getKeyColor(Flags.SNEAKING));
        }
        body.popMatrix();
        
        useHUDPosition = false;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        boolean renderHUD = updateHUDPosition();
        
        if(renderHUD) {
            useHUDPosition = true;
            render();
        }
    }

    /**
     * Updates the HUD's position by applying transitions whenever it is activated.
     * 
     * @return A boolean determining if the HUD should be rendered, or skip when offscreen or when being rendered in <code>MenuScreen</code>
     */
    private boolean updateHUDPosition() {
        float uiWidth = UIRender.getUIwidth();
        
        
        if(!Config.showInputs())
            return false;
        
        if(MinecraftClient.getInstance().currentScreen instanceof MenuScreen)
            return false;
        
        if(!(EventHandler.session instanceof PlaybackSession) || !((PlaybackSession) EventHandler.session).isPlaying()) {
            smoothHUDpos.grab(uiWidth + 5); // offscreen
            return smoothHUDpos.getValue() <  uiWidth;
        }
        
        smoothHUDpos.grab(uiWidth - 10 - size);
        
        return smoothHUDpos.getValue() <  uiWidth;

    }

    private int getKeyColor(Flags flag) {
        if (parkourFrame == null)
            return 0xFF888888;
        return parkourFrame.getFlag(flag) ? 0xFFEFEFEF : 0xFF888888;
    }
}