package world;

import org.lwjgl.util.vector.Vector3f;
import factory.GeometryFactory;
import shapes.geography.GeographyFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.glStencilFunc;

public class scene{
    private static ArrayList<WorldObject> objs;
    public static Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

    public static Vector3f focalPos = new Vector3f(0,0,0);
    public static Vector3f cameraPos = new Vector3f(500,500,500);

    public void drawScene(){
        for(WorldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
            if(wo.isGrid){
                wo.drawVBOs();
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
                objs.set(index, new WorldObject((x, y) -> GeographyFactory.geographyFunction(x, y, time)));

            }else if (wo.isPlane){
            }
            index++;
        }
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