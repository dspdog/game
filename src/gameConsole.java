import factory.TextureFactory;
import utils.StringHelper;
import utils.glHelper;
import utils.time;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by user on 1/23/2015.
 */
public class gameConsole {

    static private String theCmds[][] = {
            {"persp", "perspective mode", "Perspective mode enabled! Use FOV to change field of view."},
            {"ortho", "orthogonal mode", "Ortho mode enabled! Use PERSP to enable perspective mode."},
            {"fov",   "field of view", "Field of view now __X"},
            {"top", "top view", "Now viewing top view"},
            {"side", "side view", "Now viewing side view"},
            {"front", "front view", "Now viewing front view"},
            {"orbit", "orbit camera mode", "Orbit camera mode enabled"},
            {"fly", "flying camera mode", "Flying camera mode enabled"},
            {"speed", "camera speed", "Camera speed now __X"},
            {"sync", "frame limiter", "Frame limit now __X"},
            {"unsync", "disable frame limiter", "Frame limiter disabled"},
            {"save", "save state", "State saved."},
            {"load", "load state", "State loaded."},
            {"shader", "pixel shader", "Now using __X shader"},
            {"details", "toggle render details", "Render details display toggled"},
            {"reset", "reset state", "Everything reset."}
    };

    static final String errorCode = "<ERROR>";
    static final String confirmCode = "<CONFIRM>";
    static final String submitCode = "<SUBMITTED>";

    static String defaultHelpMsg = "Type HELP to see a list of commands.";

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
        commandString+= submitCode + ">" + cmd+"\n";
        commandString+=parseCommand(cmd) + "\n";
    }

    private static String parseCommand(String cmd){
        String[] splitData = cmd.split(" ");

        String theCmd = cmd.toLowerCase();
        String theParam = " ";

        if(splitData.length>1){ //multi-arg commands
            theCmd = splitData[0];
            theParam = splitData[1];
        }

        for(int i=0; i<theCmds.length; i++){
            String cmdTitle = theCmds[i][0];
            String cmdDesc = theCmds[i][1];
            String cmdConfirmation = theCmds[i][2].replace("__X", theParam);

            if(theCmd.equals(cmdTitle)){

                if(theParam.equals(" ") && theCmds[i][2].contains("__X")){ //if user types wrong number of params
                    return "Missing parameter. Use " + cmdTitle + " X to set " + cmdDesc + " to X";
                }

                //TRY-CATCH TO ACTUALLY CHANGE STUFF

                return confirmCode + cmdConfirmation;
            }
        }

        return errorCode + "Unrecognized command: " + cmd + ". " + defaultHelpMsg;
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

    public static void draw(int screenwidth, int screenheight, int consoleWidth, int consoleHeight, float x, float y, float z){
        updateInputString();
        if (time.getTime() - lastConsoleUpdate > updatePeriodMS) {
            lastConsoleUpdate = time.getTime();
            String theString = statusString + "\n" + (gameInputs.consoleIsEnabled ? "\n" + StringHelper.getLastXLines(commandString, 12) : "") + inputString;
            consoleTexture = getConsoleTexture(theString, consoleWidth, consoleHeight, gameInputs.consoleIsEnabled);
        }
        glHelper.enableTransparency();
        glHelper.prepare2D(screenwidth, screenheight);
        GeometryFactory.plane2D(consoleTexture, consoleWidth, consoleHeight, x, y, z);
    }

    static public int getConsoleTexture(String str, int width, int height, boolean background){  //http://www.java-gaming.org/index.php?topic=25516.0
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        if(background){
            g2d.setColor(new Color(0.25f,0.25f,0.25f,0.75f));
            g2d.fillRect(0,0,width,height);
        }

        drawOutlinedConsoleText(str, g2d);

        return TextureFactory.loadTexture(img);
    }

    static public void drawOutlinedConsoleText(String str, Graphics2D g2d){
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));

        String[] splitData = str.split("\n");
        int yOffset=0;
        int pixelPerLine = 14;

        int baseX = 4;
        int baseY = 18;

        Color shadowColor = Color.black;
        Color errorColor = Color.red.darker();
        Color confirmColor = Color.yellow;
        Color submittedColor = Color.gray;
        Color cmdLineColor = Color.white;

        String errorCode = gameConsole.errorCode;
        String confirmCode = gameConsole.confirmCode;
        String submitCode = gameConsole.submitCode;

        g2d.setColor(shadowColor); //drop shadow

        for(int x=0; x<2; x++){
            for(int y=0; y<2; y++){
                if(!(x==0 && y==0)){
                    yOffset=0;
                    for (String eachSplit : splitData) {
                        String cleanString = eachSplit.replace(errorCode, "").replace(confirmCode, "").replace(submitCode, "");
                        g2d.drawString(cleanString, baseX+x, baseY+yOffset+y);
                        yOffset+=pixelPerLine;
                    }
                }
            }
        }

        g2d.setColor(cmdLineColor); //text
        yOffset=0;
        for (String eachSplit : splitData) {
            if(eachSplit.contains(errorCode)){g2d.setColor(errorColor);}
            else if(eachSplit.contains(confirmCode)){g2d.setColor(confirmColor);}
            else if(eachSplit.contains(submitCode)){g2d.setColor(submittedColor);}
            else{g2d.setColor(cmdLineColor);}

            String cleanString = eachSplit.replace(errorCode, "").replace(confirmCode, "").replace(submitCode, "");
            g2d.drawString(cleanString, baseX, baseY+yOffset);
            yOffset+=pixelPerLine;
        }
    }
}
