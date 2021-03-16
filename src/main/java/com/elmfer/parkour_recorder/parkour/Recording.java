package com.elmfer.parkour_recorder.parkour;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Vector3d;

public class Recording implements List<ParkourFrame>
{
	/**Number of bytes needed to represent a recording. Used for serializing.**/
	public static final int BYTES = 56;
	
	/**A fixed directory where the recording are going to be saved.**/
	public static final File SAVING_DIRECTORY = new File("parkour-saves");
	
	/**File extension to identify recording files.**/
	public static final String FILE_EXTENSION = ".pkr";
	
	/**Filters out files that doesn't end with the {@code FILE_EXTENSION} or if the format is unknown.**/
	public static final FileFilter FILE_FILTER = new FileFilter() 
	{	
		@Override
		public boolean accept(File pathname)
		{	
			if(pathname.getAbsolutePath().endsWith(FILE_EXTENSION))
			{
				Deserializer.FileHeader header = new Deserializer.FileHeader(pathname);
				SavingFormat format = SavingFormat.getFormatFromID(header.FORMAT_ID);
				
				return format != null;
			}
			return false;
		}
	};
	
	/**Ensures that when the recordings are saved, its data has the correct byte order.**/
	private static final ByteOrder ENDIANESS = ByteOrder.BIG_ENDIAN;
	
	public final Vector3d initPos;
	public final List<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
	
	private final List<ParkourFrame> frames = new ArrayList<ParkourFrame>();
	
	public int startingFrame = 0;
	public Vector3d lastPos = new Vector3d(0, 0, 0);
	
	protected String originalName = null;
	protected File recordFile = null;
	protected File originalFile = null;
	
	private String name;
	
	public Recording(Vector3d startingPos) {
		initPos = startingPos;
		this.name = null;
	}
	public Recording() {
		initPos = new Vector3d(0, 0, 0);
		name = null;
	}
	
	public Recording(Recording copy)
	{
		frames.addAll(copy.frames);
		initPos = copy.initPos;
		lastPos = copy.lastPos;
		name = copy.name;
		originalName = copy.originalName;
		recordFile = copy.recordFile;
		originalFile = copy.originalFile;
		startingFrame = copy.startingFrame;
	}
	
	@Override
	public String toString()
	{
		//Get Vectors that rounds to the nearest tenth
		double initPosX = Math.round(initPos.x * 10.0) / 10.0;
		double initPosY = Math.round(initPos.y * 10.0) / 10.0;
		double initPosZ = Math.round(initPos.z * 10.0) / 10.0;
		Vector3d initPos = new Vector3d(initPosX, initPosY, initPosZ);
		double lastPosX = Math.round(lastPos.x * 10.0) / 10.0;
		double lastPosY = Math.round(lastPos.y * 10.0) / 10.0;
		double lastPosZ = Math.round(lastPos.z * 10.0) / 10.0;
		Vector3d lastPos = new Vector3d(lastPosX, lastPosY, lastPosZ);
		
		//Create formmated string
		String s = name + "\n\n";
		String tab = ":\n   ";
		if(recordFile != null) s += I18n.format("recording.file") + tab + (recordFile != null ? recordFile.getName() : "[" + I18n.format("recording.unsaved") + "]") + "\n\n";
		s += I18n.format("recording.recording_length") + tab + frames.size() / 20.0f + "s\n\n";
		s += I18n.format("recording.starting_position") + tab + initPos + "\n\n";
		s += I18n.format("recording.ending_position") + tab + lastPos + "\n\n";
		return s;
	}
	
	public File getFile()
	{
		return recordFile;
	}
	
	public String getOriginalName()
	{
		return originalName;
	}
	
	public File getOriginalFile()
	{
		return originalFile;
	}
	
