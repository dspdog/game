//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class Simplify {

	static ArrayList<Triangle> triangles = new ArrayList<>();
	static ArrayList<Vertex> vertices = new ArrayList<>();
	static ArrayList<Ref> refs = new ArrayList<>();

	//
	// Main simplification function
	//
	// target_count  : target nr. of triangles
	// agressiveness : sharpness to increase the threashold.
	//                 5..8 are good numbers
	//                 more iterations yield higher quality
	//

	static void unDeleteAllTriangles(){for(Triangle triangle : triangles){triangle.deleted=false;}}

	static void unDirtyAllTriangles(){for(Triangle triangle : triangles){triangle.dirty=false;}}

	static void resetVertexMatrices(){for(Vertex vertex : vertices){vertex.q=new SymetricMatrix(0.0f);}}

	static void calculateTriangleEdgeErrors(){
		for(Triangle triangle : triangles){
			Vector3f p=new Vector3f(); //unused
			for(int j=0; j<3; j++){
				triangle.err[j]=calculate_error(triangle.vertexIndex[j],triangle.vertexIndex[(j+1)%3],p);
			}
			triangle.minimumErr=Math.min(triangle.err[0], Math.min(triangle.err[1], triangle.err[2]));
		}
	}

	static void calculateTriangleNormals(){ //also updates vertex matrices
		for(Triangle triangle : triangles) {
			Vector3f normal = new Vector3f();
			Vector3f[] vertexPos = new Vector3f[3];
			for(int j=0; j<3; j++){vertexPos[j]=vertices.get(triangle.vertexIndex[j]).p;}
			normal = Vector3f.cross(vertexPos[1].translate(-vertexPos[0].x, -vertexPos[0].y, -vertexPos[0].z), vertexPos[2].translate(-vertexPos[0].x, -vertexPos[0].y, -vertexPos[0].z), normal).normalise(normal);
			triangle.normal = normal;

			for(int j=0; j<3; j++){
				vertices.get(triangle.vertexIndex[j]).q =
						vertices.get(triangle.vertexIndex[j]).q.summedWith(
								new SymetricMatrix(normal.x, normal.y, normal.z,
										-Vector3f.dot(normal, vertexPos[0])));
			}
		}
	}

	void simplify_mesh(int target_count, double agressiveness) //aggressiveness 7
	{
		unDeleteAllTriangles();

		// main iteration loop

		int deleted_triangles=0;
		ArrayList<Boolean> deleted0, deleted1;
		int triangle_count=triangles.size();

		for(int iteration=0; iteration<100; ++iteration) {
			boolean enoughDeleted = triangle_count-deleted_triangles<=target_count;
			if(enoughDeleted)break;

			if(iteration%5==0){update_mesh(iteration);}
			unDirtyAllTriangles();

			//
			// All triangles with edges below the threshold will be removed
			//
			// The following numbers works well for most models.
			// If it does not, try to adjust the 3 parameters
			//
			double threshold = 0.000000001*Math.pow((double) (iteration + 3), agressiveness); //what is this magic?

			// remove vertices & mark deleted triangles
			for(Triangle triangle : triangles){
				if(triangle.minimumErr>threshold) continue;
				if(triangle.deleted) continue;
				if(triangle.dirty) continue;

				for(int j=0; j<3; j++){
					if(triangle.err[j]<threshold) {
						int i0=triangle.vertexIndex[ j     ]; Vertex v0 = vertices.get(i0);
						int i1=triangle.vertexIndex[(j+1)%3]; Vertex v1 = vertices.get(i1);

						// Border check
						if(v0.isOnABorder != v1.isOnABorder)  continue;

						// Compute vertex to collapse to
						Vector3f p = new Vector3f(0,0,0);
						calculate_error(i0,i1,p);

						deleted0 = new ArrayList<Boolean>(v0.triangleReferenceCount); // normals temporarily
						deleted1 = new ArrayList<Boolean>(v1.triangleReferenceCount); // normals temporarily

						for(int d=0; d<v0.triangleReferenceCount; d++){
							deleted0.add(false);
						}
						for(int d=0; d<v1.triangleReferenceCount; d++){
							deleted1.add(false);
						}

						// dont remove if flipped
						if( flipped(p,i0,i1,v0,v1,deleted0) ) continue;
						if( flipped(p,i1,i0,v1,v0,deleted1) ) continue;

						// not flipped, so remove edge
						v0.p=p;
						v0.q=v1.q.summedWith(v0.q);
						int tstart=refs.size();

						update_triangles(i0,v0,deleted0,deleted_triangles);
						update_triangles(i0,v1,deleted1,deleted_triangles);

						int tcount=refs.size()-tstart;

						if(tcount<=v0.triangleReferenceCount)
						{
							// save ram
							if(tcount>0){
								refs.set(v0.triangleReferenceStart, refs.get(tstart));
								//memcpy(&refs[v0.triangleReferenceStart],&refs[triangleReferenceStart],triangleReferenceCount*sizeof(Ref));
							}
						}
						else
							// append
							v0.triangleReferenceStart =tstart;

						v0.triangleReferenceCount =tcount;
						break;
					}

				}
				}

		}
		System.out.println("deleted?" + deleted_triangles);
		// clean up mesh
		compact_mesh();

		// ready
		long timeEnd=System.currentTimeMillis();
		/*printf("%s - %d/%d %d%% removed in %d ms\normal",__FUNCTION__,
				triangle_count-deleted_triangles,
				triangle_count,deleted_triangles*100/triangle_count,
				timeEnd-timeStart);*/

	}




	// compact triangles, compute edge error and build reference list

	public void update_mesh(int iteration){
		if(iteration>0) { // compact triangles
			int dst=0;

			for(int i=0; i<triangles.size(); i++){
				Triangle _tri = triangles.get(i);
				if(!_tri.deleted) {
					triangles.set(dst, _tri);
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
			resetVertexMatrices();
			calculateTriangleNormals();
			calculateTriangleEdgeErrors();
		}

		// Init Reference ID list

		for(Vertex vertex : vertices){
			vertex.triangleReferenceStart =0;
			vertex.triangleReferenceCount =0;
		}

		for(Triangle triangle : triangles) { //first pass for triangleReferenceCount
			for(int j=0; j<3; j++){
				vertices.get(triangle.vertexIndex[j]).triangleReferenceCount++;
			}
		}

		int tstart=0;
		for(Vertex vertex : vertices){
			vertex.triangleReferenceStart =tstart;
			tstart += vertex.triangleReferenceCount;
			vertex.triangleReferenceCount =0;
		}

		// Write References
		refs = new ArrayList<>();//triangles.size()*3);

		for(int r=0; r<triangles.size()*3; r++){
			refs.add(new Ref());
		}

		for(int i=0; i<triangles.size(); i++) {
			Triangle triangle=triangles.get(i);
			for(int j=0; j<3; j++){
				Vertex vertex=vertices.get(triangle.vertexIndex[j]);
				refs.get(vertex.triangleReferenceStart + vertex.triangleReferenceCount).triangleIndex = i;
				refs.get(vertex.triangleReferenceStart + vertex.triangleReferenceCount).vertex0or1or2 = j;
				vertex.triangleReferenceCount++;
			}
		}

		// Identify boundary : vertices[].isOnABorder=0,1
		if( iteration == 0 )
		{
			ArrayList<Integer> vcount,vids;

			for(Vertex vertex : vertices){
				vertex.isOnABorder =false;
			}


			for(Vertex vertex : vertices){
				vcount = new ArrayList<Integer>();
				vids = new ArrayList<Integer>();

				for(int j=0; j<vertex.triangleReferenceCount; j++) {
					int k=refs.get(vertex.triangleReferenceStart +j).triangleIndex;
					Triangle t=triangles.get(k);
					for(int h=0; h<3; h++) {
						int ofs=0,id=t.vertexIndex[h];
						while(ofs<vcount.size()) {
							if(vids.get(ofs)==id)break;
							ofs++;
						}
						if(ofs==vcount.size()) {
							vcount.add(1);
							vids.add(id);
						}
						else{
							vcount.set(ofs, vcount.get(ofs)+1);
						}
					}
				}
				for(int j=0;j<vcount.size(); j++){
					if(vcount.get(j)==1){
						vertices.get(vids.get(j)).isOnABorder =true;
					}
				}
			}
		}
	}


	// Check if a triangle flips when this edge is removed

	boolean flipped(Vector3f p,int i0,int i1,Vertex v0,Vertex v1,ArrayList<Boolean> deleted)
	{
		int bordercount=0;

		for(int k=0; k<v0.triangleReferenceCount; k++){
			Triangle t=triangles.get(refs.get(v0.triangleReferenceStart +k).triangleIndex);
			if(t.deleted)return false;

			int s=refs.get(v0.triangleReferenceStart +k).vertex0or1or2;
			int id1=t.vertexIndex[(s+1)%3];
			int id2=t.vertexIndex[(s+2)%3];

			if(id1==i1 || id2==i1) // delete ?
			{
				bordercount++;
				deleted.set(k,true);
				return false;
			}
			Vector3f d1 = new Vector3f(vertices.get(id1).p.x-p.x,vertices.get(id1).p.y-p.y,vertices.get(id1).p.z-p.z); d1 = d1.normalise(d1);
			Vector3f d2 = new Vector3f(vertices.get(id2).p.x-p.x,vertices.get(id2).p.y-p.y,vertices.get(id2).p.z-p.z); d2 = d2.normalise(d2);
			if(Math.abs(Vector3f.dot(d1, d2))>0.999) return true;

			Vector3f n= new Vector3f();
			Vector3f.cross(d1,d2,n);
			n = n.normalise(n);
			deleted.set(k,false);
			if(Vector3f.dot(n, t.normal)<0.2) return true;
		}

		return false;
	}

	// Update triangle connections and edge error after a edge is collapsed

	void update_triangles(int i0,Vertex vertex, ArrayList<Boolean> deleted,int deleted_triangles)
	{
		Vector3f p=new Vector3f();
		for(int k=0; k<vertex.triangleReferenceCount; k++)
		{
			Ref ref=refs.get(vertex.triangleReferenceStart +k);
			Triangle triangle=triangles.get(ref.triangleIndex);

			if(triangle.deleted)continue;
			if(deleted.get(k)) {
				triangle.deleted = true;
				deleted_triangles++;
				continue;
			}
			triangle.vertexIndex[ref.vertex0or1or2]=i0;
			triangle.dirty = true;
			triangle.err[0] = calculate_error(triangle.vertexIndex[0],triangle.vertexIndex[1],p);
			triangle.err[1] = calculate_error(triangle.vertexIndex[1],triangle.vertexIndex[2],p);
			triangle.err[2] = calculate_error(triangle.vertexIndex[2],triangle.vertexIndex[0],p);
			triangle.minimumErr=Math.min(triangle.err[0], Math.min(triangle.err[1], triangle.err[2]));
			refs.add(ref);
		}
	}

	void compact_mesh(){
		int dst=0;
		int numDeleted=0;
		for(Vertex vertex : vertices){
			vertex.triangleReferenceCount =0;
		}
		for(Triangle triangle : triangles){
			if(!triangle.deleted) {
				triangles.set(dst,triangle); //[dst]=t;
				dst++;
				for(int j=0; j<3; j++){
					vertices.get(triangle.vertexIndex[j]).triangleReferenceCount =1;
				}
			}else{
				numDeleted++;
			}
		}

		System.out.println(numDeleted +" Deleted");

		triangles.subList(dst, triangles.size()).clear(); //triangles.resize(dst);//remove everything after dst

		dst=0;

		for(Vertex vertex : vertices){
			if(vertex.triangleReferenceCount >0) {
				vertex.triangleReferenceStart =dst;
				vertices.get(dst).p=vertex.p;
				dst++;
			}
		}

		for(Triangle triangle : triangles){
			for(int j=0; j<3; j++){
				triangle.vertexIndex[j]=vertices.get(triangle.vertexIndex[j]).triangleReferenceStart;
			}
		}

		vertices.subList(dst, vertices.size()).clear(); //vertices.resize(dst);//remove everything after dst
	}

	// Error between vertex and Quadric

	static double vertex_error(SymetricMatrix q, double x, double y, double z)
	{
		return   q.m[0]*x*x + 2*q.m[1]*x*y + 2*q.m[2]*x*z + 2*q.m[3]*x + q.m[4]*y*y
				+ 2*q.m[5]*y*z + 2*q.m[6]*y + q.m[7]*z*z + 2*q.m[8]*z + q.m[9];
	}

	// Error for one edge

	static double calculate_error(int vertex_index1, int vertex_index2, Vector3f p_result){
		return calculate_error(vertices.get(vertex_index1), vertices.get(vertex_index2), p_result);
	}

	static double calculate_error(Vertex v1, Vertex v2, Vector3f p_result)
	{
		// compute interpolated vertex

		SymetricMatrix q = v1.q.summedWith(v2.q);
		boolean   border = v1.isOnABorder & v2.isOnABorder ;
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
			Vector3f p1=v1.p;
			Vector3f p2=v2.p;
			Vector3f p3=new Vector3f((p1.x+p2.x)/2, (p1.y+p2.y)/2, (p1.z+p2.z)/2); //(p1+p2)/2;
			double error1 = vertex_error(q, p1.x,p1.y,p1.z);
			double error2 = vertex_error(q, p2.x,p2.y,p2.z);
			double error3 = vertex_error(q, p3.x,p3.y,p3.z);
			error = Math.min(error1, Math.min(error2, error3));
			if (Math.abs(error1 - error)<0.000001) p_result=p1;
			if (Math.abs(error2 - error)<0.000001) p_result=p2;
			if (Math.abs(error3 - error)<0.000001) p_result=p3;
		}
		return error;
	}

	// Global Variables & Structures

	class Triangle{
		boolean deleted;
		boolean dirty;
		int vertexIndex[];
		double err[];
		double minimumErr;
		Vector3f normal;

		public Triangle(){
			deleted=false;
			dirty=false;
			vertexIndex = new int[3];
			err = new double[3];
			minimumErr = 999999;
		}
	}

	public class Vertex{
		Vector3f p;
		int triangleReferenceStart =0;
		int triangleReferenceCount =0;
		boolean isOnABorder =false;
		int index=-1;
		SymetricMatrix q;

		public Vertex(){
			triangleReferenceStart =0;
			triangleReferenceCount =0;
			isOnABorder =false;
		}
	}

	class Ref{
		int triangleIndex =0;
		int vertex0or1or2 =0;

		public Ref(){
			triangleIndex =0;
			vertex0or1or2 =0;
		}
	}
}
