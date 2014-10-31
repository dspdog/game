import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.util.glu.GLU.*;

public class game {

    public void start() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();

        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        initGL(); // init OpenGL

        while (!Display.isCloseRequested()) {
            update();
            renderGL();

            Display.update();
            Display.sync(60); // cap fps to 60fps
        }

        Display.destroy();
    }

    public void update() {

    }

    public void initGL() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        gluPerspective(85.0f, ((float)800) / ((float)600), 0.01f, 500f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public void renderGL() {
        double rotationx = Mouse.getX();
        double rotationy = Mouse.getY();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glLoadIdentity();
        gluLookAt(100,100,100,50,50,50,0,1,0);
        GL11.glPushMatrix();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            GL11.glTranslatef(50,50,50);
            GL11.glRotatef((float) rotationx, 1f, 0f, 0f);
            GL11.glRotatef((float) rotationy, 0f, 1f, 0f);
            GL11.glTranslatef(-50,-50,-50);
            GeometryFactory.gridCube(100);
        GL11.glPopMatrix();
    }

    public static void main(String[] argv) {
        game fullscreenExample = new game();
        fullscreenExample.start();
    }
}