import factory.TextureFactory;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import shapes.geography.GeographyFactory;
import world.*;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    private int fps;
    private long lastFPS;
    private long startTime;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;


    scene myScene;

    gameWorldLogic myLogic;

    public float scrollPos = 1.0f;

    boolean handlesFound = false;

    public gameWorldRender(gameWorldLogic gl){
        myFOV = 75;
        myWidth = 1024;
        myHeight = 1024;
        myLogic=gl;
        myScene=gl.myScene;
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
            pollInput();
            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

        myLogic.end();
        Display.destroy();
        System.exit(1);
    }

    boolean W_down = false;
    boolean A_down = false;
    boolean S_down = false;
    boolean D_down = false;
    boolean Q_down = false;
    boolean E_down = false;

    public void pollInput() {
        if (Mouse.isButtonDown(0)) {
            onMouseDown();
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                    A_down=true;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                    S_down=true;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                    D_down=true;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                    W_down=true;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
                    Q_down=true;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_E) {
                    E_down=true;
                }
            } else {
                if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                    A_down=false;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                    S_down=false;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                    D_down=false;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                    W_down=false;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
                    Q_down=false;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_E) {
                    E_down=false;
                }
            }
        }

        float step = 0.1f;
        if(A_down){
            scene.cameraPosDesired.x+=step;
        }
        if(S_down){
            scene.cameraPosDesired.z+=step;
        }
        if(W_down){
            scene.cameraPosDesired.z-=step;
        }
        if(D_down){
            scene.cameraPosDesired.x-=step;
        }
        if(Q_down){
            scene.cameraPosDesired.y-=step;
        }
        if(E_down){
            scene.cameraPosDesired.y+=step;
        }
    }

    public void onMouseDown(){

        //sampling:

        IntBuffer ib = BufferUtils.createIntBuffer(1);
        //FloatBuffer fb = BufferUtils.createFloatBuffer(1);

        glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL_STENCIL_INDEX, GL_UNSIGNED_INT, ib);

        if(myScene.idsMap.containsKey(ib.get(0)+"")){
            System.out.println("SELECTED " + myScene.idsMap.get(ib.get(0)+"").name );
            myScene.focusOn(myScene.idsMap.get(ib.get(0) + ""));
        }else{
            System.out.println("SELECTED NONE ");
        }

        //glReadPixels(Mouse.getX(), Mouse.getY() - 1, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, fb);
        //System.out.println("depth?" +fb.get(0));
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


        myScene = new scene();
        //myScene.addWorldObject(new WorldObject(TextureFactory.proceduralTexture()));
        //myLogic.theTree.updateCSG();
        //myScene.addWorldObject(new WorldObject(myLogic.theTree.myCSG));

        myScene.addWorldObject(new WorldObject((float x, float y, float t) -> GeographyFactory.oceanWaves(x, y, t), 1));
        myScene.addWorldObject(new WorldObject((float x, float y, float t) -> GeographyFactory.island(x, y, t), 1000));
    }

    public void renderGL() {

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
        glClearColor(0.5f,0.5f,0.5f,1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        glLoadIdentity();
        //gluLookAt(myScene.cameraPosDesired.x,myScene.cameraPosDesired.y,myScene.cameraPosDesired.z,myScene.focalPos.x,myScene.focalPos.y,myScene.focalPos.z,0,1,0);
        glPushMatrix();
        glRotatef((float) rotationx, 1f, 0f, 0f);
        glRotatef((float) rotationy, 0f, 1f, 0f);
            glTranslatef(myScene.cameraPosDesired.x, myScene.cameraPosDesired.y, myScene.cameraPosDesired.z);
        //glScalef(zoom, zoom, zoom);


            //glTranslatef(-myScene.cameraPosDesired.x, -myScene.cameraPosDesired.y, -myScene.cameraPosDesired.z);

            myScene.drawScene();

        glPopMatrix();
    }




}