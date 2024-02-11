package com.elmfer.prmod.ui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.elmfer.prmod.animation.Smoother;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.widgets.Button;
import com.elmfer.prmod.ui.widgets.Widget;
import com.elmfer.prmod.ui.window.TimeFormatSelectionWindow;
import com.elmfer.prmod.ui.window.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;

public class MenuScreen extends UIScreen {
    private static final float NAV_BAR_HEIGHT = 15.0f;
    private static final float MARGIN = 3.0f;

    // Screens; views
    private LoadRecordingView loadView = new LoadRecordingView();
    private SaveRecordingView saveView = new SaveRecordingView();
    private TimelineView timelineView = new TimelineView();
    private OptionView optionView = new OptionView();
    private ModTitleScreenView modTitleScreenView = new ModTitleScreenView();

    // Tabs
    private Tab loadTab = new Tab(loadView);
    private Tab saveTab = new Tab(saveView);
    private Tab timelineTab = new Tab(timelineView);
    private Tab optionTab = new Tab(optionView);
    private Tab modTitleTab = new Tab(modTitleScreenView);
    private final Tab[] tabs = { loadTab, saveTab, timelineTab, optionTab, modTitleTab };

    private Smoother tabSelectorX1 = new Smoother();
    private Smoother tabSelectorX2 = new Smoother();

    private static int pageSelected = 0; // 0 = load, 1 = save, 2 = timeline, 3 = titlescreen

    public MenuScreen() {
        super();

        loadTab.setText(I18n.translate("com.prmod.load"));
        saveTab.setText(I18n.translate("com.prmod.save"));
        timelineTab.setText(I18n.translate("com.prmod.timeline"));
        optionTab.setText(I18n.translate("com.prmod.options"));
        modTitleTab.setText(I18n.translate("com.prmod.parkour_recorder"));

        loadTab.width = UIRender.getStringWidth(loadTab.getText()) + 10;
        saveTab.width = UIRender.getStringWidth(saveTab.getText()) + 10;
        timelineTab.width = UIRender.getStringWidth(timelineTab.getText()) + 10;
        optionTab.width = UIRender.getStringWidth(optionTab.getText()) + 10;
        modTitleTab.width = UIRender.getStringWidth(modTitleTab.getText()) + 10;

        loadTab.setAction((t) -> {
            showTabView(loadTab);

            tabSelectorX1.grab(t.x - MARGIN);
            tabSelectorX2.grab(t.x + t.width + MARGIN);
            pageSelected = 0;
        });
        saveTab.setAction((t) -> {
            showTabView(saveTab);

            tabSelectorX1.grab(t.x - MARGIN);
            tabSelectorX2.grab(t.x + t.width + MARGIN);
            pageSelected = 1;
        });
        timelineTab.setAction((t) -> {
            showTabView(timelineTab);

            tabSelectorX1.grab(t.x - MARGIN);
            tabSelectorX2.grab(t.x + t.width + MARGIN);
            pageSelected = 2;
        });
        optionTab.setAction((t) -> {
            showTabView(optionTab);

            tabSelectorX1.grab(t.x - MARGIN);
            tabSelectorX2.grab(t.x + t.width + MARGIN);
            pageSelected = 3;
        });
        modTitleTab.setAction((t) -> {
            showTabView(modTitleTab);

            tabSelectorX1.grab(t.x - MARGIN);
            tabSelectorX2.grab(t.x + t.width + MARGIN);
            pageSelected = 4;
        });

        timelineView.x = NAV_BAR_HEIGHT + MARGIN;

        tabSelectorX1.setSpeed(20);
        tabSelectorX2.setSpeed(20);
        tabSelectorX1.setValueAndGrab(0);
        tabSelectorX2.setValueAndGrab(20);
        initFirstTab();
    }

    /** When gui closes. **/
    @Override
    public void removed() {
        super.removed();
        timelineView.onExit();
        modTitleScreenView.onExit();
    }

    private void showTabView(Tab ptab) {
        for (Tab tab : tabs)
            tab.tabView.setVisible(false);
        ptab.tabView.setVisible(true);
        ptab.tabView.refresh();
    }

