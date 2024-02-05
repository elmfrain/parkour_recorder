package com.elmfer.prmod.ui;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.elmfer.prmod.ParkourRecorder;
import com.elmfer.prmod.animation.Smoother;
import com.elmfer.prmod.ui.MenuScreen.IMenuTabView;
import com.elmfer.prmod.ui.UIRender.Anchor;
import com.elmfer.prmod.ui.UIRender.Direction;
import com.elmfer.prmod.ui.widgets.Widget;
import com.elmfer.prmod.util.HTTPSFetcher;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

public class ModTitleScreenView extends Widget implements IMenuTabView {
	private static HTTPSFetcher changelog = null;
	private static String offlineChangelog = null;
	private Viewport changelogViewport = new Viewport();
	private Smoother changelogScrool = new Smoother();

	private Smoother fovTransition = new Smoother();
	private Smoother logoAngleSpeed = new Smoother();
	private float prevLogoRotation = -90.0f;
	private float logoRotation = -90.0f;
	private int logoOpacityCounter = 0;
	/**
	 * The state for which the user controls the rotation of the mod's logo
	 * 
	 * At -2: Logo rotates on its own, but waits for user to move the cursor before
	 * giving control of the rotation to user At -1: User has control of the
	 * rotation At 0 -> 39: No rotation input, but counts up to 40 in this phase At
	 * 40 <=: The logo rotates on its own
	 */
	private int userControlState = 40;
	private float prevMouseX = 0.0f;
	private float cursorSpeed = 0.0f;

	@Override
	public void onCursorMove(float mouseX, float mouseY) {
		cursorSpeed = mouseX - prevMouseX;
		prevMouseX = mouseX;

		if (userControlState == -2)
			userControlState = -1;
	}

	@Override
	public void onMouseClicked(int button) {
		// TODO Auto-generated method stub
		if (button == 0 && UIInput.getUICursorY() > 15
				&& !changelogViewport.isHovered(UIInput.getUICursorX(), UIInput.getUICursorY())) {
			userControlState = -2;
		}
	}

	@Override
	public void onMouseReleased(int button) {
		// TODO Auto-generated method stub
		if (button == 0) {
			if (userControlState == -2)
				userControlState = 40;
			else if (userControlState == -1)
				userControlState = 0;
		}
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
		if (changelogViewport.isHovered(UIInput.getUICursorX(), UIInput.getUICursorY())) {
			changelogScrool.grab(changelogScrool.grabbingTo() + scrollAmount * 0.2);
		} else {
			logoRotation += scrollAmount * 0.15f;
			userControlState = 15;
		}
	}

	@Override
	public void refresh() {
		if (!ModLogoRenderer.isLoaded())
		{
			ModLogoRenderer.load();
			prevLogoRotation = -90.0f;
			logoRotation = -90.0f;
		}

		logoAngleSpeed.setValueAndGrab(1.8);
		logoAngleSpeed.setSpeed(5.0);

		fovTransition.setSpeed(1.8);
		if (logoOpacityCounter < 4)
			fovTransition.setValue(10.0);

		if (offlineChangelog == null)
			loadOfflineChangelog();
		
		if (changelog == null)
			changelog = new HTTPSFetcher("https://prmod.elmfer.com/changelog.txt");
	}

	@Override
	public void update(SidedUpdate side) {
		if (side == SidedUpdate.CLIENT) {
			if (logoOpacityCounter < 25 && isVisible())
				logoOpacityCounter++;

			prevLogoRotation = logoRotation;
			logoRotation += logoAngleSpeed.getValue();

			if (userControlState >= 0)
				userControlState++;
			fovTransition.grab(38.0f);

			if (userControlState >= 40 || userControlState == -2)
				logoAngleSpeed.grab(1.8);
			else if (userControlState == -1)
				logoAngleSpeed.grab(cursorSpeed * 3);
			else
				logoAngleSpeed.grab(0.0);

			cursorSpeed = 0.0f;
		}
	}

