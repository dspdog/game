package utils;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import simplify.SuperCSG;
import simplify.Triangle;

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
        SuperCSG supercsg = new SuperCSG(csg);
        final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(supercsg.triangles.size() * 9);
        for(simplify.Triangle triangle : supercsg.triangles){
                vertex_data.put(triangle.verts[0].pos.x).
                            put(triangle.verts[0].pos.y).
                            put(triangle.verts[0].pos.z).
                            put(triangle.verts[1].pos.x).
                            put(triangle.verts[1].pos.y).
                            put(triangle.verts[1].pos.z).
                            put(triangle.verts[2].pos.x).
                            put(triangle.verts[2].pos.y).
                            put(triangle.verts[2].pos.z);
        }
        vertex_data.flip();
        return vertex_data;
    }

    static FloatBuffer getCSGColorData(CSG csg){
        SuperCSG supercsg = new SuperCSG(csg); //TODO make a superCSG.coloringMode Enum -- per vertex, per face, per vertex-face?

        final FloatBuffer color_data = BufferUtils.createFloatBuffer(supercsg.triangles.size() * 9);
        for(simplify.Triangle triangle : supercsg.triangles){
            color_data.put(triangle.verts[0].color.x).
                       put(triangle.verts[0].color.y).
                       put(triangle.verts[0].color.z).
                       put(triangle.verts[1].color.x).
                       put(triangle.verts[1].color.y).
                       put(triangle.verts[1].color.z).
                       put(triangle.verts[2].color.x).
                       put(triangle.verts[2].color.y).
                       put(triangle.verts[2].color.z);
        }
        color_data.flip();
        return color_data;
    }

}
