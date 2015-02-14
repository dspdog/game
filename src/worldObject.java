import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.FileUtil;
import factory.GeometryFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.util.vector.Vector3f;
import shapes.tree;
import simplify.SimplifyCSG;
import utils.RandomHelper;
import utils.time;

import java.io.IOException;
import java.nio.file.Paths;

public class worldObject {

    Vector3f velocity = new Vector3f();
    Vector3f position = new Vector3f();
    Vector3f rotation = new Vector3f();

    tree myTree;
    CSG myCSG;
    CSGProgram myCSGProg;
    long lastVBOUpdate=0;

    int[] VBOHandles;
    String name="";

    int stencilId = 0;//(int)((System.currentTimeMillis()+Math.random()*1000)%255);
    public String randomName =  RandomStringUtils.randomAlphanumeric(5).toUpperCase();
    int vertices = 0;

    int numTris = 0;
    int numPolys = 0;
    int numVerts = 0;
    int numVertsUnique = 0;

    WOType myType = WOType.NONE;

    public enum WOType {
        NONE, CSG, CSGProgram, TREE
    }

    public void getGeometryData(){
        switch (myType){
            case TREE:
                numTris = myTree.vertices/3; //? guessing
                return;
            case CSG:
                numTris = myCSG.numTriangles;

                SimplifyCSG.loadCSG(myCSG);
                numPolys=SimplifyCSG.polys;
                numVerts=SimplifyCSG.vertsNonUnique;
                numVertsUnique=SimplifyCSG.vertsUnique;
                return;
            case CSGProgram:
                numTris = myCSGProg.myCSG.numTriangles;

                SimplifyCSG.loadCSG(myCSGProg.myCSG);
                numPolys=SimplifyCSG.polys;
                numVerts=SimplifyCSG.vertsNonUnique;
                numVertsUnique=SimplifyCSG.vertsUnique;
                return;
        }
        numTris=-1;
    }

    public worldObject(){
        position = RandomHelper.randomPosition(100);
        rotation = RandomHelper.randomRotation();
        velocity = new Vector3f(0,0,0);
    }

    public worldObject(tree tree){
        this();
        name="TREE_" + randomName;
        myType=WOType.TREE;
        myTree = tree;
        rotation=new Vector3f(0,0,0);
        position=new Vector3f(0,0,0);
        updateVBOs();
        getStencilId();
        getGeometryData();
    }

    public worldObject(CSG csg){
        this();
        name="CSG_" + randomName;
        myType=WOType.CSG;
        myCSG = csg;
        updateVBOs();
        getStencilId();
        getGeometryData();
    }

    public worldObject(CSGProgram csg){
        this();
        name="CSGProg_" + randomName;
        myType=WOType.CSGProgram;
        myCSGProg = csg;
        csg.myWorldObject = this;
        updateVBOs();
        getStencilId();
        getGeometryData();
    }


    public worldObject setPos(Vector3f pos){
        position.set(pos);
        return this;
    }

    public void updateVBOs(){
        switch (myType){
            case TREE:
                if(myTree!=null){
                    VBOHandles = GeometryFactory.treeVBOQuadHandles(myTree);
                    vertices = myTree.vertices;
                }
                break;
            case CSG:
                VBOHandles = GeometryFactory.csgVBOHandles(myCSG);
                break;
            case CSGProgram:
                VBOHandles = GeometryFactory.csgVBOHandles(myCSGProg.toCSG());
                break;
        }
        lastVBOUpdate= time.getTime();
    }

    public void move(float dt){
        position.translate(velocity.x*dt, velocity.y*dt, velocity.z*dt);
    }

    public void save(){
        boolean isCSG = myType == WOType.CSG || myType == WOType.CSGProgram;
        try {
            switch (myType){
                case TREE:
                    System.out.println("Cant save a tree :(");
                    break;
                case CSG:
                    FileUtil.write(
                            Paths.get("./savedObjects/" +name + ".stl"),
                            myCSG.toStlString()
                    );
                    System.out.println("SAVED ./savedObjects/" +name + ".stl");
                    break;
                case CSGProgram:
                    FileUtil.write(
                            Paths.get("./savedObjects/" +name + ".stl"),
                            myCSGProg.myCSG.toStlString()
                    );
                    System.out.println("SAVED " + name + ".stl");
                    break;
                default:
                    System.out.println("SAVED ./savedObjects/" +name + ".stl");
            }
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getStencilId(){
        stencilId = Math.abs(name.hashCode()%255);
        return stencilId;
    }
}