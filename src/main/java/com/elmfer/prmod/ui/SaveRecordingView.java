package com.elmfer.prmod.ui;

import java.util.Stack;

import org.joml.Vector3f;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.parkour.PlaybackSession;
import com.elmfer.prmod.parkour.Recording;
import com.elmfer.prmod.parkour.RecordingSession;
import com.elmfer.prmod.parkour.SessionHUD;
import com.elmfer.prmod.render.GraphicsHelper;
import com.elmfer.prmod.ui.MenuScreen.IMenuTabView;
import com.elmfer.prmod.ui.widgets.Button;
import com.elmfer.prmod.ui.widgets.Widget;
import com.elmfer.prmod.ui.window.ConfirmationWindow;
import com.elmfer.prmod.ui.window.NamingWindow;
import com.elmfer.prmod.ui.window.OverrideWindow;
import com.elmfer.prmod.ui.window.OverrideWindow.SaveToNew;
import com.elmfer.prmod.ui.window.Window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

public class SaveRecordingView extends Widget implements IMenuTabView {
    private ButtonListView buttonsView = new ButtonListView();
    private Stack<Recording> selections = new Stack<Recording>();

    private Button saveLastButton = new Button(I18n.translate("com.prmod.save_last"));
    private Button clearHistoryButton = new Button(I18n.translate("com.prmod.clear_history"));
    private Button saveButton = new Button(I18n.translate("com.prmod.save"));
    private Button removeButton = new Button(I18n.translate("com.prmod.remove_selected"));
    private Button openButton = new Button(I18n.translate("com.prmod.open"));

    public SaveRecordingView() {
        super();

        addWidgets(saveLastButton, clearHistoryButton, saveButton, removeButton, openButton, buttonsView);

        clearHistoryButton.setAction((b) -> {
            String title = I18n.translate("com.prmod.clear_history");
            Window.createWindow(v -> {
                return new ConfirmationWindow(title, this::clearHistory);
            });
        });
        removeButton.setAction(b -> {
            String title = 1 < selections.size() ? I18n.translate("com.prmod.remove_selected_?")
                    : I18n.translate("com.prmod.should_remove_?");
            Window.createWindow(v -> {
                return new ConfirmationWindow(title, this::remove);
            });
        });
        openButton.setAction(b -> {
            EventHandler.session.cleanUp();
            EventHandler.session = new PlaybackSession(selections.lastElement());
            SessionHUD.fadedness = 200;
            MinecraftClient.getInstance().setScreen(null);
        });
        saveLastButton.setAction(b -> {
            selectLast();
            onSave(b);
        });
        saveButton.setAction(this::onSave);

        updateRecordList();
    }

    protected void buttonListCallback(Button button) {
        if (!UIInput.isCtrlPressed())
            selections.clear();

        int i = 0;
        for (Widget w : buttonsView.getChildrenWidgets()) {
            if (!(w instanceof Button))
                continue;
            Button b = (Button) w;

            b.setHighlighted(selections.contains(EventHandler.recordHistory.get(i++)));
            b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
        }

        int buttonIndex = buttonsView.getChildrenWidgets().indexOf(button);
        if (selections.contains(EventHandler.recordHistory.get(buttonIndex))) {
            selections.remove(EventHandler.recordHistory.get(buttonIndex));
            button.setHighlighted(false);
            if (!selections.isEmpty()) {
                Button b = (Button) buttonsView.getChildrenWidgets()
                        .get(EventHandler.recordHistory.indexOf(selections.lastElement()));
                b.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            }
        } else {
            button.setHighlighted(true);
            button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            selections.push(EventHandler.recordHistory.get(buttonIndex));
        }
    }

