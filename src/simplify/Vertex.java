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

    boolean isDirty=false;

    SymmetricMatrix q;

    HashSet<Triangle> myTriangles;
    HashSet<Vertex> myEdges; //edges containing this vertex

    Vertex shortestEdge = null; //pre-calculated shortest edge, reset when vertex dirty

    int index; //index in csg vertex list

    public Vertex(){
        myTriangles = new HashSet<>();
        myEdges = new HashSet<>();
        triangleReferenceStart =0;
        triangleReferenceCount =0;
        isOnABorder =false;
        isDirty=false;
    }

    public Vertex getNext(){ //random next edge
        return (Vertex) myEdges.toArray()[(int)Math.random()* myEdges.size()];
    }

    public Vertex addNext(Vertex v){
        myEdges.add(v);
        return this;
    }

    public Vertex shortestEdge(){
        if(this.shortestEdge !=null){return this.shortestEdge;}
        float shortest=999999;
        Vertex shortestV = null;
        for(Vertex v : myEdges){
            float dist = this.distance(v);
            if(dist<shortest){
                shortest=dist;
                shortestV=v;
            }
        }
        this.shortestEdge = shortestV;
        return this.shortestEdge;
    }

    public void edgesDirty(){
        this.shortestEdge = null;
        this.isDirty=true;
        for(Vertex v : myEdges){
            v.isDirty=true;
        }
    }

    public boolean isBorder(Vertex edge){
        boolean _isBorder = trianglesContainingThisEdge(edge).size()==1;
        isOnABorder = _isBorder || isOnABorder;
        return _isBorder;
    }

    public HashSet<Triangle> trianglesContainingThisEdge(Vertex edge){
        //loop over edges of triangles that contain this vert or the other
        HashSet<Triangle> trianglesWithMe = new HashSet<>();

        for(Triangle triangle : myTriangles){
            if(triangle.containsVertex(edge)){
                trianglesWithMe.add(triangle);
            }
        }

        for(Triangle triangle : edge.myTriangles){
            if(triangle.containsVertex(this)){
                trianglesWithMe.add(triangle);
            }
        }

        return trianglesWithMe;
    }

    public float distance(Vertex v){
        if(v==null) return 0; //TODO correct?
        float x=this.pos.x-v.pos.x;
        float y=this.pos.y-v.pos.y;
        float z=this.pos.z-v.pos.z;
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    public Vertex removeNext(Vertex v){
        myEdges.remove(v);
        return this;
    }

    public Vertex addTriangle(Triangle tri){
        myTriangles.add(tri);
        return this;
    }

    public Vertex removeTriangle(Triangle tri){
        myTriangles.remove(tri);
        return this;
    }
}
