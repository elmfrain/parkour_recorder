package com.elmfer.prmod.parkour;

import java.nio.ByteBuffer;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BlockHitCapture {
    
    public final BlockHitResult blockHitResult;
    
    private Direction direction;
    private boolean missed;
    private boolean isInsideBlock;
    
    public BlockHitCapture(BlockHitResult blockHitResult) {
        this.blockHitResult = blockHitResult;
    }
    
    public BlockHitCapture(BlockHitCapture copy) {
        blockHitResult = copy.blockHitResult;
    }
    
    private BlockHitCapture(Vec3d pos, BlockPos blockPos, byte flags) {
        if ((flags & 0xC0) == 0) {
            blockHitResult = null;
            return;
        }
        
        setFlagsFromByte(flags);
            
        if (missed)
            blockHitResult = BlockHitResult.createMissed(pos, direction, blockPos);
        else
            blockHitResult = new BlockHitResult(pos, direction, blockPos, isInsideBlock);
    }
    
    public int getSerializedSize() {
        // Return 1 byte if blockHitResult is null, otherwise return the size of the serialized BlockHitResult
        if (blockHitResult == null)
            return 1;
        
        // One Vec3d
        // One BlockPos
        // Flags containing boolean values and direction
        return Double.BYTES * 3 + Integer.BYTES * 3 + 1;
    }
    
    public static BlockHitCapture deSerialize(ByteBuffer buffer, SaveFormat format) {
        return Deserializer.getDeserializer(format).deSerialize(buffer);
    }
    
    public byte[] serialize() {
        byte[] data = new byte[getSerializedSize()];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        if (blockHitResult == null) {
            buffer.put((byte) 0);
            return data;
        }
            
        byte flags = getFlagsByte();
        buffer.put(flags);
        Vec3d pos = blockHitResult.getPos();
        buffer.putDouble(pos.x);
        buffer.putDouble(pos.y);
        buffer.putDouble(pos.z);
        BlockPos blockPos = blockHitResult.getBlockPos();
        buffer.putInt(blockPos.getX());
        buffer.putInt(blockPos.getY());
        buffer.putInt(blockPos.getZ());

        return data;
    }
    
    private byte getFlagsByte() {
        if (blockHitResult == null)
            return 0;
        
        byte flags = 0;
        
        flags |= blockHitResult.getSide().getId() & 0x0F; // Direction, 4 bits
        flags |= blockHitResult.getType() == BlockHitResult.Type.BLOCK ? 0 : 0x10; // Block or miss, 1 bit
        flags |= blockHitResult.isInsideBlock() ? 0x20 : 0; // Inside block, 1 bit
        flags |= 0xC0; // remaining 2 bits reserved, non-null indicator
        
        return flags;
    }
    
    private void setFlagsFromByte(byte flags) {
        direction = Direction.byId(flags & 0x0F);
        missed = (flags & 0x10) != 0;
        isInsideBlock = (flags & 0x20) != 0;
    }
    
    /** Internal deserializers. **/
    private static abstract class Deserializer {
        public abstract BlockHitCapture deSerialize(ByteBuffer buffer);

        public static Deserializer getDeserializer(SaveFormat format) {
            switch (format) {
            case V1_1_3_0:
                return V1_1_3_0;
            default:
                return null;
            }
        }

        public static Deserializer V1_1_3_0 = new Deserializer() {
            @Override
            public BlockHitCapture deSerialize(ByteBuffer buffer) {
                byte flags = buffer.get();
                if (flags == 0)
                    return new BlockHitCapture((BlockHitResult) null);
                
                double x = buffer.getDouble();
                double y = buffer.getDouble();
                double z = buffer.getDouble();
                Vec3d pos = new Vec3d(x, y, z);
                int blockX = buffer.getInt();
                int blockY = buffer.getInt();
                int blockZ = buffer.getInt();
                BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
                
                return new BlockHitCapture(pos, blockPos, flags);
            }
        };
    }
}
