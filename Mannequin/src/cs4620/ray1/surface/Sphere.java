package cs4620.ray1.surface;

import java.util.ArrayList;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected final double M_2PI = 2 * Math.PI;

	public Sphere() {
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		
		//transform the ray into object space
		Ray ray = untransformRay(rayIn);
		
		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		double qz = o.z - c.z;
		double dd = d.lenSq();
		double qd = qx * d.x + qy * d.y + qz * d.z;
		double qq = qx * qx + qy * qy + qz * qz;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray
		// (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;
		double t = 0;
		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		} else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		} else {
			return false; // Neither intersection was in the ray's half line.
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			outRecord.surface = this;
			outRecord.normal.set(outRecord.location).sub(center).normalize();
			double theta = Math.asin(outRecord.normal.y);
			double phi = Math.atan2(outRecord.normal.x, outRecord.normal.z);
			double u = (phi + Math.PI) / (2 * Math.PI);
			double v = (theta - Math.PI / 2) / Math.PI;
			outRecord.texCoords.set(u, v);
			
			//Keep track of object space coordinates
			outRecord.objLocaton.set(outRecord.location.clone());
			
			//Modify back the outRecord location and normal
		    tMat.mulPos(outRecord.location);
		    tMatTInv.mulDir(outRecord.normal);
			outRecord.normal.normalize();
		}

		return true;
	}

	public void computeBoundingBox() {
		
		//Average is simply the center, multiplied my the transformation matrix
		averagePosition = center.clone();
		tMat.mulPos(averagePosition);
		
		//Calculate the bounding rectangle. Then, do the same thing
		//that was done for box.
		Vector3d awayVec  = new Vector3d(radius);
		Vector3d minPt = center.clone().sub(awayVec);
		Vector3d maxPt = center.clone().add(awayVec);
		
		//Remainder is exactly the same as it was for box
		ArrayList<Vector3d> corners = new ArrayList<Vector3d>(8);
		corners.add(minPt.clone());
		corners.add(maxPt.clone());
		
		//Two parts max
		corners.add(new Vector3d(minPt.x, maxPt.y, maxPt.z));
		corners.add(new Vector3d(maxPt.x, minPt.y, maxPt.z));
		corners.add(new Vector3d(maxPt.x, maxPt.y, minPt.z));
		
		//Two parts min
		corners.add(new Vector3d(minPt.x, minPt.y, maxPt.z));
		corners.add(new Vector3d(minPt.x, maxPt.y, minPt.z));
		corners.add(new Vector3d(maxPt.x, minPt.y, minPt.z));
		
		double posInf = Double.POSITIVE_INFINITY;
		minBound = new Vector3d(posInf, posInf, posInf);
		double negInf = Double.NEGATIVE_INFINITY;
		maxBound = new Vector3d(negInf, negInf, negInf);
		
		for (Vector3d corner : corners){
			tMat.mulPos(corner);
			maxBound.x = Math.max(maxBound.x, corner.x);
			maxBound.y = Math.max(maxBound.y, corner.y);
			maxBound.z = Math.max(maxBound.z, corner.z);
			minBound.x = Math.min(minBound.x, corner.x);
			minBound.y = Math.min(minBound.y, corner.y);
			minBound.z = Math.min(minBound.z, corner.z);
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}