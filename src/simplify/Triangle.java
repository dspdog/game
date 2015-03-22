package simplify;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/11/2015.
 */
public class Triangle{
    boolean deleted;
    boolean dirty;
    public Vertex[] verts;
    double err[];
    double minimumErr;
    public Vector3d normal;
    double area=0;

    public Triangle(){
        deleted=false;
        dirty=false;
        verts = new Vertex[3];
        err = new double[3];
        minimumErr = 999999;
    }

    public Triangle(Vertex vert1, Vertex vert2, Vertex vert3){
        this();
        verts = new Vertex[]{vert1,vert2,vert3};
    }

    public void getNormal(){
        Vector3d e1 = new Vector3d(verts[2].pos.x - verts[1].pos.x, verts[2].pos.y - verts[1].pos.y, verts[2].pos.z - verts[1].pos.z);
        Vector3d e2 = new Vector3d(verts[0].pos.x - verts[1].pos.x, verts[0].pos.y - verts[1].pos.y, verts[0].pos.z - verts[1].pos.z);
        normal = e1.cross(e2).normalized(); //new Vector3d(vert.normal.x,vert.normal.y,vert.normal.z);
    }

    public void getArea(){
        area=Math.sqrt(myAreaSquared());
    }

    public float myAreaSquared(){
        //http://www.iquilezles.org/blog/?p=1579
        //Area-squared = (2ab + 2bc + 2ca – a² – b² – c²)/16 where a b c are side-lengths-squared
        float a = distSquared(verts[0],verts[1]);
        float b = distSquared(verts[1],verts[2]);
        float c = distSquared(verts[2],verts[0]);
        return (2*a*b + 2*b*c + 2*c*a - a*a - b*b - c*c)/16f;
    }

    private float distSquared(Vertex v1, Vertex v2){
        float x = v1.pos.x-v2.pos.x;
        float y = v1.pos.y-v2.pos.y;
        float z = v1.pos.z-v2.pos.z;
        return x*x+y*y+z*z;
    }

    public boolean containsVertex(Vertex needle){
        for(Vertex haystack : verts){
            if(haystack==needle)return true;
        }
        return false;
    }
}

