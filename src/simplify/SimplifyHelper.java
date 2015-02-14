package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

/**
 * Created by user on 2/11/2015.
 */
public class SimplifyHelper extends Simplify{
    static void unDeleteAllTriangles(){for(Triangle triangle : triangles){triangle.deleted=false;}}
    static void unDirtyAllTriangles(){ for(Triangle triangle : triangles){triangle.dirty=false;}}
    static void resetVertexMatrices(){ for(Vertex vertex : vertices)	 {vertex.q=new SymmetricMatrix(0.0f);}}
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
            for(int j=0; j<3; j++){vertexPos[j]=vertices.get(triangle.vertexIndex[j]).pos;}
            normal = Vector3f.cross(vertexPos[1].translate(-vertexPos[0].x, -vertexPos[0].y, -vertexPos[0].z), vertexPos[2].translate(-vertexPos[0].x, -vertexPos[0].y, -vertexPos[0].z), normal).normalise(normal);
            triangle.normal = normal;

            for(int j=0; j<3; j++){
                vertices.get(triangle.vertexIndex[j]).q =
                        vertices.get(triangle.vertexIndex[j]).q.summedWith(
                                new SymmetricMatrix(normal.x, normal.y, normal.z,
                                        -Vector3f.dot(normal, vertexPos[0])));
            }
        }
    }

    static void identifyBoundries(){
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

    static void setupRefs(){
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
            vertex.triangleReferenceStart = tstart;
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
    }

    //ERROR CALCULATIONS

    // Error between vertex and Quadric

    static double vertex_error(SymmetricMatrix q, double x, double y, double z)
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

        SymmetricMatrix q = v1.q.summedWith(v2.q);
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
            Vector3f p1=v1.pos;
            Vector3f p2=v2.pos;
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

    //COLLAPSE OPERATIONS:

    // Update triangle connections and edge error after a edge is collapsed

    static void update_triangles(int i0,Vertex vertex, ArrayList<Boolean> deleted)
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
            triangle.err[0] = SimplifyHelper.calculate_error(triangle.vertexIndex[0], triangle.vertexIndex[1], p);
            triangle.err[1] = SimplifyHelper.calculate_error(triangle.vertexIndex[1], triangle.vertexIndex[2], p);
            triangle.err[2] = SimplifyHelper.calculate_error(triangle.vertexIndex[2], triangle.vertexIndex[0], p);
            triangle.minimumErr=Math.min(triangle.err[0], Math.min(triangle.err[1], triangle.err[2]));
            refs.add(ref);
        }
    }

    // Check if a triangle flips when this edge is removed

    static boolean flipped(Vector3f edgeErrVec, int v0Index, int v1Index, Vertex v0, Vertex v1, ArrayList<Boolean> deleted)
    {
        int bordercount=0;

        for(int k=0; k<v0.triangleReferenceCount; k++){
            Triangle t=triangles.get(refs.get(v0.triangleReferenceStart +k).triangleIndex);
            if(t.deleted)return false;

            int s=refs.get(v0.triangleReferenceStart +k).vertex0or1or2;
            int id1=t.vertexIndex[(s+1)%3];
            int id2=t.vertexIndex[(s+2)%3];

            if(id1==v1Index || id2==v1Index) // delete ?
            {
                bordercount++;
                deleted.set(k,true);
                return false;
            }
            Vector3f d1 = new Vector3f(vertices.get(id1).pos.x-edgeErrVec.x,vertices.get(id1).pos.y-edgeErrVec.y,vertices.get(id1).pos.z-edgeErrVec.z); d1 = d1.normalise(d1);
            Vector3f d2 = new Vector3f(vertices.get(id2).pos.x-edgeErrVec.x,vertices.get(id2).pos.y-edgeErrVec.y,vertices.get(id2).pos.z-edgeErrVec.z); d2 = d2.normalise(d2);
            if(Math.abs(Vector3f.dot(d1, d2))>0.999) return true;

            Vector3f n= new Vector3f();
            Vector3f.cross(d1,d2,n);
            n = n.normalise(n);
            deleted.set(k,false);
            if(Vector3f.dot(n, t.normal)<0.2) return true;
        }

        return false;
    }
}
