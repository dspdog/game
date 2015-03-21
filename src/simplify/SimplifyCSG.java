package simplify;

import eu.mihosoft.vrl.v3d.*;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by user on 2/12/2015.
 */
public final class SimplifyCSG{

    public static int vertsNonUnique = 0;
    public static int vertsUnique = 0;
    public static int polys = 0;

    static final ArrayList<Triangle> triangles = new ArrayList<>();
    static final ArrayList<Vertex> vertices = new ArrayList<>();
    private static final Comparator<Vertex> compareEdges = (v1, v2) -> v1.distance(v1.shortestEdge()) > v2.distance(v2.shortestEdge()) ? 1 : -1;
    public static final PriorityQueue<Vertex> vertsByEdgeLength = new PriorityQueue<Vertex>(10000, compareEdges);
    public static final HashMap<String, Vertex> uniqueVerts = new HashMap<>();

    public static CSG myCSG = null;

    public static CSG loadCSG(CSG csg){

        vertsNonUnique = 0;
        vertsUnique = 0;
        polys = 0;

        vertsByEdgeLength.clear();
        vertices.clear();
        triangles.clear();
        uniqueVerts.clear();

        //building vertex-index list....
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
            int size = poly.vertices.size();
            for(int v = 1; v < size - 1; v++) {
                Triangle triangle = new Triangle(
                    uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(0)))),
                    uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v)))),
                    uniqueVerts.get(getVertexString(convertCSGVert2myVert(poly.vertices.get(v+1))))
                );
                if(triangle.myAreaSquared()>0){
                    //building edges
                    triangle.verts[0].addNext(triangle.verts[1]).addTriangle(triangle);
                    triangle.verts[1].addNext(triangle.verts[2]).addTriangle(triangle);
                    triangle.verts[2].addNext(triangle.verts[0]).addTriangle(triangle);
                    triangles.add(triangle);
                }
            }
        }

        myCSG = csg;
        return myCSG;
    }

    public static PriorityQueue<Vertex> getVertsByEdgeLength(){
        vertsByEdgeLength.clear();
        vertsByEdgeLength.addAll(uniqueVerts.values().stream().collect(Collectors.toList()));
        return vertsByEdgeLength;
    }

    static int initialBorders = -1;
    public static int removeBorders(Collection vertexList){
        int borderEdges=0;

        for(Triangle triangle : triangles){
            Vertex edge1 = triangle.verts[0];
            Vertex edge2 = triangle.verts[1];
            Vertex edge3 = triangle.verts[2];

            if(edge1.isBorder(edge2)){
                vertexList.remove(edge1);
                vertexList.remove(edge2);
                borderEdges++;
            }

            if(edge2.isBorder(edge3)){
                vertexList.remove(edge3);
                vertexList.remove(edge2);
                borderEdges++;
            }

            if(edge3.isBorder(edge1)){
                vertexList.remove(edge1);
                vertexList.remove(edge3);
                borderEdges++;
            }
        }

        if(initialBorders==-1){
            initialBorders=borderEdges;
            System.out.println("initial border edges " +  initialBorders);
        }

        System.out.println("border edges " +  borderEdges);
        return borderEdges;
    }

    public static CSG simplifyMyCSG(){
        PriorityQueue<Vertex> prioritizedVerts = getVertsByEdgeLength();
        removeBorders(prioritizedVerts);

        float minEdgeLength=5f;
        int removalsPerIteration = 5000;

        if(prioritizedVerts.size()>removalsPerIteration)
        for(int i=0; i<removalsPerIteration; i++){
            Vertex shortEdgeStart = prioritizedVerts.remove();
            if(shortEdgeStart.isDirty)continue;
            Vertex shortEdgeEnd = shortEdgeStart.shortestEdge();

            if(shortEdgeEnd!=null){
                boolean borderSkip = shortEdgeEnd.isOnABorder || shortEdgeStart.isOnABorder; //(shortEdgeStart.isOnABorder && !shortEdgeEnd.isOnABorder) || (!shortEdgeStart.isOnABorder && shortEdgeEnd.isOnABorder); //xor
                if(!shortEdgeEnd.isDirty && !borderSkip){
                    if(shortEdgeStart.distance(shortEdgeEnd)>minEdgeLength)break;
                    shortEdgeStart.edgesDirty();
                    shortEdgeEnd.edgesDirty();
                    Vector3f avPos = new Vector3f(
                            (shortEdgeStart.pos.x + shortEdgeEnd.pos.x)/2,
                            (shortEdgeStart.pos.y + shortEdgeEnd.pos.y)/2,
                            (shortEdgeStart.pos.z + shortEdgeEnd.pos.z)/2
                    );
                    shortEdgeStart.pos.set(avPos.x, avPos.y, avPos.z);
                    shortEdgeEnd.pos.set(avPos.x, avPos.y, avPos.z);
                }
            }
        }

        CSG simplifiedCSG = CSG.fromPolygons(polygonsFromTriangles());
        return loadCSG(simplifiedCSG);
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
        float roundToNearesth = 100.0f;
        return  Float.toString((int)(vertex.pos.x*roundToNearesth)/roundToNearesth) +
                Float.toString((int)(vertex.pos.y*roundToNearesth)/roundToNearesth) +
                Float.toString((int)(vertex.pos.z*roundToNearesth)/roundToNearesth);
    }
}
