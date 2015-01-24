import factory.TextureFactory;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import utils.glHelper;
import utils.time;

/**
 * Created by user on 1/23/2015.
 */
public class gameConsole {
    static int consoleTexture = 0;
    static long lastConsoleUpdate = 0;
    static String consoleString = "";
    static long updatePeriodMS = 100;

    public static void setConsoleString(String newString){
        consoleString = newString;
    }

    public static void draw(int screenwidth, int screenheight, int consoleWidth, int consoleHeight, float x, float y, float z){
        if (time.getTime() - lastConsoleUpdate > updatePeriodMS) {
            lastConsoleUpdate = time.getTime();
            consoleTexture = TextureFactory.stringToTexture(consoleString, consoleWidth, consoleHeight);
        }
        glHelper.enableTransparency();
        glHelper.prepare2D(screenwidth, screenheight);
        GeometryFactory.plane2D(consoleTexture, consoleWidth, consoleHeight, x, y, z);
    }
}
