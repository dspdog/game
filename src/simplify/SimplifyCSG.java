package simplify;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Vertex;
import factory.CSGFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 2/8/2015.
 */
public class SimplifyCSG {

    public static double zeroAreaThresh = -1f;

    public static CSG simplifyCSG(CSG csg){ //TODO make this a CSG-with-LOD class?

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
                if(tri.myArea()>zeroAreaThresh){
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

        ArrayList<mySimplify.Vertex> vertsOnHoles = new ArrayList<mySimplify.Vertex>();

        int maxTris=0;
        int holes = 0;
        for(mySimplify.Vertex vert : simply.vertices){
            if(vert.triangles.size() > maxTris){
                maxTris = vert.triangles.size();
            }

            //detect holes - if # neighbor tris != # neighbor verts

            if(vert.updateHole()){
                vertsOnHoles.add(vert);
                holes++;
            }
        }

        for(mySimplify.Vertex vert : vertsOnHoles){//use these verts like polys to fill the holes
            //System.out.println(vert.getNeighborVertsWithHoles().size() + " NEIGHBS W HOLES");
            if(vert.hasHole){//might be changed by this loop...
                mySimplify.Vertex _prevVert=null;
                int num=0;
                for(mySimplify.Vertex _vert : vert.getNeighborVertsWithHoles()){
                    if(num>0){
                        mySimplify.Triangle newTri = simply.getTriangle(vert.index, _vert.index, _prevVert.index);
                        //simply.triangles.add(newTri);
                    }
                    _prevVert=_vert;
                    num++;
                }
            }



        }

        //getNeighborVertsWithHoles

        System.out.println("Simplifier: Verts - " + vertsNonUnique + " VertsUnique - " + vertsUnique + " Polys " + polys + " Tris " + tris + " Skipped " + skipped + " MaxConx " + maxTris);

        mySimplify.Vertex randomVert;
        int iterations = 1000;
        int vertsDeleted=0;


        for(int i=0; i<iterations; i++){
            do{
                randomVert = simply.vertices.get((int)(Math.random()*simply.vertices.size()));}
            while(randomVert.deleted); //skip deleted verts

            float brownScale = 1/10f;
/*
            if(randomVert.hasHole)brownScale=0;

            randomVert.pos.translate(
                    (float)(Math.random()*brownScale),
                    (float)(Math.random()*brownScale),
                    (float)(Math.random()*brownScale)) ;*/
            if(!randomVert.hasHole){
                randomVert.pos = randomVert.getNeighborVertsAverage();
            }
                    //=randomVert.getNeighborVertsAverage();


        }

        System.out.println("HOLES: "  + holes);

        //Create new CSG from triangles...
        ArrayList<Polygon> polyList = new ArrayList<Polygon>();

        for(mySimplify.Triangle triangle : simply.triangles){
            if(triangle.myArea()>zeroAreaThresh && !triangle.deleted){
                polyList.add(triangle.asPoly()); //TODO vertex normals
            }
        }

        return CSG.fromPolygons(polyList);
    }

    public static void replaceVertWithVert(mySimplify.Vertex oldVert, mySimplify.Vertex newVert, mySimplify simply){
        //add vert to list (unless it already exists)
        if(!simply.vertices.contains(newVert)){
            simply.vertices.add(newVert);
            newVert.index = simply.vertices.size()-1;
        }

        oldVert.deleted=true; //mark oldVert as removed

        for(mySimplify.Triangle tri : oldVert.triangles){ //go thru all triangles in oldVert.triangles
            //remove oldVert, add vert to Tri
            tri.verts.remove(oldVert);
            if(!tri.verts.contains(newVert)){tri.verts.add(newVert);}
        }
    }

    public static String getVertexString(Vertex vertex){
        float roundToNearesth = 10000.0f;
        return  Float.toString(Math.round(vertex.pos.x*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.y*roundToNearesth)/roundToNearesth) +
                Float.toString(Math.round(vertex.pos.z*roundToNearesth)/roundToNearesth);
    }

}
