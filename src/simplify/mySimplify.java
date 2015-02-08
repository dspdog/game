//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;

public class mySimplify {

	ArrayList<Triangle> triangles = new ArrayList<>();
	ArrayList<Vertex> vertices = new ArrayList<>();

	public Vertex fromVertex(eu.mihosoft.vrl.v3d.Vertex vertex){
		Vertex v = new Vertex();
		v.pos = new Vector3f((float)vertex.pos.x,(float)vertex.pos.y,(float)vertex.pos.z);
		return v;
	}

	public Triangle getTriangle(int v0, int v1, int v2){
		Triangle tri = new Triangle();
		tri.setVerts(v0,v1,v2);// = new int[]{v0,v1,v2};
		return tri;
	}

	// Global Variables & Structures

	class Triangle{
		ArrayList<mySimplify.Vertex> verts = new ArrayList<>();
		Vector3f normal;
		int myIndex=-1;

		public Triangle(){
		}

		public void setVerts(int v0, int v1, int v2){
			verts = new ArrayList<>();
			verts.add(vertices.get(v0));
			verts.add(vertices.get(v1));
			verts.add(vertices.get(v2));
		}

		public float myArea(){
			if(verts.size()<3){
				System.out.println("tri doesnt have 3 verts?");
				return 0;
			}else{
				float sideA = dist(vertices.get(0), vertices.get(1));
				float sideB = dist(vertices.get(1), vertices.get(2));
				float sideC = dist(vertices.get(2), vertices.get(0));
				float semi = (sideA+sideB+sideC)/2f;
				float area = (float)Math.sqrt(semi*(semi-sideA)*(semi-sideB)*(semi-sideC));

				return area;
			}
		}

		public float dist(Vertex v1, Vertex v2){
			float dx = v1.pos.x - v2.pos.x;
			float dy = v1.pos.y - v2.pos.y;
			float dz = v1.pos.z - v2.pos.z;
			return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
	}

	public class Vertex{
		HashSet<Triangle> triangles = new HashSet<>();

		Vector3f pos;
		int index=-1;

		public Vertex(){

		}
	}

}
