package cs4620.ray1.shader;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Lambertian material scatters light equally in all directions. BRDF value is
 * a constant
 *
 * @author ags
 */
public class Lambertian extends Shader {

	/** The color of the surface. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord inDiffuseColor) { diffuseColor.set(inDiffuseColor); }

	public Lambertian() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "lambertian: " + diffuseColor;
	}

	/**
	 * Evaluate the intensity for a given intersection using the Lambert shading model.
	 * 
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record) {

		//Reset the output color to be zero
		outIntensity.setZero();
		
		//To be able to user texture colors
		Colord dColor = (texture == null) ? diffuseColor : texture.getTexColor(record.texCoords);
		
		//Check each light
		for (Light light : scene.getLights()){
			
			//Use simple ray here - utility function handles setting ray to correct values
			Ray shadowRay = new Ray();
			
			//Only add from light source if not in shadow
			if (!isShadowed(scene, light, record, shadowRay)){
				
				Vector3d towardLight = light.position.clone().sub(record.location);
				double r = towardLight.len();
				towardLight.normalize();
								
				double normalDotLight = record.normal.dot(towardLight);
				
				//Perform Lambertian calculation - first diffuse
				Vector3d intensity = dColor.clone().mul(Math.max(normalDotLight, 0));
				
				//Multiply by light intensity
				intensity.mul(light.intensity);
				
				//And by distance fall-off
				intensity.mul(1/Math.pow(r, 2));
				
				//Add to color
				outIntensity.add(intensity);
			}
		}
	}
}