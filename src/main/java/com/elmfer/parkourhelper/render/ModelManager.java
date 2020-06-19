package com.elmfer.parkourhelper.render;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.elmfer.parkourhelper.ParkourHelperMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;

public class ModelManager {
	
	private static final Map<String, Model> models = new HashMap<String, Model>();
	private static final int VERTEX_BYTES = 44;
	
	public static void loadModelFromResource(ResourceLocation model) {
		
		try {
			
			InputStream modelStream = Minecraft.getMinecraft().getResourceManager().getResource(model).getInputStream();
			BufferedInputStream modelFile = new BufferedInputStream(modelStream);
			modelFile.mark(Integer.MAX_VALUE);
			String[] modelPath = model.getResourcePath().split("/");
			String modelName = modelPath[modelPath.length - 1].replace(".ply", "");
			Scanner scanner = new Scanner(modelStream);
			int vertexCount = 0;
			int vertexTotal = 0;
			int faceCount = 0;
			int faceTotal = 0;
			boolean headerDone = false;
			ByteBuffer modelData = null;
			ByteBuffer modelIndecies = null;
			
			while(scanner.hasNextLine())
			{
				if(!headerDone)
				{
					String line = scanner.nextLine();
					if(line.contains("element vertex "))
					{vertexTotal = Integer.parseInt(line.substring(15)); continue;}
					else if(line.contains("element face "))
					{faceTotal = Integer.parseInt(line.substring(13)); continue;}
					else if(line.contains("end_header"))
					{
						headerDone = true; 
						modelData = BufferUtils.createByteBuffer(vertexTotal * VERTEX_BYTES);
						modelIndecies = BufferUtils.createByteBuffer(faceTotal * Integer.BYTES * 3);
						continue;
					}
				}
				else
				{
					if(vertexCount < vertexTotal)
					{
						modelData.putFloat(scanner.nextFloat()); // pos x 
						modelData.putFloat(scanner.nextFloat()); // pos y 
						modelData.putFloat(scanner.nextFloat()); // pos x 
						modelData.putFloat(scanner.nextFloat()); // norm x 
						modelData.putFloat(scanner.nextFloat()); // norm y
						modelData.putFloat(scanner.nextFloat()); // norm z 
						modelData.putFloat(scanner.nextFloat()); // tex s
						modelData.putFloat(scanner.nextFloat()); // tex t 
						modelData.putFloat(scanner.nextFloat() / 255.0f); // color r
						modelData.putFloat(scanner.nextFloat() / 255.0f); // color g
						modelData.putFloat(scanner.nextFloat() / 255.0f); // color b
						scanner.nextFloat(); // color a
						vertexCount++;	
					}
					else if(faceCount < faceTotal)
					{
						int a = 0, b = 0, c = 0, d = 0;
						a = scanner.nextInt();
						b = scanner.nextInt();
						c = scanner.nextInt();
						d = scanner.nextInt();
						modelIndecies.putInt(b);
						modelIndecies.putInt(c);
						modelIndecies.putInt(d);
						faceCount++;
					}
					else break;
				}
			}
			modelData.flip();
			modelIndecies.flip();
			if(models.containsKey(modelName))
				models.get(modelName).cleanUp();
			models.put(modelName, new Model(modelData, modelIndecies));
			
			scanner.close();
			modelFile.close();
		}catch(IOException e) {
			CrashReport report = new CrashReport("Invalid Model: ", e.getCause());
			Minecraft.getMinecraft().crashed(report);
		}
	}
	
	public static boolean renderModel(String modelName)
	{
		if(models.containsKey(modelName))
		{models.get(modelName).render(); return true;}
		else
		{
			loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/" + modelName + ".ply"));
		}
		return false;
	}
	
	private static class Model
	{
		int glBufferID;
		int glElemBufferID;
		int glVertArrayID;
		int vertexCount;
		
		Model(ByteBuffer modelData, ByteBuffer modelIndecies)
		{
			glBufferID = GL15.glGenBuffers();
			glElemBufferID = GL15.glGenBuffers();
			glVertArrayID = GL30.glGenVertexArrays();
			vertexCount = modelIndecies.capacity() / Integer.BYTES;
			
			GL30.glBindVertexArray(glVertArrayID);
			{
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferID);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, modelData, GL15.GL_STATIC_DRAW);
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glElemBufferID);
				GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndecies, GL15.GL_STATIC_DRAW);
				
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				GL20.glEnableVertexAttribArray(3);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, Float.BYTES * 11, Float.BYTES * 0);
				GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, Float.BYTES * 11, Float.BYTES * 3);
				GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, Float.BYTES * 11, Float.BYTES * 6);
				GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, Float.BYTES * 11, Float.BYTES * 8);
			}
			GL30.glBindVertexArray(0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		
		public void cleanUp()
		{
			OpenGlHelper.glDeleteBuffers(glBufferID);
			OpenGlHelper.glDeleteBuffers(glElemBufferID);
			GL30.glDeleteVertexArrays(glVertArrayID);
		}
		
		public void render()
		{
			int prevVertArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
			GL30.glBindVertexArray(glVertArrayID);
			GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
			GL30.glBindVertexArray(0);
		}
	}
}
