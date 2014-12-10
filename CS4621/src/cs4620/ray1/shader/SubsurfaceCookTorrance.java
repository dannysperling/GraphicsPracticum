package cs4620.ray1.shader;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import cs4620.ray1.Wood;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

public class SubsurfaceCookTorrance extends Shader {

	private static final double M_PI = 3.1415926;

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The roughness for Cook-Torrance. Opposite of shininess. */
	protected double roughness = 0.2;
	public void setRoughness(double roughness) { this.roughness = roughness; }
	
	/** The specular reflectance of light perpendicular to the surface. */
	protected double fNaught = 0.04;
	public void setFNaught(double fNaught) { this.fNaught = fNaught; }
	
	/** The refraction index of the medium (wood) */
	protected double refractionIndex = 3; //Note: Unsure for now
	public void setRefractionIndex(double refractionIndex) { this.refractionIndex = refractionIndex; }
	
	/** The angle of the subsurface fibers.*/
	protected final Vector3d fiberDirection = new Vector3d();
	public void setFiberDirection(Vector3d fiberDirection){ this.fiberDirection.set(fiberDirection.normalize()); }
	
	//TODO: Change these to be a thing in the XML file
	protected final Colord fiberColor = new Colord(Wood.darkColor);
	protected final Colord dSubColor = new Colord(Wood.darkColor.clone().div(20));
	protected final double beta = 10;
	
	protected final double random_noise = Math.random() * 50 + 225;

	public SubsurfaceCookTorrance() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "sub surface cook torrance " + diffuseColor + " " + specularColor + " " + roughness + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Cook-Torrance shading model,
	 * additionally including reflections from wood fibers beneath the surface.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray,
			IntersectionRecord record) {

		//Reset the output color to be zero
		outIntensity.setZero();
		
		//Check each light
		for (Light light : scene.getLights()){

			//Use simple ray here - utility function handles setting ray to correct values
			Ray shadowRay = new Ray();

			//Only add from light source if not in shadow
			if (!isShadowed(scene, light, record, shadowRay)){

				Vector3d lightAngle = light.position.clone().sub(record.location);
				double r = lightAngle.len();
				lightAngle.normalize();
				
				Vector3d viewingAngle = ray.direction.clone().negate();
				viewingAngle.normalize();
				
				Vector3d halfAngle = lightAngle.clone().add(viewingAngle).normalize();
			
				double nDotL = record.normal.dot(lightAngle);
				double nDotV = record.normal.dot(viewingAngle);

				//Perform Cook-Torrance calculation - only do things if the dot product >= 0
				if (nDotL < 0){
					continue;
				}
				
				double specTerm = getFresnelTerm(viewingAngle, halfAngle)/M_PI;
				specTerm *= getMicrofacetDistribution(halfAngle, record.normal);
				specTerm *= getGeometricAttenuation(halfAngle, record.normal, viewingAngle, lightAngle);
				specTerm /= nDotV * nDotL;
				
				//Factor of the intensity
				Colord dColor = Wood.getPixelColor(record.location.x, record.location.y, 
												   record.location.z, random_noise);	
				Vector3d color = dColor.clone().addMultiple(specTerm, specularColor);
				
				//Degree of reflection
				color.mul(nDotL);
				
				//TiTr Term
				double TiTr = (1 - getFresnelTerm(lightAngle, record.normal))*(1 - getFresnelTerm(viewingAngle, record.normal));
				Vector3d ff = getFiberReflection(lightAngle, viewingAngle);
				color.addMultiple(TiTr, dSubColor.clone().add(ff));

				//Then light intensity
				color.mul(light.intensity);

				//Then distance fall-off
				color.mul(1/Math.pow(r, 2));
				
				//Add to the color
				outIntensity.add(color);
			}
		}
	}
	
	/**
	 * Based on the actual Fresnel Equation
	 */
	private double getFresnelTerm(Vector3d incidenceAngle, Vector3d normalAngle) {
		
		//Angle of Incidence
		double aI = incidenceAngle.angle(normalAngle);
		
		//Air
		double n1 = 1;
		double n2 = refractionIndex;
		
		//Angle of transmission
		double aT = Math.asin(Math.sin(aI) * n1 / n2);
		
		//Two polarizations of reflected light
		double Rs = Math.pow((n1*Math.cos(aI) - n2*Math.cos(aT))/(n1*Math.cos(aI) + n2*Math.cos(aT)),2);
		double Rt = Math.pow((n1*Math.cos(aT) - n2*Math.cos(aI))/(n1*Math.cos(aT) + n2*Math.cos(aI)),2);
		
		//Total reflectence
		return (Rs + Rt) / 2;
	}
	
	private double getMicrofacetDistribution(Vector3d halfAngle, Vector3d normalAngle){
		
		double mSq = roughness*roughness;
		double nDotH = normalAngle.dot(halfAngle);
		
		double firstTerm = mSq * Math.pow(nDotH, 4);
		firstTerm = Math.pow(firstTerm, -1);

		double secondTerm = Math.exp((Math.pow(nDotH, 2) - 1) / (mSq * Math.pow(nDotH, 2)));
		
		return firstTerm * secondTerm;
	}

	private double getGeometricAttenuation(Vector3d halfAngle, Vector3d normalAngle, Vector3d viewingAngle,
											Vector3d lightAngle){
		
		double nDotH = normalAngle.dot(halfAngle);
		double nDotV = normalAngle.dot(viewingAngle);
		double vDotH = viewingAngle.dot(halfAngle);
		double nDotL = normalAngle.dot(lightAngle);
		
		double firstTerm = 2 * nDotH * nDotV / vDotH;
		double secondTerm = 2 * nDotH * nDotL / vDotH;
		
		return Math.min(Math.min(1, firstTerm), secondTerm);
	}
	
	private Vector3d getFiberReflection(Vector3d vI, Vector3d vR) {
		
		//TODO: Lamination using s
		double psi_i = Math.asin(vI.dot(fiberDirection));
		double psi_r = Math.asin(vR.dot(fiberDirection));
		double psi_h = psi_r + psi_i;
		double psi_d = psi_r - psi_i;
		
		double sigma = beta;
		double x = psi_h;
		double gaus = 1/(sigma * Math.sqrt(2 * M_PI)) * Math.exp(-(x * x)/(2 * sigma * sigma));
		
		return fiberColor.clone().mul(gaus).div(Math.pow(Math.cos(psi_d / 2), 2));
	}

}