    @Override
    public void draw() {
        if (!isVisible())
            return;

        // Styling
        int bodyMargin = GuiStyle.Gui.bodyMargin();
        int margin = GuiStyle.Gui.margin();
        int smallMargin = GuiStyle.Gui.smallMargin();
        int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();
        int fade1 = GraphicsHelper.getIntColor(GuiStyle.Gui.fade1());
        int fade2 = GraphicsHelper.getIntColor(GuiStyle.Gui.fade2());
        float listWidth = 0.7f;

        String worldName = " - " + Recording.getCurrentWorldName(MinecraftClient.getInstance());

        Viewport all = new Viewport();
        Viewport body = new Viewport(all);
        body.left = body.top = bodyMargin;
        body.right -= bodyMargin;
        body.bottom -= bodyMargin;
        Viewport listBody = new Viewport(body);
        listBody.left = listBody.top = margin;
        listBody.right = (int) ((listBody.getParent().getWidth() - margin * 2) * listWidth);
        listBody.bottom -= margin;
        Viewport list = new Viewport(listBody);
        list.left = margin;
        list.right -= margin;
        list.top = margin + UIRender.getStringHeight() + margin;
        list.bottom -= margin;
        Viewport actionsBody = new Viewport(body);
        actionsBody.left = listBody.right + margin;
        actionsBody.top = margin;
        actionsBody.right -= margin;
        Viewport actions = new Viewport(actionsBody);
        actions.left = margin;
        actions.top = margin + UIRender.getStringHeight() + margin;
        actions.right -= margin;
        actions.bottom = actions.top + 2 * (smallMargin + shortButtonHeight);
        actionsBody.bottom = actions.bottom + margin;
        Viewport asideBody = new Viewport(body);
        asideBody.left = listBody.right + margin;
        asideBody.top = actionsBody.bottom + margin;
        asideBody.right -= margin;
        asideBody.bottom -= margin;
        Viewport aside = new Viewport(asideBody);
        aside.left = margin;
        aside.top = UIRender.getStringHeight() + margin * 2;
        aside.right -= margin;
        aside.bottom = aside.top + (smallMargin + shortButtonHeight) * 3;
        Viewport desc = new Viewport(asideBody);
        desc.left = margin;
        desc.right -= margin;
        desc.top = aside.bottom;
        desc.bottom -= margin;

        body.pushMatrix(false);
        {
            UIRender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
        }
        body.popMatrix();

        listBody.pushMatrix(false);
        {
            UIRender.drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
            UIRender.drawString(I18n.translate("com.prmod.record_history") + worldName, margin, margin, 0xFFFFFFFF);
        }
        listBody.popMatrix();

        buttonsView.setViewport(list);
        buttonsView.draw();

        actionsBody.pushMatrix(true);
        {
            UIRender.drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
            UIRender.drawString(I18n.translate("com.prmod.actions"), margin, margin, 0xFFFFFFFF);
        }
        actionsBody.popMatrix();

        actions.pushMatrix(false);
        {
            Button[] buttons = { saveLastButton, clearHistoryButton };
            int i = 0;
            for (Button button : buttons) {
                button.width = aside.getWidth();
                button.height = shortButtonHeight;
                button.y = (shortButtonHeight + smallMargin) * i++;
                button.draw();
                button.setEnabled(!EventHandler.recordHistory.isEmpty());
            }
        }
        actions.popMatrix();

        asideBody.pushMatrix(true);
        {
            UIRender.drawGradientRect(0, 0, actionsBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);

            String subTitle = I18n.translate("com.prmod.information");
            subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
            UIRender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
        }
        asideBody.popMatrix();

        aside.pushMatrix(false);
        {
            Button[] buttons = { saveButton, removeButton, openButton };
            int i = 2;
            for (Button button : buttons) {
                button.width = aside.getWidth();
                button.height = shortButtonHeight;
                button.y = (shortButtonHeight + smallMargin) * (i++ - 2);
                button.draw();
                button.setEnabled(!selections.isEmpty());
            }

            if (selections.size() > 1)
                removeButton.setText(I18n.translate("com.prmod.remove_selected"));
            else
                removeButton.setText(I18n.translate("com.prmod.remove"));
        }
        aside.popMatrix();

        all.pushMatrix(false);
        {
            boolean flag = EventHandler.session.isActive();
            openButton.setEnabled(!flag && openButton.isEnabled());

            if (openButton.isHovered() && flag && !selections.isEmpty()) {
                String warning = I18n.translate("com.prmod.cannot_open_while_recording_or_playing");
                UIRender.drawHoveringText(warning, UIInput.getUICursorX(), UIInput.getUICursorY());
            }
        }
        all.popMatrix();

        if (!selections.isEmpty() && desc.getHeight() > 0) {
            desc.pushMatrix(true);
            {
                String[] lines = selections.lastElement().toString().split("\n");
                for (int i = 0; i < lines.length; i++) {
                    UIRender.drawString(lines[i], 0, UIRender.getStringHeight() * i, 0xFFFFFFFF);
                }
            }
            desc.popMatrix();
        }
    }

