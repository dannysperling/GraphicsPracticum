package cs4620.ray1.surface;

import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;
import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector3d;

/**
 * A class that represents an Axis-Aligned box. When the scene is built, the Box
 * is split up into a Mesh of 12 Triangles.
 * 
 * @author sjm324
 *
 */
public class Box extends Surface {

	/* The mesh that represents this Box. */
	private Mesh mesh;

	/* The corner of the box with the smallest x, y, and z components. */
	protected final Vector3d minPt = new Vector3d();

	public void setMinPt(Vector3d minPt) {
		this.minPt.set(minPt);
	}

	/* The corner of the box with the largest x, y, and z components. */
	protected final Vector3d maxPt = new Vector3d();

	public void setMaxPt(Vector3d maxPt) {
		this.maxPt.set(maxPt);
	}

	/* Generate a Triangle mesh that represents this Box. */
	private void buildMesh() {
		// Create the OBJMesh
		MeshData box = new MeshData();

		box.vertexCount = 8;
		box.indexCount = 36;

		// Add positions
		box.positions = BufferUtils.createFloatBuffer(box.vertexCount * 3);
		box.positions.put(new float[] { (float) minPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) minPt.y,
				(float) maxPt.z, (float) minPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) minPt.y,
				(float) maxPt.z });

		box.indices = BufferUtils.createIntBuffer(box.indexCount);
		box.indices.put(new int[] { 0, 1, 2, 0, 2, 3, 0, 5, 1, 0, 4, 5, 0, 7,
				4, 0, 3, 7, 4, 6, 5, 4, 7, 6, 2, 5, 6, 2, 1, 5, 2, 6, 7, 2, 7,
				3 });
		this.mesh = new Mesh(box);
		
		//set transformations and absorptioins
		this.mesh.setTransformation(this.tMat, this.tMatInv, this.tMatTInv);
		
		this.mesh.shader = this.shader;
	}

	public void computeBoundingBox() {

		// Test out the 8 points, get the minimum x, y, and z
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
		
		minBound = new Vector3d(Double.POSITIVE_INFINITY);
		maxBound = new Vector3d(Double.NEGATIVE_INFINITY);
		
		for (Vector3d corner : corners){
			tMat.mulPos(corner);
			maxBound.x = Math.max(maxBound.x, corner.x);
			maxBound.y = Math.max(maxBound.y, corner.y);
			maxBound.z = Math.max(maxBound.z, corner.z);
			minBound.x = Math.min(minBound.x, corner.x);
			minBound.y = Math.min(minBound.y, corner.y);
			minBound.z = Math.min(minBound.z, corner.z);
		}
		
		//Average is the average of the transformed min and max points
		averagePosition = new Vector3d(corners.get(0).clone().add(corners.get(1)).div(2));
	}

	public boolean intersect(IntersectionRecord outRecord, Ray ray) {
		return false;
	}

	public void appendRenderableSurfaces(ArrayList<Surface> in) {
		buildMesh();
		mesh.appendRenderableSurfaces(in);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Box ";
	}

}