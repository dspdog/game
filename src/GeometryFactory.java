import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.newdawn.slick.opengl.*;
import org.lwjgl.opengl.GL11;

import shapes.tree;
import utils.CubeMarcher;
import utils.ShaderHelper;
import utils.glHelper;

import java.nio.FloatBuffer;

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

    static void plane(Texture tex){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex.getTextureID());
        int size = 250;
        glBegin(GL11.GL_QUADS);

        glTexCoord2f(0, 0);
        glColor3f(0, 0, 0);
        glVertex3f(0, 0, 0);

        glTexCoord2f(1, 0);
        glColor3f(1, 1, 1);
        glVertex3f(size, 0, 0);

        glTexCoord2f(1, 1);
        glColor3f(1, 1, 1);
        glVertex3f(size, 0, size);

        glTexCoord2f(0, 1);
        glColor3f(1, 1, 1);
        glVertex3f(0, 0, size);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    static void plane(int tex){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex);
        int size = 550;
        glBegin(GL11.GL_QUADS);

        glTexCoord2f(0, 0);
        glColor3f(0, 0, 0);
        glVertex3f(0, 0, 0);

        glTexCoord2f(1, 0);
        glColor3f(1, 1, 1);
        glVertex3f(size, 0, 0);

        glTexCoord2f(1, 1);
        glColor3f(1, 1, 1);
        glVertex3f(size, 0, size);

        glTexCoord2f(0, 1);
        glColor3f(1, 1, 1);
        glVertex3f(0, 0, size);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }


    static void shaderOverlay(int tex, int width, int height){
        glHelper.prepare2D(width, height);
        ShaderHelper.bindShaders();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GeometryFactory.plane2D(tex, width, height, 0, 0, 0, true);
        ShaderHelper.releaseShaders();
    }

    static void plane2D(int tex, int size, float x, float y){
        plane2D(tex, size, size, x, y);
    }
    static void plane2D(int tex, int width, int height, float x, float y){ plane2D(tex,width,height,x,y,0);}
    static void plane2D(int tex, int width, int height, float x, float y, float z){ plane2D(tex,width,height,x,y,z,false);}
    static void plane2D(int tex, int width, int height, float x, float y, float z, boolean reverseY){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex);
        glBegin(GL11.GL_QUADS);

        glTexCoord2f(0, reverseY ? 1 : 0);
        glColor3f(1, 1, 1);
        glVertex3f(x, y, z);

        glTexCoord2f(1, reverseY ? 1 : 0);
        glColor3f(1, 1, 1);
        glVertex3f(width+x, y, z);

        glTexCoord2f(1, reverseY ? 0 : 1);
        glColor3f(1, 1, 1);
        glVertex3f(width+x, height+y, z);

        glTexCoord2f(0, reverseY ? 0 : 1);
        glColor3f(1, 1, 1);
        glVertex3f(x, height+y,z);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }


    static void sprite(int tex){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex);
        int size = 256;
        glBegin(GL11.GL_QUADS);

        glTexCoord2f(0, 0);
        glColor3f(1, 1, 1);
        glVertex3f(0, 0, 0);

        glTexCoord2f(1, 0);
        glColor3f(1, 1, 1);
        glVertex3f(glHelper.cameraXVector.x * size, glHelper.cameraXVector.y * size, glHelper.cameraXVector.z * size);

        glTexCoord2f(1, 1);
        glColor3f(1, 1, 1);
        glVertex3f(glHelper.cameraXVector.x * size + glHelper.cameraYVector.x * size,
                    glHelper.cameraXVector.y * size + glHelper.cameraYVector.y * size,
                    glHelper.cameraXVector.z * size + glHelper.cameraYVector.z * size);


        glTexCoord2f(0, 1);
        glColor3f(1, 1, 1);
        glVertex3f(glHelper.cameraYVector.x * size, glHelper.cameraYVector.y * size, glHelper.cameraYVector.z * size);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    static int[] treeVBOLineHandles(shapes.tree theTree){
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, tree.vertex_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, tree.color_data, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static int[] treeVBOQuadHandles(shapes.tree theTree){
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, tree.vertex_data2, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, tree.color_data2, GL_STATIC_DRAW);
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
            glBegin(GL_TRIANGLE_FAN); //http://stackoverflow.com/questions/8043923/gl-triangle-fan-explanation
            glColor3f((float)poly.vertices.get(0).normal.x, (float)poly.vertices.get(0).normal.y, (float)poly.vertices.get(0).normal.z);
            glVertex3f((float)poly.vertices.get(0).pos.x, (float)poly.vertices.get(0).pos.y, (float)poly.vertices.get(0).pos.z);
            for(int v=1; v<poly.vertices.size(); v++){
                glColor3f((float)poly.vertices.get(v).normal.x, (float)poly.vertices.get(v).normal.y, (float)poly.vertices.get(v).normal.z);
                glVertex3f((float)poly.vertices.get(v).pos.x, (float)poly.vertices.get(v).pos.y, (float)poly.vertices.get(v).pos.z);
            }
            glEnd();
        }
    }

    static int[] csgVBOHandles(CSG csg){

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
        final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(csg.numTriangles*9);
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

    interface gridFunction{
        float getValue(int x, int y);
    }

    static int[] gridVBOHandles(gridFunction d){
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, functionGridVertexData(d), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, functionGridColorData(d), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    static FloatBuffer functionGridVertexData(gridFunction d){

        int step = 1;
        final FloatBuffer vert_data = BufferUtils.createFloatBuffer((256/step * 256/step)*9*2);

        for(int x=0; x<256; x+=step){
            for(int z=0; z<256; z+=step){

                //glColor3f(d.getValue(x, z)/32f,d.getValue(x+step, z)/32f,d.getValue(x, z+step)/32f); //color as normal

                //fill up buffers
                vert_data.put(x).put(d.getValue(x, z)).put(z)
                         .put(x).put(d.getValue(x, z+step)).put(z+step)
                         .put(x+step).put(d.getValue(x+step, z)).put(z)

                         .put(x + step).put(d.getValue(x+step, z)).put(z)
                         .put(x).put(d.getValue(x, z+step)).put(z+step)
                         .put(x+step).put(d.getValue(x+step, z+step)).put(z+step);
            }
        }

        vert_data.flip();
        return vert_data;
    }

    static FloatBuffer functionGridColorData(gridFunction d){

        int step = 1;
        final FloatBuffer color_data = BufferUtils.createFloatBuffer((256/step * 256/step)*9*2);

        for(int x=0; x<256; x+=step){
            for(int z=0; z<256; z+=step){

                //glColor3f(d.getValue(x, z)/32f,d.getValue(x+step, z)/32f,d.getValue(x, z+step)/32f); //color as normal

                //fill up buffers
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);
                color_data.put(d.getValue(x, z)/32f).put(d.getValue(x+step, z)/32f).put(d.getValue(x, z+step)/32f);

            }
        }

        color_data.flip();
        return color_data;
    }





    static void drawFunctionGrid(gridFunction d){
        int step = 1;
        for(int x=0; x<256; x+=step){
            for(int z=0; z<256; z+=step){
                glBegin(GL_TRIANGLES);
                    glColor3f(d.getValue(x, z)/32f,d.getValue(x+step, z)/32f,d.getValue(x, z+step)/32f); //color as normal

                    glVertex3f(x, d.getValue(x, z), z);
                    glVertex3f(x, d.getValue(x,z+step),z+step);
                    glVertex3f(x+step, d.getValue(x+step,z),z);

                    glVertex3f(x+step, d.getValue(x+step,z),z);
                    glVertex3f(x, d.getValue(x,z+step),z+step);
                    glVertex3f(x+step, d.getValue(x+step,z+step),z+step);

                glEnd();
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

        glDrawArrays(GL_TRIANGLES, 0, triangles*vertex_size);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    static void drawQuadsByVBOHandles(int quads, int[] handles){
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

        glDrawArrays(GL_QUADS, 0, 4*quads*vertex_size);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }
}
