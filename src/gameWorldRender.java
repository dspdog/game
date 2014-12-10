import factory.TextureFactory;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import shapes.geography.GeographyFactory;
import utils.ShaderHelper;
import world.*;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    private int fps;
    private long lastFPS;
    private long startTime;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;
    public gameInputs myInput;

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
        myInput = new gameInputs(myScene);
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
        //bindShaders();
        while (!Display.isCloseRequested()) {
            update();
            renderGL();
            myInput.pollInput();
            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

        releaseShaders();
        myLogic.end();
        Display.destroy();
        System.exit(1);
    }

    private void bindShaders(){
        ShaderHelper.setupShaders("screen.vert", "screen.frag");
        if(ShaderHelper.useShader)
            ARBShaderObjects.glUseProgramObjectARB(ShaderHelper.program);
    }

    private void releaseShaders(){
        if(ShaderHelper.useShader)
            ARBShaderObjects.glUseProgramObjectARB(0);
    }

    public void update() {
        updateFPS();
    }



    public void initGL() {

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(myFOV, ((float)myWidth) / ((float)myHeight), 2.01f, 25000f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        myScene = new scene();
        //myScene.addWorldObject(new WorldObject(TextureFactory.proceduralTexture()));

        WorldObject theGround = new WorldObject((float x, float y, float t) -> GeographyFactory.bowl(x, y, t)).setColor(0,1.0f,0);
        WorldObject theWaves = new WorldObject((float x, float y, float t) -> GeographyFactory.oceanWaves(x, y, t)).setUpdateInterval(10).setColor(0,0,1.0f);

        WorldObject theParticles = new WorldObject(3000, theGround, TextureFactory.ballTexture());

        //myScene.addWorldObject(new WorldObject(myLogic.theTree.getUpdatedCSG()));

        //myScene.addWorldObject(theWaves);
        //myScene.addWorldObject(theGround);
        myScene.addWorldObject(theParticles);
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
        glClearColor(0.5f,0.5f,0.75f,1f);

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