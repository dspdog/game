package simplify;

import org.lwjgl.util.vector.Vector3f;

import java.util.HashSet;

/**
 * Created by user on 2/11/2015.
 */
class Vertex{
    Vector3f pos;
    int triangleReferenceStart =0; //index within Refs array - TriangleIndex*3 + 0/1/2
    int triangleReferenceCount =0;
    boolean isOnABorder =false;
    SymmetricMatrix q;

    HashSet<Triangle> trianglesContainingMe;
    HashSet<Vertex> nextVerts; //edges containing this vertex

    int index; //index in csg vertex list

    public Vertex(){
        trianglesContainingMe = new HashSet<>();
        nextVerts = new HashSet<>();
        triangleReferenceStart =0;
        triangleReferenceCount =0;
        isOnABorder =false;
    }

    public Vertex getNext(){ //random next edge
        return (Vertex)nextVerts.toArray()[(int)Math.random()*nextVerts.size()];
    }

    public Vertex addNext(Vertex v){
        nextVerts.add(v);
        return this;
    }

    public Vertex removeNext(Vertex v){
        nextVerts.remove(v);
        return this;
    }

    public Vertex addTriangle(Triangle tri){
        trianglesContainingMe.add(tri);
        return this;
    }

    public Vertex removeTriangle(Triangle tri){
        trianglesContainingMe.remove(tri);
        return this;
    }
}
