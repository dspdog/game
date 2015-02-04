import eu.mihosoft.vrl.v3d.CSG;
import factory.GeometryFactory;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import shapes.tree;
import utils.RandomHelper;
import utils.glHelper;

public class worldObject {

    Vector3f velocity;
    Vector3f position;
    Vector3f rotation;

    tree myTree;

    int[] VBOHandles;
    String name="";

    int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
    int vertices = 0;
    boolean isTree = false;

    public worldObject(){
        position = RandomHelper.randomPosition(100);
        rotation = RandomHelper.randomRotation();
        velocity = new Vector3f(0,0,0);
    }

    public worldObject(tree tree){
        this();
        name="TREE_" + stencilId;
        isTree=true;
        myTree = tree;
        VBOHandles = GeometryFactory.treeVBOLineHandles(tree);
    }

    public void updateVBOs(){
        if(isTree){
            if(myTree!=null){
                VBOHandles = GeometryFactory.treeVBOQuadHandles(myTree);
                vertices = myTree.vertices;
            }
        }
    }

    public void logic(float dt){

    }

    public void move(float dt){
        position.translate(velocity.x*dt, velocity.y*dt, velocity.z*dt);
    }
}