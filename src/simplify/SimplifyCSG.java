package simplify;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vertex;
import org.lwjgl.util.vector.Vector3f;
import simplify.Simplify;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by user on 2/8/2015.
 */
public class SimplifyCSG {
    public static void simplifyCSG(CSG csg){

        //building vertex-index list....

        int vertsNonUnique = 0;
        int vertsUnique = 0;
        int polys = 0;

        int tris=0;

        Simplify simply = new Simplify();
        HashMap<String, Simplify.Vertex> uniqueVerts = new HashMap<>();
        for(Polygon poly : csg.getPolygons()){
            polys++;
            for(Vertex vertex : poly.vertices){
                vertsNonUnique++;
                uniqueVerts.put(getVertexString(vertex), simply.fromVertex(vertex));
            }
        }


        for(Simplify.Vertex vertex : uniqueVerts.values()){
            vertex.index = vertsUnique;
            vertsUnique++;
            simply.vertices.add(vertex);
        }

        tris=0;
        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){
                Vector3f pos0 = new Vector3f((float)poly.vertices.get(0).pos.x,
                                             (float)poly.vertices.get(0).pos.y,
                                             (float)poly.vertices.get(0).pos.z);
                Vector3f posv = new Vector3f((float)poly.vertices.get(v).pos.x,
                                             (float)poly.vertices.get(v).pos.y,
                                             (float)poly.vertices.get(v).pos.z);
                Vector3f posv1 = new Vector3f((float)poly.vertices.get(v+1).pos.x,
                                             (float)poly.vertices.get(v+1).pos.y,
                                             (float)poly.vertices.get(v+1).pos.z);
                tris++;
            }

           // for(Vertex vertex : poly.vertices){
            //    Simplify.Vertex vert = (Simplify.Vertex)uniqueVerts.get(getVertexString(vertex));
            //    int index = vert.index;

        }

        System.out.println("Simplifier: Verts - " + vertsNonUnique + " VertsUnique - " + vertsUnique + " Polys " + polys + " Tris " + tris);

        //get list of Triangles based on Unique Vertices list
    }

    public static String getVertexString(Vertex vertex){
        float roundToNearesth = 10000.0f;
        return  Float.toString(Math.round(vertex.pos.x*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.y*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.z*roundToNearesth)/roundToNearesth);
    }
}
