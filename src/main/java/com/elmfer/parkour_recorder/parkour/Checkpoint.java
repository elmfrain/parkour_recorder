package com.elmfer.parkour_recorder.parkour;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Checkpoint {
	
	public final String name;
	public final int frameNumber;
	public int color = 0;
	
	public Checkpoint(String name, int frameNumber)
	{
		this.name = name;
		this.frameNumber = frameNumber;
	}

	public Checkpoint(Checkpoint copy)
	{
		name = copy.name;
		frameNumber = copy.frameNumber;
	}
	
	/**Returns the number of bytes to represent this checkpoint**/
	public int getSerializedSize()
	{
		//It stores (as ints) frame number, color, and (as string) the name along with terminating character
		return Integer.BYTES * 2 + name.getBytes(StandardCharsets.UTF_8).length + 1;
	}
	
	/**Create checkpoint from raw data.**/
	public static Checkpoint deSerialize(ByteBuffer buffer, SavingFormat format)
	{
		return Deserializer.getDeserializer(format).deSerialize(buffer);
	}
	
	/**Convert checkpoint to serialized data.**/
	public byte[] serialize()
	{
		byte[] data = new byte[getSerializedSize()];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.putInt(frameNumber);
		buffer.putInt(color);
		buffer.put(name.getBytes(StandardCharsets.UTF_8));
		buffer.put((byte) 0);
		return data;
	}
	
	/**Internal deserializers.**/
	private static abstract class Deserializer
	{
		public abstract Checkpoint deSerialize(ByteBuffer buffer);
		
		public static Deserializer getDeserializer(SavingFormat format)
		{
			switch(format)
			{
			case V1_0_1_0:
				return V1_0_1_0;
			default:
				return null;
			}
		}
		
		public static Deserializer V1_0_1_0 = new Deserializer()
		{	
			@Override
			public Checkpoint deSerialize(ByteBuffer buffer)
			{
				//Get ints
				final int FRAME_NUM = buffer.getInt();
				final int COLOR = buffer.getInt();
				
				//Get name length
				int nameSize = 0;
				final int PREV_BUFFER_POS = buffer.position();
				for(int i = 0; i < buffer.capacity(); i++) if(buffer.get() != 0) nameSize++; else break;
				buffer.position(PREV_BUFFER_POS);
				
				//Store name
				byte[] nameData = new byte[nameSize];
				buffer.get(nameData);
				buffer.get(); //Skips the string terminating character
				String name = new String(nameData, StandardCharsets.UTF_8);
				
				//Create checkpoint
				Checkpoint newCheckpoint = new Checkpoint(name, FRAME_NUM);
				newCheckpoint.color = COLOR;
				
				return newCheckpoint;
			}
		};
	}
}
