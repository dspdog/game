package simplify;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/11/2015.
 */
class Triangle{
    boolean deleted;
    boolean dirty;
    int vertexIndex[];
    double err[];
    double minimumErr;
    Vector3f normal;

    public Triangle(){
        deleted=false;
        dirty=false;
        vertexIndex = new int[3];
        err = new double[3];
        minimumErr = 999999;
    }
}

