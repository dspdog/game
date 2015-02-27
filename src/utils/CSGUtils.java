package utils;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;

/**
 * Created by user on 2/15/2015.
 */
public class CSGUtils {
    public static void shakeNormals(CSG csg){
        for(Polygon poly : csg.getPolygons()){
            int numVerts = poly.vertices.size();
            for(int i=0; i<numVerts; i++){
                Vertex vert = poly.vertices.get(i);
                Vector3d normal = new Vector3d(vert.normal.x,vert.normal.y,vert.normal.z);
                float _scale = 1.0f;
                normal = normal.plus(new Vector3d(Math.random(), Math.random(), Math.random()).plus(new Vector3d(-0.5,-0.5,-0.5)).times(1*_scale)).normalized();
                vert.normal=normal;
            }
        }
    }

}
