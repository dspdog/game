package simplify;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/11/2015.
 */
class Triangle{
    boolean deleted;
    boolean dirty;
    Vertex[] verts;
    double err[];
    double minimumErr;
    Vector3f normal;

    public Triangle(){
        deleted=false;
        dirty=false;
        verts = new Vertex[3];
        err = new double[3];
        minimumErr = 999999;
    }

    public float myArea(){
        //http://www.iquilezles.org/blog/?p=1579
        //Area-squared = (2ab + 2bc + 2ca – a² – b² – c²)/16 where a b c are side-lengths-squared
        float a = distSquared(verts[0],verts[1]);
        float b = distSquared(verts[1],verts[2]);
        float c = distSquared(verts[2],verts[0]);

        return (float)Math.sqrt((2*a*b + 2*b*c + 2*c*a - a*a - b*b - c*c)/16f);
    }

    private float distSquared(Vertex v1, Vertex v2){
        float x = v1.pos.x-v2.pos.x;
        float y = v1.pos.y-v2.pos.y;
        float z = v1.pos.z-v2.pos.z;
        return x*x+y*y+z*z;
    }
}

