package com.elmfer.parkour_recorder.gui;

import com.elmfer.parkour_recorder.config.ConfigManager;
import com.elmfer.parkour_recorder.gui.MenuScreen.IMenuTabView;
import com.elmfer.parkour_recorder.gui.UIrender.Anchor;
import com.elmfer.parkour_recorder.gui.UIrender.Direction;
import com.elmfer.parkour_recorder.gui.widgets.Button;
import com.elmfer.parkour_recorder.gui.widgets.Widget;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class OptionView extends Widget implements IMenuTabView
{
	private CategoryHeading recordingSection = new CategoryHeading("com.elmfer.recording_options");
	private Button enableLoopButton = new Button(getLoopButtonText());
	private CategoryHeading menuSection = new CategoryHeading("com.elmfer.menu_options");
	private Button hiddenIpButton = new Button(getHiddenIpText());
	private CategoryHeading playbackSection = new CategoryHeading("com.elmfer.playback_options");
	private Button showInputsButton = new Button(getShowInputsText());

	public OptionView()
	{
		super();

		addWidgets(enableLoopButton, hiddenIpButton);

		enableLoopButton.setAction(b ->
		{
			ConfigManager.saveLoopMode(!ConfigManager.isLoopMode());
			enableLoopButton.setText(getLoopButtonText());
		});
		
		hiddenIpButton.setAction(b ->
		{
			ConfigManager.saveHiddenIp(!ConfigManager.isHiddenIp());
			hiddenIpButton.setText(getHiddenIpText());
		});
		
		showInputsButton.setAction(b ->
		{
			ConfigManager.saveShowInputs(!ConfigManager.showInputs());
			showInputsButton.setText(getShowInputsText());
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

	private static String getHiddenIpText()
	{
		return I18n.format("com.elmfer.hidden_ip",
				ConfigManager.isHiddenIp()
						? I18n.format("com.elmfer.enabled")
						: I18n.format("com.elmfer.disabled")
		);
	}
	
	private static String getShowInputsText()
	{
		return I18n.format("com.elmfer.show_inputs",
				ConfigManager.showInputs()
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

			body.pushMatrix(false);
			{
				UIrender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
			}
			body.popMatrix();
			
			asideBody.pushMatrix(false);
			{
				UIrender.drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);

				String subTitle = I18n.format("com.elmfer.options");
				UIrender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
			}
			asideBody.popMatrix();

			aside.pushMatrix(false);
			{
				Widget[] buttons =
				{
				 	recordingSection, enableLoopButton,
				 	menuSection, hiddenIpButton,
				 	playbackSection, showInputsButton
				};
				
				int i = 0;
				for(Widget button : buttons)
				{	
					button.width = aside.getWidth();
					button.height = shortButtonHeight;
					button.y = (shortButtonHeight + smallMargin) * i++;
					
					if(button instanceof Button)
					{
						button.x = 15;
						button.width -= 15;
					}
					
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
	
	private static class CategoryHeading extends Widget
	{
		String translationKey = "";
		
		public CategoryHeading(String translationKey)
		{
			this.translationKey = translationKey;
		}
		
		@Override
		public void onCursorMove(float mouseX, float mouseY)
		{
		}

		@Override
		public void onMouseClicked(int button)
		{
		}

		@Override
		public void onMouseReleased(int button)
		{
		}

		@Override
		public void onKeyPressed(int keyCode)
		{	
		}

		@Override
		public void onCharTyped(int charTyped)
		{
		}

		@Override
		public void onMouseScroll(int scrollAmount)
		{
		}

		@Override
		public void update(SidedUpdate side)
		{
		}

		@Override
		public void draw()
		{
			UIrender.drawGradientRect(Direction.TO_RIGHT, x, y, x + width, y + height, 0x33000000, 0);
			
			UIrender.drawString(Anchor.MID_LEFT, I18n.format(translationKey), x + 5, y + height / 2, 0xFFFFFFFF);
		}
		
	}
}
