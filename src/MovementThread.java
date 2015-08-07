import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import shapes.tree.tree;
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

    WorldObject objectUnderMouse;
    
    tree theTree;
    long frame=0;

    float rotationx=0;
    float rotationy=0;
    float rotationz=0;

    public static Vector3f dragVector = new Vector3f();
    static boolean lastRightClicked = false;
    static boolean lastLeftClicked = false;
    static float dragAmount=0;
    static float prevDragAmount=0;

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
        if(GameInputs.consoleIsEnabled)return; //skip rest of function if console is open

        if(!GameInputs.isDragging && System.currentTimeMillis()-GameInputs.dragEndTime>10){
            rotationx = 2f * (GameInputs.mouseY - RenderThread.myHeight/2)/RenderThread.myHeight;
            rotationy = 180f -2f * (GameInputs.mouseX - RenderThread.myWidth/2)/RenderThread.myWidth;
            prevDragAmount=0;
            dragAmount=0;
        }else{
            prevDragAmount = dragAmount;
            dragAmount = (GameInputs.dragStart.y-GameInputs.mouseY) + (GameInputs.dragStart.x-GameInputs.mouseX);
        }

        float amount = dragAmount-prevDragAmount;
        float dragScale = 0.1f;

        amount*=dragScale;

        dragVector= GameInputs.isDraggingLeft ?
                    (GameInputs.isDraggingRight ? new Vector3f(0,amount,0) : new Vector3f(amount,0,0)) :
                    (GameInputs.isDraggingRight ? new Vector3f(0,0,amount) : new Vector3f(0,0,0));

        if(GameScene.selectedObj!=null && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))){
            GameScene.selectedObj.shiftPos(dragVector);
            GameScene.selectObj(GameScene.selectedObj); //reset bounding box etc
        }

        lastLeftClicked=GameInputs.isDraggingLeft;
        lastRightClicked=GameInputs.isDraggingRight;
    }

    void handleKeyboardInput(float dt){

        GameInputs.pollInputs();
        if(GameInputs.consoleIsEnabled)return; //skip rest of function if console is open
        Vector3f poi = GameScene.poi;

        float speedScale = 30f;

        float speed = dt*speedScale;
        if(GameInputs.TURBO){speed*=5;}
        if(GameInputs.ANTI_TURBO){speed/=5;}

        if(GameInputs.SPINNING_CW){
            rotationz+=speed;
        }

        if(GameInputs.SPINNING_CCW){
            rotationz-=speed;
        }

        if(GameInputs.MOVING_FORWARD){
            poi.translate(
                    glHelper.cameraZVector.x*speed,
                    glHelper.cameraZVector.y*speed,
                    glHelper.cameraZVector.z*speed);
        }

        if(GameInputs.MOVING_BACKWARD){
            poi.translate(
                    -glHelper.cameraZVector.x*speed,
                    -glHelper.cameraZVector.y*speed,
                    -glHelper.cameraZVector.z*speed);
        }

        if(GameInputs.MOVING_RIGHT){
            poi.translate(
                    -glHelper.cameraXVector.x*speed,
                    -glHelper.cameraXVector.y*speed,
                    -glHelper.cameraXVector.z*speed);
        }

        if(GameInputs.MOVING_LEFT){
            poi.translate(
                    glHelper.cameraXVector.x*speed,
                    glHelper.cameraXVector.y*speed,
                    glHelper.cameraXVector.z*speed);
        }

        if(GameInputs.MOVING_UP){
            poi.translate(
                    glHelper.cameraYVector.x*speed,
                    glHelper.cameraYVector.y*speed,
                    glHelper.cameraYVector.z*speed);
        }

        if(GameInputs.MOVING_DOWN){
            poi.translate(
                    -glHelper.cameraYVector.x*speed,
                    -glHelper.cameraYVector.y*speed,
                    -glHelper.cameraYVector.z*speed);
        }

        if(GameInputs.SAVE_CURRENT_OBJ){
            GameInputs.SAVE_CURRENT_OBJ=false;
            objectUnderMouse = RenderThread.worldObject_AtMouse();
            boolean somethingIsSelected = objectUnderMouse instanceof WorldObject;
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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
