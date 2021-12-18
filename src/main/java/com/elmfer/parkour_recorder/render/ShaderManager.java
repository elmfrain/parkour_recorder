package com.elmfer.parkour_recorder.render;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Scanner;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL45;

import com.elmfer.parkour_recorder.ParkourRecorderMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ShaderManager {
	
	private static int DEFAULT_SHADER;
	private static long lastTime = System.currentTimeMillis();
	private static int countdownTime = 1000;
	
	
	static {
		DEFAULT_SHADER = makeProgram("vecshader.glsl", "fragshader.glsl");
	}
	
	public static float getFarPlaneDistance()
	{
		Minecraft mc = Minecraft.getInstance();
		return (float)(mc.gameRenderer.getDepthFar() * 16 * 1.41421356237);
	}
	public static float getNearPlaneDistance()
	{
		return 0.05f;
	}
	
	public static void reloadShaders() {
		
		if(System.currentTimeMillis() - lastTime >= countdownTime)
		{
			glDeleteProgram(DEFAULT_SHADER);
			DEFAULT_SHADER = makeProgram("vecshader.glsl", "fragshader.glsl");
			System.out.println("[Shaders] : Reloaded Shaders");
		}
	}
	
	public static int getDefaultShader()
	{
		return DEFAULT_SHADER;
	}
	
	public static void blitMCFramebuffer(RenderTarget toThisFrameBuffer)
	{
		Minecraft mc = Minecraft.getInstance();
		Window res = mc.getWindow();
		
		GL45.glBlitNamedFramebuffer(mc.getMainRenderTarget().frameBufferId, toThisFrameBuffer.frameBufferId, 0, 0, res.getWidth(), res.getHeight(), 0, 0, res.getWidth(), res.getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
	}
	
	public static int getUniformLocation(String param)
	{
		return glGetUniformLocation(DEFAULT_SHADER, param);
	}
	
	public static void importMatricies(int shader)
	{
		Minecraft mc = Minecraft.getInstance();
		float[] projectionMatrix = new float[16];
		float[] modelViewMatrix = new float[16];
		Window res = mc.getWindow();
		
		GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
		GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
		
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shader, "projection"), false, projectionMatrix);
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shader, "modelView"), false, modelViewMatrix);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex1"), 0);
		GL20.glUniform1i(GL20.glGetUniformLocation(shader, "texturesEnabled"), GL11.glIsEnabled(GL11.GL_TEXTURE_2D) ? 1 : 0);
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "displayWidth"), res.getWidth());
		GL20.glUniform1f(GL20.glGetUniformLocation(shader, "displayHeight"), res.getHeight());
	}
	
	public static void importMatricies(int shader, FloatBuffer worldSpaceMatrix, FloatBuffer normalSpaceMatrix)
	{
		importMatricies(shader);
		
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shader, "worldSpace"), false, worldSpaceMatrix);
		GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shader, "normalSpace"), false, normalSpaceMatrix);
	}
	
	public static int makeProgram(String vecShaderName, String fragShaderName) 
	{
		int vShader = glCreateShader(GL_VERTEX_SHADER);
		int fShader = glCreateShader(GL_FRAGMENT_SHADER);
		int program = glCreateProgram();
		
		glShaderSource(vShader, parseFile(new ResourceLocation(ParkourRecorderMod.MOD_ID, "shaders/" + vecShaderName)));
		glShaderSource(fShader, parseFile(new ResourceLocation(ParkourRecorderMod.MOD_ID, "shaders/" + fragShaderName)));
		
		glCompileShader(vShader);
		glCompileShader(fShader);
		
		if(glGetShaderi(vShader, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(vecShaderName);
			System.err.println("[Shaders] : Vertex Shader failed to compile!");
			System.err.println(glGetShaderInfoLog(vShader, 1000));
		}
		if(glGetShaderi(fShader, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(fragShaderName);
			System.err.println("[Shaders] : Fragment Shader failed to compile!");
			System.err.println(glGetShaderInfoLog(fShader, 1000));
		}
		
		glAttachShader(program, vShader);
		glAttachShader(program, fShader);
		
		glLinkProgram(program);
		
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		
		return program;
	}
	
	private static String parseFile(ResourceLocation file) {
		String code = "";
		
		try {
			InputStream bufferedFile = Minecraft.getInstance().getResourceManager().getResource(file).getInputStream();
			Scanner scanner = new Scanner(bufferedFile);
			while(scanner.hasNextLine()) {
				code += scanner.nextLine() + "\n";
			}
			scanner.close();
		} catch (IOException e) {
			System.err.println("Unable to read file!");
			e.printStackTrace();
		}
		return code;
	}
}