import factory.TextureFactory;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import utils.glHelper;

/**
 * Created by user on 1/23/2015.
 */
public class gameConsole {
    static int consoleTexture = 0;
    static long lastConsoleUpdate = 0;
    static String consoleString = "";

    public static void setConsoleString(String newString){
        consoleString = newString;
    }

    public static void draw(int screenwidth, int screenheight, int consoleWidth, int consoleHeight){
        if (getTime() - lastConsoleUpdate > 100) {
            lastConsoleUpdate = getTime();
            consoleTexture = TextureFactory.stringToTexture(consoleString, consoleWidth, consoleHeight);
        }
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glHelper.prepare2D(screenwidth, screenheight);
        GeometryFactory.plane2D(consoleTexture, consoleWidth, consoleHeight, 512, 0);
    }

    public static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

}
