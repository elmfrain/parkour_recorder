package com.elmfer.parkour_recorder.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.elmfer.parkour_recorder.ParkourRecorderMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class ModLogoRenderer
	{
		private static final List<MeshHeader> meshHeaders = new ArrayList<MeshHeader>();
		
		private static Mesh shadow_plane = null;
		private static Mesh pr_logo = null;
		
		private static boolean isLoaded = false;
		
		public static boolean isLoaded()
		{
			return isLoaded;
		}
		
		/**
		 * Loads mesh and textures of logo's 3D model.
		 */
		public static void load()
		{
			ResourceLocation loc = new ResourceLocation(ParkourRecorderMod.MOD_ID, "models/3d_logo_baked.bin");
			
			try
			{
				InputStream file = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
				byte[] d = IOUtils.toByteArray(file);
				ByteBuffer idata = ByteBuffer.wrap(d);
				
				idata.order(ByteOrder.LITTLE_ENDIAN);
				
				loadMeshHeaders(idata);
				loadMeshes(idata);
				isLoaded = true;
				
				if(shadow_plane == null || pr_logo == null) throw new IOException("Unable to find \"shadow_plane\" and/or \"pr_logo\" meshes!");
				
				file.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		/**
		 * Frees mesh and textures from GPU.
		 */
		public static void unload()
		{
			if(shadow_plane != null) shadow_plane.cleanUp();
			if(pr_logo != null) pr_logo.cleanUp();
			isLoaded = false;
		}
		
		/**
		 * Renders the mesh. It renders a simplified version if the mesh is not loaded.
		 */
		public static void render()
		{
			if(!isLoaded) return;
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glAlphaFunc(GL11.GL_ALWAYS, 0.0f);
			
			shadow_plane.render(GL11.GL_TRIANGLES);
			pr_logo.render(GL11.GL_TRIANGLES);
			
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
		}
		
		private static void loadMeshes(ByteBuffer buffer)
		{
			final int VERTEX_BYTES = 20;
			final int PIXEL_BYTES = 4;
			
			for(MeshHeader h : meshHeaders)
			{
				if(h.name.equals("shadow_plane") || h.name.equals("pr_logo"))
				{
					ByteBuffer verticies = BufferUtils.createByteBuffer(h.verticiesArraySize * VERTEX_BYTES);
					ByteBuffer indicies  = BufferUtils.createByteBuffer(h.indiciesArraySize * Integer.BYTES);
					ByteBuffer textureData = BufferUtils.createByteBuffer(h.textureWidth * h.textureHeight * PIXEL_BYTES);
					
					buffer.limit(h.verticiesArrayPtr + h.verticiesArraySize * VERTEX_BYTES);
					buffer.position(h.verticiesArrayPtr);
					verticies.put(buffer);
					buffer.limit(h.indiciesArrayPtr + h.indiciesArraySize * Integer.BYTES);
					buffer.position(h.indiciesArrayPtr);
					indicies.put(buffer);
					buffer.limit(h.texturePtr + h.textureWidth * h.textureHeight * PIXEL_BYTES);
					buffer.position(h.texturePtr);
					textureData.put(buffer);
					
					verticies.flip();
					indicies.flip();
					textureData.flip();
					
					Mesh mesh = new Mesh(verticies, indicies, textureData, h.textureWidth, h.textureHeight);
					if(h.name.equals("shadow_plane")) shadow_plane = mesh;
					else if(h.name.equals("pr_logo")) pr_logo = mesh;
				}
			}
		}
		
		private static void loadMeshHeaders(ByteBuffer buffer)
		{
			meshHeaders.clear();
			
			while(buffer.get(buffer.position()) != 0)
			{
				meshHeaders.add(new MeshHeader(buffer));
			}
		}
		
		private static class Mesh
		{
			int glBufferID;
			int glElemBufferID;
			int glVertArrayID;
			int glTextureID;
			int vertexCount;
			
			Mesh(ByteBuffer verticiesData, ByteBuffer indiciesData, ByteBuffer textureData, int texWidth, int texHeight)
			{
				glBufferID = GL15.glGenBuffers();
				glElemBufferID = GL15.glGenBuffers();
				glVertArrayID = GL30.glGenVertexArrays();
				vertexCount = indiciesData.capacity() / Integer.BYTES;
				
				GL30.glBindVertexArray(glVertArrayID);
				{
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferID);
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticiesData, GL15.GL_STATIC_DRAW);
					GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glElemBufferID);
					GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indiciesData, GL15.GL_STATIC_DRAW);
					
					GL20.glEnableVertexAttribArray(0);
					GL20.glEnableVertexAttribArray(1);
					GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, Float.BYTES * 5, Float.BYTES * 0);
					GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, Float.BYTES * 5, Float.BYTES * 3);
					
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);			
					GL11.glVertexPointer(3, GL11.GL_FLOAT, Float.BYTES * 5, Float.BYTES * 0);
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, Float.BYTES * 5, Float.BYTES * 3);
				}
				GL30.glBindVertexArray(0);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
				
				glTextureID = GL11.glGenTextures();
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureData);
			}
			
			public void cleanUp()
			{
				GL11.glDeleteTextures(glTextureID);
				GL15.glDeleteBuffers(glBufferID);
				GL15.glDeleteBuffers(glElemBufferID);
				GL30.glDeleteVertexArrays(glVertArrayID);
			}
			
			public void render(int drawMode)
			{
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
				GL30.glBindVertexArray(glVertArrayID);
				GL11.glDrawElements(drawMode, vertexCount, GL11.GL_UNSIGNED_INT, 0);
				GL30.glBindVertexArray(0);
				
				//Tell Minecraft the bound texture changed
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			}
		}
		
		private static class MeshHeader
		{
			String name;
			int verticiesArrayPtr, verticiesArraySize;
			int indiciesArrayPtr, indiciesArraySize;
			int texturePtr, textureWidth, textureHeight;
			
			MeshHeader(ByteBuffer buffer)
			{
				// Find the name's size in bytes
				int prevPos = buffer.position();
				int nameSize = 0;
				for (int i = 0; i < buffer.capacity(); i++)
					if (buffer.get() != 0)
						nameSize++;
					else
						break;
				buffer.position(prevPos);
				
				// Store the name
				byte[] namedata = new byte[nameSize];
				buffer.get(namedata);
				buffer.get();
				name = new String(namedata, StandardCharsets.UTF_8);
				
				// Get remaining data
				verticiesArrayPtr = buffer.getInt();
				verticiesArraySize = buffer.getInt();
				indiciesArrayPtr = buffer.getInt();
				indiciesArraySize = buffer.getInt();
				texturePtr = buffer.getInt();
				textureWidth = buffer.getInt();
				textureHeight = buffer.getInt();
			}
		}
	}