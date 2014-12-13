package cs4620.ray1.shader;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * A Texture class that repeats the texture image as necessary for UV-coordinates
 * outside the [0.0, 1.0] range.
 * 
 * @author eschweic
 *
 */
public class RepeatTexture extends Texture {

	public Colord getTexColor(Vector2d texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colord();
		}
				
		int width = image.getWidth();
		int height = image.getHeight();
		
		//Get values
		int x = (int)(texCoord.x * width + 0.5);
		int y = (int)((1 - texCoord.y) * height + 0.5);
		
		//Repeat values
		x = x % width;
		if (x < 0){
			x += width;
		}
		y = y % height;
		if (y < 0){
			y += height;
		}
		
		//Get the color
		Color color = Color.fromIntRGB(image.getRGB(x, y));
		Colord colord = new Colord();
		colord.set(color);
		return colord;
	}

}