    private void initFirstTab() {
        // Position the tabs in their correct places.
        GL11.glColorMask(false, false, false, false);
        drawTabs();
        GL11.glColorMask(true, true, true, true);

        // Load the last tab that was open
        switch (pageSelected) {
        case 0:
            loadTab.getAction().onAction(loadTab);
            break;
        case 1:
            saveTab.getAction().onAction(saveTab);
            break;
        case 2:
            timelineTab.getAction().onAction(timelineTab);
            break;
        case 3:
            optionTab.getAction().onAction(optionTab);
            break;
        case 4:
            modTitleTab.getAction().onAction(modTitleTab);
            break;
        default:
            loadTab.getAction().onAction(loadTab);
            break;
        }
    }

    @Override
    public void render(DrawContext stack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.getModelViewStack().push();
        {
            loadView.draw();
            saveView.draw();
            timelineView.draw();
            optionView.draw();
            modTitleScreenView.draw();
        }
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();

        drawNavigationBar();
        Window.drawWindows();
    }

    // Don't pause game
    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void onCursorMove(float mouseX, float mouseY) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseClicked(int button) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseReleased(int button) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onKeyPressed(int keyCode) {
        super.onKeyPressed(keyCode);

        if (Widget.getCurrentZLevel() == 0) {
            if (keyCode == GLFW.GLFW_KEY_T) {
                Window.createWindow((v) -> {
                    return new TimeFormatSelectionWindow();
                });
            }
        }
    }

    @Override
    public void onCharTyped(int charTyped) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseScroll(int scrollAmount) {
        // TODO Auto-generated method stub

    }

    private void drawNavigationBar() {
        float uiWidth = UIRender.getUIwidth();

        UIRender.drawRect(0, 0, uiWidth, NAV_BAR_HEIGHT, -1308622848);
        UIRender.drawRect(0, NAV_BAR_HEIGHT, uiWidth, NAV_BAR_HEIGHT + MARGIN, -16749608);
        UIRender.drawGradientRect(0, NAV_BAR_HEIGHT + MARGIN, uiWidth, NAV_BAR_HEIGHT * 2 + MARGIN, 1275068416, 0);

        drawTabs();
    }

    private void drawTabs() {
        // Render blue selector
        UIRender.drawGradientRect((float) tabSelectorX1.getValue(), 0, (float) tabSelectorX2.getValue(), NAV_BAR_HEIGHT,
                -16741121, -16749608);

        float tabPosition = NAV_BAR_HEIGHT;
        for (Tab tab : tabs) {
            // Position normal tabs inline with each other
            if (tab != modTitleTab) {
                tab.x = tabPosition;
                tabPosition += tab.width + NAV_BAR_HEIGHT;
            }
            // Position Mod Title tab to the far right
            else {
                float uiWidth = UIRender.getUIwidth();
                tab.x = uiWidth - MARGIN - tab.width - NAV_BAR_HEIGHT;
            }

            tab.height = NAV_BAR_HEIGHT;
            tab.draw();

            if (tab.justPressed()) {
                tabSelectorX1.grab(tab.x - MARGIN);
                tabSelectorX2.grab(tab.x + tab.width + MARGIN);
            }
        }

        // If blue selector has not defaulted to the Load tab yet
        if (tabSelectorX1.getValue() == 0.0) {
            tabSelectorX1.grab(loadTab.x - MARGIN);
            tabSelectorX2.grab(loadTab.x + loadTab.width + MARGIN);
        }
    }

    public interface IMenuTabView {
        public void refresh();

        public void setVisible(boolean visible);
    }

    private static class Tab extends Button {
        IMenuTabView tabView;

        public Tab(IMenuTabView view) {
            tabView = view;
        }

        @Override
        public void draw() {
            updateModelviewAndViewportState();

            if (isVisible()) {
                int j = getTextColor();

                UIRender.drawRect(x, y, x + width, y + height, 1275068416);

                UIRender.drawString(Anchor.CENTER, getText(), x + width / 2, y + height / 2, j);
            }
        }
    }
}
