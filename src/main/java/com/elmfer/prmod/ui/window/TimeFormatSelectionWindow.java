package com.elmfer.prmod.ui.window;

import org.lwjgl.glfw.GLFW;

import com.elmfer.prmod.ui.ButtonListView;
import com.elmfer.prmod.ui.GuiStyle;
import com.elmfer.prmod.ui.NumberLineView.TimeStampFormat;
import com.elmfer.prmod.ui.TimelineView;
import com.elmfer.prmod.ui.widgets.Button;

import net.minecraft.client.resource.language.I18n;

public class TimeFormatSelectionWindow extends Window {
    private static final int BUTTON_LIST_HEIGHT = GuiStyle.Gui.buttonHeight() * 6 + GuiStyle.Gui.smallMargin() * 8;

    /** A scrollable list of buttons to select time format. **/
    protected ButtonListView selections = new ButtonListView();

    public TimeFormatSelectionWindow() {
        super(I18n.translate("com.prmod.change_time_format"));

        // Setup button list
        for (TimeStampFormat format : TimeStampFormat.values()) {
            Button button = new Button(format.NAME);
            button.setHighlighted(TimelineView.timeStampFormat == format);
            button.setAction((b) -> selectionsPressed(b));
            selections.addWidgets(button);
        }

        height = BUTTON_LIST_HEIGHT;
        addWidgets(selections);
    }

    @Override
    protected void doDrawScreen() {
        selections.setViewport(viewport);
        selections.draw();
    }

    private void selectionsPressed(Button button) {
        TimelineView.timeStampFormat = TimeStampFormat.values()[selections.getChildrenWidgets().indexOf(button)];
        selections.getChildrenWidgets().forEach(b -> {
            if (b instanceof Button)
                ((Button) b).setHighlighted(false);
        });

        button.setHighlighted(true);
    }

    @Override
    public void onCursorMove(float mouseX, float mouseY) {
    }

    @Override
    public void onMouseClicked(int button) {
    }

    @Override
    public void onMouseReleased(int button) {
    }

    @Override
    public void onKeyPressed(int keyCode) {
        if (onCurrentZlevel()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                setShouldClose(true);
            }
        }
    }

    @Override
    public void onCharTyped(int charTyped) {
    }

    @Override
    public void onMouseScroll(int scrollAmount) {
    }
}
