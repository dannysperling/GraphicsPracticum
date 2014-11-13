package cs4620.ray1;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

public class Oak {
	public static Color woodColor = new Color(214,199,156);
	public static Color poreColor = new Color(133,116,70);
	
	public static void main(String[] args) {
		Image img = new Image(1000,1000);
		img.clear();
		
		double center_x = 200;
		double center_y = 200;
		int ring;
		double row_offset = 0.5;
		double radius;
		
		double a1, a2, a3, a4, mfactor, xc, zc, quotent;
		
		double dist;
		
		
		for (int x = 0; x < img.width; x++) {
			for (int z = 0; z < img.height; z++) {
				// Calculate row and offset
				ring = (int)Math.sqrt(x*x+z*z);
				radius = ring + row_offset;
				
				// Find pore closest to offset
				a1 = 2*Math.PI/(100*ring);
				a2 = (new Vector2d(radius,0.)).angle(new Vector2d(x,z));
				mfactor = (int)((a2/a1)+5);
				a3 = a1*mfactor;
				xc = (radius)*Math.cos(a3+PerlinNoise.noise(x, z, 0));
				zc = (radius)*Math.sin(a3+PerlinNoise.noise(x, z, 0));
				
				a4 = 2*Math.PI/(100*ring);
				quotent = a2/a4;
				quotent = quotent - (int)quotent;
				
				
				dist = (x-xc)*(x-xc)+(z-zc)*(z-zc);
				//System.out.println("Dist: " + dist + ", Radius: " + row_offset*row_offset);
				// Determine if point should be colored as a pore
				if ((dist <= .1)&&quotent < 0.8) {
					img.setPixelColor(poreColor, x, z);
				}
				else {
					img.setPixelColor(woodColor, x, z);
				}
			}
		}
		img.write("wood.png");
		
	}

}
