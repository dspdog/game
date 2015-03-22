package utils;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/15/2015.
 */
public class CSGUtils {
    public static void shakeNormals(CSG csg){
        for(Polygon poly : csg.getPolygons()){
            int numVerts = poly.vertices.size();
            for(int i=0; i<numVerts; i++){
                Vertex vert0 = poly.vertices.get((i+numVerts-1)%numVerts);
                Vertex vert1 = poly.vertices.get(i);
                Vertex vert2 = poly.vertices.get((i+1)%numVerts);
                Vector3d e1 = new Vector3d(vert2.pos.x - vert1.pos.x, vert2.pos.y - vert1.pos.y, vert2.pos.z - vert1.pos.z);
                Vector3d e2 = new Vector3d(vert0.pos.x - vert1.pos.x, vert0.pos.y - vert1.pos.y, vert0.pos.z - vert1.pos.z);

                Vector3d normal = e1.cross(e2).normalized(); //new Vector3d(vert.normal.x,vert.normal.y,vert.normal.z);
                float _scale = 1.0f;
                normal = normal.plus(new Vector3d(Math.random(), Math.random(), Math.random()).plus(new Vector3d(-0.5,-0.5,-0.5)).times(1*_scale)).normalized();
                vert1.normal=normal;
            }
        }
    }
}
