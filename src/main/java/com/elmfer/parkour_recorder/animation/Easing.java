package com.elmfer.parkour_recorder.animation;

import java.util.function.Function;

/*
 *    Credits:
 *    	-Most of these functions are referenced from "https://easings.net".
 *    	-Exceptions: INSTANT
 */

public enum Easing {
	
	LINEAR(new Function<Double, Double>(){
		
		@Override
		public Double apply(Double t) {
			
			return t;
		}
	}),
	INSTANT(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t < 1.0) {
				
				return 0.0;
			}else {
				
				return 1.0;
			}
		}
	}),
	IN_SINE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.sin((Math.PI * t - Math.PI) / 2.0) + 1.0;
		}
	}),
	OUT_SINE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.sin((Math.PI * t) / 2.0);
		}
	}),
	INOUT_SINE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return (Math.sin(Math.PI * t - (Math.PI) / 2.0)) / 2.0 + 0.5;
		}
	}),
	IN_QUAD(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t, 2.0);
		}
	}),
	OUT_QUAD(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return -1.0 * Math.pow(t - 1.0, 2.0) + 1.0;
		}
	}),
	INOUT_QUAD(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return 2.0 * Math.pow(t, 2.0);
			}else {
				
				return -2.0 * Math.pow(t - 1.0, 2.0) + 1.0;
			}
		}
	}),
	IN_CUBIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t , 3.0);
		}
	}),
	OUT_CUBIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t - 1.0, 3.0) + 1.0;
		}
	}),
	INOUT_CUBIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return 4.0 * Math.pow(t, 3.0);
			}else {
				
				return 4.0 * Math.pow(t - 1.0, 3.0) + 1.0;
			}
		}
	}),
	IN_QUART(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t, 4.0);
		}
	}),
	OUT_QUART(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return -1.0 * Math.pow(t - 1.0, 4.0) + 1.0;
		}
	}),
	INOUT_QUART(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return 8.0 * Math.pow(t, 4.0);
			}else {
				
				return -8.0 * Math.pow(t - 1.0, 4.0) + 1.0;
			}
		}
	}),
	IN_QUINT(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t, 5.0);
		}
	}),
	OUT_QUINT(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(t - 1.0, 5.0) + 1.0;
		}
	}),
	INOUT_QUINT(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return 16.0 * Math.pow(t, 5.0);
			}else {
				
				return 16.0 * Math.pow(t - 1.0, 5.0) + 1.0;
			}
		}
	}),
	IN_EXPO(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.pow(1000.0, t - 1.0);
		}
	}),
	OUT_EXPO(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return -1.0 * Math.pow(0.001, t) + 1.0;
		}
	}),
	INOUT_EXPO(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return (Math.pow(1000000.0, t - 0.5)) / 2.0;
			}else {
				
				return -1.0 / (2.0 * Math.pow(1000000.0, t - 0.5)) + 1.0;
			}
		}
	}),
	IN_CIRC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.0) {
				
				return 0.0;
			}else if (t >= 1.0) {
				
				return 1.0;
			}else {
				
				return 1.0 - Math.sqrt(1.0 - Math.pow(t, 2.0));
			}
		}
	}),
	OUT_CIRC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.0) {
				
				return 0.0;
			}else if (t >= 1.0) {
				
				return 1.0;
			}else {
				
				return Math.sqrt(1.0 - Math.pow(t - 1.0, 2.0));
			}
		}
	}),
	INOUT_CIRC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.0) {
				
				return 0.0;
			}else if(t >= 1.0) {
				
				return 1.0;
			}else if(t == 0.5) {
				
				return 0.5;
			}else if(t < 0.5) {
				
				return 0.5 - Math.sqrt(0.25 - Math.pow(t, 2.0));
			}else {
				
				return Math.sqrt(0.25 - Math.pow(t - 1.0, 2.0)) + 0.5;
			}
		}
	}),
	IN_BACK(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return 2.0 * Math.pow(t, 4.0) - Math.pow(t, 2.0);
		}
	}),
	OUT_BACK(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return -2.0 * Math.pow(t - 1.0, 4.0) + Math.pow(t - 1.0, 2.0) + 1.0;
		}
	}),
	INOUT_BACK(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5D) {
				
				return 16.0 * Math.pow(t, 4.0) - 2.0 * Math.pow(t, 2.0);
			}else {
				
				return -16.0 * Math.pow(t - 1.0, 4.0) + 2.0 * Math.pow(t - 1.0, 2.0) + 1.0;
			}
		}
	}),
	IN_ELASTIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.sin(4.5 * (t - (5.0 / 9.0)) * Math.PI) * Math.pow(t, 4.0) + Math.pow(t, 10.0);
		}
	}),
	OUT_ELASTIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			return Math.sin(4.5 * t * Math.PI) * Math.pow(t - 1.0, 4.0) - Math.pow(t - 1.0, 10.0) + 1.0;
		}
	}),
	INOUT_ELASTIC(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			if(t <= 0.5) {
				
				return Math.sin(9.0 * (t - (13.0 / 18.0)) * Math.PI) * 8.0 * Math.pow(t, 4.0) + 512.0 * Math.pow(t, 10.0);
			}else {
				
				return Math.sin(9.0 * (t - (7.0 - 18.0)) * Math.PI) * -8.0 * Math.pow(t - 1.0, 4.0) - 512.0 * Math.pow(t - 1.0, 10.0) + 1.0;
			}
		}
		
	}),
	IN_BOUNCE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			final double m = (-400.0 / 49.0);
			
			if(t <= 0.1) {
				
				return m * t * (t - 0.1);
			}else if(t <= 0.3) {
				
				return m * (t - 0.1) * (t - 0.3);
			}else if(t <= 0.65) {
				
				return m * (t - 0.3) * (t - 0.65);
			}else {
				
				return m * (t - 0.65) * (t - 1.35);
			}
		}
	}),
	OUT_BOUNCE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			final double m = (400.0 / 49.0);
			
			if(t <= 0.35) {
				
				return m * (t + 0.35) * (t - 0.35) + 1.0;
			}else if(t <= 0.7) {
				
				return m * (t - 0.35) * (t - 0.7) + 1.0;
			}else if(t <= 0.9) {
				
				return m * (t - 0.7) * (t - 0.9) + 1.0;
			}else {
				
				return m * (t - 0.9) * (t - 1.0) + 1.0;
			}
		}
	}),
	INOUT_BOUNCE(new Function<Double, Double>(){

		@Override
		public Double apply(Double t) {
			
			final double m = (800.0 / 49.0);
			
			if(t <= 0.05) {
				
				return -m * t * (t - 0.05);
			}else if(t <= 0.15) {
				
				return -m * (t - 0.05) * (t - 0.15);
			}else if(t <= 0.325) {
				
				return -m * (t - 0.15) * (t - 0.325);
			}else if(t <= 0.5) {
				
				return -m * (t - 0.325) * (t - 0.675);
			}else if (t <= 0.675) {
				
				return m * (t - 0.325) * (t - 0.675) + 1.0;
			}else if (t <= 0.85) {
				
				return m * (t - 0.675) * (t - 0.85) + 1.0;
			}else if (t <= 0.95){
				
				return m * (t - 0.85) * (t - 0.95) + 1.0;
			}else {
				
				return m * (t - 0.95) * (t - 1.0) + 1.0;
			}
		}
	});
	
	private final Function<Double, Double> function;
	
	Easing(Function<Double, Double> timeShader){
		
		function = timeShader;
	}
	
	public Function<Double, Double> getFunction(){
		
		return function;
	}
	
	public static Easing getDefault(){
		
		return LINEAR;
	}
	
}
