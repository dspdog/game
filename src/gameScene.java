import factory.GeometryFactory;
import utils.glHelper;
import utils.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.glStencilFunc;

class gameScene {
    private ArrayList<worldObject> objs;
    public Map<String, worldObject> idsMap = new HashMap<>();

    public void drawScene(){
        glHelper.updateCamVectors();
        for(worldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);

            if(wo.isCSG){
                GeometryFactory.drawTrisByVBOHandles(wo.myCSG.numTriangles, wo.VBOHandles);
            }else if(wo.isGrid){
                GeometryFactory.drawTrisByVBOHandles(256*256*2, wo.VBOHandles);
                //GeometryFactory.drawFunctionGrid(wo.myFunction);
            }else if (wo.isPlane){
                GeometryFactory.plane(wo.myTexture);
            }else if (wo.isTree){
                if(LogicThread.lastGameLogic -  RenderThread.lastVBOUpdate > 1){
                    wo.updateVBOs();
                    RenderThread.lastVBOUpdate= time.getTime();
                }
                GeometryFactory.drawQuadsByVBOHandles(wo.vertices, wo.VBOHandles);
            }
            

        }
    }

    public gameScene(){
        objs = new ArrayList<>();
    }

    public void addWorldObject(worldObject wo){
        objs.add(wo);
        idsMap.put((wo.stencilId+1)+"", wo);
    }
}
