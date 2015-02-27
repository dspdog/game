import org.lwjgl.util.vector.Vector3f;
import shapes.tree;
import utils.glHelper;
import utils.time;

public class MovementThread implements Runnable {

    public static int fps = 0;
    public static long lastFPS = 0;
    public static long startTime = 0;
    public static int mySyncFPS = 0;
    public static int myFPS = 0;

    private boolean running = false;
    public static long lastGameLogic;

    tree theTree;
    long frame=0;

    float rotationx=0;
    float rotationy=0;
    float rotationz=0;

    public MovementThread(){

    }

    void updateFPS() {
        if (time.getTime() - lastFPS > 1000) {
            myFPS = fps;
            fps = 0;
            lastFPS = time.getTime();
        }
        fps++;
    }

    void updateGameLogic(){
        frame++;
        float dt = (float)time.getDtMSf()/1000f/1000f;

        //System.out.println("dt" + dt);

        tree.cameraXVector.set(glHelper.cameraXVector);
        tree.cameraYVector.set(glHelper.cameraYVector);
        tree.cameraZVector.set(glHelper.cameraZVector);

        //gameScene.logicScene(dt);

        lastGameLogic = time.getTime();

        handleMouseInput(dt);
        handleKeyboardInput(dt);
        updateFPS();
    }

    void handleMouseInput(float dt){
        if(gameInputs.consoleIsEnabled)return; //skip rest of function if console is open
        rotationx = 2f * (gameInputs.mouseY - RenderThread.myHeight/2)/RenderThread.myHeight;
        rotationy = 180f -2f * (gameInputs.mouseX - RenderThread.myWidth/2)/RenderThread.myWidth;
    }

    void handleKeyboardInput(float dt){

        gameInputs.pollInputs();
        if(gameInputs.consoleIsEnabled)return; //skip rest of function if console is open
        Vector3f poi = gameScene.poi;

        float speed = dt;
        if(gameInputs.TURBO){speed*=5;}
        if(gameInputs.ANTI_TURBO){speed/=5;}

        if(gameInputs.SPINNING_CW){
            rotationz+=speed;
        }

        if(gameInputs.SPINNING_CCW){
            rotationz-=speed;
        }

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

        if(gameInputs.SAVE_CURRENT_OBJ){
            gameInputs.SAVE_CURRENT_OBJ=false;
            worldObject objectUnderMouse = RenderThread.worldObject_AtMouse();
            boolean somethingIsSelected = objectUnderMouse instanceof worldObject;
            if(somethingIsSelected){
                objectUnderMouse.save();
            }
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
        long frame=0;
        initWorld();
        running=true;

        while(running){
            try {
                frame++;
                updateGameLogic();
                //if(frame%10==0) //faster framerates
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
