import factory.CSGFactory;
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

    public static WorldObject pointerObject = null;
    public static WorldObject selectionObject = null;
    public static WorldObject selectedObj = null;

    public static Vector3f poi;
    //public static long numTris = 0;
    static float coordsScale=5.0f;

    public static void drawCursor(){
        GeometryFactory.reticle((int)GameInputs.cursorX, RenderThread.myHeight - (int)GameInputs.cursorY);
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

    public static void pointAtObj(WorldObject obj){
        if(obj!=null && pointerObject != null && obj!= pointerObject && obj!= selectionObject){
            pointerObject.setCSG(CSGFactory.pointyBoxBounds(obj.getCSG().getBounds()));
            pointerObject.position.set(obj.position);
        }

        if(obj==null && pointerObject != null){
            pointerObject.position.set(1000000,1000000,1000000);
        }
    }

    public static void selectObj(WorldObject obj){
        if(obj!=null && pointerObject != null && obj!= pointerObject && obj!= selectionObject){
            selectedObj = obj;
            selectionObject.setCSG(CSGFactory.cornerBoxBounds(obj.getCSG().getBounds()));
            selectionObject.position.set(obj.position);
        }
    }

    public static void logicScene(float dt){
        pointAtObj(RenderThread.worldObject_AtMouse());
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

    public static void setPointerObject(WorldObject wo){
        if(pointerObject != null){objs.remove(pointerObject);}
        pointerObject = wo;
        objs.add(pointerObject);
        idsMap.put(wo.stencilId+"", wo);
    }

    public static void setSelectionObject(WorldObject wo){
        if(selectionObject != null){objs.remove(selectionObject);}
        selectionObject = wo;
        objs.add(selectionObject);
        idsMap.put(wo.stencilId+"", wo);
    }
}
