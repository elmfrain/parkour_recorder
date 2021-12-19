package com.elmfer.parkour_recorder.parkour;

import java.nio.ByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**Stores movement inputs and player's states for one tick.**/
public class ParkourFrame {
	
	/**Number of bytes needed to represent a frame. Used for serializing.**/
	public static final int BYTES = 42;
	
	public final float headYaw;
	public final float headPitch;
	public final float armYawOffset;
	public final float armPitchOffset;
	public final double posX;
	public final double posY;
	public final double posZ;
	private final short flags;
	
	/**Create a frame from keybinds and the player's state.**/
	public ParkourFrame(Options gameSettingsIn, LocalPlayer playerIn)
	{
		//Get Rotational Data
		headYaw = playerIn.yHeadRot;
		headPitch = playerIn.getXRot();
		armYawOffset = playerIn.yHeadRot - playerIn.yBob;
		armPitchOffset = playerIn.getXRot() - playerIn.xBob;
		
		//Get Positional Data
		Vec3 playerPos = playerIn.getPosition(0.0f);
		posX = playerPos.x;
		posY = playerPos.y;
		posZ = playerPos.z;
		
		//Get Input Data
		short flags = 0;
		Input playerInput = playerIn.input;
		if(playerInput.jumping) flags |= Flags.JUMPING.value;
		if(playerInput.shiftKeyDown) flags |= Flags.SNEAKING.value;
		if(gameSettingsIn.keyUp.isDown()) flags |= Flags.FORWARD.value;
		if(gameSettingsIn.keyLeft.isDown()) flags |= Flags.LEFT_STRAFE.value;
		if(gameSettingsIn.keyRight.isDown()) flags |= Flags.RIGHT_STRAFE.value;
		if(gameSettingsIn.keyDown.isDown()) flags |= Flags.BACKWARD.value;
		if(playerIn.isSprinting()) flags |= Flags.SPRINTING.value;
		if(gameSettingsIn.keyAttack.isDown()) flags |= Flags.HITTING.value;
		if(gameSettingsIn.keyUse.isDown()) flags |= Flags.USING.value;
		if(playerIn.isOnGround()) flags |= Flags.ON_GROUND.value;
		if(playerIn.getAbilities().flying) flags |= Flags.FLYING.value;
		this.flags = flags;
	}
	
	/**Manually set the states. Internal use only**/
	private ParkourFrame(short flags, float headYaw, float headPitch, double posX, double posY, double posZ, float handYaw, float handPitch)
	{
		this.flags = flags;
		this.headYaw = headYaw;
		this.headPitch = headPitch;
		this.armYawOffset = handYaw;
		this.armPitchOffset = handPitch;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}
	
	/**Create frame from raw data.**/
	public static ParkourFrame deSerialize(ByteBuffer buffer, SavingFormat format)
	{
		return Deserializer.getDeserializer(format).deSerialize(buffer);
	}
	
	/**Set the entity's movement inputs from the frame**/
	public void setMovementInput(Input input, Player entityIn)
	{
		Minecraft mc = Minecraft.getInstance();
		
		input.up = getFlag(Flags.FORWARD);
		input.down = getFlag(Flags.BACKWARD);
		input.left = getFlag(Flags.LEFT_STRAFE);
		input.right = getFlag(Flags.RIGHT_STRAFE);
		input.jumping = getFlag(Flags.JUMPING);
		input.shiftKeyDown = getFlag(Flags.SNEAKING);
		entityIn.setShiftKeyDown(getFlag(Flags.SNEAKING));
		entityIn.setSprinting(getFlag(Flags.SPRINTING));
		mc.player.getAbilities().flying = getFlag(Flags.FLYING);
	}
	
	@Override
	public String toString()
	{
		String s = "";
		s += Boolean.toString(getFlag(Flags.JUMPING)) + " ";
		s += Boolean.toString(getFlag(Flags.BACKWARD)) + " ";
		s += Boolean.toString(getFlag(Flags.LEFT_STRAFE)) + " ";
		s += Boolean.toString(getFlag(Flags.RIGHT_STRAFE)) + " ";
		s += Boolean.toString(getFlag(Flags.FORWARD)) + " ";
		s += Boolean.toString(getFlag(Flags.SNEAKING)) + " ";
		s += Boolean.toString(getFlag(Flags.SPRINTING)) + " ";
		s += Boolean.toString(getFlag(Flags.HITTING)) + " ";
		s += Boolean.toString(getFlag(Flags.USING)) + " ";
		s += Float.toString(headYaw) + " ";
		s += Float.toString(headPitch) + '\n';
		return s;
	}
	
	public boolean getFlag(Flags flag)
	{
		return (flags & flag.value) != 0;
	}
	
	/**Convert frame to serialized data.**/
	public byte[] serialize()
	{
		byte[] data = new byte[BYTES];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.putShort(flags);
		buffer.putFloat(headYaw);
		buffer.putFloat(headPitch);
		buffer.putDouble(posX);
		buffer.putDouble(posY);
		buffer.putDouble(posZ);
		buffer.putFloat(armYawOffset);
		buffer.putFloat(armPitchOffset);
		
		return data;
	}
	
	/**An enum that stores which bit belongs to each flag.**/
	public static enum Flags
	{
		JUMPING((short) 0x001),
		SNEAKING((short) 0x002),
		FORWARD((short) 0x004),
		LEFT_STRAFE((short) 0x008),
		RIGHT_STRAFE((short) 0x010),
		BACKWARD((short) 0x020),
		SPRINTING((short) 0x040),
		HITTING((short) 0x080),
		USING((short) 0x100),
		ON_GROUND((short) 0x200),
		FLYING((short) 0x400);
		
		public final short value;
		
		Flags(short flag)
		{
			value = flag;
		}
	}
	
	/**Internal deserializers.**/
	private static abstract class Deserializer
	{
		public abstract ParkourFrame deSerialize(ByteBuffer buffer);
		
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
		
		public static Deserializer V1_0_0_0 = new Deserializer()
		{
			@Override
			public ParkourFrame deSerialize(ByteBuffer buffer)
			{
				short flags = buffer.getShort();
				float headYaw = buffer.getFloat();
				float headPitch = buffer.getFloat();
				double posX = buffer.getDouble();
				double posY = buffer.getDouble();
				double posZ = buffer.getDouble();
				return new ParkourFrame(flags, headYaw, headPitch, posX, posY, posZ, 0.0f, 0.0f);
			}
		};
		public static Deserializer V1_0_1_0 = new Deserializer()
		{	
			@Override
			public ParkourFrame deSerialize(ByteBuffer buffer)
			{
				short flags = buffer.getShort();
				float headYaw = buffer.getFloat();
				float headPitch = buffer.getFloat();
				double posX = buffer.getDouble();
				double posY = buffer.getDouble();
				double posZ = buffer.getDouble();
				float handYawOffset = buffer.getFloat();
				float handPitchOffset = buffer.getFloat();
				return new ParkourFrame(flags, headYaw, headPitch, posX, posY, posZ, handYawOffset, handPitchOffset);
			}
		};
	}
}
