package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Matrix3d;
import egl.math.Vector2d;
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
    }
    a = v0.x-v1.x;
    b = v0.y-v1.y;
    c = v0.z-v1.z;
    
    d = v0.x-v2.x;
    e = v0.y-v2.y;
    f = v0.z-v2.z;
    
    this.setShader(shader);
  }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param rayIn the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
	  
	  //Using Barycentric ray-triangle intersection as described in the notes.
	  //Noting the usefulness of variables a, b, c, d, e and f, creating matrix:
	  // a d x_d
	  // b e y_d
	  // c f z_d
	  Vector3d col0 = new Vector3d(a, b ,c);
	  Vector3d col1 = new Vector3d(d, e, f);
	  Vector3d col2 = rayIn.direction.clone();
	  
	  //Establish vector on right side of the equation, a - p:
	  //x_a - x_p
	  //y_a - y_p
	  //z_a - z_p
	  Vector3d v0 = owner.getPosition(index.x);
	  Vector3d aSubP = v0.clone().sub(rayIn.origin);
	  
	  //Using Cramer's rule
	  Matrix3d matrixA = new Matrix3d(col0, col1, col2);
	  double detA = matrixA.determinant();
	  
	  //If determinant of A equals zero, unsolvable, no intersection
	  if (detA == 0){
		  return false;
	  }
	  
	  //Substituting vector into matrix
	  Matrix3d matrixA0 = new Matrix3d(aSubP, col1, col2);
	  Matrix3d matrixA1 = new Matrix3d(col0, aSubP, col2);
	  Matrix3d matrixA2 = new Matrix3d(col0, col1, aSubP);
	  
	  double beta = matrixA0.determinant() / detA;
	  double gamma = matrixA1.determinant() / detA;
	  double t = matrixA2.determinant() / detA;
	  
	  //Check parameters on beta, gamma, and t
	  if (!(beta > 0 && gamma > 0 && beta + gamma < 1)){
		  return false;
	  }
	  if (!(rayIn.start < t && t < rayIn.end)){
		  return false;
	  }
	  
	  //Intersection occurs!
	  //Calculate position of intersection
	  Vector3d position = rayIn.origin.clone().addMultiple(t, rayIn.direction);
	  
	  //Set 3 of the 5 values
	  outRecord.t = t;
	  outRecord.location.set(position);
	  outRecord.surface = this;
	  
	  double alpha = 1 - beta - gamma;
	  
	  //Set normal
	  if (!owner.hasNormals()){
		  outRecord.normal.set(norm);
		  outRecord.normal.normalize();	//Thanks. That was a fun bug.
	  } else {
		  Vector3d normal = new Vector3d();
		  
		  normal.addMultiple(alpha, owner.getNormal(index.x));
		  normal.addMultiple(beta, owner.getNormal(index.y));
		  normal.addMultiple(gamma, owner.getNormal(index.z));
		  
		  normal.normalize();
		  outRecord.normal.set(normal);
	  }
	  
	  //Set uv texture coordinates, if available
	  if (owner.hasUVs()){
		  Vector2d texCoords = new Vector2d();
		  
		  texCoords.addMultiple(alpha, owner.getUV(index.x));
		  texCoords.addMultiple(beta, owner.getUV(index.y));
		  texCoords.addMultiple(gamma, owner.getUV(index.z));
		  
		  outRecord.texCoords.set(texCoords);
	  }
	  
	  return true;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Triangle ";
  }
}