	/**Convert the recording into streamable data.**/
	public byte[] serialize()
	{
		//Header data; name, format id
		byte[] nameBytes = name != null ? name.getBytes(StandardCharsets.UTF_8) : new byte[0];
		byte[] headerData = name != null ? new byte[nameBytes.length + 1 + Integer.BYTES] : new byte[1 + Integer.BYTES];
		ByteBuffer header = ByteBuffer.wrap(headerData);
		
		//Record data; starting position, ending position, frames, checkpoints, etc.
		int allCheckpointsSize = 0; //Total serialized size in bytes of all checkpoints
		for(Checkpoint c : checkpoints) allCheckpointsSize += c.getSerializedSize();
		byte[] recordingData = new byte[BYTES + ParkourFrame.BYTES * frames.size() + allCheckpointsSize];
		ByteBuffer recording = ByteBuffer.wrap(recordingData);
		
		//Fill header data
		if(name != null) header.put(nameBytes);
		header.put((byte) 0);
		header.putInt(SavingFormat.LATEST.ID);
		
		//Fill recording data
		recording.putInt(frames.size());
		recording.putInt(checkpoints.size());
		recording.putDouble(initPos.x); recording.putDouble(initPos.y); recording.putDouble(initPos.z);
		recording.putDouble(lastPos.x); recording.putDouble(lastPos.y); recording.putDouble(lastPos.z);
		for(ParkourFrame frame : frames) recording.put(frame.serialize());
		for(Checkpoint checkpoint : checkpoints) recording.put(checkpoint.serialize());
		
		
		//Compress recording data; Speed is prioritized over best compression
		byte[] compressedData = new byte[recordingData.length];
		Deflater compressor = new Deflater(Deflater.BEST_SPEED);
		compressor.setInput(recordingData);
		compressor.finish();
		int compressedDataSize = compressor.deflate(compressedData);
		compressor.end();
		
		//Join header, data size, and compressed data into one array
		byte[] serializedRecordingData = new byte[headerData.length + Integer.BYTES + compressedDataSize];
		ByteBuffer serializedRecording = ByteBuffer.wrap(serializedRecordingData);
		serializedRecording.put(headerData);
		serializedRecording.putInt(recordingData.length); //Will aid in decompression when loading file
		serializedRecording.put(compressedData, 0, compressedDataSize);
		
		return serializedRecordingData;
	}
	
	public void rename(String newName) { name = newName; }
	
	/**
	 * Saves the recording.
	 * @param threaded Save the recording on a different thread.
	 * @param mustSave Determines if it's mandatory to save.
	 * If set to {@code false}, recording will not save if it is not originally loaded from a file.
	 * @param override Override the original recording's file.
	 * If set to {@code true}, recording will save to the orignal file, if any.
	 * Else, it saves to a new file.
	 **/
	public void save(boolean threaded, boolean mustSave, boolean override)
	{
		if(!mustSave && recordFile == null) return;
		
		//Threaded saving; used for saving while adding or removing checkpoints
		if(threaded) new SavingThread(this, override).start();
		else
		{
			Minecraft mc = Minecraft.getInstance();
			String worldName = getWorldPath(mc);
			String recordFileName = getFormattedTime();
			
			//Make sure that the file's name is the same when being overriden
			String prevName = this.name;
			this.name = this.name == null ? recordFileName : this.originalFile != null && override ? originalName : this.name;
			
			//Determine the file to save to
			File worldDir = new File(SAVING_DIRECTORY.getPath() + '/' + worldName);
			File file = override && originalFile != null ? originalFile : new File(worldDir.getPath() + '/' + recordFileName + FILE_EXTENSION);
			
			try 
			{
				worldDir.mkdirs();
				file.createNewFile();
				
				FileOutputStream stream = new FileOutputStream(file);
				
				stream.write(serialize());
				
				stream.close();
			} 
			catch (IOException e) { e.printStackTrace(); }
			
			if(override) this.name = prevName;
		}
	}
	
	public static boolean deleteSave(Recording record)
	{	
		if(record.recordFile != null)
			return record.recordFile.delete();
		else 
			return false;
	}
	
