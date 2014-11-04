import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import shapes.tree;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class GeometryFactory {
    static void gridCube(){
        float res = 100;
        glBegin(GL_LINES);
        for(float x=0; x<res; x++){
            for(float y=0; y<res; y++){
                for(float z=0; z<res; z++){
                    glColor3f(x / res, y / res, z / res);
                    glVertex3f(x * 1f, y * 1f, z * 1f);
                    glVertex3f((x + 1) * 1f, y * 1f, z * 1f);
                    glVertex3f(x * 1f, y * 1f, z * 1f);
                    glVertex3f(x * 1f, (y + 1) * 1f, z * 1f);
                }
            }
        }
        glEnd();
    }

    static void gridFlat(){
        float res = 100;
        glBegin(GL_LINES);
        for(float x=0; x<res; x++){
            for(float z=0; z<res; z++){
                float y=0;
                glColor3f(x / res, y / res, z / res);
                glVertex3f(x * 1f, y * 1f, z * 1f);
                glVertex3f((x + 1) * 1f, y * 1f, z * 1f);
                glVertex3f(x * 1f, y * 1f, z * 1f);
                glVertex3f(x * 1f, (y + 1) * 1f, z * 1f);
            }
        }
        glEnd();
    }

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

    /*static void addObj(Object o){
        if(o instanceof tree){
            treeVA((shapes.tree) o);
        }
    }*/

    static void tree(shapes.tree theTree){
        float scale = 0.12f;
        float thickness = 0f;
        for(int i=0; i<theTree.vertices; i++){
            glLineWidth(theTree.getS1(i) / scale * thickness);
            glBegin(GL_LINES);

            glColor3f(
                    theTree.getX1(i) * scale / 100f,
                    theTree.getY1(i) * scale / 100f,
                    theTree.getZ1(i) * scale / 100f);
            glColor3f(
                    theTree.getX2(i) * scale / 100f,
                    theTree.getY2(i) * scale / 100f,
                    theTree.getZ2(i) * scale / 100f);

            glVertex3f(
                    theTree.getX1(i) * scale,
                    theTree.getY1(i) * scale,
                    theTree.getZ1(i) * scale);
            glVertex3f(
                    theTree.getX2(i) * scale,
                    theTree.getY2(i) * scale,
                    theTree.getZ2(i) * scale);

            glEnd();
        }
        
    }

    static int[] treeVBOHandles(shapes.tree theTree){
        float scale = 0.12f;
        int vertices = theTree.vertices;

        int vertex_size = 3; // X, Y, Z,
        int color_size = 3; // R, G, B,

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(vertices * vertex_size * 2);
        FloatBuffer color_data = BufferUtils.createFloatBuffer(vertices * color_size * 2);

        for(int i=0; i<vertices; i++){
            color_data.put(theTree.getX1(i)*scale/100f).
                    put(theTree.getY1(i)*scale/100f).
                    put(theTree.getZ1(i)*scale/100f).
                    put(theTree.getX2(i)*scale/100f).
                    put(theTree.getY2(i)*scale/100f).
                    put(theTree.getZ2(i)*scale/100f);

            vertex_data.put(theTree.getX1(i)*scale).
                    put(theTree.getY1(i)*scale).
                    put(theTree.getZ1(i)*scale).
                    put(theTree.getX2(i)*scale).
                    put(theTree.getY2(i)*scale).
                    put(theTree.getZ2(i)*scale);
        }

        color_data.flip();
        vertex_data.flip();

        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, color_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static void treeVBO(int vertices, int[] handles){

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
}
