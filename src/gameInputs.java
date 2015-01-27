import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import utils.StringHelper;

/**
 * Created by user on 1/23/2015.
 */
public class gameInputs {

    static boolean consoleIsEnabled = false;
    static String inputString = "";

    static public void pollInputs() { //adapted from http://ninjacave.com/lwjglbasics2

        if (Mouse.isButtonDown(0)) {
            int x = Mouse.getX();
            int y = Mouse.getY();

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
                                if(!inputString.equals("")){gameConsole.submitCommand(inputString);}
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
