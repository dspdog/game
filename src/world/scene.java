package world;

import org.lwjgl.util.vector.Vector3f;
import factory.GeometryFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.glStencilFunc;

public class scene{
    private ArrayList<WorldObject> objs;
    public Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

    public Vector3f focalPos = new Vector3f(0,0,0);
    public Vector3f cameraPos = new Vector3f(500,500,500);

    public void drawScene(){
        for(WorldObject wo : objs){
            glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
            if(wo.isCSG || wo.isGrid){
                GeometryFactory.drawTrisByVBOHandles(wo.triangles, wo.VBOHandles);
            }else if (wo.isPlane){
                GeometryFactory.billboardCheatSphericalBegin();
                GeometryFactory.plane(wo.myTextureId);
                GeometryFactory.billboardEnd();
            }
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