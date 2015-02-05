import eu.mihosoft.vrl.v3d.CSG;
import factory.GeometryFactory;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import shapes.tree;
import utils.RandomHelper;
import utils.glHelper;

public class worldObject {

    Vector3f velocity = new Vector3f();
    Vector3f position = new Vector3f();
    Vector3f rotation = new Vector3f();

    tree myTree;
    CSG myCSG;

    int[] VBOHandles;
    String name="";

    int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
    int vertices = 0;

    WOType myType = WOType.NONE;

    public enum WOType {
        NONE, CSG, TREE
    }

    public worldObject(){
        position = RandomHelper.randomPosition(100);
        rotation = RandomHelper.randomRotation();
        velocity = new Vector3f(0,0,0);
    }

    public worldObject(tree tree){
        this();
        name="TREE_" + stencilId;
        myType=WOType.TREE;
        myTree = tree;
        updateVBOs();
    }

    public worldObject(CSG csg){
        this();
        name="CSG_" + stencilId;
        myType=WOType.CSG;
        myCSG = csg;
        updateVBOs();
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
        }
    }

    public void move(float dt){
        position.translate(velocity.x*dt, velocity.y*dt, velocity.z*dt);
    }
}