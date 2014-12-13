package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector3d;
import egl.math.Vector3i;
import cs4620.ray1.shader.Shader;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
	/** The normal vector of this triangle, if vertex normals are not specified */
	Vector3d norm;

	/** The mesh that contains this triangle */
	Mesh owner;

	/** 3 indices to the vertices of this triangle. */
	Vector3i index;

	double a, b, c, d, e, f;

	public Triangle(Mesh owner, Vector3i index, Shader shader) {
		this.owner = owner;
		this.index = new Vector3i(index);

		Vector3d v0 = owner.getPosition(index.x);
		Vector3d v1 = owner.getPosition(index.y);
		Vector3d v2 = owner.getPosition(index.z);

		if (!owner.hasNormals()) {
			Vector3d e0 = new Vector3d(), e1 = new Vector3d();
			e0.set(v1).sub(v0);
			e1.set(v2).sub(v0);
			norm = new Vector3d();
			norm.set(e0).cross(e1);
			norm.normalize();
		}
		a = v0.x - v1.x;
		b = v0.y - v1.y;
		c = v0.z - v1.z;

		d = v0.x - v2.x;
		e = v0.y - v2.y;
		f = v0.z - v2.z;

		this.setShader(shader);
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param rayIn
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
		
		//Modify the ray into object space
		Ray ray = untransformRay(rayIn);		
		
		Vector3d v0 = owner.getPosition(index.x).clone();
		
		double g = ray.direction.x;
		double h = ray.direction.y;
		double i = ray.direction.z;
		double j = v0.x - ray.origin.x;
		double k = v0.y - ray.origin.y;
		double l = v0.z - ray.origin.z;
		double M = a * (e * i - h * f) + b * (g * f - d * i) + c
				* (d * h - e * g);

		double ei_hf = e * i - h * f;
		double gf_di = g * f - d * i;
		double dh_eg = d * h - e * g;
		double ak_jb = a * k - j * b;
		double jc_al = j * c - a * l;
		double bl_kc = b * l - k * c;

		double t = -(f * (ak_jb) + e * (jc_al) + d * (bl_kc)) / M;
		if (t > ray.end || t < ray.start)
			return false;

		double beta = (j * (ei_hf) + k * (gf_di) + l * (dh_eg)) / M;
		if (beta < 0 || beta > 1)
			return false;

		double gamma = (i * (ak_jb) + h * (jc_al) + g * (bl_kc)) / M;
		if (gamma < 0 || gamma + beta > 1)
			return false;

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			
			
			outRecord.surface = this;

			if (norm != null) {
				outRecord.normal.set(norm);
			} else {
				outRecord.normal
						.setZero()
						.addMultiple(1 - beta - gamma, owner.getNormal(index.x))
						.addMultiple(beta, owner.getNormal(index.y))
						.addMultiple(gamma, owner.getNormal(index.z));
			}
			
			
			outRecord.normal.normalize();
			if (owner.hasUVs()) {
				outRecord.texCoords.setZero()
						.addMultiple(1 - beta - gamma, owner.getUV(index.x))
						.addMultiple(beta, owner.getUV(index.y))
						.addMultiple(gamma, owner.getUV(index.z));
			}
			
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
		
		//Get the points
		Vector3d v0 = owner.getPosition(index.x).clone();
		Vector3d v1 = owner.getPosition(index.y).clone();
		Vector3d v2 = owner.getPosition(index.z).clone();
		
		//Multiply to get in world space
		tMat.mulPos(v0);
		tMat.mulPos(v1);
		tMat.mulPos(v2);
		
		//Get the average position
		double avgX = (v0.x + v1.x + v2.x) / 3;
		double avgY = (v0.y + v1.y + v2.y) / 3;
		double avgZ = (v0.z + v1.z + v2.z) / 3;
		averagePosition = new Vector3d(avgX, avgY, avgZ);
		
		//Get the bounding positions
		double maxX = Math.max(Math.max(v0.x, v1.x), v2.x);
		double minX = Math.min(Math.min(v0.x, v1.x), v2.x);
		
		double maxY = Math.max(Math.max(v0.y, v1.y), v2.y);
		double minY = Math.min(Math.min(v0.y, v1.y), v2.y);
		
		double maxZ = Math.max(Math.max(v0.z, v1.z), v2.z);
		double minZ = Math.min(Math.min(v0.z, v1.z), v2.z);
		
		//Best. method. ever.
		minBound = new Vector3d(minX, minY, minZ);
		maxBound = new Vector3d(maxX, maxY, maxZ);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Triangle ";
	}
}