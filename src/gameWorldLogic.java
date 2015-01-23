import org.lwjgl.Sys;
import shapes.tree;
import utils.glHelper;

public class gameWorldLogic implements Runnable {

    private boolean running = false;
    long lastGameLogic;

    tree theTree;

    public gameWorldLogic(){

    }

    void updateGameLogic(){
        tree.cameraXVector.set(glHelper.cameraXVector);
        tree.cameraYVector.set(glHelper.cameraYVector);
        tree.cameraZVector.set(glHelper.cameraZVector);
        theTree.perturb(false, false, 0.1250f*4f);

        //theTree.updateCSG();
        //cm.generateTris(theTree);
        lastGameLogic = getTime();
    }

    public void end(){
        running=false;
    }

    void initWorld(){
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

    long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
