package com.elmfer.parkour_recorder.gui;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class OptionView extends Widget implements IMenuTabView
{

	private Button enableLoopButton = new Button(getLoopButtonText());

	public OptionView()
	{
		super();

		addWidgets(enableLoopButton);

		enableLoopButton.setAction(b ->
		{
			ConfigManager.saveLoopMode(!ConfigManager.isLoopMode());
			enableLoopButton.setText(getLoopButtonText());
		});
	}

	private static String getLoopButtonText()
	{
		return I18n.format("com.elmfer.loop",
				ConfigManager.isLoopMode()
						? I18n.format("com.elmfer.enabled")
						: I18n.format("com.elmfer.disabled")
		);
	}

	@Override
	public void draw()
	{
		if(!isVisible()) return;
		
		// Styling
		int bodyMargin = GuiStyle.Gui.bodyMargin();
		int margin = GuiStyle.Gui.margin();
		int smallMargin = GuiStyle.Gui.smallMargin();
		int shortButtonHeight = GuiStyle.Gui.shortButtonHeight();

		Viewport all = new Viewport();
		Viewport body = new Viewport(all);
		body.left = body.top = bodyMargin;
		body.right -= bodyMargin;
		body.bottom -= bodyMargin;
		Viewport asideBody = new Viewport(body);
		asideBody.top = margin;
		asideBody.bottom -= margin;
		asideBody.left = margin;
		asideBody.right -= margin;
		Viewport aside = new Viewport(asideBody);
		aside.left = margin;
		aside.right -= margin;
		aside.top = margin + UIrender.getStringHeight() + margin;
		aside.bottom = aside.top + (shortButtonHeight + smallMargin) * 3;

		GL11.glPushMatrix();
		{
			int fade1 = 1711276032;
			int fade2 = 0;

			asideBody.pushMatrix(true);
			{
				UIrender.drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);

				String subTitle = I18n.format("com.elmfer.option");
				UIrender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();

			aside.pushMatrix(true);
			{
				Button[] buttons = {enableLoopButton};
				int i = 0;
				for(Button button : buttons)
				{
					button.width = aside.getWidth();
					button.height = shortButtonHeight;
					button.y = (shortButtonHeight + smallMargin) * i++;
					button.draw();
				}
			}
			aside.popMatrix();
		}
		GL11.glPopMatrix();
	}

	public void refresh()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onCursorMove(float mouseX, float mouseY)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseClicked(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseReleased(int button)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyPressed(int keyCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCharTyped(int charTyped)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMouseScroll(int scrollAmount)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(SidedUpdate side)
	{
		// TODO Auto-generated method stub
		
	}
}
