import eu.mihosoft.vrl.v3d.CSG;
import org.newdawn.slick.opengl.Texture;
import shapes.tree;

public class worldObject { //have this handle all the interactions w/ geometryfactory...
    CSG myCSG;
    tree myTree;
    Texture myTexture;
    GeometryFactory.gridFunction myFunction;
    int[] VBOHandles;

    String name="";

    int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
    int vertices = 0;
    boolean isCSG = false;
    boolean isGrid = false;
    boolean isPlane = false;
    boolean isTree = false;

    public worldObject(tree tree){
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
}