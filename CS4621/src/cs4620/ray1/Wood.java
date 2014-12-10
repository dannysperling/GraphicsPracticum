package cs4620.ray1;

import egl.math.Colord;

public class Wood {
	public static Colord woodColor = new Colord((144.0/255.0),(112.0/255.0),(90.0/255.0));
	public static Colord darkColor = new Colord((93.0/255.0), (52.0/255.0), (21.0/255.0));

	public static Colord getPixelColor(double x, double y, double z, double random_noise) {
		
		double largeScale = 350;
		double noise = PerlinNoise.noise(largeScale*x/random_noise, 
				largeScale*y/random_noise, largeScale*z/random_noise);
				
		double particle_noise = PerlinNoise.noise(largeScale*x, largeScale*y, largeScale*z)/20;
		
		double smallScale = 5;
		
		double dist = Math.sqrt(x*x + y*y) * smallScale;
		dist = Math.pow(1.5, dist) + noise + particle_noise;
		
		double t = dist-Math.floor(dist);
		Colord thisColor = new Colord();
		
		double f = f(t,1);
		thisColor.set(woodColor.clone().mul(f).addMultiple(1 - f, darkColor));
		
		return thisColor;
	}
	
	public static double f (double t, double factor) {
		return ((factor + Math.cos(2 * Math.PI * Math.pow(1-t, 0.25)))/(factor+1));
	}

}