    private void override() {
        selections.lastElement().save(false, true, true);
        EventHandler.recordHistory.remove(selections.lastElement());
        selections.pop();
        updateRecordList();
    }

    private void selectLast() {
        selections.clear();
        for (int i = 0; i < buttonsView.getChildrenWidgets().size(); i++) {
            Button b = (Button) buttonsView.getChildrenWidgets().get(i);
            b.setHighlighted(selections.contains(EventHandler.recordHistory.get(i)));
            ;
            b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
            ;
        }

        selections.push(EventHandler.recordHistory.get(EventHandler.recordHistory.size() - 1));
        Button button = (Button) buttonsView.getChildrenWidgets()
                .get(EventHandler.recordHistory.indexOf(selections.lastElement()));
        button.setHighlighted(true);
        button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
    }

    private void onSave(Button button) {
        // If recording was originally opened from a file, prompt overriding alert box
        if (selections.lastElement().getOriginalFile() != null) {
            // When Save As New button is pressed from alert box
            SaveToNew saveNewCallback = () -> {
                NamingWindow namerBox = (NamingWindow) Window.createWindow(v -> {
                    return new NamingWindow(I18n.translate("com.prmod.name_recording"), (String s) -> {
                        return s.length() > 0;
                    }, this::save);
                });
                namerBox.getTextField().setText(selections.lastElement().getName());
                namerBox.getTextField().setCursorAtEnd(false);
                namerBox.getTextField().setFocused(true);
            };

            // Create Override box
            String boxMessage = I18n.translate("com.prmod.recording_was_loaded_from") + ":\n"
                    + selections.lastElement().getOriginalFile().getName();
            Window.createWindow(v -> {
                return new OverrideWindow(I18n.translate("com.prmod.override_recording_?"), boxMessage, this::override,
                        saveNewCallback);
            });
        } else {
            // Create namer box
            NamingWindow namerBox = (NamingWindow) Window.createWindow(v -> {
                return new NamingWindow(I18n.translate("com.prmod.name_recording"), (String s) -> {
                    return s.length() > 0;
                }, this::save);
            });
            namerBox.getTextField().setText(selections.lastElement().getName());
            namerBox.getTextField().setCursorAtEnd(false);
            namerBox.getTextField().setFocused(true);
        }
    }

    public void refresh() {
        updateRecordList();
    }

    private void save(String newName) {
        selections.lastElement().rename(newName);
        selections.lastElement().save(false, true, false);
        EventHandler.recordHistory.remove(selections.lastElement());
        selections.pop();
        Window.closeWindows();
        updateRecordList();
    }

    private void remove() {
        for (int i = 0; i < selections.size(); i++)
            EventHandler.recordHistory.remove(selections.get(i));
        selections.clear();
        updateRecordList();
    }

    private void clearHistory() {
        EventHandler.recordHistory.clear();
        EventHandler.session.cleanUp();
        selections.clear();
        if (!EventHandler.session.isActive())
            EventHandler.session = new RecordingSession();
        updateRecordList();
    }

    private void updateRecordList() {
        buttonsView.getChildrenWidgets().forEach(b -> b.close());
        buttonsView.getChildrenWidgets().clear();
        for (int i = 0; i < EventHandler.recordHistory.size(); i++) {
            Button button = new Button(EventHandler.recordHistory.get(i).getName());
            button.setHighlighted(selections.contains(EventHandler.recordHistory.get(i)));
            button.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
            button.setAction(this::buttonListCallback);
            button.setzLevel(0);
            buttonsView.addWidgets(button);
        }

        if (!selections.isEmpty()) {
            int latestSelection = EventHandler.recordHistory.indexOf(selections.lastElement());
            if (latestSelection > -1) {
                Button button = (Button) buttonsView.getChildrenWidgets().get(latestSelection);
                button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            }
        }

        if (selections.size() > 1)
            removeButton.setText(I18n.translate("com.prmod.remove_selected"));
        else
            removeButton.setText(I18n.translate("com.prmod.remove"));
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onCharTyped(int charTyped) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseScroll(int scrollAmount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(SidedUpdate side) {
        // TODO Auto-generated method stub

    }
}
