//http://voxels.blogspot.com/2014/05/quadric-mesh-simplification-with-source.html

package simplify;

import org.lwjgl.util.vector.Vector3f;
import utils.ArrayUtils;

import java.util.ArrayList;

public class Simplify {
	static ArrayList<Triangle> triangles = new ArrayList<>();
	static ArrayList<Vertex> vertices = new ArrayList<>();
	static ArrayList<Ref> refs = new ArrayList<>();

	static int deleted_triangles;

	//
	// Main simplification function
	//
	// target_count  : target nr. of triangles
	// agressiveness : sharpness to increase the threashold.
	//                 5..8 are good numbers
	//                 more iterations yield higher quality
	//

	static void simplify_mesh(int target_count, double agressiveness) //aggressiveness 7
	{
		SimplifyHelper.unDeleteAllTriangles();

		// main iteration loop

		deleted_triangles=0;
		ArrayList<Boolean> deleted0, deleted1;
		int triangle_count=triangles.size();

		for(int iteration=0; iteration<100; ++iteration) {
			boolean enoughDeleted = triangle_count-deleted_triangles<=target_count;
			if(enoughDeleted)break;

			if(iteration%5==0){update_mesh(iteration);}
			SimplifyHelper.unDirtyAllTriangles();

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
						int v0Index=triangle.vertexIndex[ j     ]; Vertex v0 = vertices.get(v0Index);
						int v1Index=triangle.vertexIndex[(j+1)%3]; Vertex v1 = vertices.get(v1Index);

						// Border check
						if(v0.isOnABorder != v1.isOnABorder)  continue;

						// Compute vertex to collapse to
						Vector3f edgeErrVec = new Vector3f(0,0,0); //TODO check if this is set to anything!
						SimplifyHelper.calculate_error(v0Index, v1Index, edgeErrVec);

						deleted0 = new ArrayList<Boolean>(); // normals temporarily
						deleted1 = new ArrayList<Boolean>(); // normals temporarily

						for(int d=0; d<v0.triangleReferenceCount; d++){
							deleted0.add(false);
						}
						for(int d=0; d<v1.triangleReferenceCount; d++){
							deleted1.add(false);
						}

						// dont remove if flipped
						if( SimplifyHelper.flipped(edgeErrVec, v0Index, v1Index, v0, v1, deleted0) ) continue;
						if( SimplifyHelper.flipped(edgeErrVec, v1Index, v0Index, v1, v0, deleted1) ) continue;

						// not flipped, so remove edge
						v0.pos =edgeErrVec;
						v0.q=v1.q.summedWith(v0.q);
						int tstart=refs.size();

						SimplifyHelper.update_triangles(v0Index, v0, deleted0);
						SimplifyHelper.update_triangles(v0Index, v1, deleted1);

						int tcount=refs.size()-tstart;

						if(tcount<=v0.triangleReferenceCount)
						{
							// save ram
							if(tcount>0){
								for(int _i=0; _i<tcount; _i++){
									refs.set(v0.triangleReferenceStart+_i, refs.get(tstart+_i)); //TODO is this right? (loop needed to emulate this memcpy?)
								}
								//memcpy(&refs[v0.triangleReferenceStart],&refs[triangleReferenceStart],triangleReferenceCount*sizeof(Ref));
							}
						}
						else {
							// append
							v0.triangleReferenceStart = tstart;
						}

						v0.triangleReferenceCount =tcount;
						break;
					}

				}
				}

		}
		//System.out.println("deleted?" + deleted_triangles);
		// clean up mesh
		compact_mesh();
	}




	// compact triangles, compute edge error and build reference list

	public static void update_mesh(int iteration){
		if(iteration>0) { // compact triangles
			int lastTriangleIndex=0;
			for(int i=0; i<triangles.size(); i++){
				Triangle _tri = triangles.get(i);
				if(!_tri.deleted) {
					triangles.set(lastTriangleIndex, _tri);
					lastTriangleIndex++;
				}
			}
			ArrayUtils.resize(triangles,lastTriangleIndex);
		}

		//
		// Init Quadrics by Plane & Edge Errors
		//
		// required at the beginning ( iteration == 0 )
		// recomputing during the simplification is not required,
		// but mostly improves the result for closed meshes
		//
		if( iteration == 0 ) {
			SimplifyHelper.resetVertexMatrices();
			SimplifyHelper.calculateTriangleNormals();
			SimplifyHelper.calculateTriangleEdgeErrors();
		}

		SimplifyHelper.setupRefs();

		if( iteration == 0 ) {
			SimplifyHelper.identifyBoundries();
		}
	}

	static void compact_mesh(){
		int lastTriangleIndex=0;
		int numDeleted=0;
		for(Vertex vertex : vertices){vertex.triangleReferenceCount =0;}
		for(Triangle triangle : triangles){
			if(!triangle.deleted) {
				triangles.set(lastTriangleIndex,triangle);
				lastTriangleIndex++;
				for(int j=0; j<3; j++){
					vertices.get(triangle.vertexIndex[j]).triangleReferenceCount =1;
				}
			}else{
				numDeleted++;
			}
		}

		//System.out.println(numDeleted + " compacted out");
		ArrayUtils.resize(triangles, lastTriangleIndex);

		int lastVertexIndex=0;

		for(Vertex vertex : vertices){
			if(vertex.triangleReferenceCount >0) {
				vertex.triangleReferenceStart =lastVertexIndex;
				vertices.get(lastVertexIndex).pos =vertex.pos;
				lastVertexIndex++;
			}
		}

		for(Triangle triangle : triangles){
			for(int j=0; j<3; j++){
				triangle.vertexIndex[j]=vertices.get(triangle.vertexIndex[j]).triangleReferenceStart;
			}
		}
		ArrayUtils.resize(vertices, lastVertexIndex);
	}
}
