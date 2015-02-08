//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class mySimplify {

	ArrayList<Triangle> triangles = new ArrayList<>();
	ArrayList<Vertex> vertices = new ArrayList<>();
	ArrayList<Ref> refs = new ArrayList<>();



	public Vertex fromVertex(eu.mihosoft.vrl.v3d.Vertex vertex){
		Vertex v = new Vertex();
		v.p = new Vector3f((float)vertex.pos.x,(float)vertex.pos.y,(float)vertex.pos.z);
		return v;
	}

	public Triangle getTriangle(int v0, int v1, int v2){
		Triangle tri = new Triangle();
		tri.v = new int[]{v0,v1,v2};
		return tri;
	}

	// Global Variables & Structures

	class Triangle{
		boolean deleted;
		boolean dirty;
		int v[];
		double err[];
		Vector3f normal;

		public Triangle(){
			deleted=false;
			dirty=false;
			v = new int[3];
			err = new double[4];
		}
	}

	public class Vertex{
		Vector3f p;
		int tstart=0;
		int tcount=0;
		int border=0;
		int index=-1;
		SymetricMatrix q;

		public Vertex(){
			tstart=0;
			tcount=0;
			border=0;
		}
	}

	class Ref{
		int tid=0;
		int tvertex=0;

		public Ref(){
			tid=0;
			tvertex=0;
		}
	}
}
