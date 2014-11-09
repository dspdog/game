import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import shapes.tree;

import java.util.List;

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

    int[] treeVBOHandles = new int[2];
    int treeVerts = 0;
    int[] cubeMarcherVBOHandles = new int[2];
    int cubesVerts = 0;

    boolean handlesFound = false;

    public gameWorldRender(gameWorldLogic gl){
        myFOV = 75;
        myWidth = 1024;
        myHeight = 1024;
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
                    glDeleteBuffers(cubeMarcherVBOHandles[0]);
                    glDeleteBuffers(cubeMarcherVBOHandles[1]);
                    glDeleteBuffers(treeVBOHandles[0]);
                    glDeleteBuffers(treeVBOHandles[1]);
                }

                cubeMarcherVBOHandles = GeometryFactory.cubeMarcherVBOHandles(myLogic.cm);
                cubesVerts = myLogic.cm.theTriangles.size()*3;

                treeVBOHandles = GeometryFactory.treeVBOHandles(myLogic.theTree);
                treeVerts = myLogic.theTree.vertices;

                lastVBOUpdate=getTime();
                handlesFound=true;
            }

            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

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

        //CSG cube = new Cube(2).toCSG();
        CSG sphere = new Sphere(1.25).toCSG();

        Vector3f centerPt = new Vector3f(50,50,50);

        double rotationx = Mouse.getY();
        double rotationy = Mouse.getX();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        glLoadIdentity();
        gluLookAt(500,500,500,centerPt.x,centerPt.y,centerPt.z,0,1,0);
        glPushMatrix();
            glScalef(5f, 5f, 5f);
            glTranslatef(centerPt.x, centerPt.y, centerPt.z);
            glRotatef((float) rotationy, 0f, 1f, 0f);
            glRotatef((float) rotationx, 1f, 0f, 0f);
            glTranslatef(-centerPt.x, -centerPt.y, -centerPt.z);
            GeometryFactory.plane();
            GeometryFactory.drawPolys(sphere.getPolygons());
            if(handlesFound){
                GeometryFactory.drawLinesByVBOHandles(treeVerts, treeVBOHandles);
                //List<Polygon>


                //GeometryFactory.drawTrisByVBOHandles(cubesVerts, cubeMarcherVBOHandles);
            }
        glPopMatrix();
    }


}