package com.elmfer.prmod.ui.window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.elmfer.prmod.ui.GuiStyle;
import com.elmfer.prmod.ui.UIRender;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.UIRender.Direction;
import com.elmfer.prmod.ui.Viewport;
import com.elmfer.prmod.ui.widgets.Button;
import com.elmfer.prmod.ui.widgets.Widget;
import com.mojang.blaze3d.systems.RenderSystem;

abstract public class Window extends Widget {

    private static final List<Window> LOADED_WINDOWS = new ArrayList<Window>();

    String title;
    private boolean shouldClose = false;
    protected Viewport viewport;
    protected CloseButton closeButton = new CloseButton();

    public Window(String titleIn) {
        super();
        title = titleIn;
        closeButton.setAction((b) -> setShouldClose(true));
        addWidgets(closeButton);
        height = 40;
    }

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    abstract protected void doDrawScreen();

    @Override
    public void update(SidedUpdate side) {
        if (side == SidedUpdate.CLIENT) {
            if (shouldClose) {
                close();
                LOADED_WINDOWS.remove(this);
                if (LOADED_WINDOWS.size() > 0)
                    Widget.setCurrentZLevel(LOADED_WINDOWS.get(LOADED_WINDOWS.size() - 1).getzLevel());
                else
                    Widget.setCurrentZLevel(0);
            }
        }
    }

    @Override
    public void draw() {
        int boxWidth = GuiStyle.AlertBox.boxWidth();
        int titleHeight = GuiStyle.AlertBox.titleHeight();
        int margin = (int) GuiStyle.Gui.margin();
        int smallMargin = GuiStyle.Gui.smallMargin();

        Viewport all = new Viewport();
        Viewport box = new Viewport(all);
        box.left = all.getWidth() / 2 - boxWidth / 2;
        box.top = (int) (all.getHeight() / 2.0f - (titleHeight + margin * 2.0f + height) / 2.0f);
        box.right = all.getWidth() / 2 + boxWidth / 2;
        box.bottom = (int) (all.getHeight() / 2.0f + (titleHeight + margin * 2.0f + height) / 2.0f);
        Viewport title = new Viewport(box);
        title.bottom = UIRender.getStringHeight() * 2;
        viewport = new Viewport(box);
        viewport.left = margin;
        viewport.top = title.bottom + margin;
        viewport.right -= margin;
        viewport.bottom -= margin;

        box.pushMatrix(false);
        {
            UIRender.drawGradientRect(Direction.TO_BOTTOM, -smallMargin, box.getHeight(), boxWidth + smallMargin,
                    box.getHeight() + 10.0f, -1946157056, 0);
            UIRender.drawGradientRect(-smallMargin, -smallMargin, box.getWidth() + smallMargin,
                    box.getHeight() + smallMargin, -12632257, -14277082);
        }
        box.popMatrix();

        title.pushMatrix(false);
        {
            UIRender.drawGradientRect(Direction.TO_RIGHT, 0, 0, title.getWidth(), title.getHeight(), -1728053248,
                    1275068416);

            closeButton.height = (int) title.getHeight() - smallMargin * 2;
            closeButton.width = closeButton.height;
            closeButton.y = smallMargin;
            closeButton.x = (int) (title.getWidth() - smallMargin - closeButton.width);
            closeButton.draw();

            Viewport message = new Viewport(title);
            message.right -= closeButton.width + smallMargin * 2;

            message.pushMatrix(false);
            UIRender.drawString(Anchor.MID_LEFT, this.title, margin, (int) (title.getHeight() / 2), 0xFFFFFFFF);
            message.popMatrix();
        }
        title.popMatrix();

        doDrawScreen();
    }

    /**
     * Creates the window within this method to correctly set the z values of all
     * widgets (like buttons) this window may contain.
     * 
     * @param windowCreater a window you want to create
     * 
     * @return the window madeby the creater
     */
    public static Window createWindow(Function<Void, Window> windowCreater) {
        int newZLevel = LOADED_WINDOWS.size() + 1;
        Widget.setCurrentZLevel(newZLevel);

        Window newWindow = windowCreater.apply(null);

        LOADED_WINDOWS.add(newWindow);

        return newWindow;
    }

    public static void drawWindows() {
        int i = 0;
        for (Window window : LOADED_WINDOWS) {
            RenderSystem.getModelViewStack().push();
            {
                RenderSystem.getModelViewStack().translate(4 * i, 4 * i++, 0);
                window.draw();
            }
            RenderSystem.getModelViewStack().pop();
            i++;
        }
    }

    public static boolean areWindowsOpen() {
        return LOADED_WINDOWS.size() > 0;
    }

    public static void closeWindows() {
        Widget.setCurrentZLevel(0);
        LOADED_WINDOWS.forEach((w) -> {
            w.setShouldClose(true);
        });
    }

    private static class CloseButton extends Button {
        @Override
        public void draw() {
            updateModelviewAndViewportState();

            int color = isHovered() && isEnabled() ? -439615488 : -864092160;

            UIRender.drawRect(x, y, x + width, y + height, color);
        }
    }
}