	@Override
	public void draw() {
		if (!isVisible())
			return;

		float uiWidth = UIRender.getUIwidth();
		float uiHeight = UIRender.getUIheight();
		float yCursor = UIRender.getStringHeight() * 4 + 19;

		// Draw main body
		UIRender.drawGradientRect(uiWidth / 4 - 10, 15, uiWidth * 3 / 4 + 10, uiHeight * 5 / 6, 1711276032, 0);
		UIRender.drawRect(uiWidth / 3, 18, uiWidth * 2 / 3, yCursor, 1275068416);
		UIRender.drawGradientRect(Direction.TO_RIGHT, uiWidth / 4 - 10, 18, uiWidth / 3, yCursor, 0, 1275068416);
		UIRender.drawGradientRect(Direction.TO_LEFT, uiWidth * 2 / 3, 18, uiWidth * 3 / 4 + 10, yCursor, 0, 1275068416);

		//Render Title
		String title = I18n.translate("com.prmod.parkour_recorder");
		float titleWidth = UIRender.getStringWidth(title);
		RenderSystem.getModelViewStack().push();
		{
			RenderSystem.getModelViewStack().scale(2.0f, 2.0f, 1.0f);
			RenderSystem.applyModelViewMatrix();
			UIRender.drawString(Anchor.TOP_CENTER, title, uiWidth / 4, 13.5f, -10066330);
			UIRender.drawString(Anchor.TOP_CENTER, title, uiWidth / 4, 12.5f, 0xFFFFFFFF);
		}
		RenderSystem.getModelViewStack().pop();
		RenderSystem.applyModelViewMatrix();

		// Render tagline
		String tagline = "by elmfer - v" + ParkourRecorder.MOD_VERSION;
		UIRender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 12, -10066330);
		UIRender.drawString(tagline, uiWidth / 2 - titleWidth, yCursor - 13, 0xFFFFFFFF);

		// Render slogan
		yCursor += 8;
		UIRender.drawString(Anchor.CENTER, I18n.translate("com.prmod.record_and_playback_your_parkour_sessions"),
				uiWidth / 2, yCursor + 1, -10066330);
		UIRender.drawString(Anchor.CENTER, I18n.translate("com.prmod.record_and_playback_your_parkour_sessions"),
				uiWidth / 2, yCursor, 0xFFFFFFFF);

		// Render changelog title and body
		changelogViewport = new Viewport();
		changelogViewport.left = (int) uiWidth / 4 - 5;
		changelogViewport.top = uiHeight * 0.75f;
		changelogViewport.right = uiWidth * 3 / 4 + 5;
		changelogViewport.bottom = uiHeight;

		changelogViewport.pushMatrix(false);
		{
			UIRender.drawRect(0, 0, changelogViewport.getWidth(), changelogViewport.getHeight(), 2130706432);
			UIRender.drawRect(0, 0, changelogViewport.getWidth(), 12, 1711276032);
			UIRender.drawString(Anchor.MID_LEFT, I18n.translate("com.prmod.changelog"), 6, 6, 0xFFFFFFFF);
		}
		changelogViewport.popMatrix();

