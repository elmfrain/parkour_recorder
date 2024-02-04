package com.elmfer.prmod.parkour;

import java.nio.ByteBuffer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/** Stores movement inputs and player's states for one tick. **/
public class Frame {

	/** Number of bytes needed to represent a frame. Used for serializing. **/
	public static final int BYTES = 42;

	public final float headYaw;
	public final float headPitch;
	public final float armYawOffset;
	public final float armPitchOffset;
	public final double posX;
	public final double posY;
	public final double posZ;
	private final short flags;

	/** Create a frame from keybinds and the player's state. **/
	public Frame(GameOptions gameSettingsIn, ClientPlayerEntity playerIn) {
		// Get Rotational Data
		headYaw = playerIn.headYaw;
		headPitch = playerIn.getPitch();
		armYawOffset = playerIn.headYaw - playerIn.renderYaw;
		armPitchOffset = playerIn.getPitch() - playerIn.renderPitch;

		// Get Positional Data
		Vec3d playerPos = playerIn.getPos();
		posX = playerPos.x;
		posY = playerPos.y;
		posZ = playerPos.z;

		// Get Input Data
		short flags = 0;
		Input playerInput = playerIn.input;
		if (playerInput.jumping) flags |= Flags.JUMPING.value;
		if (playerInput.sneaking) flags |= Flags.SNEAKING.value;
		if (gameSettingsIn.forwardKey.isPressed()) flags |= Flags.FORWARD.value;
		if (gameSettingsIn.leftKey.isPressed()) flags |= Flags.LEFT_STRAFE.value;
		if (gameSettingsIn.rightKey.isPressed()) flags |= Flags.RIGHT_STRAFE.value;
		if (gameSettingsIn.backKey.isPressed()) flags |= Flags.BACKWARD.value;
		if (playerIn.isSprinting()) flags |= Flags.SPRINTING.value;
		if (gameSettingsIn.attackKey.isPressed()) flags |= Flags.HITTING.value;
		if (gameSettingsIn.useKey.isPressed()) flags |= Flags.USING.value;
		if (playerIn.isOnGround()) flags |= Flags.ON_GROUND.value;
		if (playerIn.getAbilities().flying) flags |= Flags.FLYING.value;
		this.flags = flags;
	}

	/** Manually set the states. Internal use only **/
	private Frame(short flags, float headYaw, float headPitch, double posX, double posY, double posZ,
			float handYaw, float handPitch) {
		this.flags = flags;
		this.headYaw = headYaw;
		this.headPitch = headPitch;
		this.armYawOffset = handYaw;
		this.armPitchOffset = handPitch;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	/** Create frame from raw data. **/
	public static Frame deSerialize(ByteBuffer buffer, SaveFormat format) {
		return Deserializer.getDeserializer(format).deSerialize(buffer);
	}

	/** Set the entity's movement inputs from the frame **/
	public void setMovementInput(Input input, PlayerEntity entityIn) {
		MinecraftClient mc = MinecraftClient.getInstance();

		input.pressingForward = getFlag(Flags.FORWARD);
		input.pressingBack = getFlag(Flags.BACKWARD);
		input.pressingLeft = getFlag(Flags.LEFT_STRAFE);
		input.pressingRight = getFlag(Flags.RIGHT_STRAFE);
		input.jumping = getFlag(Flags.JUMPING);
		input.sneaking = getFlag(Flags.SNEAKING);
		entityIn.setSneaking(getFlag(Flags.SNEAKING));
		entityIn.setSprinting(getFlag(Flags.SPRINTING));
		mc.player.getAbilities().flying = getFlag(Flags.FLYING);
	}

	public boolean getFlag(Flags flag) {
		return (flags & flag.value) != 0;
	}

	/** Convert frame to serialized data. **/
	public byte[] serialize() {
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

	/** An enum that stores which bit belongs to each flag. **/
	public static enum Flags {
		JUMPING((short) 0x001), SNEAKING((short) 0x002), FORWARD((short) 0x004), LEFT_STRAFE((short) 0x008),
		RIGHT_STRAFE((short) 0x010), BACKWARD((short) 0x020), SPRINTING((short) 0x040), HITTING((short) 0x080),
		USING((short) 0x100), ON_GROUND((short) 0x200), FLYING((short) 0x400), ATTACK((short) 0x800),
		USE((short) 0x1000);

		public final short value;

		Flags(short flag) {
			value = flag;
		}
	}

	/** Internal deserializers. **/
	private static abstract class Deserializer {
		public abstract Frame deSerialize(ByteBuffer buffer);

		public static Deserializer getDeserializer(SaveFormat format) {
			switch (format) {
			case V1_0_0_0:
				return V1_0_0_0;
			case V1_0_1_0:
				return V1_0_1_0;
			default:
				return null;
			}
		}

		public static Deserializer V1_0_0_0 = new Deserializer() {
			@Override
			public Frame deSerialize(ByteBuffer buffer) {
				short flags = buffer.getShort();
				float headYaw = buffer.getFloat();
				float headPitch = buffer.getFloat();
				double posX = buffer.getDouble();
				double posY = buffer.getDouble();
				double posZ = buffer.getDouble();
				return new Frame(flags, headYaw, headPitch, posX, posY, posZ, 0.0f, 0.0f);
			}
		};
		public static Deserializer V1_0_1_0 = new Deserializer() {
			@Override
			public Frame deSerialize(ByteBuffer buffer) {
				short flags = buffer.getShort();
				float headYaw = buffer.getFloat();
				float headPitch = buffer.getFloat();
				double posX = buffer.getDouble();
				double posY = buffer.getDouble();
				double posZ = buffer.getDouble();
				float handYawOffset = buffer.getFloat();
				float handPitchOffset = buffer.getFloat();
				return new Frame(flags, headYaw, headPitch, posX, posY, posZ, handYawOffset, handPitchOffset);
			}
		};
	}
}