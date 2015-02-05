import factory.GeometryFactory;
import utils.glHelper;
import utils.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

class gameScene {
    private static ArrayList<worldObject> objs = new ArrayList<>();
    public static Map<String, worldObject> idsMap = new HashMap<>();

    static float coordsScale=5.0f;

    public static void drawScene(){
        glHelper.updateCamVectors();
        for(worldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
            glPushMatrix();

            glTranslatef(wo.position.x*coordsScale,wo.position.y*coordsScale,wo.position.z*coordsScale);
            glRotatef(wo.rotation.x,1,0,0);
            glRotatef(wo.rotation.y,0,1,0);
            glRotatef(wo.rotation.z,0,0,1);
            switch (wo.myType){
                case TREE:
                    if(LogicThread.lastGameLogic -  RenderThread.lastVBOUpdate > 1){
                        wo.updateVBOs();
                        RenderThread.lastVBOUpdate= time.getTime();
                    }
                    GeometryFactory.drawQuadsByVBOHandles(wo.vertices, wo.VBOHandles);
                    break;
                case CSG:
                    GeometryFactory.drawTrisByVBOHandles(wo.myCSG.numTriangles, wo.VBOHandles);
                    break;
            }
            glPopMatrix();
        }
    }

    public gameScene(){
        objs = new ArrayList<>();
    }

    public static void addWorldObject(worldObject wo){
        objs.add(wo);
        idsMap.put((wo.stencilId+1)+"", wo);
    }
}
