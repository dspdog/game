import factory.TextureFactory;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import shapes.geography.GeographyFactory;
import shapes.tree.tree;
import world.WorldObject;
import world.scene;

public class gameWorldLogic implements Runnable {

    boolean running = false;
    long lastGameLogic;


    private int fps;
    private long lastFPS;
    private long startTime;
    public static int myFPS = 0;

    tree theTree;

    scene myScene;

    public CubeMarcher cm = new CubeMarcher();
    public gameWorldLogic(scene _myScene){
        myScene = _myScene;
        lastFPS = getTime();
    }

    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            myFPS = fps;
            //System.out.println("logic FPS: " + myFPS);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }


    public void updateGameLogic(){
        //theTree.perturb(false, false, 2.50f);
        //theTree.getUpdatedCSG();
        //cm.generateTris(theTree);
        updateFPS();
        myScene.sceneLogic();
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
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
