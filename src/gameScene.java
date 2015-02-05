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

    public static void drawScene(){
        glHelper.updateCamVectors();
        for(worldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);

            glTranslatef(wo.position.x,wo.position.y,wo.position.z);
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
            glTranslatef(-wo.position.x,-wo.position.y,-wo.position.z);

            //glPopMatrix();
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
