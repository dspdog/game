package factory;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.*;
import org.lwjgl.opengl.GL11;
import shapes.cloud.particle;
import shapes.cloud.sphCloud;
import world.scene;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static void cloud(sphCloud cloud, int texId){
        long time = System.currentTimeMillis();
        float r,g,b;
        if(sphCloud.gridInited)
        for(particle p : cloud.theParticles){
            if(p!=null){
                r = (p.pos.x-sphCloud.lowerCorner.x)/(sphCloud.upperCorner.x - sphCloud.lowerCorner.x);
                g = (p.pos.y-sphCloud.lowerCorner.y)/(sphCloud.upperCorner.y - sphCloud.lowerCorner.y);
                b = (p.pos.z-sphCloud.lowerCorner.z)/(sphCloud.upperCorner.z - sphCloud.lowerCorner.z);
               // float d = p.myNeighbors.size()/12f;



                glColor3f(r, g, b);
                drawCircle(p, true);
            }
        }
        glColor3f(0f, 0f, 0f);
        drawBox(sphCloud.lowerCorner, sphCloud.upperCorner);
        glColor3f(0.5f, 0f, 0f);
        drawBox(sphCloud.lowerCornerBoundsFinal, sphCloud.upperCornerBoundsFinal);
       // glColor3f(0f, 0f, 0f);
       // drawCloudBoundsBox();
    }

    static void drawBox(Vector3f upperCorner, Vector3f lowerCorner){
        glBegin(GL11.GL_LINES);
        glVertex3f(lowerCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(lowerCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(lowerCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(lowerCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, lowerCorner.y, lowerCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, upperCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, lowerCorner.z);
        glVertex3f(upperCorner.x, upperCorner.y, lowerCorner.z);
        glVertex3f(lowerCorner.x, upperCorner.y, lowerCorner.z);
        glEnd();
    }

    static void drawCircle(particle p, boolean cartoonMode){

        float fatness = 1.35f;

        glVertex3f(p.pos.x, p.pos.y, p.pos.z);
        glBegin(GL11.GL_TRIANGLE_FAN);
        int segs = 6;
        float scale = sphCloud.gridSize/4f*fatness;
        for(int i=0; i<segs; i++){
            float sin = (float)(Math.sin(i * 2 * Math.PI / segs))*scale;
            float cos = (float)(Math.cos(i * 2 * Math.PI / segs))*scale;
            glVertex3f(p.pos.x+(scene.cameraYVector.x*sin+scene.cameraXVector.x*cos),
                       p.pos.y+(scene.cameraYVector.y*sin+scene.cameraXVector.y*cos),
                       p.pos.z+(scene.cameraYVector.z*sin+scene.cameraXVector.z*cos));
        }
        glEnd();

        if(cartoonMode){
            scale = sphCloud.gridSize/4f*1.1f*fatness;
            float s = -0.25f;
            glColor3f(0, 0, 0);
            glVertex3f(p.pos.x+scene.cameraZVector.x*s, p.pos.y+scene.cameraZVector.y*s, p.pos.z+scene.cameraZVector.z*s);
            glBegin(GL11.GL_TRIANGLE_FAN);
            for(int i=0; i<segs; i++){
                float sin = (float)(Math.sin(i * 2 * Math.PI / segs))*scale;
                float cos = (float)(Math.cos(i * 2 * Math.PI / segs))*scale;
                glVertex3f(p.pos.x+(scene.cameraYVector.x*sin+scene.cameraXVector.x*cos)+scene.cameraZVector.x*s,
                        p.pos.y+(scene.cameraYVector.y*sin+scene.cameraXVector.y*cos)+scene.cameraZVector.y*s,
                        p.pos.z+(scene.cameraYVector.z*sin+scene.cameraXVector.z*cos)+scene.cameraZVector.z*s);
            }
            glEnd();
        }
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

    public static void plane(int texid){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texid);
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
        glVertex3f(size, size, 0);

        glTexCoord2f(0, 1);
        glColor3f(1, 1, 1);
        glVertex3f(0, size, 0);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    public static FloatBuffer[] getCSGVertexData(CSG csg, int tris){
        final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(tris*9);
        final FloatBuffer color_data = BufferUtils.createFloatBuffer(tris*9);
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
        vertex_data.flip();
        color_data.flip();
        return new FloatBuffer[]{vertex_data, color_data};
    }

    public static int getTriangles(CSG csg){
        int tris=0;

        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){
                tris++;
            }
        }
        return tris;
    }


    public interface gridFunction{
        float getValue(float x, float y, float t);
    }

   public static void findUniqueVerts(FloatBuffer[] fbs){

       FloatBuffer posBuffer = fbs[0];
       FloatBuffer colorBuffer = fbs[1];

       HashMap<Integer, float[]> hashedVertColorList = new HashMap<>();
       HashMap<Integer, ArrayList<Integer>> orderedTriangleListOfHashes = new HashMap<>();
       hashedVertColorList.clear();
       orderedTriangleListOfHashes.clear();
       int dataPts = fbs[0].limit();
       int dups = 0;
       int nondups = 0;
       int triangleNo =0;
       for(int i=0 ;i<dataPts-3; i+=3){
           float x=posBuffer.get(i);
           float y=posBuffer.get(i + 1);
           float z=posBuffer.get(i + 2);

           float r=colorBuffer.get(i);
           float g=colorBuffer.get(i + 1);
           float b=colorBuffer.get(i + 2);

           int hash = vertexHash(x, y, z);
           triangleNo = (int)(i / 3);
           if(!orderedTriangleListOfHashes.containsKey(triangleNo)){
               orderedTriangleListOfHashes.put(triangleNo, new ArrayList<Integer>());
           }
           orderedTriangleListOfHashes.get(triangleNo).add(hash);

           if(hashedVertColorList.containsKey(hash)){
               dups++;
           }else{
               nondups++;
               hashedVertColorList.put(hash, new float[]{x,y,z, r,g,b});
           }
       }

       int reducedIndex=0;
       ArrayList<Integer> orderedHashList = new ArrayList<>();
       ArrayList<Vector3f> orderedVertList = new ArrayList<>(); ///////////////////////////////////////////
       ArrayList<Vector3f> orderedColorList = new ArrayList<>(); //////////////////////////////////////////

       HashMap<Integer, Integer> reduceIndexByHash = new HashMap<>();

       for (Map.Entry<Integer, float[]> entry : hashedVertColorList.entrySet()) {
           orderedHashList.add(entry.getKey());
           reduceIndexByHash.put(entry.getKey(), reducedIndex);
           orderedVertList.add(new Vector3f(entry.getValue()[0],entry.getValue()[1],entry.getValue()[2])); //xyz
           orderedColorList.add(new Vector3f(entry.getValue()[3],entry.getValue()[4],entry.getValue()[5])); //rgb
           reducedIndex++;
       }

       ArrayList<Vector3f> triangleListOfVertIndices = new ArrayList<>();//////////////////////////////////

       for (Map.Entry<Integer, ArrayList<Integer>> hashesHolder : orderedTriangleListOfHashes.entrySet()) {
           if(hashesHolder.getValue().size()>2){
               triangleListOfVertIndices.add(new Vector3f(
                       reduceIndexByHash.get(hashesHolder.getValue().get(0)),
                       reduceIndexByHash.get(hashesHolder.getValue().get(1)),
                       reduceIndexByHash.get(hashesHolder.getValue().get(2))));
           }else{
               //error?
           }
       }

       //System.out.println("DUPES " + dups + " unique " + nondups + "tris " + triangleNo);
   }

    private static int vertexHash(float x, float y, float z){
        //http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf
        // hash(x,y,z) = ( x p1 xor y p2 xor z p3) mod n
        // where p1, p2, p3 are large prime numbers, in
        // our case 73856093, 19349663, 83492791

        return (int)(x*73856093)^(int)(y*19349663)^(int)(z*83492791);
    }

    public static int[] VBOHandles(FloatBuffer[] fbs){

        //findUniqueVerts(fbs);

        //FloatBuffer[] fbs = functionGridVertexData(d);

        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, fbs[0], GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, fbs[1], GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle};
    }

    public static int gridSize = 256;
    public static int gridStep = 2;

    public static FloatBuffer[] functionGridVertexData(gridFunction d, float t, float offsetx, float offsetz, Vector3f color){
        int step = gridStep;
        float r = color.x/32f;
        float g = color.y/32f;
        float b = color.z/32f;
        final FloatBuffer vert_data = BufferUtils.createFloatBuffer((gridSize/step * gridSize/step)*9*2);
        final FloatBuffer color_data = BufferUtils.createFloatBuffer((gridSize/step * gridSize/step)*9*2);

        for(float _x=0; _x<gridSize; _x+=step){
            for(float __z=0; __z<gridSize; __z+=step){

                float _z = __z*4;
                float _zs = (__z+step)*4;
                //xz
                float x = (float)Math.cos(Math.PI*2/gridSize*_x)*_z + gridSize/2 ;
                float z = (float)Math.sin(Math.PI * 2 / gridSize * _x)*_z + gridSize/2 ;
                //xz+
                float x2 = (float)Math.cos(Math.PI*2/gridSize*_x)*_zs + gridSize/2 ;
                float z2 = (float)Math.sin(Math.PI*2/gridSize*_x)*_zs + gridSize/2 ;
                //x+z
                float x3 = (float)Math.cos(Math.PI*2/gridSize*(_x+step))*_z + gridSize/2 ;
                float z3 = (float)Math.sin(Math.PI*2/gridSize*(_x+step))*_z + gridSize/2 ;
                //x+z+
                float x4 = (float)Math.cos(Math.PI*2/gridSize*(_x+step))*_zs + gridSize/2 ;
                float z4 = (float)Math.sin(Math.PI*2/gridSize*(_x+step))*_zs + gridSize/2 ;

                //4 xz's, in polar coords

                vert_data.put(x).put(d.getValue(x+offsetx, z+offsetz,t)).put(z)
                         .put(x2).put(d.getValue(x2+offsetx, z2+offsetz,t)).put(z2)
                         .put(x3).put(d.getValue(x3+offsetx, z3+offsetz,t)).put(z3)

                         .put(x3).put(d.getValue(x3+offsetx, z3+offsetz,t)).put(z3)
                         .put(x2).put(d.getValue(x2+offsetx, z2+offsetz,t)).put(z2)
                         .put(x4).put(d.getValue(x4+offsetx, z4+offsetz,t)).put(z4);



                color_data.put(d.getValue(x+offsetx, z+offsetz,t)* r).put(d.getValue(x+offsetx, z+offsetz,t)* g).put(d.getValue(x+offsetx, z+offsetz,t)* b);
                color_data.put(d.getValue(x2+offsetx, z2+offsetz,t)* r).put(d.getValue(x2+offsetx, z2+offsetz,t)* g).put(d.getValue(x2+offsetx, z2+offsetz,t)* b);
                color_data.put(d.getValue(x3+offsetx, z3+offsetz,t)* r).put(d.getValue(x3+offsetx, z3+offsetz,t)* g).put(d.getValue(x3+offsetx, z3+offsetz,t)* b);
                color_data.put(d.getValue(x3+offsetx, z3+offsetz,t)* r).put(d.getValue(x3+offsetx, z3+offsetz,t)* g).put(d.getValue(x3+offsetx, z3+offsetz,t)* b);
                color_data.put(d.getValue(x2+offsetx, z2+offsetz,t)* r).put(d.getValue(x2+offsetx, z2+offsetz,t)* g).put(d.getValue(x2+offsetx, z2+offsetz,t)* b);
                color_data.put(d.getValue(x4+offsetx, z4+offsetz,t)* r).put(d.getValue(x4+offsetx, z4+offsetz,t)* g).put(d.getValue(x4+offsetx, z4+offsetz,t)* b);

            }
        }
        vert_data.flip();
        color_data.flip();
        return new FloatBuffer[]{vert_data, color_data};
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


    public static void billboardCheatSphericalBegin() { //scale-ignoring easy billboarding function  //http://www.lighthouse3d.com/opengl/billboarding/index.php?billCheat
        FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
        int i,j;

        // save the current modelview matrix
        glPushMatrix();
        // get the current modelview matrix
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, modelview);

        // undo all rotations
        // beware all scaling is lost as well
        for( i=0; i<3; i++ )
            for( j=0; j<3; j++ ) {
                if ( i==j )
                    modelview.put(i*4+j,1.0f);
                else
                    modelview.put(i*4+j,0.0f);
            }
        // set the modelview with no rotations
        GL11.glLoadMatrix(modelview);
    }



    public static void billboardEnd() {

        // restore the previously
        // stored modelview matrix
        glPopMatrix();
    }
}
