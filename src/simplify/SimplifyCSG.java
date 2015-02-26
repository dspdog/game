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

        //TODO skip zero-area triangles

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

        int skippedTris = 0;
        //GETTING TRIS, ADDING TO ARRAY...
        for(Polygon poly : csg.getPolygons()){
            for(int v = 1; v < poly.vertices.size() - 1; v++) {
                Triangle triangle = new Triangle(
                        uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(0)))),
                        uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v)))),
                        uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v + 1))))
                );

                if(triangle.myAreaSquared()>0){
                    //building edges
                    triangle.verts[0].addNext(triangle.verts[1]).addTriangle(triangle);
                    triangle.verts[1].addNext(triangle.verts[2]).addTriangle(triangle);
                    triangle.verts[2].addNext(triangle.verts[0]).addTriangle(triangle);
                    triangles.add(triangle);
                }else{
                    skippedTris++;
                }
            }
        }
        System.out.println("Removed " + skippedTris + " tris");
    }

    public static CSG simplifyCSG(CSG csg){

        //TODO order verts by size or err metric...

        /*
        //http://stackoverflow.com/questions/683041/java-how-do-i-use-a-priorityqueue
        PriorityQueue<Patient> patientQueue = new PriorityQueue<Patient>(10, new Comparator<Patient>() {
            public int compare(Patient patient1, Patient patient2) {
                return (patient1.isEmergencyCase() == patient2.isEmergencyCase()) ? (Integer.valueOf(patient1.getId()).compareTo(patient2.getId()))
                                                                                  : (patient1.isEmergencyCase() ? -1 : 1);
            }
        });

        patientQueue.add(new Patient(1, "Patient1", false));
         */

        loadCSG(csg);
        System.out.println("simp");

        int removedXVerts = 10;

        for(int i=0; i<removedXVerts; i++){
            Vertex randomVertex = vertices.get((int)(Math.random()*vertsUnique));
            Vertex nextVertex = randomVertex.getNext();

            Vector3f avPos = new Vector3f(
                    (randomVertex.pos.x + nextVertex.pos.x)/2,
                    (randomVertex.pos.y + nextVertex.pos.y)/2,
                    (randomVertex.pos.z + nextVertex.pos.z)/2
            );

            randomVertex.pos.set(avPos.x, avPos.y, avPos.z);
            nextVertex.pos.set(avPos.x, avPos.y, avPos.z);
        }

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
        list.add(convertmyVert2CSGVert(triangle.verts[0]));
        list.add(convertmyVert2CSGVert(triangle.verts[1]));
        list.add(convertmyVert2CSGVert(triangle.verts[2]));
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
