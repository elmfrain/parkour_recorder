package com.elmfer.prmod.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.joml.Vector3f;

import com.elmfer.prmod.EventHandler;
import com.elmfer.prmod.parkour.PlaybackSession;
import com.elmfer.prmod.parkour.Recording;
import com.elmfer.prmod.parkour.SessionHUD;
import com.elmfer.prmod.ui.MenuScreen.IMenuTabView;
import com.elmfer.prmod.ui.widgets.Button;
import com.elmfer.prmod.ui.widgets.Widget;
import com.elmfer.prmod.ui.window.ConfirmationWindow;
import com.elmfer.prmod.ui.window.NamingWindow;
import com.elmfer.prmod.ui.window.Window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

public class LoadRecordingView extends Widget implements IMenuTabView {

    private List<Recording> records = null;
    private Stack<Recording> selections = new Stack<Recording>();
    private ButtonListView buttonsView = new ButtonListView();

    private Button openButton = new Button(I18n.translate("com.prmod.open"));
    private Button deleteButton = new Button(I18n.translate("com.prmod.delete"));
    private Button renameButton = new Button(I18n.translate("com.prmod.rename"));

    public LoadRecordingView() {
        super();

        addWidgets(openButton, deleteButton, renameButton, buttonsView);

        openButton.setAction(b -> {
            EventHandler.session.cleanUp();
            EventHandler.session = new PlaybackSession(selections.lastElement());
            SessionHUD.fadedness = 200;
            MinecraftClient.getInstance().setScreen(null);
        });
        deleteButton.setAction(b -> {
            String title = selections.size() > 1 ? I18n.translate("com.prmod.delete_all_?")
                    : I18n.translate("com.prmod.should_delete_?");
            Window.createWindow(v -> {
                return new ConfirmationWindow(title, this::delete);
            });
        });
        renameButton.setAction(b -> {
            String title = I18n.translate("com.prmod.rename_recording");
            NamingWindow renameBox = (NamingWindow) Window.createWindow(v -> {
                return new NamingWindow(title, (String s) -> {
                    return s.length() > 0;
                }, this::rename);
            });
            renameBox.getTextField().setText(selections.lastElement().getName());
            renameBox.getTextField().setCursorAtEnd(false);
            renameBox.getTextField().setFocused(true);
        });

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

            b.setHighlighted(selections.contains(records.get(i++)));
            b.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
        }

        int buttonIndex = buttonsView.getChildrenWidgets().indexOf(button);
        if (selections.contains(records.get(buttonIndex))) {
            selections.remove(records.get(buttonIndex));
            button.setHighlighted(false);
            if (!selections.isEmpty()) {
                Button b = (Button) buttonsView.getChildrenWidgets().get(records.indexOf(selections.lastElement()));
                b.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            }
        } else {
            button.setHighlighted(true);
            button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            selections.push(records.get(buttonIndex));
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
        Viewport asideBody = new Viewport(body);
        asideBody.top = margin;
        asideBody.bottom -= margin;
        asideBody.left = listBody.right + margin;
        asideBody.right -= margin;
        Viewport aside = new Viewport(asideBody);
        aside.left = margin;
        aside.right -= margin;
        aside.top = margin + UIRender.getStringHeight() + margin;
        aside.bottom = aside.top + (shortButtonHeight + smallMargin) * 3;
        Viewport desc = new Viewport(asideBody);
        desc.left = margin;
        desc.right -= margin;
        desc.top = aside.bottom;
        desc.bottom -= margin;

        int fade1 = 1711276032;
        int fade2 = 0;

        body.pushMatrix(false);
        {
            UIRender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
        }
        body.popMatrix();

        listBody.pushMatrix(true);
        {
            UIRender.drawGradientRect(0, 0, listBody.getWidth(), listBody.getHeight() / 6, fade1, fade2);
            UIRender.drawString(I18n.translate("com.prmod.list") + worldName, margin, margin, 0xFFFFFFFF);
        }
        listBody.popMatrix();

        buttonsView.setViewport(list);
        buttonsView.draw();

        asideBody.pushMatrix(true);
        {
            UIRender.drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);

            String subTitle = I18n.translate("com.prmod.information");
            subTitle = 1 < selections.size() ? subTitle + " (" + Integer.toString(selections.size()) + ")" : subTitle;
            UIRender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
        }
        asideBody.popMatrix();

        aside.pushMatrix(true);
        {
            Button[] buttons = { openButton, deleteButton, renameButton };
            int i = 0;
            for (Button button : buttons) {
                button.width = aside.getWidth();
                button.height = shortButtonHeight;
                button.y = (shortButtonHeight + smallMargin) * i++;
                button.draw();
                button.setEnabled(!selections.isEmpty());
            }

            if (selections.size() > 1)
                deleteButton.setText(I18n.translate("com.prmod.delete_selected"));
            else
                deleteButton.setText(I18n.translate("com.prmod.delete"));
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

    public void refresh() {
        updateRecordList();
    }

    private void rename(String newName) {
        selections.lastElement().rename(newName);
        selections.lastElement().save(false, true, true);

        updateRecordList();
    }

    private void delete() {
        for (int i = 0; i < selections.size(); i++)
            Recording.deleteSave(selections.get(i));

        records = Arrays.asList(Recording.loadSaves());
        selections.clear();

        updateRecordList();
    }

    private void updateRecordList() {
        records = Arrays.asList(Recording.loadSaves());

        buttonsView.getChildrenWidgets().forEach(b -> b.close());
        buttonsView.getChildrenWidgets().clear();

        for (int i = 0; i < records.size(); i++) {
            Button button = new Button(records.get(i).getName());
            button.setHighlighted(selections.contains(records.get(i)));
            ;
            button.setHighlightTint(new Vector3f(0.0f, 0.3f, 0.0f));
            ;
            button.setAction(this::buttonListCallback);
            button.setzLevel(0);
            buttonsView.addWidgets(button);
        }

        if (!selections.isEmpty()) {
            int latestSelection = records.indexOf(selections.lastElement());
            if (latestSelection > -1) {
                Button button = (Button) buttonsView.getChildrenWidgets().get(latestSelection);
                button.setHighlightTint(new Vector3f(0.0f, 0.5f, 0.0f));
            }
        }

        if (selections.size() > 1)
            deleteButton.setText(I18n.translate("com.prmod.delete_selected"));
        else
            deleteButton.setText(I18n.translate("com.prmod.delete"));
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
