import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import utils.StringHelper;

/**
 * Created by user on 1/23/2015.
 */
public class gameInputs {

    static boolean consoleIsEnabled = false;
    static String inputString = "";

    static float mouseX =0;
    static float mouseY =0;

    static boolean MOVING_LEFT=false;
    static boolean MOVING_RIGHT=false;
    static boolean MOVING_FORWARD=false;
    static boolean MOVING_BACKWARD=false;
    static boolean MOVING_UP=false;
    static boolean MOVING_DOWN=false;

    static public void pollInputs() { //adapted from http://ninjacave.com/lwjglbasics2
        mouseX = Mouse.getX();
        mouseY = Mouse.getY();

        MOVING_LEFT=Keyboard.isKeyDown(Keyboard.KEY_A);
        MOVING_RIGHT=Keyboard.isKeyDown(Keyboard.KEY_D);
        MOVING_FORWARD=Keyboard.isKeyDown(Keyboard.KEY_W);
        MOVING_BACKWARD=Keyboard.isKeyDown(Keyboard.KEY_S);

        MOVING_UP=Keyboard.isKeyDown(Keyboard.KEY_Q);
        MOVING_DOWN=Keyboard.isKeyDown(Keyboard.KEY_E);
        
        if (Mouse.isButtonDown(0)) {


            //System.out.println("MOUSE DOWN @ X: " + x + " Y: " + y);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            //System.out.println("SPACE KEY IS DOWN");
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_GRAVE ) {
                    consoleIsEnabled = !consoleIsEnabled;
                }else{
                    if(consoleIsEnabled){
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
            } else {
                //if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                //    System.out.println("A Key Released");
                //}
            }

            inputString = StringHelper.stripNonPrinting(inputString);
        }
    }
}
