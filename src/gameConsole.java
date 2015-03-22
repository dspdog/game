import factory.GeometryFactory;
import factory.TextureFactory;
import org.lwjgl.util.vector.Vector3f;
import utils.StringHelper;
import utils.glHelper;
import utils.time;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by user on 1/23/2015.
 */
public class GameConsole {

    static int consoleTexture = 0;
    static long lastConsoleUpdate = 0;
    static String statusString = "";
    static String inputString = "";

    static long updatePeriodMS = 200;

    static long startTime = time.getTime();

    public static void setStatusString(String newString){statusString = newString;}
    public static void setInputString(String newString){
        inputString = newString;
    }

    public static Vector3f pos = new Vector3f(0,0,0);
    public static Vector3f size = new Vector3f(512,512,0);

    public static void setPos(int x, int y){
        pos.set(x,y,0);
    }

    public static void setSize(int w, int h){
        size.set(w,h,0);
    }

    private static void updateInputString(){
        String str = "";
        if(GameInputs.consoleIsEnabled){
            str+=">>>";
            str+= GameInputs.inputString;
            if((time.getTime() - startTime)%500<250){ //blink at 2hz
                str+="_";
            }
        }else{
            str+="\nPRESS ~ FOR CONSOLE";
        }
        inputString = str;
    }

    public static void draw(int screenwidth, int screenheight, float alpha){

        int lines_To_Draw = 24;

        updateInputString();
        if (time.getTime() - lastConsoleUpdate > updatePeriodMS) {
            lastConsoleUpdate = time.getTime();
            String theString = statusString + "\n" + (GameInputs.consoleIsEnabled ? "\n" + StringHelper.getLastXLines(gameCommands.commandString, lines_To_Draw) : "") + inputString;
            consoleTexture = getConsoleTexture(theString, (int)size.x, (int)size.y, alpha, GameInputs.consoleIsEnabled);
        }
        glHelper.enableTransparency();
        glHelper.prepare2D(screenwidth, screenheight);
        GeometryFactory.plane2D(consoleTexture, (int)size.x, (int)size.y, pos.x, pos.y, pos.z);
    }

    static final BufferedImage img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
    static final Graphics2D g2d = img.createGraphics();
    static public int getConsoleTexture(String str, int width, int height, float alpha, boolean background){  //http://www.java-gaming.org/index.php?topic=25516.0

        g2d.setBackground(new Color(255, 255, 255, 0));
        g2d.clearRect(0, 0, 512, 512);

        if(background){
            g2d.setColor(new Color(0.25f,0.25f,0.25f,alpha));
            g2d.fillRect(0,0,512,512);
        }
        drawOutlinedConsoleText(str, g2d);
        return TextureFactory.loadTexture(img);
    }

    static public void drawOutlinedConsoleText(String str, Graphics2D g2d){
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        String[] splitData = str.split("\n");
        int yOffset=0;
        int pixelPerLine = 14;

        int baseX = 4;
        int baseY = 18;

        Color shadowColor = Color.black;
        Color errorColor = Color.orange;
        Color confirmColor = Color.yellow;
        Color submittedColor = Color.gray;
        Color cmdLineColor = Color.white;

        String errorCode = gameCommands.errorCode;
        String confirmCode = gameCommands.confirmCode;
        String submitCode = gameCommands.submitCode;

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
