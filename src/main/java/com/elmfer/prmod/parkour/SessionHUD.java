package com.elmfer.prmod.parkour;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.ParkourRecorder;
import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.UIScreen;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

/**
 * A HUD element that renders the mod's recording session state (e.g. "Playing",
 * "Stopped", "Recording")
 */
public class SessionHUD implements HudElement {

    public static final Identifier HUD_ID = Identifier.of(ParkourRecorder.MOD_ID, "session_hud");

    private int fadedness = 0;
    private boolean increaseOpacity = false;

    public void tick() {
        fadedness += increaseOpacity ? 25 : 0;
        fadedness = Math.max(0, fadedness - 5);
    }

    public void render() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen instanceof UIScreen)
            return;

        increaseOpacity = false;
        String message = I18n.translate("com.prmod.stopped");
        if (EventHandler.session instanceof RecordingSession) {
            if (((RecordingSession) EventHandler.session).onOverride) {
                message = I18n.translate("com.prmod.overriding");
                String name = ((RecordingSession) EventHandler.session).recording.getName();
                name = name == null ? "[" + I18n.translate("com.prmod.unamed") + "]" : name;
                message += ": " + name;
                increaseOpacity = true;
            } else if (((RecordingSession) EventHandler.session).isWaitingForPlayer()) {
                increaseOpacity = true;
                message = I18n.translate("com.prmod.waiting_for_player");
            } else if (((RecordingSession) EventHandler.session).isRecording) {
                message = I18n.translate("com.prmod.recording");
                increaseOpacity = true;
            }
        } else if (EventHandler.session instanceof PlaybackSession) {
            if (((PlaybackSession) EventHandler.session).isPlaying()) {
                increaseOpacity = true;
                message = I18n.translate("com.prmod.playing");
            } else if (((PlaybackSession) EventHandler.session).isWaitingForPlayer()) {
                increaseOpacity = true;
                message = I18n.translate("com.prmod.waiting_for_player");
            }
            String name = ((PlaybackSession) EventHandler.session).recording.getName();
            name = name == null ? "[" + I18n.translate("com.prmod.unamed") + "]" : name;
            message += " - " + name;
        }
        
        fadedness = Math.min(200, fadedness);
        if (fadedness < 5) return;
        
        Window res = mc.getWindow();

        int border = 10;
        int lip = 2;
        int stringWidth = mc.textRenderer.getWidth(message);
        int stringHeight = mc.textRenderer.fontHeight;

        int width = res.getScaledWidth();
        float fade = Math.min(100, fadedness) / 100.0f;
        int c = GraphicsHelper.getIntColor(0.9f, 0.9f, 0.9f, fade);
        int c1 = GraphicsHelper.getIntColor(0.0f, 0.0f, 0.0f, 0.2f * fade);

        boolean showLoopIcon = EventHandler.session instanceof PlaybackSession
                ? ((PlaybackSession) EventHandler.session).recording.isLoop()
                : true;

        RenderSystem.getModelViewStack().identity();

        if (Config.isLoopMode() && showLoopIcon) {
            UIRender.drawRect(width - stringWidth - border - lip * 3 - stringHeight, border - lip,
                    width - border + lip, border + stringHeight + lip, c1);

            UIRender.drawIcon("loop_icon", width - border - stringWidth - stringHeight, border + border / 2,
                    stringHeight, c);
        } else
            UIRender.drawRect(width - stringWidth - border - lip, border - lip, width - border + lip,
                    border + stringHeight + lip, c1);

        UIRender.drawString(message, width - stringWidth - border, border, c);
    }
    
    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        render();
    }
}
