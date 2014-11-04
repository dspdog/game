import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import shapes.tree;
import org.lwjgl.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    private int fps;
    private long lastFPS;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;

    long lastVBOUpdate=0;

    gameWorldLogic myLogic;

    int[] vboHandles = new int[2];
    int verts = 0;
    boolean handlesFound = false;

    public gameWorldRender(gameWorldLogic gl){
        myFOV = 75;
        myWidth = 640;
        myHeight = 480;
        myLogic=gl;
        handlesFound=false;
    }

    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            myFPS = fps;
            Display.setTitle("FPS: " + myFPS);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void start() {

        lastFPS = getTime(); //initialise lastFPS by setting to current Time



        try {
            Display.setDisplayMode(new DisplayMode(myWidth, myHeight));
            Display.create();

        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        initGL();

        while (!Display.isCloseRequested()) {
            update();
            renderGL();

            if(myLogic.lastGameLogic - lastVBOUpdate > 10){

                if(handlesFound){
                    glDeleteBuffers(vboHandles[0]);
                    glDeleteBuffers(vboHandles[1]);
                }

                vboHandles = GeometryFactory.treeVBOHandles(myLogic.theTree);
                verts = myLogic.theTree.vertices;
                lastVBOUpdate=getTime();
                handlesFound=true;
            }

            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

        glDeleteBuffers(vboHandles[0]);
        glDeleteBuffers(vboHandles[1]);

        myLogic.end();
        Display.destroy();
    }

    public void update() {
        updateFPS();
    }

    public void initGL() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(myFOV, ((float)myWidth) / ((float)myHeight), 0.01f, 2500f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    public void renderGL() {
        Vector3f centerPt = new Vector3f(50,50,50);

        double rotationx = Mouse.getY();
        double rotationy = Mouse.getX();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glLoadIdentity();
        gluLookAt(500,500,500,centerPt.x,centerPt.y,centerPt.z,0,1,0);
        glPushMatrix();
            glScalef(3.5f, 3.5f, 3.5f);
            glTranslatef(centerPt.x, centerPt.y, centerPt.z);
            glRotatef((float) rotationy, 0f, 1f, 0f);
            glRotatef((float) rotationx, 1f, 0f, 0f);
            glTranslatef(-centerPt.x, -centerPt.y, -centerPt.z);
            //GeometryFactory.plane();
            if(handlesFound){
                GeometryFactory.treeVBO(verts, vboHandles);
            }
        glPopMatrix();
    }

}