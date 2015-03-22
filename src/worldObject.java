import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import factory.GeometryFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.util.vector.Vector3f;
import simplify.SimplifyCSG;
import utils.time;

import java.io.IOException;
import java.nio.file.Paths;

public class WorldObject {

    Vector3f velocity = new Vector3f();
    Vector3f position = new Vector3f();
    Vector3f rotation = new Vector3f();

    CSG myCSG;
    CSGProgram myCSGProg;
    long lastVBOUpdate=0;

    int[] VBOHandles;
    String name="";

    int stencilId = 0;//(int)((System.currentTimeMillis()+Math.random()*1000)%255);
    public String randomName =  RandomStringUtils.randomAlphanumeric(5).toUpperCase();
    int vertices = 0;

    //GEOMETRY DATA
    int numTris = 0;
    int oldTris = 0; //numTris non-zero
    int numPolys = 0;
    int numVerts = 0;
    int numVertsUnique = 0;

    WOType myType = WOType.NONE;

    boolean VBODirty = true;
    boolean isCSG = false;

    public enum WOType {
        NONE, CSG, CSGProgram, TREE
    }

    public void updateGeometryData(){

        if(isCSG){ //CSG types
            SimplifyCSG.loadCSG(getCSG());
            VBODirty = true;
            numPolys=SimplifyCSG.polys;
            numVerts=SimplifyCSG.vertsNonUnique;
            numVertsUnique=SimplifyCSG.vertsUnique;
            return;
        }
        numTris=-1;
    }

    public WorldObject(){
        position = new Vector3f(0,0,0); //RandomHelper.randomPosition(100);
        rotation = new Vector3f(0,0,0); //RandomHelper.randomRotation();
        velocity = new Vector3f(0,0,0);
    }

    public WorldObject(CSG csg){
        this();
        isCSG=true;
        name="CSG_" + randomName;
        myType=WOType.CSG;
        myCSG = csg;
        getStencilId();
        updateGeometryData();
    }

    public WorldObject(CSGProgram csg){
        this();
        isCSG=true;
        name="CSGProg_" + randomName;
        myType=WOType.CSGProgram;
        myCSGProg = csg;
        csg.myWorldObject = this;
        getStencilId();
        updateGeometryData();
    }

    public WorldObject setPos(Vector3f pos){
        position.set(pos);
        return this;
    }

    public void updateVBOs(){
        if(VBODirty){
            if(isCSG){
                VBOHandles = GeometryFactory.csgVBOHandles(getCSG());
                numTris = getCSG().numTriangles;
            }
            VBODirty =false;
            lastVBOUpdate= time.getTime();
        }
    }

    public void move(float dt){
        position.translate(velocity.x*dt, velocity.y*dt, velocity.z*dt);
    }

    public void save(){
        if(isCSG){
            try {
                FileUtil.write(
                        Paths.get("./savedObjects/" +name + ".stl"),
                        getCSG().toStlString()
                );
                System.out.println("SAVED " + name + ".stl");
            } catch (IOException ex) {
                System.out.println(ex.getStackTrace());
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getStencilId(){
        stencilId = Math.abs(name.hashCode()%255);
        return stencilId;
    }

    public CSG getCSG(){
        switch (myType){
            case CSG:
                myCSG.getTriangles();
                return myCSG;
            case CSGProgram:
                return myCSGProg.myCSG;
        }
        return null;
    }

    public void setCSG(CSG csg){
        VBODirty = true;
        switch (myType){
            case CSG:
                myCSG = csg;
                break;
            case CSGProgram:
                myCSGProg.myCSG = csg;
                break;
        }
    }
}