//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

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
		CopyOnWriteArrayList<Vertex> verts = new CopyOnWriteArrayList<>();
		Vector3f normal;
		int myIndex=-1;
		boolean deleted=false;

		public Polygon asPoly(){
			Polygon poly = new Polygon(verts.get(0).asV3Dvertex(), verts.get(1).asV3Dvertex(), verts.get(2).asV3Dvertex());
			//poly.getStorage().set("hole",verts.get(0).hasHole || verts.get(1).hasHole || verts.get(2).hasHole);
			return poly;
		}

		public Triangle(){
		}

		public void setVerts(int v0, int v1, int v2){
			verts = new CopyOnWriteArrayList<>();
			verts.add(vertices.get(v0));
			verts.add(vertices.get(v1));
			verts.add(vertices.get(v2));
		}

		public float myArea(){

			for(Vertex v : verts){
				if(v.deleted){verts.remove(v);}
			}

			if(verts.size()<3){
				//System.out.println("tri doesnt have 3 verts?");
				return -1; //degenerate triangle
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
		boolean deleted=false;
		boolean hasHole=false;

		public eu.mihosoft.vrl.v3d.Vertex asV3Dvertex(){
			return new eu.mihosoft.vrl.v3d.Vertex(new Vector3d(pos.x, pos.y, pos.z), new Vector3d(0, 0, 0));
		}

		public boolean updateHole(){
			if(numNeighborVerts() != numNeighborTris()){
				hasHole=true;
			}
			return hasHole;
		}

		public HashSet<Vertex> getNeighborVertsWithHoles(){
			HashSet<Vertex> neighborsNextToHoles = new HashSet<>();
			for(Triangle tri : triangles){
				for(Vertex v : tri.verts){
					if(v.hasHole)
					neighborsNextToHoles.add(v);
				}
			}

			neighborsNextToHoles.remove(this);

			return neighborsNextToHoles;
		}

		public HashSet<Vertex> getNeighborVerts(){
			HashSet<Vertex> neighbors = new HashSet<>();
			for(Triangle tri : triangles){
				for(Vertex v : tri.verts){
					neighbors.add(v);
				}
			}

			neighbors.remove(this);

			return neighbors;
		}

		public Vector3f getNeighborVertsAverage(){

			Vector3f total= new Vector3f(pos.x,pos.y,pos.z);
			float scaleDown=1.0f;
			HashSet<Vertex> neighbors = getNeighborVerts();
			for(Vertex vert : neighbors){
				total.translate(vert.pos.x,vert.pos.y,vert.pos.z);
				scaleDown++;
			}

			return new Vector3f(total.x/scaleDown, total.y/scaleDown, total.z/scaleDown);
		}

		public int numNeighborTris(){
			return triangles.size();
		}

		public int numNeighborVerts(){
			return getNeighborVerts().size();
		}

		public Vertex(){

		}
	}

}
