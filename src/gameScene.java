import factory.GeometryFactory;
import org.lwjgl.util.vector.Vector3f;
import utils.glHelper;
import utils.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

class gameScene {
    private static CopyOnWriteArrayList<worldObject> objs = new CopyOnWriteArrayList<>();
    public static Map<String, worldObject> idsMap = new HashMap<>();

    public static Vector3f poi;
    //public static long numTris = 0;
    static float coordsScale=5.0f;

    public static void drawCursor(){
        GeometryFactory.reticle(RenderThread.myWidth/2, RenderThread.myHeight/2);
    }

    public static void drawScene(){
        int tris = 0;
        glHelper.updateCamVectors();
        for(worldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId, -1);
            glPushMatrix();

            glTranslatef(wo.position.x*coordsScale,wo.position.y*coordsScale,wo.position.z*coordsScale);
            glRotatef(wo.rotation.x,1,0,0);
            glRotatef(wo.rotation.y,0,1,0);
            glRotatef(wo.rotation.z,0,0,1);
            wo.updateVBOs();

            switch (wo.myType){
                case TREE:
                    /*if(LogicThread.lastGameLogic -  RenderThread.lastVBOUpdate > 1){ //TODO use wo.lastVBOUpdate instead of RenderThread
                        wo.updateVBOs();
                        RenderThread.lastVBOUpdate= time.getTime();
                    }
                    GeometryFactory.drawQuadsByVBOHandles(wo.vertices, wo.VBOHandles);*/
                    break;
                case CSG:
                    //tris+=wo.myCSG.numTriangles;
                    GeometryFactory.drawTrisByVBOHandles(wo.myCSG.numTriangles, wo.VBOHandles);
                    break;
                case CSGProgram:
                    //tris+=wo.myCSGProg.myCSG.numTriangles;
                    /*if(wo.myCSGProg.lastBuildTime>wo.lastVBOUpdate || wo.myCSGProg.lastSimplifyTime>wo.lastVBOUpdate){
                        wo.updateVBOs();
                        wo.lastVBOUpdate= time.getTime();
                    }*/

                    wo.oldTris = wo.myCSGProg.myCSG.numTriangles == 0 ? wo.oldTris : wo.myCSGProg.myCSG.numTriangles;
                    GeometryFactory.drawTrisByVBOHandles(wo.oldTris, wo.VBOHandles);
                    break;
            }
            glPopMatrix();
        }
        glPopMatrix();
       // numTris=tris;
    }

    public static void logicScene(float dt){
        for(worldObject wo : objs){
           switch (wo.myType){
                case TREE:
                    wo.myTree.perturb(false, false, 01.250f*dt);
                    wo.VBODirty = true;
                    break;
                case CSG:
                    //static CSG - do nothing
                    break;
                case CSGProgram:
                    wo.myCSGProg.iterate(dt);
                    break;
            }
        }
    }

    public gameScene(){
        objs = new CopyOnWriteArrayList<>();
    }

    public static void addWorldObject(worldObject wo){
        objs.add(wo);
        idsMap.put(wo.stencilId+"", wo);
    }
}
