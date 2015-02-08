import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import shapes.tree;
import utils.glHelper;
import utils.time;

public class LogicThread implements Runnable {

    private boolean running = false;
    public static long lastGameLogic;

    tree theTree;
    long frame=0;

    float rotationx=0;
    float rotationy=0;
    float rotationz=0;

    public LogicThread(){

    }

    void updateGameLogic(){
        frame++;
        float dt = time.getDtMS()*0.1f;

        tree.cameraXVector.set(glHelper.cameraXVector);
        tree.cameraYVector.set(glHelper.cameraYVector);
        tree.cameraZVector.set(glHelper.cameraZVector);

        gameScene.logicScene(dt);

        lastGameLogic = time.getTime();

        handleMouseInput(dt);
        handleKeyboardInput(dt);
    }

    void handleMouseInput(float dt){
        if(gameInputs.consoleIsEnabled)return; //skip rest of function if console is open
        rotationx = -180f * gameInputs.mouseY / RenderThread.myHeight;
        rotationy = gameInputs.mouseX;
        rotationz = 0;
    }

    void handleKeyboardInput(float dt){
        gameInputs.pollInputs();
        if(gameInputs.consoleIsEnabled)return; //skip rest of function if console is open
        Vector3f poi = gameScene.poi;

        float speed = dt;
        if(gameInputs.TURBO){speed*=5;}

        if(gameInputs.MOVING_FORWARD){
            poi.translate(
                    glHelper.cameraZVector.x*speed,
                    glHelper.cameraZVector.y*speed,
                    glHelper.cameraZVector.z*speed);
        }

        if(gameInputs.MOVING_BACKWARD){
            poi.translate(
                    -glHelper.cameraZVector.x*speed,
                    -glHelper.cameraZVector.y*speed,
                    -glHelper.cameraZVector.z*speed);
        }

        if(gameInputs.MOVING_RIGHT){
            poi.translate(
                    -glHelper.cameraXVector.x*speed,
                    -glHelper.cameraXVector.y*speed,
                    -glHelper.cameraXVector.z*speed);
        }

        if(gameInputs.MOVING_LEFT){
            poi.translate(
                    glHelper.cameraXVector.x*speed,
                    glHelper.cameraXVector.y*speed,
                    glHelper.cameraXVector.z*speed);
        }

        if(gameInputs.MOVING_UP){
            poi.translate(
                    glHelper.cameraYVector.x*speed,
                    glHelper.cameraYVector.y*speed,
                    glHelper.cameraYVector.z*speed);
        }

        if(gameInputs.MOVING_DOWN){
            poi.translate(
                    -glHelper.cameraYVector.x*speed,
                    -glHelper.cameraYVector.y*speed,
                    -glHelper.cameraYVector.z*speed);
        }
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
