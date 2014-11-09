import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import shapes.tree;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class GeometryFactory {

    static void plane(){
        int size = 250;
        glBegin(GL11.GL_QUADS);
        glColor3f(0, 0, 0);
        glVertex3f(0, 0, 0);
        glColor3f(1, 0, 0);
        glVertex3f(size, 0, 0);
        glColor3f(1, 0, 1);
        glVertex3f(size, 0, size);
        glColor3f(0, 0, 1);
        glVertex3f(0, 0, size);
        glEnd();
    }

    static int[] treeVBOHandles(shapes.tree theTree){
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, theTree.vertex_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, theTree.color_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static int[] cubeMarcherVBOHandles(CubeMarcher cm){
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, cm.vertex_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, cm.color_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static void drawCSG(CSG csg){

        for(Polygon poly : csg.getPolygons()){
            if(poly.vertices.size() == 3){
                glBegin(GL11.GL_TRIANGLES);
                glColor3f((float)poly.vertices.get(0).normal.x, (float)poly.vertices.get(0).normal.y, (float)poly.vertices.get(0).normal.z);
                glVertex3f((float)poly.vertices.get(0).pos.x, (float)poly.vertices.get(0).pos.y, (float)poly.vertices.get(0).pos.z);
                glColor3f((float)poly.vertices.get(1).normal.x, (float)poly.vertices.get(1).normal.y, (float)poly.vertices.get(1).normal.z);
                glVertex3f((float)poly.vertices.get(1).pos.x, (float)poly.vertices.get(1).pos.y, (float)poly.vertices.get(1).pos.z);
                glColor3f((float)poly.vertices.get(2).normal.x, (float)poly.vertices.get(2).normal.y, (float)poly.vertices.get(2).normal.z);
                glVertex3f((float)poly.vertices.get(2).pos.x, (float)poly.vertices.get(2).pos.y, (float)poly.vertices.get(2).pos.z);
                glEnd();
            }else if(poly.vertices.size() == 4){
                glBegin(GL11.GL_QUADS);
                glColor3f((float)poly.vertices.get(0).normal.x, (float)poly.vertices.get(0).normal.y, (float)poly.vertices.get(0).normal.z);
                glVertex3f((float)poly.vertices.get(0).pos.x, (float)poly.vertices.get(0).pos.y, (float)poly.vertices.get(0).pos.z);
                glColor3f((float)poly.vertices.get(1).normal.x, (float)poly.vertices.get(1).normal.y, (float)poly.vertices.get(1).normal.z);
                glVertex3f((float)poly.vertices.get(1).pos.x, (float)poly.vertices.get(1).pos.y, (float)poly.vertices.get(1).pos.z);
                glColor3f((float)poly.vertices.get(2).normal.x, (float)poly.vertices.get(2).normal.y, (float)poly.vertices.get(2).normal.z);
                glVertex3f((float)poly.vertices.get(2).pos.x, (float)poly.vertices.get(2).pos.y, (float)poly.vertices.get(2).pos.z);
                glColor3f((float)poly.vertices.get(3).normal.x, (float)poly.vertices.get(3).normal.y, (float)poly.vertices.get(3).normal.z);
                glVertex3f((float)poly.vertices.get(3).pos.x, (float)poly.vertices.get(3).pos.y, (float)poly.vertices.get(3).pos.z);
                glEnd();
            }else{
                System.out.println("VERTS?" + poly.vertices.size());
            }

        }
    }

    static void drawLinesByVBOHandles(int vertices, int[] handles){
        int vertex_size = 3; // X, Y, Z,
        int color_size = 3; // R, G, B,

        int vbo_vertex_handle = handles[0];
        int vbo_color_handle = handles[1];

        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glVertexPointer(vertex_size, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glColorPointer(color_size, GL_FLOAT, 0, 0L);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDrawArrays(GL_LINES, 0, vertices*2);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    static void drawTrisByVBOHandles(int triangles, int[] handles){
        int vertex_size = 3; // X, Y, Z,
        int color_size = 3; // R, G, B,

        int vbo_vertex_handle = handles[0];
        int vbo_color_handle = handles[1];

        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glVertexPointer(vertex_size, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glColorPointer(color_size, GL_FLOAT, 0, 0L);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDrawArrays(GL_TRIANGLES, 0, triangles);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }
}
