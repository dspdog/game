import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Sphere;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import shapes.tree;
import sun.font.TrueTypeFont;
import utils.SimplexNoise;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    private int fps;
    private long lastFPS;
    private long startTime;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;

    long lastVBOUpdate=0;

    gameWorldLogic myLogic;

    public float scrollPos = 1.0f;

    boolean handlesFound = false;

    public gameWorldRender(gameWorldLogic gl){
        myFOV = 75;
        myWidth = 1024;
        myHeight = 1024;
        myLogic=gl;
        handlesFound=false;
        startTime=getTime();

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
            //Display.create(new PixelFormat(0, 16, 8, 16)); //anti aliasing 16x max
            Display.create(new PixelFormat(0, 16, 8));
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        initGL();

        while (!Display.isCloseRequested()) {
            update();
            renderGL();
            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

        myTexture.release();
        myLogic.end();
        Display.destroy();
        System.exit(1);
    }

    public void update() {
        updateFPS();
    }

    Texture myTexture;
    scene myScene;

    public void initGL() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(myFOV, ((float)myWidth) / ((float)myHeight), 0.01f, 2500f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        try {
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        myScene = new scene();
        myScene.addWorldObject(new WorldObject(myTexture));
        myLogic.theTree.updateCSG();
        myScene.addWorldObject(new WorldObject(myLogic.theTree.myCSG));
        myScene.addWorldObject(new WorldObject((x, y) -> (float)(SimplexNoise.noise(x/20f,y/20f)+1f)*2f));
    }

    public void renderGL() {
        Vector3f centerPt = new Vector3f(50,50,50);

        double rotationx = 90f- 180f * Mouse.getY()/myHeight;
        double rotationy = Mouse.getX();

        int scroll = Mouse.getDWheel();

        int window_width = Display.getWidth(); //glutGet(GLUT_WINDOW_WIDTH);
        int window_height = Display.getHeight();
        int index = 0;


        if(scroll<0){
            scrollPos*=0.95f;
        }else if(scroll>0){
            scrollPos*=1.05f;
        }

        float zoom = 5f*scrollPos;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        glLoadIdentity();
        gluLookAt(500,500,500,centerPt.x,centerPt.y,centerPt.z,0,1,0);
        glPushMatrix();
            glScalef(zoom, zoom, zoom);
            glTranslatef(centerPt.x, centerPt.y, centerPt.z);
        glRotatef((float) rotationy, 0f, 1f, 0f);
            glRotatef((float) rotationx, 1f, 0f, 0f);
            glTranslatef(-centerPt.x, -centerPt.y, -centerPt.z);

            myScene.drawScene();

        glPopMatrix();

        //sampling:

        //IntBuffer ib = BufferUtils.createIntBuffer(1);
        //FloatBuffer fb = BufferUtils.createFloatBuffer(1);

        //glReadPixels(Mouse.getX(), window_height - Mouse.getY() - 1, 1, 1, GL_STENCIL_INDEX, GL_UNSIGNED_INT, ib);
        //System.out.println("index?" +ib.get(0));

        //glReadPixels(Mouse.getX(), window_height - Mouse.getY() - 1, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, fb);
        //System.out.println("depth?" +fb.get(0));
    }

    class scene{
        private ArrayList<WorldObject> objs;

        public void drawScene(){
            for(WorldObject wo : objs){
                glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
                if(wo.isCSG){
                    GeometryFactory.drawTrisByVBOHandles(wo.myCSG.numTriangles, wo.VBOHandles);
                }else if(wo.isGrid){
                    GeometryFactory.drawFunctionGrid(wo.myFunction);
                }else if (wo.isPlane){
                    GeometryFactory.plane(wo.myTexture);
                }
            }
        }

        public scene(){
            objs = new ArrayList<>();
        }

        public void addWorldObject(WorldObject wo){
            objs.add(wo);
        }
    }

    public class WorldObject{ //have this handle all the interactions w/ geometryfactory...
        CSG myCSG;
        Texture myTexture;
        GeometryFactory.gridFunction myFunction;
        int[] VBOHandles;

        int stencilId = (int)(System.currentTimeMillis()%100); //for stencil buffer
        boolean isCSG = false;
        boolean isGrid = false;
        boolean isPlane = false;

        public WorldObject(CSG csg){
            isCSG=true;
            myCSG = csg;
            csg.getTriangles();
            VBOHandles = GeometryFactory.csgVBOHandles(csg);
            //System.out.println("ok! " + csg.numTriangles() + "tris");
        }

        public WorldObject(Texture texture){
            isPlane = true;
            myTexture = texture;
        }

        public WorldObject(GeometryFactory.gridFunction d){
            isGrid=true;
            myFunction = d;
            //TODO store grid in memory, no re-calc
        }
    }
}