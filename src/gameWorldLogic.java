import org.lwjgl.Sys;
import shapes.tree;

public class gameWorldLogic implements Runnable {

    boolean running = false;
    long lastGameLogic;

    tree theTree;

    public CubeMarcher cm = new CubeMarcher();
    public gameWorldLogic(){

    }

    public void updateGameLogic(){
        theTree.perturb(false, false, 2.50f);
        cm.generateTris(theTree);
        lastGameLogic = getTime();
    }

    public void end(){
        running=false;
    }

    public void initWorld(){
        theTree = new tree();
        theTree.setToPreset(0); //1 or 0 or 9
        theTree.reIndex();
    }

    @Override
    public void run() {

        initWorld();

        running=true;
        while(running){
            try {
                updateGameLogic();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
