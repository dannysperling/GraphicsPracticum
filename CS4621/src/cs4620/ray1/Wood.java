package cs4620.ray1;

import egl.math.Color;
import egl.math.Colord;

public class Wood {
	public static Colord woodColor = new Colord((144.0/255.0),(112.0/255.0),(90.0/255.0));
	public static Colord darkColor = new Colord((93.0/255.0), (52.0/255.0), (21.0/255.0));
	//public static Colord rayColor new Colord
	public static void main(String[] args) {
		int z = 1;
		int num_pixels = 1000;
		Image img = new Image(num_pixels, num_pixels);
		img.clear();
		
		double random_noise = Math.random() * 50 + 225;
		double random_i = Math.random() * Math.PI/6;
		double random_j = Math.random() * Math.PI/12 + Math.PI * (5.0/12.0);
		for (int i = 0; i < num_pixels; i++) {
			for (int j = 0; j < num_pixels; j++) {
				
				// Rings
				double newI = Math.cos(random_i) * i;
				double newJ = Math.cos(random_j) * j;
				
				double newK = Math.sin(random_i) * i;
				double noise = PerlinNoise.noise(newI/random_noise, 
						newJ/random_noise, newK/random_noise);
				
				double div = 10;
				
				double particle_noise = PerlinNoise.noise(newK, newI, newJ)/20;
				
				double dist = Math.sqrt(Math.pow((newI/((num_pixels)/div)-(div/2)),2) 
						+ Math.pow((newJ/(num_pixels/div)-(div/2)),2));
				dist = Math.pow(1.5, dist) + noise + particle_noise;
				
				double t = dist-Math.floor(dist);
				Colord thisColor = new Colord();
				
				double y = y(t,z);
				thisColor.set(woodColor.r()*y+(1-y)*darkColor.r(), 
						woodColor.g()*y+(1-y)*darkColor.g(), 
						woodColor.b()*y+(1-y)*darkColor.b());
				
				
				img.setPixelColor(thisColor, i, j);
			}
		}
		
		img.write("wood.png");
		

	}
	
	public static double y (double t, double z) {
		return ((z + Math.cos(2 * Math.PI * Math.pow(1-t, 0.25)))/(z+1));
	}

}
