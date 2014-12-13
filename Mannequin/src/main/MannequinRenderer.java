package main;

import java.util.ArrayList;

import cs4620.common.SceneCamera;
import cs4620.common.SceneObject;
import cs4620.common.SceneRaster;
import cs4620.ray1.Image;
import cs4620.ray1.Light;
import cs4620.ray1.RayTracer;
import cs4620.ray1.RayTracer.ScenePath;
import cs4620.ray1.Scene;
import cs4620.ray1.camera.PerspectiveCamera;
import cs4620.ray1.shader.SubsurfaceCookTorrance;
import cs4620.ray1.surface.Mesh;
import cs4620.ray1.surface.Surface;
import egl.math.Colord;
import egl.math.Matrix4;
import egl.math.Matrix4d;
import egl.math.Vector3;
import egl.math.Vector3d;
import ext.java.Parser;


public class MannequinRenderer {

	public static void main(String[] args) {
		
		Parser p = new Parser();
		String fname = "data/scenes/ray1/manikin.xml";
		SceneRaster s = (SceneRaster)p.parse(fname, SceneRaster.class);
		
		RayTracer.sceneWorkspace = new ScenePath(RayTracer.directory, "manikin.xml");
		String path = "../../meshes/mannequin/";
		Scene rayScene = new Scene();
		
		//The shader
		SubsurfaceCookTorrance shader = new SubsurfaceCookTorrance();
		shader.setDiffuseColor(new Colord(0.64, 0.16, 0.16));
		shader.setSpecularColor(new Colord(1, 1, 1));
		shader.setRoughness(0.4);
		shader.setFiberDirection(new Vector3d(0, 1, 0));
		rayScene.addShader(shader);
		
		rayScene.setImage(new Image(1800, 1800));
		rayScene.setExposure(2.0);
		rayScene.setSamples(9);
		
		ArrayList<Surface> surfaces = new ArrayList<Surface>();
		ArrayList<SurfaceTree> treeNodes = new ArrayList<SurfaceTree>();
		ArrayList<String> names = new ArrayList<String>();
		for (SceneObject sceneObject : s.objects){
			if (sceneObject.mesh != null){
				if (!sceneObject.mesh.equals("Sphere")) {
					Mesh curSurface = new Mesh();
					curSurface.setData(path + sceneObject.mesh);
					curSurface.setShader(shader);
					surfaces.add(curSurface);
					
					String curName = sceneObject.getID().name;
					names.add(curName);
					treeNodes.add(new SurfaceTree(curName, sceneObject.parent, sceneObject.transformation));
				} else {
					Light light = new Light();
					light.setIntensity(new Colord(0.5, 0.5, 0.5));
					light.setPosition(new Vector3d(sceneObject.transformation.mulPos(new Vector3())));
					rayScene.addLight(light);
				}
			}
			
			if (sceneObject instanceof SceneCamera){
				SceneCamera cam = (SceneCamera) sceneObject;
				
				PerspectiveCamera perCam = new PerspectiveCamera();
				perCam.setViewUp(new Vector3d(0, 1, 0));
				perCam.setViewPoint(new Vector3d(cam.transformation.mulPos(new Vector3())));
				perCam.setViewDir(new Vector3d(cam.transformation.mulDir(new Vector3(0, 0, -1))));
				perCam.setprojDistance(2.0);
				
				perCam.initView();
				rayScene.setCamera(perCam);
			}
		}
		
		SurfaceTree root = constructTree(treeNodes);
		ArrayList<Surface> renderableSurfaces = new ArrayList<Surface>();
		for (int i = 0; i < surfaces.size(); i++){
			Surface curSurface = surfaces.get(i);
			double[] m = new double[16];
			for (int j = 0; j < 16; j++){
				m[j] = root.getTransform(names.get(i)).m[j];
			}
			Matrix4d a = new Matrix4d(m);
			Matrix4d aInv = a.clone().invert();
			Matrix4d aTInv = aInv.clone().transpose();
			
			curSurface.setTransformation(a, aInv, aTInv);
			curSurface.appendRenderableSurfaces(renderableSurfaces);
		}		
		Surface surfaceArray[] = new Surface[renderableSurfaces.size()];
		renderableSurfaces.toArray(surfaceArray);
		rayScene.getAccelStruct().build(surfaceArray);
		
		RayTracer.renderImage(rayScene);
		rayScene.getImage().write(RayTracer.sceneWorkspace.getFile() + ".png");
	}

	
	private static SurfaceTree constructTree(ArrayList<SurfaceTree> treeNodes){
		
		//Construct the tree
		SurfaceTree root = null;
		for (SurfaceTree tree : treeNodes){
			System.out.println(tree.parentName);
			if (tree.parentName.equals("World")){
				root = tree;
			} else {
				for (SurfaceTree posPar : treeNodes){
					if (tree.parentName.equals(posPar.name)){
						posPar.addChild(tree);
						break;
					}
				}
			}
		}
		
		//Propagate the transformations
		root.setTransform(new Matrix4());
		
		return root;
	}
	
	private static class SurfaceTree {
		
		private String name = "";
		private String parentName = "";
		private ArrayList<SurfaceTree> children = new ArrayList<SurfaceTree>();
		private Matrix4 myTransform = new Matrix4();
		private Matrix4 worldTransform = new Matrix4();
		
		public SurfaceTree(String name, String parentName, Matrix4 myTransform){
			this.name = name;
			this.parentName = parentName;
			this.myTransform.set(myTransform);
		}
		
		public void addChild(SurfaceTree tree){
			children.add(tree);
		}
		
		public void setTransform(Matrix4 transform){
			worldTransform.set(transform.clone().mulBefore(myTransform));
			for (SurfaceTree child : children){
				child.setTransform(worldTransform);
			}
		}
		
		public Matrix4 getTransform(String surfaceName){
			if (name.equals(surfaceName)){
				return worldTransform;
			} else {
				Matrix4 trans = new Matrix4();
				for (SurfaceTree tree : children){
					trans.mulAfter(tree.getTransform(surfaceName));
				}
				return trans;
			}
		}
	}
}
