package com.elmfer.prmod.render;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.elmfer.prmod.ParkourRecorder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class ModelManager {
	
	private static final List<Consumer<Integer>> SCHEDULED_OPERATIONS = new ArrayList<>();
	private static final Map<String, Model> models = new HashMap<String, Model>();
	private static final int VERTEX_BYTES = 36;
	
	public static void onRenderTick()
	{
		SCHEDULED_OPERATIONS.forEach((o) -> o.accept(0));
		SCHEDULED_OPERATIONS.clear();
	}
	
	public static void loadModelFromResource(Identifier model) {
		
		try {
			
			InputStream modelStream = MinecraftClient.getInstance().getResourceManager().open(model);
			BufferedInputStream modelFile = new BufferedInputStream(modelStream);
			modelFile.mark(Integer.MAX_VALUE);
			String[] modelPath = model.getPath().split("/");
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
						modelData.put((byte) scanner.nextInt()); //color r
						modelData.put((byte) scanner.nextInt()); //color g
						modelData.put((byte) scanner.nextInt()); //color b
						modelData.put((byte) scanner.nextInt()); //color a
						vertexCount++;	
					}
					else if(faceCount < faceTotal)
					{
						int a = 0, b = 0, c = 0;
						scanner.nextInt();
						a = scanner.nextInt();
						b = scanner.nextInt();
						c = scanner.nextInt();
						modelIndecies.putInt(a);
						modelIndecies.putInt(b);
						modelIndecies.putInt(c);
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
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean renderModel(String modelName, int drawMode)
	{
		if(models.containsKey(modelName))
		{
			models.get(modelName).render(drawMode);
			return true;
		}
		else
		{
			SCHEDULED_OPERATIONS.add((i) -> {loadModelFromResource(new Identifier(ParkourRecorder.MOD_ID, "models/" + modelName + ".ply"));});
			return false;
		}
	}
	
	public static boolean renderModel(String modelName)
	{
		return renderModel(modelName, GL11.GL_TRIANGLES);
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
			
			int prevBind = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
			int prevArryBufBind = GL11.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
			int prevElemBufBind = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
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
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT,         false, VERTEX_BYTES, Float.BYTES * 0);
				GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT,         false, VERTEX_BYTES, Float.BYTES * 3);
				GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT,         false, VERTEX_BYTES, Float.BYTES * 6);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE,  true, VERTEX_BYTES, Float.BYTES * 8);
			}
			GL30.glBindVertexArray(prevBind);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevArryBufBind);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevElemBufBind);
		}
		
		public void cleanUp()
		{
			GL15.glDeleteBuffers(glBufferID);
			GL15.glDeleteBuffers(glElemBufferID);
			GL30.glDeleteVertexArrays(glVertArrayID);
		}
		
		public void render(int drawMode)
		{
			int prevBinding = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
			GL30.glBindVertexArray(glVertArrayID);
			GL11.glDrawElements(drawMode, vertexCount, GL11.GL_UNSIGNED_INT, 0);
			GL30.glBindVertexArray(prevBinding);
		}
	}
	
	public static void clearCachedModels()
	{
		for(Model model : models.values())
			model.cleanUp();
		
		models.clear();
	}
}
