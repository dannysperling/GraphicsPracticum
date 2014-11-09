package cs4620.ray1;

import java.util.ArrayList;
import java.util.List;

import cs4620.ray1.camera.Camera;
import egl.math.Colord;
import cs4620.ray1.shader.Shader;
import cs4620.ray1.shader.Texture;
import cs4620.ray1.surface.Surface;

/**
 * The scene is just a collection of objects that compose a scene. The camera,
 * the list of lights, and the list of surfaces.
 *
 * @author ags, pramook
 */
public class Scene {
	
	/** The camera for this scene. */
	protected Camera camera;
	public void setCamera(Camera camera) { this.camera = camera; }
	public Camera getCamera() { return this.camera; }
	
	/** The background color for this scene. Any rays that don't hit a surface
	 *  return this color.
	 */
	protected Colord backColor = new Colord();
	public void setBackColor(Colord color) { this.backColor.set(color); }
	public Colord getBackColor() { return this.backColor; }
	
	/** The amount of exposure to be used for this Scene. */
	protected double exposure= 1.0;
	/**
	 * Sets the exposure of this Scene.
	 * @param exposure
	 * 		The exposure to be used; must be greater than 0.
	 */
	public void setExposure(double exposure) {
		if(exposure > 0.0) this.exposure= exposure;
	}
	public double getExposure() { return this.exposure; }
	
	/** The list of lights for the scene. */
	protected ArrayList<Light> lights = new ArrayList<Light>();
	public void addLight(Light toAdd) { lights.add(toAdd); }
	public List<Light> getLights() { return this.lights; }
	
	/** The list of surfaces for the scene. */
	protected ArrayList<Surface> surfaces = new ArrayList<Surface>();
	public void addSurface(Surface toAdd) { surfaces.add(toAdd); }
	public List<Surface> getSurfaces() { return this.surfaces; }
	public void setSurfaces(ArrayList<Surface> s) { surfaces = s; }
	
	/** The list of textures for the scene. */
	protected ArrayList<Texture> textures = new ArrayList<Texture>();
	public void addTexture(Texture toAdd) { textures.add(toAdd); }
	public List<Texture> getTextures() { return this.textures; }
	
	/** The list of shaders in the scene. */
	protected ArrayList<Shader> shaders = new ArrayList<Shader>();
	public void addShader(Shader toAdd) { shaders.add(toAdd); }
	
	/** Image to be produced by the renderer **/
	protected Image outputImage;
	public Image getImage() { return this.outputImage; }
	public void setImage(Image outputImage) { this.outputImage = outputImage; }
	
	/**
	 * Set outRecord to the first intersection of ray with the scene. Return true
	 * if there was an intersection and false otherwise. If no intersection was
	 * found outRecord is unchanged.
	 *
	 * @param outRecord the output IntersectionRecord
	 * @param ray the ray to intersect
	 * @return true if and intersection is found.
	 */
	public boolean getFirstIntersection(IntersectionRecord outRecord, Ray ray) {
		return intersect(outRecord, ray, false);
		
	}
	
	/**
	 * Shadow ray calculations can be considerably accelerated by not bothering to find the
	 * first intersection.  This record returns any intersection of the ray and the surfaces
	 * and returns true if one is found.
	 * @param ray the ray to intersect
	 * @return true if any intersection is found
	 */
	public boolean getAnyIntersection(Ray ray) {
		return intersect(new IntersectionRecord(), ray, true);	
	}
	
	private boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
		
		//Use a local copy so it's modifiable
		Ray localRayIn = new Ray(rayIn);
		boolean somethingIntersected = false;
		
		//Check every surface for intersections
		for (Surface surface : surfaces){
			boolean intersects = surface.intersect(outRecord, localRayIn);
			
			//Possibly return immediately on an intersection
			if (intersects){
				if (anyIntersection){
					return true;
				}
				
				//Note that we did have an intersection
				somethingIntersected = true;
				
				//Update the local ray to only intersect with closer objects from now on
				localRayIn.end = outRecord.t;
			}
		}
		
		return somethingIntersected;
	}
}