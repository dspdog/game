import org.lwjgl.examples.Game;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import utils.StringHelper;

/**
 * Created by user on 1/23/2015.
 */
public class GameInputs {

    static boolean consoleIsEnabled = false;
    static String inputString = "";

    static float mouseX =0;
    static float mouseY =0;
    static float cursorX =0;
    static float cursorY =0;

    static boolean MOVING_LEFT=false;
    static boolean MOVING_RIGHT=false;
    static boolean MOVING_FORWARD=false;
    static boolean MOVING_BACKWARD=false;
    static boolean MOVING_UP=false;
    static boolean MOVING_DOWN=false;

    static boolean SPINNING_CW=false;
    static boolean SPINNING_CCW=false;

    static boolean TURBO=false;
    static boolean ANTI_TURBO=false;
    static boolean SAVE_CURRENT_OBJ = false;

    static boolean endProgram = false;

    static boolean isDragging = false;
    static boolean isDraggingLeft = false;
    static boolean isDraggingRight = false;
    static Vector3f dragStart = new Vector3f();
    static Vector3f dragEnd = new Vector3f();
    static long dragEndTime = 0;

    static public void clickEvent(){
       WorldObject origObj = RenderThread.worldObject_AtMouse();
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
            WorldObject dupe = GameScene.dupeObj(origObj);
            GameScene.selectObj(dupe);
        }else{
            GameScene.selectObj(origObj);
        }
    }

    static public void pollInputs() { //adapted from http://ninjacave.com/lwjglbasics2
        Mouse.setGrabbed(true);

        if(consoleIsEnabled){
            Mouse.setClipMouseCoordinatesToWindow(true);
            cursorX = Mouse.getX();
            cursorY = Mouse.getY();
        }else{
            Mouse.setClipMouseCoordinatesToWindow(false);
            cursorX = RenderThread.myWidth/2;
            cursorY = RenderThread.myHeight/2;
        }

        mouseX = Mouse.getX();
        mouseY = Mouse.getY();

        MOVING_LEFT=Keyboard.isKeyDown(Keyboard.KEY_A);
        MOVING_RIGHT=Keyboard.isKeyDown(Keyboard.KEY_D);
        MOVING_FORWARD=Keyboard.isKeyDown(Keyboard.KEY_W);
        MOVING_BACKWARD=Keyboard.isKeyDown(Keyboard.KEY_S);

        MOVING_UP=Keyboard.isKeyDown(Keyboard.KEY_Q);
        MOVING_DOWN=Keyboard.isKeyDown(Keyboard.KEY_E);

        SPINNING_CW=Keyboard.isKeyDown(Keyboard.KEY_R);
        SPINNING_CCW=Keyboard.isKeyDown(Keyboard.KEY_F);

        TURBO=Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        ANTI_TURBO=Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);

        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))endProgram=true;

        isDraggingLeft=Mouse.isButtonDown(0);
        isDraggingRight=Mouse.isButtonDown(1);

        if (isDraggingLeft || isDraggingRight) { //any normal click...
            if(!isDragging){
                clickEvent();
                isDragging=true;
                dragStart = new Vector3f(mouseX,mouseY,0);
            }
        }

        if (!isDraggingRight && !isDraggingLeft) { //unclicked
            if(isDragging){
                isDragging=false;
                dragEnd = new Vector3f(mouseX,mouseY,0);
                dragEndTime = System.currentTimeMillis();
                Mouse.setCursorPosition((int)dragStart.x,(int)dragStart.y);
            }
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_GRAVE ) {
                    consoleIsEnabled = !consoleIsEnabled;
                }else{
                    if(consoleIsEnabled){
                        handleConsoleInput();
                    }
                }
            } else {
                //if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                //    GameScene.dupeObj(GameScene.selectedObj);
                //}
            }
            inputString = StringHelper.stripNonPrinting(inputString);
        }
    }

    public static void handleConsoleInput(){
        if (Keyboard.getEventKey() == Keyboard.KEY_BACK ) { //CONSOLE BACKSPACE
            if(inputString.length()>0)
                inputString=inputString.substring(0, inputString.length()-1);
        }else{
            if (Keyboard.getEventKey() == Keyboard.KEY_RETURN ) { //CONSOLE SUBMIT (RETURN KEY)
                if(!inputString.equals("")){gameCommands.submitCommand(inputString);}
                inputString="";
            }else{
                inputString+=(Keyboard.getEventCharacter()); //CONSOLE INPUT
            }
        }
    }
}