		// Render the changelog
		changelogViewport.top += 17;
		changelogViewport.left += 5;
		changelogViewport.right -= 5;
		changelogViewport.bottom -= 5;
		changelogViewport.pushMatrix(true);
		{
			RenderSystem.getModelViewStack().push();
			{
				RenderSystem.getModelViewStack().translate(0.0, -changelogScrool.getValue(), 0.0);
				RenderSystem.applyModelViewMatrix();

				yCursor = 0.0f;
				// Process changelog line per line
				String changelogText = changelog.hasFailed() ? offlineChangelog : changelog.stringContent();
				for (String line : changelogText.split("\n")) {
					String formats = "";
					int linePos = 0;
					int lineLen = line.length();

					// Determine indentation
					float xCursor = 0.0f;
					int i = 0;
					for (; i < lineLen; i++) {
						if (line.charAt(i) != ' ')
							break;
						xCursor += 5.0f;
						lineLen--;
					}
					line = line.substring(i);

					// Render the line with as much space as it needs
					// Wraps the line around when it reaches the width of this viewport
					// All text formatting is preserved for each line
					do {
						String subline = UIRender.splitStringToFit(line.substring(linePos),
								changelogViewport.getWidth() - xCursor, " ");
						UIRender.drawString(formats + subline, xCursor, yCursor, 0xFFFFFFFF);
						linePos += subline.length();
						formats += UIRender.getTextFormats(subline);

						yCursor += UIRender.getStringHeight() + 2;
					} while (linePos < lineLen);
				}

				final float MIN_SCROLL = 0;
				final float MAX_SCROLL = yCursor - changelogViewport.getHeight() / 2;
				if (changelogScrool.getValue() < 0)
					changelogScrool.grab(MIN_SCROLL);
				else if (changelogScrool.getValue() > MAX_SCROLL)
					changelogScrool.grab(MAX_SCROLL);
			}
			RenderSystem.getModelViewStack().pop();
		}
		changelogViewport.popMatrix();

		// Draw the 3D logo
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDisable(GL11.GL_CULL_FACE);

		Matrix4f prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
		RenderSystem.getProjectionMatrix().identity();
		RenderSystem.getProjectionMatrix().translate(new Vector3f(0.0f, 0.04f, 0.0f));
		RenderSystem.getProjectionMatrix().mul((new Matrix4f()).perspective((float) fovTransition.getValue(),
				(float) UIRender.getWindowWidth() / (float) UIRender.getWindowHeight(), 0.05f, 50.0f));

		RenderSystem.getModelViewStack().push();
		{
			float rotation = (float) Math
					.toRadians(prevLogoRotation + (logoRotation - prevLogoRotation) * UIRender.getPartialTicks());
			RenderSystem.getModelViewStack().peek().getPositionMatrix().identity();
			AxisAngle4f xRot = new AxisAngle4f(0.0907571f, 0.0f, 0.0f, rotation);
			RenderSystem.getModelViewStack().peek().getPositionMatrix().mul(new Matrix4f().rotate(xRot)); // 5.2 deg on x axis
			RenderSystem.getModelViewStack().translate(0.0, -1.51, -10.0);
			AxisAngle4f yRot = new AxisAngle4f(0.0f, 1.0f, 0.0f, (float) Math.toRadians(180.0f));
			RenderSystem.getModelViewStack().peek().getPositionMatrix().mul(new Matrix4f().rotate(yRot)); // 180 deg on y
			RenderSystem.applyModelViewMatrix();

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, logoOpacityCounter / 25.0f);
			ModLogoRenderer.render();
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
		RenderSystem.getModelViewStack().pop();
		RenderSystem.applyModelViewMatrix();

		RenderSystem.setProjectionMatrix(prevProjection, VertexSorter.BY_Z);
		// End draw 3D logo
	}

	public void onExit() {
		ModLogoRenderer.unload();
		changelog = null;
		offlineChangelog = null;
	}

	private static void loadOfflineChangelog() {
		Identifier loc = new Identifier(ParkourRecorder.MOD_ID, "changelog.txt");

		try {
			InputStream file = MinecraftClient.getInstance().getResourceManager().getResource(loc).get().getInputStream();
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			byte[] fileChunk = new byte[4096];
			int bytesRead = 0;
			while(0 < (bytesRead = file.read(fileChunk)))
			{
				os.write(fileChunk, 0, bytesRead);
			}

			offlineChangelog = new String(os.toByteArray(), StandardCharsets.ISO_8859_1);
			
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			offlineChangelog = "Failed to load changelog!";
		}
	}

}