	public static Recording[] loadSaves()
	{
		Minecraft mc = Minecraft.getInstance();
		File worldDir = new File(SAVING_DIRECTORY.getPath() + '/' + getWorldPath(mc));
		Recording[] records = new Recording[0];
		
		if(worldDir.exists() && worldDir.isDirectory())
		{
			File[] fileList = worldDir.listFiles(FILE_FILTER);
			records = new Recording[fileList.length];
			try {
				
				for(int i = 0; i < records.length; i++)
				{
					FileInputStream input = new FileInputStream(fileList[i]);
					byte[] fileData = new byte[(int) fileList[i].length()];
					ByteBuffer buffer = ByteBuffer.wrap(fileData);
					input.read(fileData);
					Recording record =  deSerialize(buffer);
					record.recordFile = fileList[i];
					record.originalFile = record.recordFile;
					records[i] = record;
					
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return records;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**Create recording from raw data. May throw an exception if file is corrupted.**/
	public static Recording deSerialize(ByteBuffer buffer) throws BufferOverflowException, BufferUnderflowException
	{
		//Make sure the buffer loads data in the correct order
		buffer.order(ENDIANESS);
		
		//Get header from file
		Deserializer.FileHeader header = new Deserializer.FileHeader(buffer);
		SavingFormat format = SavingFormat.getFormatFromID(header.FORMAT_ID);
		buffer.rewind();
		
		//If file is in an unknown format
		if(format == null) return null;
		
		return Deserializer.getDeserializer(format).deSerialize(buffer);
	}
	
	public static String getFormattedTime()
	{
		return new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
	}
	
	public static String getCurrentWorldName(Minecraft mc)
	{
		if(mc.getIntegratedServer() != null) 
		{
			return mc.getIntegratedServer().func_240793_aU_().getWorldName();
		}
		else return mc.getCurrentServerData().serverName + ": " + mc.getCurrentServerData().serverIP;
	}
	
	private static String getWorldPath(Minecraft mc)
	{
		if(mc.getIntegratedServer() != null) 
		{
			return "local/" + mc.getIntegratedServer().func_240793_aU_().getWorldName();
		}
		else return "servers/" + mc.getCurrentServerData().serverIP.replace('.', '-').replace(':', '_');
	}
	
	public ParkourFrame get(int index)
	{
		return frames.get(index);
	}
	
	@Override
	public boolean add(ParkourFrame e) {
		return frames.add(e);
	}
	@Override
	public boolean addAll(Collection<? extends ParkourFrame> c) {
		return frames.addAll(c);
	}
	@Override
	public boolean contains(Object o) {
		return frames.contains(o);
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		return frames.containsAll(c);
	}
	@Override
	public boolean isEmpty() {
		return frames.isEmpty();
	}
	@Override
	public Iterator<ParkourFrame> iterator() {
		return frames.iterator();
	}
	
	public ParkourFrame remove(int index) {
		return frames.remove(index);
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		return frames.removeAll(c);
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return frames.retainAll(c);
	}
	@Override
	public int size() {
		return frames.size();
	}
	@Override
	public Object[] toArray() {
		return frames.toArray();
	}
	@Override
	public <T> T[] toArray(T[] a) {
		return frames.toArray(a);
	}
	@Override
	public void clear() {
		frames.clear();
	}
	@Override
	public boolean remove(Object o) {
		return frames.remove(o);
	}
	@Override
	public void add(int index, ParkourFrame element)
	{
		frames.add(index, element);
	}
	@Override
	public boolean addAll(int index, Collection<? extends ParkourFrame> c)
	{
		return frames.addAll(index, c);
	}
	@Override
	public int indexOf(Object o)
	{
		return frames.indexOf(o);
	}
	@Override
	public int lastIndexOf(Object o)
	{
		return frames.lastIndexOf(o);
	}
	@Override
	public ListIterator<ParkourFrame> listIterator()
	{
		return frames.listIterator();
	}
	@Override
	public ListIterator<ParkourFrame> listIterator(int index)
	{
		return frames.listIterator(index);
	}
	@Override
	public ParkourFrame set(int index, ParkourFrame element)
	{
		return frames.set(index, element);
	}
	@Override
	public Recording subList(int fromIndex, int toIndex)
	{
		//Get the new initial position
		List<ParkourFrame> subList = frames.subList(fromIndex, toIndex);
		int lastIndex = subList.size() - 1;
		Vector3d initPos = new Vector3d(subList.get(0).posX, subList.get(0).posY, subList.get(0).posZ);
		
		//Create Sliced Recording
		Recording subedRecording = new Recording(initPos);
		subedRecording.lastPos = new Vector3d(subList.get(lastIndex).posX, subList.get(lastIndex).posY, subList.get(lastIndex).posZ);
		subedRecording.frames.addAll(subList);
		subedRecording.name = name;
		subedRecording.originalName = originalName;
		subedRecording.originalFile = originalFile;
		subedRecording.checkpoints.addAll(checkpoints);
		subedRecording.startingFrame = startingFrame;
		
		//Remove checkpoints outside of the specified range
		subedRecording.checkpoints.removeIf((Checkpoint c) -> { return c.frameNumber < fromIndex || toIndex < c.frameNumber; });
		
		return subedRecording;
	}
	
	/**Internal saving-thread worker.**/
	private static class SavingThread extends Thread
	{
		final Recording recording;
		final boolean override;
		
		public SavingThread(Recording recording, boolean override)
		{
			this.recording = recording;
			this.override = override;
		}
		
		@Override
		public void run()
		{
			Minecraft mc = Minecraft.getInstance();
			String worldName = getWorldPath(mc);
			String recordFileName = getFormattedTime();
			
			//Make sure that the file's name is the same when being overriden
			String prevName = recording.name;
			recording.name = recording.name == null ? recordFileName : recording.originalFile != null && override ? recording.originalName : recording.name;
			
			//Determine file to save to
			File worldDir = new File(SAVING_DIRECTORY.getPath() + '/' + worldName);
			File file = override && recording.originalFile != null ? recording.originalFile : new File(worldDir.getPath() + '/' + recordFileName + FILE_EXTENSION);
			
			try 
			{
				worldDir.mkdirs();
				file.createNewFile();
				
				FileOutputStream stream = new FileOutputStream(file);
				
				stream.write(recording.serialize());
				
				stream.close();
			} 
			catch (IOException e) { e.printStackTrace(); }
			
			if(override) recording.name = prevName;
		}
	}
	
	/**Internal deserializers.**/
	private static abstract class Deserializer
	{
		public abstract Recording deSerialize(ByteBuffer buffer) throws BufferOverflowException, BufferUnderflowException;
		
		public static Deserializer getDeserializer(SavingFormat format)
		{
			switch(format)
			{
			case V1_0_0_0:
				return V1_0_0_0;
			case V1_0_1_0:
				return V1_0_1_0;
			default:
				return null;
			}
		}
		
		/**
		 * Stores the name and format-id of a saved recording.
		 * All saved files, dispite the format (the mod version used to save), starts with the name and format-id, the header.
		 **/
		public static class FileHeader
		{
			public final String NAME;
			public final int FORMAT_ID;
			
			/**Deserializes the header**/
			public FileHeader(ByteBuffer buffer)
			{
				String name = "";
				int formatID = Integer.MIN_VALUE;
				
				try
				{
					//Find the name's size in bytes
					int nameSize = 0;
					for(int i = 0; i < buffer.capacity(); i++)
						if(buffer.get() != 0) nameSize++; else break;
					buffer.rewind();
					
					//Store the name
					byte[] namedata = new byte[nameSize];
					buffer.get(namedata);
					buffer.get();
					name = new String(namedata, StandardCharsets.UTF_8);
					
					//Store the format-id
					formatID = buffer.getInt();
				}
				catch(Exception e) { e.printStackTrace(); }
				
				NAME = name;
				FORMAT_ID = formatID;
			}
			
			/**Deserializes the header. Used to determine if format of file is known.**/
			public FileHeader(File file)
			{
				String name = "";
				int formatID = Integer.MIN_VALUE;
				try
				{
					RandomAccessFile inputFile = new RandomAccessFile(file, "r");
					
					//Find the name's size in bytes
					int nameSize = 0;
					for(int i = inputFile.read(); i != -1; i = inputFile.read())
						if(i != 0) nameSize++; else break;
					inputFile.seek(0);
					
					//Store the name
					byte[] namedata = new byte[nameSize];
					inputFile.read(namedata);
					inputFile.read();
					name = new String(namedata, StandardCharsets.UTF_8);
					
					//Store the format-id
					byte[] idInt = new byte[Integer.BYTES];
					ByteBuffer idData = ByteBuffer.wrap(idInt);
					idData.order(ENDIANESS);
					inputFile.read(idInt);
					formatID = idData.getInt();
					
					inputFile.close();
				}
				catch(IOException e){ e.printStackTrace(); }
				
				NAME = name;
				FORMAT_ID = formatID;
			}
		}
		
		public static Deserializer V1_0_0_0 = new Deserializer()
		{
			@Override
			public Recording deSerialize(ByteBuffer buffer)
			{
				//Deserialize Header
				FileHeader header = new FileHeader(buffer);
				SavingFormat format = SavingFormat.getFormatFromID(header.FORMAT_ID);
				
				//Get Vectors
				Vector3d initPos = new Vector3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
				buffer.getDouble(); buffer.getDouble(); buffer.getDouble(); //Skip 24 bytes, v1.0.0.0 originally had a initVelocity vector
				Vector3d lastPos = new Vector3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
				buffer.getDouble(); buffer.getDouble(); buffer.getDouble(); //Skip 24 bytes, v1.0.0.0 originally had a lastVelocity vector
				
				//Create new recording from file
				Recording newRecording = new Recording(initPos);
				newRecording.name = header.NAME.length() == 0 ? null : header.NAME;
				newRecording.originalName = header.NAME;
				newRecording.lastPos = lastPos;
				
				//In mod version v1.0.0.0, FORMAT_ID is used to store the size of the recording
				for(int i = 0; i < header.FORMAT_ID; i++)
					newRecording.add(ParkourFrame.deSerialize(buffer, format));
				
				return newRecording;
			}
		};
		
		public static Deserializer V1_0_1_0 = new Deserializer()
		{
			@Override
			public Recording deSerialize(ByteBuffer buffer)
			{
				//Deserialize Header
				FileHeader header = new FileHeader(buffer);
				SavingFormat format = SavingFormat.getFormatFromID(header.FORMAT_ID);
				
				//Uncompress record data
				byte[] recordingData = new byte[buffer.getInt()]; //Size is read from file
				byte[] compressedRecordingData = new byte[buffer.remaining()];
				buffer.get(compressedRecordingData);
				Inflater decompressor = new Inflater();
				decompressor.setInput(compressedRecordingData);
				try { decompressor.inflate(recordingData); } 
				catch (DataFormatException e) { e.printStackTrace(); }
				
				//Get frame count, checkpoint count, and vectors
				ByteBuffer recording = ByteBuffer.wrap(recordingData);
				final int NUM_FRAMES = recording.getInt();
				final int NUM_CHECKPOINTS = recording.getInt();
				Vector3d initPos = new Vector3d(recording.getDouble(), recording.getDouble(), recording.getDouble());
				Vector3d lastPos = new Vector3d(recording.getDouble(), recording.getDouble(), recording.getDouble());
				
				//Create new recording from file
				Recording newRecording = new Recording(initPos);
				newRecording.name = header.NAME.length() == 0 ? null : header.NAME;
				newRecording.originalName = header.NAME;
				newRecording.lastPos = lastPos;
				
				//Get frames
				for(int i = 0; i < NUM_FRAMES; i++) newRecording.add(ParkourFrame.deSerialize(recording, format));
				
				//Get checkpoints
				for(int i = 0; i < NUM_CHECKPOINTS; i++) newRecording.checkpoints.add(Checkpoint.deSerialize(recording, format));
				
				return newRecording;
			}
		};

	}
}
