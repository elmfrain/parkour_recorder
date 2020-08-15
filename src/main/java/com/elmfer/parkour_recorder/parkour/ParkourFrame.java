package com.elmfer.parkour_recorder.parkour;

import java.nio.ByteBuffer;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.vector.Vector3d;

public class ParkourFrame {
	
	public static final int BYTES = 34;
	
	public final float headYaw;
	public final float headPitch;
	public final double posX;
	public final double posY;
	public final double posZ;
	private final short flags;
	
	public ParkourFrame(net.minecraft.client.GameSettings gameSettingsIn, ClientPlayerEntity playerIn)
	{
		headYaw = playerIn.getRotationYawHead();
		headPitch = playerIn.rotationPitch;
		Vector3d playerPos = playerIn.getPositionVec();
		posX = playerPos.x;
		posY = playerPos.y;
		posZ = playerPos.z;
		short flags = 0;
		MovementInput playerInput = playerIn.movementInput;
		if(playerInput.jump) flags |= 0x001;
		if(playerInput.sneaking) flags |= 0x002;
		if(gameSettingsIn.keyBindForward.isKeyDown()) flags |= 0x004;
		if(gameSettingsIn.keyBindLeft.isKeyDown()) flags |= 0x008;
		if(gameSettingsIn.keyBindRight.isKeyDown()) flags |= 0x010;
		if(gameSettingsIn.keyBindBack.isKeyDown()) flags |= 0x020;
		if(playerIn.isSprinting()) flags |= 0x040;
		if(gameSettingsIn.keyBindAttack.isKeyDown()) flags |= 0x080;
		if(gameSettingsIn.keyBindUseItem.isKeyDown()) flags |= 0x100;
		if(!playerIn.isAirBorne) flags |= 0x200;
		this.flags = flags;
	}
	
	private ParkourFrame(short flags, float headYaw, float headPitch, double posX, double posY, double posZ)
	{
		this.flags = flags;
		this.headYaw = headYaw;
		this.headPitch = headPitch;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}
	
	public static ParkourFrame deSerialize(ByteBuffer buffer)
	{
		short flags = buffer.getShort();
		float headYaw = buffer.getFloat();
		float headPitch = buffer.getFloat();
		double posX = buffer.getDouble();
		double posY = buffer.getDouble();
		double posZ = buffer.getDouble();
		return new ParkourFrame(flags, headYaw, headPitch, posX, posY, posZ);
	}
	
	public void setInput(MovementInput input, PlayerEntity playerIn)
	{
		input.forwardKeyDown = getFlag(Flags.FORWARD);
		input.backKeyDown = getFlag(Flags.BACKWARD);
		input.leftKeyDown = getFlag(Flags.LEFT_STRAFE);
		input.rightKeyDown = getFlag(Flags.RIGHT_STRAFE);
		input.jump = getFlag(Flags.JUMPING);
		input.sneaking = getFlag(Flags.SNEAKING);
		playerIn.setSneaking(getFlag(Flags.SNEAKING));
		playerIn.setSprinting(getFlag(Flags.SPRINTING));
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
		
		return data;
	}
	
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
		ON_GROUND((short) 0x200);
		
		public final short value;
		
		Flags(short flag)
		{
			value = flag;
		}
	}
}
