import org.lwjgl.Sys;
import shapes.tree;
import utils.glHelper;
import utils.time;

public class LogicThread implements Runnable {

    private boolean running = false;
    public static long lastGameLogic;

    tree theTree;

    public LogicThread(){

    }

    void updateGameLogic(){

        float dt = time.getDtMS()*0.1f;

        tree.cameraXVector.set(glHelper.cameraXVector);
        tree.cameraYVector.set(glHelper.cameraYVector);
        tree.cameraZVector.set(glHelper.cameraZVector);
        theTree.perturb(false, false, 0.1250f*dt);

        //theTree.updateCSG();
        //cm.generateTris(theTree);
        lastGameLogic = time.getTime();

        gameInputs.pollInputs();
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
}
