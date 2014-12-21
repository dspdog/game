package factory;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Polygon;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.*;
import org.lwjgl.opengl.GL11;
import shapes.cloud.kParticleCloud;
import world.scene;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class GeometryFactory {

    public static void kcloud(kParticleCloud cloud){
        /*long time = System.currentTimeMillis();
        float r,g,b;

        for(int particleNo=0; particleNo<cloud.numParticles; particleNo++){
            particle p = cloud.getParticle(particleNo);

            r = (p.pos.x-cloud.lowerX)/(cloud.upperX - cloud.lowerX);
            g = (p.pos.y-cloud.lowerY)/(cloud.upperY - cloud.lowerY);
            b = (p.pos.z-cloud.lowerZ)/(cloud.upperZ - cloud.lowerZ);

            glColor3f(r, g, b);
            drawCircle(p, false);
        }


        //glColor3f(0.5f, 0f, 0f);
        //drawBox(sphCloud.lowerCornerBoundsFinal, sphCloud.upperCornerBoundsFinal);
*/
        glColor4f(0f, 0f, 0f, 0.5f);
        drawBox(cloud.lowerBoxBounds, cloud.upperBoxBounds);

        glColor4f(0.75f, 0f, 0f, 0.5f);
        drawBox(cloud.lowerBounds, cloud.upperBounds);

        glColor4f(0f, 0f, 0.5f, 0.5f);
        drawBox(cloud.lowerDenseBounds, cloud.upperDenseBounds);
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

    public static void plane(int tex, int size){
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex);
        glBegin(GL11.GL_QUADS);

        glColor3f(1, 1, 1);

        glTexCoord2f(0, 0);
        glVertex3f(0, 0, 0);

        glTexCoord2f(1, 0);
        glVertex3f(size, 0, 0);

        glTexCoord2f(1, 1);
        glVertex3f(size, size, 0);

        glTexCoord2f(0, 1);
        glVertex3f(0, size, 0);

        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    public static FloatBuffer[] getCSGVertexData(CSG csg, int tris){
        final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(tris*9);
        final FloatBuffer color_data = BufferUtils.createFloatBuffer(tris*9);
        final FloatBuffer tex_data = BufferUtils.createFloatBuffer(tris*6);

        for(Polygon poly : csg.getPolygons()){
            for(int v=1; v<poly.vertices.size()-1; v++){
                vertex_data.put((float)poly.vertices.get(0).pos.x).
                            put((float)poly.vertices.get(0).pos.y).
                            put((float) poly.vertices.get(0).pos.z).
                            put((float) poly.vertices.get(v).pos.x).
                            put((float) poly.vertices.get(v).pos.y).
                            put((float) poly.vertices.get(v).pos.z).
                            put((float) poly.vertices.get(v + 1).pos.x).
                            put((float) poly.vertices.get(v + 1).pos.y).
                            put((float) poly.vertices.get(v + 1).pos.z);

                tex_data.put((float)poly.vertices.get(0).pos.x).
                        put((float)poly.vertices.get(0).pos.y).
                        put((float)poly.vertices.get(v).pos.x).
                        put((float)poly.vertices.get(v).pos.y).
                        put((float)poly.vertices.get(v+1).pos.x).
                        put((float)poly.vertices.get(v+1).pos.y);


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
        tex_data.flip();
        return new FloatBuffer[]{vertex_data, color_data, tex_data};
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
        int vbo_vertex_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glBufferData(GL_ARRAY_BUFFER, fbs[0], GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_color_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glBufferData(GL_ARRAY_BUFFER, fbs[1], GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vbo_texture_coords_handle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_texture_coords_handle);
        glBufferData(GL_ARRAY_BUFFER, fbs[2], GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new int[]{vbo_vertex_handle,vbo_color_handle, vbo_texture_coords_handle};
    }

    public static int gridSize = 256;
    public static int gridStep = 2;
    public static int cloudTriangles = 0;

    public static FloatBuffer[] cloudVertexData(kParticleCloud kCloud){
        int numParticles = kCloud.numParticles;
        int vertsPerTriangle = 3;
        int trisPerSprite = 6; //for hexagons use 6 - actually only uses (N-2) tris

        float alpha = 1.00f;

        boolean alphaModulate = true;//!kCloud.neighborsReset;

        float _origAlpha = alpha;
        boolean useSquares = true;

        if(useSquares)
            trisPerSprite =2;

        cloudTriangles=numParticles*trisPerSprite;

        final FloatBuffer vert_data = BufferUtils.createFloatBuffer(numParticles*vertsPerTriangle*3*trisPerSprite);
        final FloatBuffer color_data = BufferUtils.createFloatBuffer(numParticles*vertsPerTriangle*4*trisPerSprite);
        final FloatBuffer tex_data = BufferUtils.createFloatBuffer(numParticles*vertsPerTriangle*2*trisPerSprite);

        long time = System.currentTimeMillis();

        Integer[] particlesByDist = new Integer[numParticles];

        int MAX_DIST = 1024;

        ArrayList<Integer>[] particlesByDistList = new ArrayList[MAX_DIST];
        //get particles by dist

        for(int distI=0; distI<numParticles; distI++){
            int dist = Math.max(0, Math.min(MAX_DIST-1, (int) kCloud.cameraDistance(distI)));
            if(particlesByDistList[dist]==null){
                particlesByDistList[dist]=new ArrayList<Integer>();
            }
            particlesByDistList[dist].add(distI);
        }

        ArrayList<Integer> distList = new ArrayList<>();
        for(int i=0; i<particlesByDistList.length; i++){
            if(particlesByDistList[i]!=null)
            distList.addAll(particlesByDistList[i]);
        }

        //System.out.println(particlesByDist.length + " particles by dist ");

        for(int distI=0; distI<numParticles; distI++){
            int particle = distList.get(distI); //particlesByDist[distI];
            if(alphaModulate)
                alpha=_origAlpha*kCloud.getTotalNeighbors(particle)/((float)Math.pow(kCloud.averageNeighbors, 1.0f));

            //float fatness = kCloud.pressure[particle]/kCloud.averageP*5f; //higher pressure bigger (explodes?)
            //float fatness = Math.min(30f, Math.max(10f, kCloud.density[particle]/kCloud.averageD*5f)); //denser bigger, range 10-30
            float fatness = 22f;

            //float fatness = 1f;

            float xN = (kCloud.positionX[particle]-kCloud.lowerX)/(kCloud.upperX-kCloud.lowerX);
            float yN = (kCloud.positionY[particle]-kCloud.lowerY)/(kCloud.upperY-kCloud.lowerY);
            float zN = (kCloud.positionZ[particle]-kCloud.lowerZ)/(kCloud.upperZ-kCloud.lowerZ);

            float cxX = scene.cameraXVector.x*fatness;
            float cxY = scene.cameraXVector.y*fatness;
            float cxZ = scene.cameraXVector.z*fatness;

            float cyX = scene.cameraYVector.x*fatness;
            float cyY = scene.cameraYVector.y*fatness;
            float cyZ = scene.cameraYVector.z*fatness;


            if(useSquares){

                float x = kCloud.positionX[particle] - cxX/2 - cyX/2;
                float y = kCloud.positionY[particle] - cxY/2 - cyY/2;
                float z = kCloud.positionZ[particle] - cxZ/2 - cyZ/2;

                vert_data.put(x).put(y).put(z);
                vert_data.put(x+cyX).put(y+cyY).put(z+cyZ);
                vert_data.put(x+cxX).put(y+cxY).put(z+cxZ);

                vert_data.put(x+cxX).put(y+cxY).put(z+cxZ);
                vert_data.put(x+cyX).put(y+cyY).put(z+cyZ);
                vert_data.put(x+cyX+cxX).put(y+cyY+cxY).put(z+cyZ+cxZ);

                float size = 0.75f;

                tex_data.put(0).put(0);
                tex_data.put(0).put(size);
                tex_data.put(size).put(0);

                tex_data.put(size).put(0);
                tex_data.put(0).put(size);
                tex_data.put(size).put(size);

                color_data.put(xN).put(yN).put(zN).put(alpha);
                color_data.put(xN).put(yN).put(zN).put(alpha);
                color_data.put(xN).put(yN).put(zN).put(alpha);
                color_data.put(xN).put(yN).put(zN).put(alpha);
                color_data.put(xN).put(yN).put(zN).put(alpha);
                color_data.put(xN).put(yN).put(zN).put(alpha);
            }else{
                int segs = trisPerSprite;
                float extraRotation = (float)(2f*Math.PI*particle/numParticles + 17f* 2f*Math.PI*(time%1000)/1000f);
                float scale = 1/2f;
                float sin0 = (float) (Math.sin(extraRotation)) * scale;
                float cos0 = (float) (Math.cos(extraRotation)) * scale;

                for(int i=1; i<segs-1; i++) {

                    float sin1 = (float) (Math.sin(i * 2 * Math.PI / segs + extraRotation)) * scale;
                    float cos1 = (float) (Math.cos(i * 2 * Math.PI / segs + extraRotation)) * scale;
                    float sin2 = (float) (Math.sin((i+1) * 2 * Math.PI / segs + extraRotation)) * scale;
                    float cos2 = (float) (Math.cos((i+1) * 2 * Math.PI / segs + extraRotation)) * scale;

                    float x = kCloud.positionX[particle];
                    float y = kCloud.positionY[particle];
                    float z = kCloud.positionZ[particle];

                    float dx0 = cxX*sin0 + cyX*cos0;
                    float dy0 = cxY*sin0 + cyY*cos0;
                    float dz0 = cxZ*sin0 + cyZ*cos0;

                    float dx1 = cxX*sin1 + cyX*cos1;
                    float dy1 = cxY*sin1 + cyY*cos1;
                    float dz1 = cxZ*sin1 + cyZ*cos1;

                    float dx2 = cxX*sin2 + cyX*cos2;
                    float dy2 = cxY*sin2 + cyY*cos2;
                    float dz2 = cxZ*sin2 + cyZ*cos2;

                    vert_data.put(x+dx0).put(y+dy0).put(z+dz0);
                    vert_data.put(x+dx1).put(y+dy1).put(z+dz1);
                    vert_data.put(x+dx2).put(y+dy2).put(z+dz2);

                    float tx=0.375f; float ty=0.375f;
                    float scale2=0.75f;
                    tex_data.put(tx+cos0*scale2).put(ty+sin0*scale2);
                    tex_data.put(tx+cos1*scale2).put(ty + sin1 * scale2);
                    tex_data.put(tx+cos2*scale2).put(ty+sin2*scale2);

                    color_data.put(xN).put(yN).put(zN).put(alpha);
                    color_data.put(xN).put(yN).put(zN).put(alpha);
                    color_data.put(xN).put(yN).put(zN).put(alpha);
                }
            }




            //float d = kCloud.getNumberOfNeighbors(particle)/6f;
            //float d = kCloud.getDensity(particle)/kCloud.averageD;
            /*color_data.put(d).put(d).put(d);
            color_data.put(d).put(d).put(d);
            color_data.put(d).put(d).put(d);
            color_data.put(d).put(d).put(d);
            color_data.put(d).put(d).put(d);
            color_data.put(d).put(d).put(d);*/


        }

        vert_data.flip();
        color_data.flip();
        tex_data.flip();
        return new FloatBuffer[]{vert_data, color_data, tex_data};
    }

    public static FloatBuffer[] functionGridVertexData(gridFunction d, float t, float offsetx, float offsetz, Vector3f color){
        int step = gridStep;
        float r = color.x/32f;
        float g = color.y/32f;
        float b = color.z/32f;
        final FloatBuffer vert_data = BufferUtils.createFloatBuffer((gridSize/step * gridSize/step)*9*2);
        final FloatBuffer color_data = BufferUtils.createFloatBuffer((gridSize/step * gridSize/step)*9*2);
        final FloatBuffer tex_data = BufferUtils.createFloatBuffer((gridSize/step * gridSize/step)*9*2);

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

                tex_data.put(x).put(z)
                        .put(x2).put(z2)
                        .put(x3).put(z3)

                        .put(x3).put(z3)
                        .put(x2).put(z2)
                        .put(x4).put(z4);

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
        tex_data.flip();
        return new FloatBuffer[]{vert_data, color_data, tex_data};
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


    public static void drawTrisByVBOHandles(int triangles, int[] handles, Texture tex){
        //http://www.java-gaming.org/index.php?topic=18710.0
        glColor3f(1.0f, 1.0f, 1.0f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_COLOR_MATERIAL);
        glEnable (GL_BLEND);
        glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        int vertex_size = 3; // X, Y, Z,
        int color_size = 4; // R, G, B, A
        int text_coord_size = 2; // X, Y,

        int vbo_vertex_handle = handles[0];
        int vbo_color_handle = handles[1];
        int vbo_texture_coord_handle = handles[2];

        glBindTexture(GL_TEXTURE_2D, tex.getTextureID());

        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );


        glBindBuffer(GL_ARRAY_BUFFER, vbo_texture_coord_handle);
        glTexCoordPointer(text_coord_size, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_color_handle);
        glColorPointer(color_size, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        glVertexPointer(vertex_size, GL_FLOAT, 0, 0L);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_TEXTURE_2D);

        glDrawArrays(GL_TRIANGLES, 0, triangles*vertex_size);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisable(GL_TEXTURE_2D);

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
