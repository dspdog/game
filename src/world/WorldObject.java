package world;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import factory.GeometryFactory;
import shapes.cloud.kParticleCloud;
import shapes.cloud.sphCloud;

import java.nio.FloatBuffer;

public class WorldObject{ //have this handle all the interactions w/ geometryfactory...
    CSG myCSG;
    int myTextureId;
    GeometryFactory.gridFunction myFunction;
    sphCloud myCloud;
    public kParticleCloud myKCloud;
    Vector3f myPos = new Vector3f(0,0,0);
    Vector3f myLastDrawnPos = new Vector3f(0,0,0);
    Vector3f myColor = new Vector3f(0.5f, 0.5f, 0.5f);

    long lastFunctionUpdate = 0;
    int functionUpdatePeriod = 0;

    int[] VBOHandles;
    int triangles = 0;
    public String name="";

    int stencilId = (int)((Math.random()*100 + System.currentTimeMillis())%255); //for stencil buffer
    boolean isCSG = false;
    boolean isGrid = false;
    boolean isPlane = false;
    boolean isCloud = false;
    boolean isKCloud = false;

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

   /* public WorldObject(int textureId){
        name="TEX_" + stencilId;
        isPlane = true;
        myTextureId = textureId;
    }*/

    public WorldObject(GeometryFactory.gridFunction d){
        name="GRID_" + stencilId;
        isGrid=true;
        myFunction = d;
        forceUpdateGridFb();
        triangles = (GeometryFactory.gridSize* GeometryFactory.gridSize/GeometryFactory.gridStep/GeometryFactory.gridStep)*2;
    }

    public WorldObject(int numParticles, WorldObject collisionObject, int texId){
        myTextureId = texId;
        name="PARTICLES_" + stencilId;
        isCloud = true;
        myCloud = new sphCloud(numParticles, collisionObject);
    }

    public WorldObject(int numParticles){
        name="KPARTICLES_" + stencilId;
        isKCloud = true;
        myKCloud = new kParticleCloud(numParticles);
        myfb = GeometryFactory.cloudVertexData(myKCloud);
        triangles = GeometryFactory.cloudTriangles;
    }

    public WorldObject setUpdateInterval(int interval){
        functionUpdatePeriod = interval;
        return this;
    }

    public WorldObject setColor(float r, float g, float b){
        myColor = new Vector3f(r, g, b);
        forceUpdateGridFb();
        return this;
    }

    public void forceUpdateGridFb(){
        float time = (System.currentTimeMillis()%1000000)/1000.0f;
        myfb = GeometryFactory.functionGridVertexData(myFunction, time, myPos.x, myPos.z, myColor);
        lastFunctionUpdate = System.currentTimeMillis();
        myLastDrawnPos = new Vector3f(myPos.x, myPos.y, myPos.z);
        hasVBOHandles=false;
    }

    public void forceUpdateCloudFb(){
        float time = (System.currentTimeMillis()%1000000)/1000.0f;
        if(kParticleCloud.ready){
            myfb = GeometryFactory.cloudVertexData(myKCloud);
            lastFunctionUpdate = System.currentTimeMillis();
            hasVBOHandles=false;
        }
    }

    public void updateGridFb(){
        if(System.currentTimeMillis()-lastFunctionUpdate > functionUpdatePeriod){
            forceUpdateGridFb();
        }
    }

    public void updateCloudFb(){
        if(System.currentTimeMillis()-lastFunctionUpdate > functionUpdatePeriod){
            forceUpdateCloudFb();
        }
    }

    public void setPos(float x, float y, float z){
        myPos = new Vector3f(x,y,z);
    }

    public Vector3f getCenter(){
        if(isGrid){
            return new Vector3f( myPos.x - GeometryFactory.gridSize/2, 0,  myPos.z - GeometryFactory.gridSize/2);
        }else if(isCSG){
            Vector3d center = myCSG.getBounds().getCenter();
            return new Vector3f((float)center.x, (float)center.y, (float)center.z);
        }
        return new Vector3f(GeometryFactory.gridSize/2, 0, GeometryFactory.gridSize/2);
    }
}