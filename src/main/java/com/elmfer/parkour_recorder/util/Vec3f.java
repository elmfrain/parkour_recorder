package com.elmfer.parkour_recorder.util;

import java.util.EnumSet;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class Vec3f
{
	public static final Vec3f ZERO = new Vec3f(0.0f, 0.0f, 0.0f);
	
	public final float x;
	public final float y;
	public final float z;
	
	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3f(Vec3f copy)
	{
		x = copy.x;
		y = copy.y;
		z = copy.z;
	}
	
	public Vec3f subtractReverse(Vec3f vec) {
	      return new Vec3f(vec.x - this.x, vec.y - this.y, vec.z - this.z);
	   }

	   /**
	    * Normalizes the vector to a length of 1 (except if it is the zero vector)
	    */
	   public Vec3f normalize() {
	      float d0 = (float)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	      return d0 < 1.0E-4D ? ZERO : new Vec3f(this.x / d0, this.y / d0, this.z / d0);
	   }

	   public float dotProduct(Vec3f vec) {
	      return this.x * vec.x + this.y * vec.y + this.z * vec.z;
	   }

	   /**
	    * Returns a new vector with the result of this vector x the specified vector.
	    */
	   public Vec3f crossProduct(Vec3f vec) {
	      return new Vec3f(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
	   }

	   public Vec3f subtract(Vec3f vec) {
	      return this.subtract(vec.x, vec.y, vec.z);
	   }

	   public Vec3f subtract(float x, float y, float z) {
	      return this.add(-x, -y, -z);
	   }

	   public Vec3f add(Vec3f vec) {
	      return this.add(vec.x, vec.y, vec.z);
	   }

	   /**
	    * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
	    * vector.
	    */
	   public Vec3f add(float x, float y, float z) {
	      return new Vec3f(this.x + x, this.y + y, this.z + z);
	   }

	   /**
	    * Euclidean distance between this and the specified vector, returned as float.
	    */
	   public float distanceTo(Vec3f vec) {
	      float d0 = vec.x - this.x;
	      float d1 = vec.y - this.y;
	      float d2 = vec.z - this.z;
	      return (float)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	   }

	   /**
	    * The square of the Euclidean distance between this and the specified vector.
	    */
	   public float squareDistanceTo(Vec3f vec) {
	      float d0 = vec.x - this.x;
	      float d1 = vec.y - this.y;
	      float d2 = vec.z - this.z;
	      return d0 * d0 + d1 * d1 + d2 * d2;
	   }

	   public float squareDistanceTo(float xIn, float yIn, float zIn) {
	      float d0 = xIn - this.x;
	      float d1 = yIn - this.y;
	      float d2 = zIn - this.z;
	      return d0 * d0 + d1 * d1 + d2 * d2;
	   }

	   public Vec3f scale(float factor) {
	      return this.mul(factor, factor, factor);
	   }

	   public Vec3f inverse() {
	      return this.scale(-1.0f);
	   }

	   public Vec3f mul(Vec3f p_216369_1_) {
	      return this.mul(p_216369_1_.x, p_216369_1_.y, p_216369_1_.z);
	   }

	   public Vec3f mul(float factorX, float factorY, float factorZ) {
	      return new Vec3f(this.x * factorX, this.y * factorY, this.z * factorZ);
	   }

	   /**
	    * Returns the length of the vector.
	    */
	   public float length() {
	      return (float)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	   }

	   public float lengthSquared() {
	      return this.x * this.x + this.y * this.y + this.z * this.z;
	   }

	   public boolean equals(Object p_equals_1_) {
	      if (this == p_equals_1_) {
	         return true;
	      } else if (!(p_equals_1_ instanceof Vec3f)) {
	         return false;
	      } else {
	         Vec3f Vec3f = (Vec3f)p_equals_1_;
	         if (Float.compare(Vec3f.x, this.x) != 0) {
	            return false;
	         } else if (Float.compare(Vec3f.y, this.y) != 0) {
	            return false;
	         } else {
	            return Float.compare(Vec3f.z, this.z) == 0;
	         }
	      }
	   }

	   public String toString() {
	      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	   }

	   public Vec3f rotatePitch(float pitch) {
	      float f = MathHelper.cos(pitch);
	      float f1 = MathHelper.sin(pitch);
	      float d0 = this.x;
	      float d1 = this.y * (float)f + this.z * (float)f1;
	      float d2 = this.z * (float)f - this.y * (float)f1;
	      return new Vec3f(d0, d1, d2);
	   }

	   public Vec3f rotateYaw(float yaw) {
	      float f = MathHelper.cos(yaw);
	      float f1 = MathHelper.sin(yaw);
	      float d0 = this.x * (float)f + this.z * (float)f1;
	      float d1 = this.y;
	      float d2 = this.z * (float)f - this.x * (float)f1;
	      return new Vec3f(d0, d1, d2);
	   }

	   /**
	    * returns a Vec3f from given pitch and yaw degrees as Vec2f
	    */
	   public static Vec3f fromPitchYaw(Vec2f p_189984_0_) {
	      return fromPitchYaw(p_189984_0_.x, p_189984_0_.y);
	   }

	   /**
	    * returns a Vec3f from given pitch and yaw degrees
	    */
	   public static Vec3f fromPitchYaw(float pitch, float yaw) {
	      float f = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
	      float f1 = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
	      float f2 = -MathHelper.cos(-pitch * ((float)Math.PI / 180F));
	      float f3 = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
	      return new Vec3f((float)(f1 * f2), (float)f3, (float)(f * f2));
	   }

	   public Vec3f align(EnumSet<Direction.Axis> axes) {
	      float d0 = axes.contains(Direction.Axis.X) ? (float)MathHelper.floor(this.x) : this.x;
	      float d1 = axes.contains(Direction.Axis.Y) ? (float)MathHelper.floor(this.y) : this.y;
	      float d2 = axes.contains(Direction.Axis.Z) ? (float)MathHelper.floor(this.z) : this.z;
		return new Vec3f(d0, d1, d2);
	   }

	   public final float getX() {
	      return this.x;
	   }

	   public final float getY() {
	      return this.y;
	   }

	   public final float getZ() {
	      return this.z;
	   }
}
