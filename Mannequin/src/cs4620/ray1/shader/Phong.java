package cs4620.ray1.shader;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Phong extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The exponent controlling the sharpness of the specular reflection. */
	protected double exponent = 1.0;
	public void setExponent(double exponent) { this.exponent = exponent; }

	public Phong() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Phong shading model.
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
				
				//Perform Phong calculation - only do things if the dot product >= 0
				if (normalDotLight < 0){
					continue;
				}
				Vector3d towardViewer = ray.direction.clone().negate();

				//First diffuse
				Vector3d intensity = dColor.clone().mul(normalDotLight);
				
				//Then specular
				Vector3d h = towardLight.clone().add(towardViewer);
				h.normalize();
				double normalDotH = record.normal.dot(h);
				double specMultiple = Math.pow(Math.max(normalDotH, 0), exponent);
				intensity.addMultiple(specMultiple, specularColor);
				
				//Then light intensity
				intensity.mul(light.intensity);
				
				//Then distance fall-off
				intensity.mul(1/Math.pow(r, 2));
				
				//Add to the color
				outIntensity.add(intensity);
			}
		}
	}

}