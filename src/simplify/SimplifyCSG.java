package simplify;

import eu.mihosoft.vrl.v3d.*;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 2/12/2015.
 */
public class SimplifyCSG extends Simplify{

    public static int vertsNonUnique = 0;
    public static int vertsUnique = 0;
    public static int polys = 0;

    public static void loadCSG(CSG csg){

        vertsNonUnique = 0;
        vertsUnique = 0;
        polys = 0;

        vertices = new ArrayList<>();
        triangles = new ArrayList<>();
        refs = new ArrayList<>();

        //building vertex-index list....

        HashMap<String, Vertex> uniqueVerts = new HashMap<>();
        for(Polygon poly : csg.getPolygons()){
            polys++;
            //GETTING UNIQUE VERTS...
            for(eu.mihosoft.vrl.v3d.Vertex _vertex : poly.vertices){
                Vertex vertex = convertCSGVert2myVert(_vertex);
                vertsNonUnique++;
                uniqueVerts.put(getVertexString(vertex), vertex);
            }
        }

        //PUTTING VERTS INTO ARRAY...
        for(Vertex vertex : uniqueVerts.values()){ //as in CSG.java
            vertsUnique++;
            vertices.add(vertex);
            vertex.index = vertices.size()-1;
        }

        //GETTING TRIS, ADDING TO ARRAY...
        for(Polygon poly : csg.getPolygons()){
            for(int v = 1; v < poly.vertices.size() - 1; v++) {
                int v1 = uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(0)))).index;
                int v2 = uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v)))).index;
                int v3 = uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v+1)))).index;

                Triangle triangle = new Triangle();
                triangle.vertexIndex=new int[]{v1,v2,v3};
                triangles.add(triangle);
            }
        }
    }

    public static CSG simplifyCSG(CSG csg){
        simplify_mesh(1800, 7);
        CSG simplifiedCSG = CSG.fromPolygons(polygonsFromTriangles());
        return simplifiedCSG;
    }

    public static ArrayList<Polygon> polygonsFromTriangles(){
        ArrayList<Polygon> polyList = new ArrayList<>();

        for(Triangle triangle : triangles){
            //if(triangle.deleted) System.out.println("Deleted tri?");
            polyList.add(new Polygon(getCSGVertexListForMyTriangle(triangle)));
        }

        return polyList;
    }

    public static ArrayList<eu.mihosoft.vrl.v3d.Vertex> getCSGVertexListForMyTriangle(Triangle triangle){
        ArrayList<eu.mihosoft.vrl.v3d.Vertex> list = new ArrayList<>();
        list.add(convertmyVert2CSGVert(vertices.get(triangle.vertexIndex[0])));
        list.add(convertmyVert2CSGVert(vertices.get(triangle.vertexIndex[1])));
        list.add(convertmyVert2CSGVert(vertices.get(triangle.vertexIndex[2])));
        return list;
    }

    public static eu.mihosoft.vrl.v3d.Vertex convertmyVert2CSGVert(Vertex _vertex){
        Vector3d normal = new Vector3d(0,0,0); //calculated later by SimplifyHelper.calculateTriangleNormals
        eu.mihosoft.vrl.v3d.Vertex res = new eu.mihosoft.vrl.v3d.Vertex(new Vector3d(_vertex.pos.x, _vertex.pos.y,_vertex.pos.z), normal);
        return res;
    }

    public static Vertex convertCSGVert2myVert(eu.mihosoft.vrl.v3d.Vertex _vertex){
        Vertex res = new Vertex();
        res.pos = new Vector3f((float)_vertex.pos.x, (float)_vertex.pos.y, (float)_vertex.pos.z);
        return res;
    }

    public static String getVertexString(Vertex vertex){
        float roundToNearesth = 1000.0f;
        return  Float.toString(Math.round(vertex.pos.x*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.y*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.z*roundToNearesth)/roundToNearesth);
    }

}
