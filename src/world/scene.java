package world;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import factory.GeometryFactory;
import shapes.geography.GeographyFactory;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;

public class scene{
    private static CopyOnWriteArrayList<WorldObject> objs;
    public static Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

    public static Vector3f focalPos = new Vector3f(0,0,0);
    public static Vector3f cameraPosDesired = new Vector3f(0,-30,0);
    public static Vector3f cameraXVector = new Vector3f(0,0,0);
    public static Vector3f cameraYVector = new Vector3f(0,0,0);
    public static Vector3f cameraZVector = new Vector3f(0,0,0);

    private static Vector3f cameraPosRealtime = new Vector3f(0,-30,0);

    public void drawScene(){
        cameraPosRealtime = getCamPos();
        for(WorldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
            if(wo.isGrid){
                glTranslatef(wo.myLastDrawnPos.x, wo.myLastDrawnPos.y, wo.myLastDrawnPos.z);
                wo.drawVBOs();
                glTranslatef(-wo.myLastDrawnPos.x, -wo.myLastDrawnPos.y, -wo.myLastDrawnPos.z);
            }else if (wo.isPlane){
                GeometryFactory.billboardCheatSphericalBegin();
                GeometryFactory.plane(wo.myTextureId);
                GeometryFactory.billboardEnd();
            }if(wo.isCSG){
                wo.drawVBOs();
            }
        }
    }

    public void sceneLogic(){
        Iterator<WorldObject> wi = objs.iterator();
        while (wi.hasNext()){
            WorldObject wo=wi.next();
            if(wo.isGrid){

                wo.updateGridFb();
                wo.setPos(cameraPosRealtime.x, 0f,cameraPosRealtime.z);
            }else if (wo.isPlane){
            }
        }
    }

    public Vector3f getCamPos(){//http://www.gamedev.net/topic/397751-how-to-get-camera-position/
        getCamVecs();
        FloatBuffer mdl = BufferUtils.createFloatBuffer(16);
        // save the current modelview matrix
        //glPushMatrix();
        // get the current modelview matrix
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, mdl);

        return new Vector3f(-(mdl.get(0) * mdl.get(12) + mdl.get(1) * mdl.get(13) + mdl.get(2) * mdl.get(14)),
                            -(mdl.get(4) * mdl.get(12) + mdl.get(5) * mdl.get(13) + mdl.get(6) * mdl.get(14)),
                            -(mdl.get(8) * mdl.get(12) + mdl.get(9) * mdl.get(13) + mdl.get(10) * mdl.get(14)));
    }

    public void getCamVecs(){//http://www.gamedev.net/topic/397751-how-to-get-camera-position/
        FloatBuffer mdl = BufferUtils.createFloatBuffer(16);
        // save the current modelview matrix
        //glPushMatrix();
        // get the current modelview matrix
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, mdl);

        cameraXVector = new Vector3f(mdl.get(0),mdl.get(4),mdl.get(8));
        cameraYVector = new Vector3f(mdl.get(1),mdl.get(5),mdl.get(9));
        cameraZVector = new Vector3f(mdl.get(2),mdl.get(6),mdl.get(10));
    }

    public scene(){
        objs = new CopyOnWriteArrayList<>();
    }

    public WorldObject addWorldObject(WorldObject wo){
        objs.add(wo);
        System.out.println("ADDING " + (wo.name));
        idsMap.put((wo.stencilId+1)+"", wo);
        return wo;
    }

    public void focusOn(WorldObject wo){
        focalPos = wo.getCenter();
    }
}