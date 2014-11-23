package world;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import factory.GeometryFactory;

import java.nio.FloatBuffer;

public class WorldObject{ //have this handle all the interactions w/ geometryfactory...
    CSG myCSG;
    int myTextureId;
    GeometryFactory.gridFunction myFunction;

    int[] VBOHandles;
    int triangles = 0;
    public String name="";

    int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
    boolean isCSG = false;
    boolean isGrid = false;
    boolean isPlane = false;

    boolean hasVBOHandles=false;

    FloatBuffer[] myfb;

    public void drawVBOs(){
        if(!hasVBOHandles){
            VBOHandles = GeometryFactory.VBOHandles(myfb);
            hasVBOHandles=true;
        }

        GeometryFactory.drawTrisByVBOHandles(triangles, VBOHandles);
    }

    public WorldObject(CSG csg){
        name="CSG_" + stencilId;
        isCSG=true;
        myCSG = csg;
        triangles = GeometryFactory.getTriangles(csg);
        myfb = GeometryFactory.getCSGVertexData(csg, triangles);
    }

    public WorldObject(Texture texture){
        name="TEX_" + stencilId;
        isPlane = true;
        myTextureId = texture.getTextureID();
    }

    public WorldObject(int textureId){
        name="TEX_" + stencilId;
        isPlane = true;
        myTextureId = textureId;
    }

    public WorldObject(GeometryFactory.gridFunction d){
        name="GRID_" + stencilId;
        isGrid=true;
        myFunction = d;
        myfb = GeometryFactory.functionGridVertexData(d);
        triangles = (GeometryFactory.gridSize* GeometryFactory.gridSize/GeometryFactory.gridStep/GeometryFactory.gridStep)*2;
    }

    public Vector3f getCenter(){
        if(isGrid){
            return new Vector3f(GeometryFactory.gridSize/2, 0, GeometryFactory.gridSize/2);
        }else if(isCSG){
            Vector3d center = myCSG.getBounds().getCenter();
            return new Vector3f((float)center.x, (float)center.y, (float)center.z);
        }
        return new Vector3f(GeometryFactory.gridSize/2, 0, GeometryFactory.gridSize/2);
    }
}