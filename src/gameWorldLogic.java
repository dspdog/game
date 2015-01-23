import org.lwjgl.Sys;
import shapes.tree;
import utils.CubeMarcher;

public class gameWorldLogic implements Runnable {

    boolean running = false;
    long lastGameLogic;

    tree theTree;

    public CubeMarcher cm = new CubeMarcher();
    public gameWorldLogic(){

    }

    public void updateGameLogic(){
        tree.cameraXVector.set(gameWorldRender.cameraXVector);
        tree.cameraYVector.set(gameWorldRender.cameraYVector);
        tree.cameraZVector.set(gameWorldRender.cameraZVector);
        theTree.perturb(false, false, 0.1250f*4f);

        //theTree.updateCSG();
        //cm.generateTris(theTree);
        lastGameLogic = getTime();
    }

    public void end(){
        running=false;
    }

    public void initWorld(){
        theTree = new tree();
        theTree.setToPreset(9); //1 or 0 or 9
        theTree.reIndex();
    }

    @Override
    public void run() {

        initWorld();

        running=true;
        while(running){
            try {
                updateGameLogic();
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
