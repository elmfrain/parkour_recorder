package com.elmfer.prmod.ui.widgets;

import org.lwjgl.opengl.GL11;

import com.elmfer.prmod.ui.UIInput;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.UIRender.Direction;
import com.elmfer.prmod.ui.UIRender.Stencil;

public class Slider extends Button {
    private static final float KNOB_WIDTH = 8.0f;

    private double amount = 0.0;
    private double previousAmount = 0.0;

    public Slider() {
        super();
    }

    public Slider(String text) {
        super(text);
    }

    @Override
    public void update(SidedUpdate side) {
        super.update(side);

        if (side == SidedUpdate.CLIENT) {
            setText(String.format("Slider %.2f", amount));
        }

        if (side == SidedUpdate.RENDER) {
            if (isPressed()) {
                float cursorX = UIInput.getUICursorX() - mvVpState.xTranslation;
                amount = (cursorX - x - KNOB_WIDTH / 2) / (width - KNOB_WIDTH);
            }
            amount = Math.min(1.0, Math.max(amount, 0.0));
        }
    }

    @Override
    public void draw() {
        updateModelviewAndViewportState();

        updateTransitions();
        int knobColor = getBackgroundColor();
        int textColor = getTextColor();

        float knobPosition = (float) ((width - KNOB_WIDTH) * amount + x);
        float shadowWidth = KNOB_WIDTH / 2;

        Stencil.pushStencilState();
        {
            Stencil.enableTest();
            Stencil.enableWrite();
            Stencil.setOperation(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            Stencil.setFunction(GL11.GL_ALWAYS, 1);
            UIRender.drawRect(x, y, x + width, y + height, 1275068416);

            Stencil.disableWrite();
            Stencil.setFunction(GL11.GL_EQUAL, 1);

            UIRender.drawGradientRect(Direction.TO_LEFT, knobPosition - shadowWidth, y, knobPosition, y + height,
                    855638016, 0);// Left Shadow
            UIRender.drawRect(knobPosition, y, knobPosition + KNOB_WIDTH, y + height, knobColor);// Knob
            UIRender.drawGradientRect(Direction.TO_RIGHT, knobPosition + KNOB_WIDTH, y,
                    knobPosition + KNOB_WIDTH + shadowWidth, y + height, 855638016, 0);// Right Shadow

            if (!getIcon().isEmpty()) {
                UIRender.drawIcon(getIcon(), x + width / 2, y + height / 2, DEFAULT_ICON_SCALE, textColor);
            } else {
                UIRender.drawString(Anchor.CENTER, getText(), x + width / 2, y + height / 2, textColor);
            }
        }
        Stencil.popStencilState();
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public void onCursorMove(float mouseX, float mouseY) {

    }

    public boolean hasKnobMoved() {
        return amount != previousAmount;
    }
}
