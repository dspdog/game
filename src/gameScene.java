import factory.GeometryFactory;
import org.lwjgl.util.vector.Vector3f;
import utils.glHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

class GameScene {
    private static CopyOnWriteArrayList<WorldObject> objs = new CopyOnWriteArrayList<>();
    public static Map<String, WorldObject> idsMap = new HashMap<>();

    public static WorldObject selectionObject = null;

    public static Vector3f poi;
    //public static long numTris = 0;
    static float coordsScale=5.0f;

    public static void drawCursor(){
        GeometryFactory.reticle(RenderThread.myWidth/2, RenderThread.myHeight/2);
    }

    public static void drawScene(){
        int tris = 0;
        glHelper.updateCamVectors();
        for(WorldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId, -1);
            glPushMatrix();

            glTranslatef(wo.position.x*coordsScale,wo.position.y*coordsScale,wo.position.z*coordsScale);
            glRotatef(wo.rotation.x,1,0,0);
            glRotatef(wo.rotation.y,0,1,0);
            glRotatef(wo.rotation.z,0,0,1);
            wo.updateVBOs();

            switch (wo.myType){
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

        WorldObject objAtMouse = RenderThread.worldObject_AtMouse();

        if(selectionObject!=null && objAtMouse!=null){
            selectionObject.position.set(RenderThread.worldObject_AtMouse().position);
            //System.out.println(selectionObject.name);
        }

        for(WorldObject wo : objs){
           switch (wo.myType){
                case CSG:
                    //static CSG - do nothing
                    break;
                case CSGProgram:
                    wo.myCSGProg.iterate(dt);
                    break;
            }
        }
    }

    public GameScene(){
        objs = new CopyOnWriteArrayList<>();
    }

    public static void addWorldObject(WorldObject wo){
        objs.add(wo);
        idsMap.put(wo.stencilId+"", wo);
    }

    public static void setSelectionObject(WorldObject wo){
        selectionObject = wo;
        objs.add(wo);
        idsMap.put(wo.stencilId+"", wo);
    }
}
