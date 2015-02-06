//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Simplify {

	ArrayList<Triangle> triangles = new ArrayList<>();
	ArrayList<Vertex> vertices = new ArrayList<>();
	ArrayList<Ref> refs = new ArrayList<>();

	// compact triangles, compute edge error and build reference list

	public void update_mesh(int iteration){
		if(iteration>0) // compact triangles
		{
			int dst=0;

			for(int i=0; i<triangles.size(); i++){
				if(!triangles.get(i).deleted)
				{
					triangles.set(dst, triangles.get(i));
					dst++;
				}
			}
			triangles.subList(dst, triangles.size()).clear();
		}

		//
		// Init Quadrics by Plane & Edge Errors
		//
		// required at the beginning ( iteration == 0 )
		// recomputing during the simplification is not required,
		// but mostly improves the result for closed meshes
		//
		if( iteration == 0 ) {
			for(int i=0; i<vertices.size(); i++){
				vertices.get(i).q=new SymetricMatrix(0.0f);
			}

			for(int i=0; i<triangles.size(); i++) {
				Triangle t=triangles.get(i);
				Vector3f n = new Vector3f();;
				Vector3f[] p = new Vector3f[3];

				for(int j=0; j<3; j++){
					p[j]=vertices.get(t.v[j]).p;
				}

				n = Vector3f.cross(p[1].translate(-p[0].x, -p[0].y, -p[0].z), p[2].translate(-p[0].x, -p[0].y, -p[0].z), n).normalise(n);

				t.n=n;

				for(int j=0; j<3; j++){
					vertices.get(t.v[j]).q =
							vertices.get(t.v[j]).q.summedWith(
									new SymetricMatrix(n.x, n.y, n.z,
									-Vector3f.dot(n, p[0])));
				}
			}

			for(int i=0; i<triangles.size(); i++){
				// Calc Edge Error
				Triangle t=triangles.get(i);
				Vector3f p=new Vector3f();

				for(int j=0; j<3; j++){
					t.err[j]=calculate_error(t.v[j],t.v[(j+1)%3],p);
				}
				t.err[3]=Math.min(t.err[0], Math.min(t.err[1], t.err[2]));
			}
		}

		// Init Reference ID list
		for(int i=0; i<vertices.size(); i++) {
			vertices.get(i).tstart=0;
			vertices.get(i).tcount=0;
		}
		for(int i=0; i<triangles.size(); i++) {
			Triangle t=triangles.get(i);
			for(int j=0; j<3; j++){
				vertices.get(t.v[j]).tcount++;
			}
		}
		int tstart=0;
		for(int i=0; i<vertices.size(); i++) {
			Vertex v=vertices.get(i);
			v.tstart=tstart;
			tstart+=v.tcount;
			v.tcount=0;
		}

		// Write References
		refs = new ArrayList<>(triangles.size()*3);
		for(int i=0; i<triangles.size(); i++) {
			Triangle t=triangles.get(i);
			for(int j=0; j<3; j++){
				Vertex v=vertices.get(t.v[j]);
				refs.get(v.tstart+v.tcount).tid=i;
				refs.get(v.tstart+v.tcount).tvertex=j;
				v.tcount++;
			}
		}

		// Identify boundary : vertices[].border=0,1
		if( iteration == 0 )
		{
			ArrayList<Integer> vcount,vids;

			for(int i=0; i<vertices.size(); i++) {
				vertices.get(i).border=0;
			}


			for(int i=0; i<vertices.size(); i++) {
				Vertex v=vertices.get(i);
				vcount = new ArrayList<Integer>();
				vids = new ArrayList<Integer>();

				for(int j=0; j<v.tcount; j++) {
					int k=refs.get(v.tstart+j).tid;
					Triangle t=triangles.get(k);
					for(int h=0; h<3; h++)
					{
						int ofs=0,id=t.v[h];
						while(ofs<vcount.size())
						{
							if(vids.get(ofs)==id)break;
							ofs++;
						}
						if(ofs==vcount.size())
						{
							vcount.add(1);
							vids.add(id);
						}
						else
							vcount.set(ofs, vcount.get(ofs)+1);
					}
				}
				for(int j=0;j<vcount.size(); j++){
					if(vcount.get(j)==1){
						vertices.get(vids.get(j)).border=1;
					}
				}
			}
		}
	}


	// Check if a triangle flips when this edge is removed

	boolean flipped(Vector3f p,int i0,int i1,Vertex v0,Vertex v1,ArrayList<Integer> deleted)
	{
		int bordercount=0;

		for(int k=0; k<v0.tcount; k++){
			Triangle t=triangles.get(refs.get(v0.tstart+k).tid);
			if(t.deleted)continue;

			int s=refs.get(v0.tstart+k).tvertex;
			int id1=t.v[(s+1)%3];
			int id2=t.v[(s+2)%3];

			if(id1==i1 || id2==i1) // delete ?
			{
				bordercount++;
				deleted.set(k,1);
				continue;
			}
			Vector3f d1 = new Vector3f(vertices.get(id1).p.x-p.x,vertices.get(id1).p.y-p.y,vertices.get(id1).p.z-p.z); d1 = d1.normalise(d1);
			Vector3f d2 = new Vector3f(vertices.get(id2).p.x-p.x,vertices.get(id2).p.y-p.y,vertices.get(id2).p.z-p.z); d2 = d2.normalise(d2);
			if(Math.abs(Vector3f.dot(d1, d2))>0.999) return true;

			Vector3f n= new Vector3f();
			Vector3f.cross(d1,d2,n);
			n = n.normalise(n);
			deleted.set(k,0);
			if(Vector3f.dot(n, t.n)<0.2) return true;
		}

		return false;
	}

	// Update triangle connections and edge error after a edge is collapsed

	void update_triangles(int i0,Vertex v,ArrayList<Boolean> deleted,int deleted_triangles)
	{
		Vector3f p=new Vector3f();
		for(int k=0; k<v.tcount; k++)
		{
			Ref r=refs.get(v.tstart+k);
			Triangle t=triangles.get(r.tid);
			if(t.deleted)continue;
			if(deleted.get(k))
			{
				t.deleted = true;
				deleted_triangles++;
				continue;
			}
			t.v[r.tvertex]=i0;
			t.dirty = true;
			t.err[0] = calculate_error(t.v[0],t.v[1],p);
			t.err[1]=calculate_error(t.v[1],t.v[2],p);
			t.err[2]=calculate_error(t.v[2],t.v[0],p);
			t.err[3]=Math.min(t.err[0], Math.min(t.err[1], t.err[2]));
			refs.add(r);
		}
	}

	void compact_mesh(){
		int dst=0;
		for(int i=0; i< vertices.size(); i++) {
			vertices.get(i).tcount=0;
		}
		for(int i=0; i< triangles.size(); i++) {
			if(!triangles.get(i).deleted) {
				Triangle t=triangles.get(i);
				triangles.set(dst,t); //[dst]=t;
				dst++;

				for(int j=0; j<3; j++){
					vertices.get(t.v[j]).tcount=1;
				}
			}
		}

		triangles.subList(dst, triangles.size()).clear(); //triangles.resize(dst);//remove everything after dst

		dst=0;
		for(int i=0; i< vertices.size(); i++) {
			if(vertices.get(i).tcount>0) {
				vertices.get(i).tstart=dst;
				vertices.get(dst).p=vertices.get(i).p;
				dst++;
			}
		}

		for(int i=0; i< triangles.size(); i++) {
			Triangle t=triangles.get(i);

			for(int j=0; j<3; j++){
				t.v[j]=vertices.get(t.v[j]).tstart;
			}
		}

		vertices.subList(dst, vertices.size()).clear(); //vertices.resize(dst);//remove everything after dst
	}

	// Error between vertex and Quadric

	double vertex_error(SymetricMatrix q, double x, double y, double z)
	{
		return   q.m[0]*x*x + 2*q.m[1]*x*y + 2*q.m[2]*x*z + 2*q.m[3]*x + q.m[4]*y*y
				+ 2*q.m[5]*y*z + 2*q.m[6]*y + q.m[7]*z*z + 2*q.m[8]*z + q.m[9];
	}


	// Error for one edge

	double calculate_error(int id_v1, int id_v2, Vector3f p_result)
	{
		// compute interpolated vertex

		SymetricMatrix q = vertices.get(id_v1).q.summedWith(vertices.get(id_v2).q);
		boolean   border = vertices.get(id_v1).border==1 & vertices.get(id_v2).border==1;
		double error=0;
		double det = q.det(0, 1, 2, 1, 4, 5, 2, 5, 7);

		if ( det != 0 && !border )
		{
			// q_delta is invertible
			p_result.x = (float)(-1/det*(q.det(1, 2, 3, 4, 5, 6, 5, 7 , 8)));	// vx = A41/det(q_delta)
			p_result.y = (float)(-1/det*(q.det(0, 2, 3, 1, 5, 6, 2, 7 , 8)));	// vy = A42/det(q_delta)
			p_result.z = (float)(-1/det*(q.det(0, 1, 3, 1, 4, 6, 2, 5,  8)));	// vz = A43/det(q_delta)
			error = vertex_error(q, p_result.x, p_result.y, p_result.z);
		}
		else
		{
			// det = 0 -> try to find best result
			Vector3f p1=vertices.get(id_v1).p;
			Vector3f p2=vertices.get(id_v2).p;
			Vector3f p3=new Vector3f((p1.x+p2.x)/2, (p1.y+p2.y)/2, (p1.z+p2.z)/2); //(p1+p2)/2;
			double error1 = vertex_error(q, p1.x,p1.y,p1.z);
			double error2 = vertex_error(q, p2.x,p2.y,p2.z);
			double error3 = vertex_error(q, p3.x,p3.y,p3.z);
			error = Math.min(error1, Math.min(error2, error3));
			if (error1 == error) p_result=p1;
			if (error2 == error) p_result=p2;
			if (error3 == error) p_result=p3;
		}
		return error;
	}

	// Global Variables & Structures

	class Triangle{
		boolean deleted;
		boolean dirty;
		int v[];
		double err[];
		Vector3f n;

		public Triangle(){
			deleted=false;
			dirty=false;
			v = new int[3];
			err = new double[4];
		}
	}

	class Vertex{
		Vector3f p;
		int tstart=0;
		int tcount=0;
		int border=0;
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
