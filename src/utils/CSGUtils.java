package utils;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;

/**
 * Created by user on 2/15/2015.
 */
public class CSGUtils {
    static public boolean doCSGSparkle = true;

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

    public static int[] csgVBOHandles(CSG csg){
        if(doCSGSparkle){
            CSGUtils.shakeNormals(csg);}

        csg.getTriangles();
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, getCSGVertexData(csg), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, getCSGColorData(csg), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static FloatBuffer getCSGVertexData(CSG csg){
        final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(csg.numTriangles * 9);
        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){
                vertex_data.put((float)poly.vertices.get(0).pos.x).
                        put((float)poly.vertices.get(0).pos.y).
                        put((float)poly.vertices.get(0).pos.z).
                        put((float)poly.vertices.get(v).pos.x).
                        put((float)poly.vertices.get(v).pos.y).
                        put((float)poly.vertices.get(v).pos.z).
                        put((float)poly.vertices.get(v+1).pos.x).
                        put((float)poly.vertices.get(v+1).pos.y).
                        put((float)poly.vertices.get(v+1).pos.z);
            }
        }
        vertex_data.flip();
        return vertex_data;
    }

    static FloatBuffer getCSGColorData(CSG csg){
        //TODO make simplifyCSG a wrapper for CSG (call it "SuperCSG") --> load into simplifyCSG, get colors from there
        final FloatBuffer color_data = BufferUtils.createFloatBuffer(csg.numTriangles*9);
        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){
                //fill up buffers
                color_data.put((float)poly.vertices.get(0).normal.x).
                        put((float)poly.vertices.get(0).normal.y).
                        put((float)poly.vertices.get(0).normal.z).
                        put((float)poly.vertices.get(v).normal.x).
                        put((float)poly.vertices.get(v).normal.y).
                        put((float)poly.vertices.get(v).normal.z).
                        put((float)poly.vertices.get(v+1).normal.x).
                        put((float)poly.vertices.get(v+1).normal.y).
                        put((float)poly.vertices.get(v+1).normal.z);
            }
        }
        color_data.flip();
        return color_data;
    }

}
