package com.elmfer.prmod.ui;

import com.elmfer.prmod.config.Config;
import com.elmfer.prmod.ui.MenuScreen.IMenuTabView;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.UIRender.Direction;
import com.elmfer.prmod.ui.widgets.Button;
import com.elmfer.prmod.ui.widgets.Widget;

import net.minecraft.client.resource.language.I18n;

public class OptionView extends Widget implements IMenuTabView
{
	private CategoryHeading recordingSection = new CategoryHeading("com.prmod.recording_options");
	private Button enableLoopButton = new Button(getLoopButtonText());
	private CategoryHeading menuSection = new CategoryHeading("com.prmod.menu_options");
	private Button hiddenIpButton = new Button(getHiddenIpText());
	private CategoryHeading playbackSection = new CategoryHeading("com.prmod.playback_options");
	private Button showInputsButton = new Button(getShowInputsText());

	public OptionView()
	{
		super();
		addWidgets(enableLoopButton, hiddenIpButton);

		enableLoopButton.setAction(b ->
		{
			Config.setLoopMode(!Config.isLoopMode());
			enableLoopButton.setText(getLoopButtonText());
		});
		
		hiddenIpButton.setAction(b ->
		{
			Config.setHiddenIp(!Config.isHiddenIp());
			hiddenIpButton.setText(getHiddenIpText());
		});
		
		showInputsButton.setAction(b ->
		{
			Config.setShowInputs(!Config.showInputs());
			showInputsButton.setText(getShowInputsText());
		});
	}

	private static String getLoopButtonText()
	{
		return I18n.translate("com.prmod.loop",
				Config.isLoopMode()
						? I18n.translate("com.prmod.enabled")
						: I18n.translate("com.prmod.disabled")
		);

	}

	private static String getHiddenIpText()
	{
		return I18n.translate("com.prmod.hidden_ip",
				Config.isHiddenIp()
						? I18n.translate("com.prmod.enabled")
						: I18n.translate("com.prmod.disabled")
		);
	}
	
	private static String getShowInputsText()
	{
		return I18n.translate("com.prmod.show_inputs",
				Config.showInputs()
						? I18n.translate("com.prmod.enabled")
						: I18n.translate("com.prmod.disabled")
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
		aside.top = margin + UIRender.getStringHeight() + margin;
		aside.bottom = aside.top + (shortButtonHeight + smallMargin) * 3;

		int fade1 = 1711276032;
		int fade2 = 0;

		body.pushMatrix(false);
		{
			UIRender.drawRect(0, 0, body.getWidth(), body.getHeight(), 1275068416);
		}
		body.popMatrix();
		
		asideBody.pushMatrix(false);
		{
			UIRender.drawGradientRect(0, 0, asideBody.getWidth(), asideBody.getHeight() / 6, fade1, fade2);

			String subTitle = I18n.translate("com.prmod.options");
			UIRender.drawString(subTitle, margin, margin, 0xFFFFFFFF);
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
			UIRender.drawGradientRect(Direction.TO_RIGHT, x, y, x + width, y + height, 0x33000000, 0);
			
			UIRender.drawString(Anchor.MID_LEFT, I18n.translate(translationKey), x + 5, y + height / 2, 0xFFFFFFFF);
		}
		
	}
}
