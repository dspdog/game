package factory;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.*;

import shapes.tree.tree;
import utils.CSGUtils;
import utils.CubeMarcher;
import utils.ShaderHelper;
import utils.glHelper;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class GeometryFactory {
    public static void plane(){
        glHelper.prepare2D(1024, 1024);
        int size = 250;
        glBegin(GL11.GL_QUADS);
        glColor3f(0, 0, 0);
        glVertex3f(0, 0, 0);
        glColor3f(1, 0, 0);
        glVertex3f(size, 0, 0);
        glColor3f(1, 0, 1);
        glVertex3f(size, size, 0);
        glColor3f(0, 0, 1);
        glVertex3f(0, size, 0);
        glEnd();
    }

    public static void reticle(int x, int y){
        glHelper.prepare2D(1024, 1024);
        int size = 12;
        int centerX = x-size/2;
        int centerY = y-size/2;
        glBegin(GL11.GL_QUADS);
        glColor3f(0, 0, 0);
        glVertex3f(centerX, centerY, 0);
        glColor3f(1, 0, 0);
        glVertex3f(centerX+size, centerY, 0);
        glColor3f(1, 0, 1);
        glVertex3f(centerX+size, centerY+size, 0);
        glColor3f(0, 0, 1);
        glVertex3f(centerX, centerY+size, 0);
        glEnd();
    }

    public static void plane(Texture tex){
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

    public static void shaderOverlay(int tex, int width, int height){
        glHelper.prepare2D(width, height);
        ShaderHelper.bindShaders();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GeometryFactory.plane2D(tex, width/4, height/4, 0, 0, 0, true);
        ShaderHelper.releaseShaders();
    }

    /*public static void depthOverlay(){
        glHelper.prepare2D(1204, 1024);
        int framebufferID= GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);

        int depthTexture = glGenTextures();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (ByteBuffer) null);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture); // attach it to the framebuffer

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        //glTexParameteri(GL_TEXTURE_2D, GL_LUMINANCE, GL_INTENSITY);

        glDrawBuffer(GL_NONE);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        ShaderHelper.bindShaders();
        GeometryFactory.plane2D(depthTexture, 512, 512, 0, 0, 0, true);
        ShaderHelper.releaseShaders();

    }*/

    public static void plane2D(int tex, int size, float x, float y){
             plane2D(tex, size, size, x, y);
         }
    public static void plane2D(int tex, int width, int height, float x, float y){ plane2D(tex,width,height,x,y,0);}
    public static void plane2D(int tex, int width, int height, float x, float y, float z){ plane2D(tex,width,height,x,y,z,false);}
    public static void plane2D(int tex, int width, int height, float x, float y, float z, boolean reverseY){
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

    public static int[] treeVBOLineHandles(tree theTree){
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

    public static int[] treeVBOQuadHandles(tree theTree){
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

    public interface gridFunction{
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

    public static void drawTrisByVBOHandles(int triangles, int[] handles){
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

    public static void drawQuadsByVBOHandles(int quads, int[] handles){
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
