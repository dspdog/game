import factory.TextureFactory;
import utils.glHelper;
import utils.time;

/**
 * Created by user on 1/23/2015.
 */
public class gameConsole {

    static String defaultHelpMsg = "Default help string!";

    static int consoleTexture = 0;
    static long lastConsoleUpdate = 0;
    static String statusString = "";
    static String inputString = "";
    static String commandString =  defaultHelpMsg + "\n";
    static long updatePeriodMS = 100;

    static long startTime = time.getTime();

    public static void setStatusString(String newString){statusString = newString;}
    public static void setInputString(String newString){
        inputString = newString;
    }

    public static void submitCommand(String cmd){
        commandString+=cmd+"\n";
        commandString+=parseCommand(cmd) + "\n";
    }

    private static String parseCommand(String cmd){
        return "Unrecognized command: " + cmd + ". " + defaultHelpMsg;
    }

    private static void updateInputString(){
        String str = "";
        if(gameInputs.consoleIsEnabled){
            str+=">>>";
            str+=gameInputs.inputString;
            if((time.getTime() - startTime)%500<250){ //blink at 2hz
                str+="_";
            }
        }else{
            str+="\nPRESS ~ FOR CONSOLE";
        }
        inputString = str;
    }

    private static String getLastXLines(String str, int x){
        String newStr="";
        String[] splitData = str.split("\n");

        for(int i=0; i<splitData.length; i++){
            if(splitData.length-i<=12){
                newStr=newStr.concat(splitData[i] + "\n");
            }
        }

        return newStr;
    }

    public static void draw(int screenwidth, int screenheight, int consoleWidth, int consoleHeight, float x, float y, float z){
        updateInputString();
        if (time.getTime() - lastConsoleUpdate > updatePeriodMS) {
            lastConsoleUpdate = time.getTime();
            String theString = statusString + "\n" + (gameInputs.consoleIsEnabled ? "\n" + getLastXLines(commandString, 12) : "") + inputString;
            consoleTexture = TextureFactory.consoleTexture(theString, consoleWidth, consoleHeight, gameInputs.consoleIsEnabled);
        }
        glHelper.enableTransparency();
        glHelper.prepare2D(screenwidth, screenheight);
        GeometryFactory.plane2D(consoleTexture, consoleWidth, consoleHeight, x, y, z);
    }
}
