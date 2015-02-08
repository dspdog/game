package simplify;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vertex;

import java.util.HashMap;

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

        mySimplify simply = new mySimplify();

        //GETTING UNIQUE VERTICES...

        HashMap<String, mySimplify.Vertex> uniqueVerts = new HashMap<>();
        for(Polygon poly : csg.getPolygons()){
            polys++;
            for(Vertex vertex : poly.vertices){
                vertsNonUnique++;
                uniqueVerts.put(getVertexString(vertex), simply.fromVertex(vertex));
            }
        }

        for(mySimplify.Vertex vertex : uniqueVerts.values()){
            vertex.index = vertsUnique;
            vertsUnique++;
            simply.vertices.add(vertex);
        }

        //DEFINING TRIANGLES IN TERMS OF UNIQUE VERT INDICES...
        //+setting triangles-per-vertex

        tris=0;
        int skipped=0;
        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){//super basic poly-to-triangles function. doesnt affect triangles, turns quads into 2 tris, etc...
                int index0 = uniqueVerts.get(getVertexString(poly.vertices.get(0))).index;
                int indexv = uniqueVerts.get(getVertexString(poly.vertices.get(v))).index;
                int indexv1 = uniqueVerts.get(getVertexString(poly.vertices.get(v+1))).index;

                mySimplify.Triangle tri = simply.getTriangle(index0, indexv, indexv1);
                if(tri.myArea()>0.001f){
                    simply.triangles.add(tri);
                    tri.myIndex=tris;
                    tris++;

                    //UPDATE THE VERTS TO HAVE REFS TO THIS TRIANGLE
                    simply.vertices.get(index0).triangles.add(tri);
                    simply.vertices.get(indexv).triangles.add(tri);
                    simply.vertices.get(indexv1).triangles.add(tri);
                }else{
                    skipped++;
                }
            }
        }
        
        int maxTris=0;
        for(mySimplify.Vertex vert : simply.vertices){
            if(vert.triangles.size() > maxTris){
                maxTris = vert.triangles.size();
            }
        }

        System.out.println("Simplifier: Verts - " + vertsNonUnique + " VertsUnique - " + vertsUnique + " Polys " + polys + " Tris " + tris + " Skipped " + skipped + " MaxConx " + maxTris);

        //TODO perform some decimations...
    }

    public static String getVertexString(Vertex vertex){
        float roundToNearesth = 10000.0f;
        return  Float.toString(Math.round(vertex.pos.x*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.y*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.z*roundToNearesth)/roundToNearesth);
    }

}
