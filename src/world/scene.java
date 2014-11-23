package world;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import factory.GeometryFactory;
import shapes.geography.GeographyFactory;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;

public class scene{
    private static ArrayList<WorldObject> objs;
    public static Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

    public static Vector3f focalPos = new Vector3f(0,0,0);
    public static Vector3f cameraPosDesired = new Vector3f(0,-30,0);

    private static Vector3f cameraPosRealtime = new Vector3f(0,-30,0);

    public void drawScene(){
        cameraPosRealtime = getCamPos();
        for(WorldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
            if(wo.isGrid){
                glTranslatef(wo.myPos.x, wo.myPos.y, wo.myPos.z);
                wo.drawVBOs();
                glTranslatef(-wo.myPos.x, -wo.myPos.y, -wo.myPos.z);
            }else if (wo.isPlane){
                GeometryFactory.billboardCheatSphericalBegin();
                GeometryFactory.plane(wo.myTextureId);
                GeometryFactory.billboardEnd();
            }
        }
    }

    public void sceneLogic(){

        int index=0;
        for(WorldObject wo : objs){
            if(wo.isGrid){
                float time = (System.currentTimeMillis()%1000000)/1000.0f;
                float offsetX = cameraPosRealtime.x - GeometryFactory.gridSize/2;
                float offsetY = cameraPosRealtime.z - GeometryFactory.gridSize/2;
                WorldObject _wo = new WorldObject((float x, float y) -> GeographyFactory.geographyFunction(x+offsetX, y+offsetY, time));
                _wo.setPos(offsetX, 0f,offsetY);
                objs.set(index, _wo);
            }else if (wo.isPlane){
            }
            index++;
        }
    }

    public Vector3f getCamPos(){//http://www.gamedev.net/topic/397751-how-to-get-camera-position/
        FloatBuffer mdl = BufferUtils.createFloatBuffer(16);
        // save the current modelview matrix
        //glPushMatrix();
        // get the current modelview matrix
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, mdl);

        return new Vector3f(-(mdl.get(0) * mdl.get(12) + mdl.get(1) * mdl.get(13) + mdl.get(2) * mdl.get(14)),
                            -(mdl.get(4) * mdl.get(12) + mdl.get(5) * mdl.get(13) + mdl.get(6) * mdl.get(14)),
                            -(mdl.get(8) * mdl.get(12) + mdl.get(9) * mdl.get(13) + mdl.get(10) * mdl.get(14)));
    }

    public scene(){
        objs = new ArrayList<>();
    }

    public void addWorldObject(WorldObject wo){
        objs.add(wo);
        System.out.println("ADDING " + (wo.stencilId+1));
        idsMap.put((wo.stencilId+1)+"", wo);
    }

    public void focusOn(WorldObject wo){
        focalPos = wo.getCenter();
    }
}