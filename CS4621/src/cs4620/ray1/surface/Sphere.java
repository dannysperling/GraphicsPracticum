package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {
  
  /** The center of the sphere. */
  protected final Vector3d center = new Vector3d();
  public void setCenter(Vector3d center) { this.center.set(center); }
  
  /** The radius of the sphere. */
  protected double radius = 1.0;
  public void setRadius(double radius) { this.radius = radius; }
  
  protected final double M_2PI = 2*Math.PI;
  
  public Sphere() { }
  
  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param ray the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {

	  //Get the position vector of the ray relative to this sphere (as if this
	  //sphere was at the origin).
	  Vector3d p = rayIn.origin.clone().sub(center);
	  Vector3d d = rayIn.direction.clone();
	  
	  double dDotP = d.dot(p);
	  double pDotP = p.dot(p);
	  
	  //Using equation t = -d*p +- sqrt((d*p)^2 - p*p + r*r)
	  //Value inside the sqrt must be positive for intersection
	  double insideSqrt = dDotP * dDotP - pDotP + radius*radius;
	  if (insideSqrt < 0){
		  return false;
	  }
	  
	  //Subtracting is closer, so check that first (other only true if inside shape)
	  double sqrtVal = Math.sqrt(insideSqrt);
	  double tVal =  -dDotP - sqrtVal;
	  if (!(rayIn.start < tVal && tVal < rayIn.end)){
		  
		  //Check addition too - if not, return false
		  tVal = -dDotP + sqrtVal;
		  if (!(rayIn.start < tVal && tVal < rayIn.end)){
			  return false;
		  }
	  }
	  
	  //Intersection occurs!
	  //Compute the position of the intersection
	  Vector3d position = rayIn.origin.clone().addMultiple(tVal, rayIn.direction);
	  
	  //Spheres are easy to calculate the exact normal for
	  Vector3d normal = position.clone().sub(center).normalize();
	  
	  //Use normal coordinates to determine UV coords
	  double u = Math.atan2(normal.x, normal.z) / M_2PI + 0.5;
	  double v = Math.asin(normal.y) / Math.PI + 0.5;
	  Vector2d texCoords = new Vector2d(u, v);
	  
	  //Set the out record values
	  outRecord.t = tVal;
	  outRecord.location.set(position);
	  outRecord.normal.set(normal);
	  outRecord.texCoords.set(texCoords);
	  outRecord.surface = this;
	  
	  return true;
  }
  
  /**
   * @see Object#toString()
   */
  public String toString() {
	  return "sphere " + center + " " + radius + " " + shader + " end";
  }

}