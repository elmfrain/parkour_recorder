package com.elmfer.prmod.mesh;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.elmfer.prmod.ParkourRecorder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class Meshes {
	
	private static final Map<String, Mesh> meshes = new HashMap<String, Mesh>();
	
	private static boolean loadedMeshes = false;
	private static boolean loadedIcons = false;
	private static boolean hasWarnedAboutMeshesAlreadyLoaded = false;
	private static boolean hasWarnedAboutIconsAlreadyLoaded = false;
	
	public static void loadMeshes() {
		if(loadedMeshes) {
			if (!hasWarnedAboutMeshesAlreadyLoaded) {
				ParkourRecorder.LOGGER.warn("Meshes have already been loaded!");
				hasWarnedAboutMeshesAlreadyLoaded = true;
			}
			return;
		}
		
		ParkourRecorder.LOGGER.info("Loading meshes...");
		
		VertexFormat format = VertexFormat.POS_COLOR;
		
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/arrow.ply")).makeRenderable(format);
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/arrow-loop-mode.ply")).makeRenderable(format);
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/finish.ply")).makeRenderable(format);
		
		loadedMeshes = true;
	}
	
	public static void loadIcons() {
		if (loadedIcons) {
			if (!hasWarnedAboutIconsAlreadyLoaded) {
				ParkourRecorder.LOGGER.warn("Icons have already been loaded!");
				hasWarnedAboutIconsAlreadyLoaded = true;
			}
			return;
		}
		
		ParkourRecorder.LOGGER.info("Loading icons...");
		
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/add_checkpoint_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/checkpoint.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/checkpoint_icon.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/down_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/end_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/left_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/loop_icon.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/next_checkpoint_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/next_frame_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/pause_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/play_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/prev_checkpoint_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/prev_frame_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/remove_checkpoint_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/rewind_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/right_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/settings_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/sneak_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/spacebar_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/sprint_key.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/start_button.ply"));
		load(new Identifier(ParkourRecorder.MOD_ID, "meshes/icons/up_key.ply"));
	}
	
	public static Mesh get(String name) {
		return meshes.get(name);
	}
	
	public static boolean hasMesh(String name) {
		return meshes.containsKey(name);
	}
	
	
	public static Mesh load(Identifier model) {
		
		try {
			
			InputStream modelStream = MinecraftClient.getInstance().getResourceManager().getResource(model).get().getInputStream();
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
			
			Mesh mesh = new Mesh(modelName);
			
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
						mesh.positions.ensureCapacity(vertexTotal * 3);
						mesh.uvs = Optional.of(new ArrayList<Float>());
						mesh.uvs.get().ensureCapacity(vertexTotal * 2);
						mesh.normals.ensureCapacity(vertexTotal * 3);
						mesh.colors = Optional.of(new ArrayList<Float>());
						mesh.colors.get().ensureCapacity(vertexTotal * 4);
						mesh.indices.ensureCapacity(faceTotal * 3);
						continue;
					}
				}
				else
				{
					if(vertexCount < vertexTotal)
					{
						mesh.positions.add(scanner.nextFloat()); // pos x
						mesh.positions.add(scanner.nextFloat()); // pos y
						mesh.positions.add(scanner.nextFloat()); // pos z
						mesh.normals.add(scanner.nextFloat()); // norm x
						mesh.normals.add(scanner.nextFloat()); // norm y
						mesh.normals.add(scanner.nextFloat()); // norm z
						mesh.uvs.get().add(scanner.nextFloat()); // tex s
						mesh.uvs.get().add(scanner.nextFloat()); // tex t
						mesh.colors.get().add(scanner.nextFloat() / 255.0f); // color r
						mesh.colors.get().add(scanner.nextFloat() / 255.0f); // color g
						mesh.colors.get().add(scanner.nextFloat() / 255.0f); // color b
						mesh.colors.get().add(scanner.nextFloat() / 255.0f); // color a
						vertexCount++;	
					}
					else if(faceCount < faceTotal)
					{
						int a = 0, b = 0, c = 0;
						scanner.nextInt();
						a = scanner.nextInt();
						b = scanner.nextInt();
						c = scanner.nextInt();
						mesh.indices.add(a);
						mesh.indices.add(b);
						mesh.indices.add(c);
						faceCount++;
					}
					else break;
				}
			}

			meshes.put(modelName, mesh);
			
			scanner.close();
			modelFile.close();
			
			return mesh;
		}catch(Exception e) {
			ParkourRecorder.LOGGER.error("Failed to load \"{}\" mesh because of: {}", model.getPath(), e.getMessage());
		}
		
		return null;
	}
}