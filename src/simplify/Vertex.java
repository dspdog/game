package simplify;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/11/2015.
 */
class Vertex{
    Vector3f pos;
    int triangleReferenceStart =0; //index within Refs array - TriangleIndex*3 + 0/1/2
    int triangleReferenceCount =0;
    boolean isOnABorder =false;
    SymmetricMatrix q;

    public Vertex(){
        triangleReferenceStart =0;
        triangleReferenceCount =0;
        isOnABorder =false;
    }
}
