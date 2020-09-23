package com.elmfer.parkour_recorder.parkour;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.Vec3d;

public class Recording implements List<ParkourFrame>
{
	public static final int BYTES = 100;
	public static final File SAVING_DIRECTORY = new File("parkour-saves");
	public static final String FILE_EXTENSION = ".pkr";
	public static final FileFilter FILE_FILTER = new FileFilter() 
	{	
		@Override
		public boolean accept(File pathname)
		{	
			return pathname.getAbsolutePath().endsWith(FILE_EXTENSION);
		}
	};
	
	public final Vec3d initPos;
	public final Vec3d initVel;
	public final List<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
	
	private final List<ParkourFrame> frames = new ArrayList<ParkourFrame>();
	
	public int startingFrame = 0;
	public Vec3d lastPos = new Vec3d(0, 0, 0);
	public Vec3d lastVel = new Vec3d(0, 0, 0);
	
	protected String originalName = null;
	protected File recordFile = null;
	
	private String name;
	
	public Recording(Vec3d startingPos, Vec3d staringVel) {
		initPos = startingPos;
		initVel = staringVel;
		this.name = null;
	}
	public Recording() {
		initPos = new Vec3d(0, 0, 0);
		initVel = new Vec3d(0, 0, 0);
		name = null;
	}
	
	public Recording(Recording copy)
	{
		frames.addAll(copy.frames);
		initPos = copy.initPos;
		initVel = copy.initVel;
		lastPos = copy.lastPos;
		lastVel = copy.lastVel;
		name = copy.name;
		originalName = copy.originalName;
		recordFile = copy.recordFile;
		startingFrame = copy.startingFrame;
	}
	
	@Override
	public String toString()
	{
		Vec3d initPos = new Vec3d((int) this.initPos.x, (int) this.initPos.y, (int) this.initPos.z);
		Vec3d lastPos = new Vec3d((int) this.lastPos.x, (int) this.lastPos.y, (int) this.lastPos.z);
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
	
	public byte[] serialize()
	{
		byte[] namedata = name == null ? new byte[0] : name.getBytes();
		byte[] data = new byte[BYTES + namedata.length + 1];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		for(byte c : namedata)
			buffer.put(c);
		buffer.put((byte) 0);
		
		buffer.putInt(frames.size());
		buffer.putDouble(initPos.x); buffer.putDouble(initPos.y); buffer.putDouble(initPos.z);
		buffer.putDouble(initVel.x); buffer.putDouble(initVel.y); buffer.putDouble(initVel.z);
		buffer.putDouble(lastPos.x); buffer.putDouble(lastPos.y); buffer.putDouble(lastPos.z);
		buffer.putDouble(lastVel.x); buffer.putDouble(lastVel.y); buffer.putDouble(lastVel.z);
		
		return data;
	}
	
	public void rename(String newName) { name = newName; }
	
	public void save()
	{
		Minecraft mc = Minecraft.getMinecraft();
		String worldName = getWorldPath(mc);
		String recordFileName = getFormattedTime();
		this.name = this.name == null ? recordFileName : this.name;
		
		File worldDir = new File(SAVING_DIRECTORY.getPath() + '/' + worldName);
		File file = recordFile != null ? recordFile : new File(worldDir.getPath() + '/' + recordFileName + FILE_EXTENSION);
		
		try 
		{
			worldDir.mkdirs();
			file.createNewFile();
			
			FileOutputStream stream = new FileOutputStream(file);
			
			stream.write(serialize());
			forEach((ParkourFrame frame) -> 
			{
				try 
				{
					stream.write(frame.serialize());
				} 
				catch (IOException e) {}
			});
			
			stream.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
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
		Minecraft mc = Minecraft.getMinecraft();
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
	
	public static Recording deSerialize(ByteBuffer buffer)
	{
		int nameSize = 0;
		for(int i = 0; i < buffer.capacity(); i++)
			if(buffer.get() != 0) nameSize++; else break;
		buffer.rewind();
		
		byte[] namedata = new byte[nameSize];
		buffer.get(namedata);
		buffer.get();
		
		String name = new String(namedata, StandardCharsets.UTF_8);
		int recordingSize = buffer.getInt();
		Vec3d initPos = new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
		Vec3d initVel = new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
		Vec3d lastPos = new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
		Vec3d lastVel = new Vec3d(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
		Recording newRecording = new Recording(initPos, initVel);
		newRecording.name = name.length() == 0 ? null : name;
		newRecording.originalName = name;
		newRecording.lastPos = lastPos;
		newRecording.lastVel = lastVel;
		
		for(int i = 0; i < recordingSize; i++)
			newRecording.add(ParkourFrame.deSerialize(buffer));
		
		return newRecording;
	}
	
	public static String getFormattedTime()
	{
		return new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime());
	}
	
	public static String getCurrentWorldName(Minecraft mc)
	{
		if(mc.getIntegratedServer() != null) 
		{
			return mc.getIntegratedServer().getFolderName();
		}
		else return mc.getCurrentServerData().serverName + ": " + mc.getCurrentServerData().serverIP;
	}
	
	private static String getWorldPath(Minecraft mc)
	{
		if(mc.getIntegratedServer() != null) 
		{
			return "local/" + mc.getIntegratedServer().getFolderName();
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
		List<ParkourFrame> subList = frames.subList(fromIndex, toIndex);
		int lastIndex = subList.size() - 1;
		Vec3d initPos = new Vec3d(subList.get(0).posX, subList.get(0).posY, subList.get(0).posZ);
		Vec3d scndPos = new Vec3d(subList.get(1).posX, subList.get(1).posY, subList.get(1).posZ);
		Vec3d initVel = scndPos.subtract(initPos);
		
		Recording subedRecording = new Recording(initPos, initVel);
		subedRecording.lastPos = new Vec3d(subList.get(lastIndex).posX, subList.get(lastIndex).posY, subList.get(lastIndex).posZ);
		subedRecording.lastVel = subedRecording.lastPos.subtract
								(subList.get(lastIndex - 1).posX, subList.get(lastIndex - 1).posY, subList.get(lastIndex - 1).posZ);
		
		subedRecording.frames.addAll(subList);
		
		subedRecording.name = name;
		subedRecording.recordFile = recordFile;
		subedRecording.originalName = originalName;
		
		return subedRecording;
	}
}
