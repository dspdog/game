import factory.GeometryFactory;
import factory.TextureFactory;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.opengl.Texture;
import shapes.cloud.kParticleCloud;
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
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void initScene(){
        Display.setTitle("Sand Castles");
        myScene = new scene();

        WorldObject theGround = new WorldObject((float x, float y, float t) -> GeographyFactory.bowl(x, y, t)).setColor(0,1.0f,0);
        WorldObject theWaves = new WorldObject((float x, float y, float t) -> GeographyFactory.oceanWaves(x, y, t)).setUpdateInterval(10).setColor(0,0,1.0f);

        WorldObject kCloud = new WorldObject(kParticleCloud.PARTICLES_MAX).setUpdateInterval(10);

        myScene.myKCloud = kCloud.myKCloud;

        myScene.addWorldObject(new WorldObject(myLogic.theTree.getUpdatedCSG()));

        myScene.addWorldObject(kCloud);
    }

    public void start() {

        lastFPS = getTime(); //initialise lastFPS by setting to current Time

        try {
            Display.setDisplayMode(new DisplayMode(myWidth, myHeight));

            Display.create(new PixelFormat(0, 16, 8));
            //Display.create(new PixelFormat(0, 16, 8, 16)); //anti aliasing 16x max
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        prepare3D();
        initScene();
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

    public void prepare3D(){ //see http://gamedev.stackexchange.com/questions/18468/making-a-hud-gui-with-opengl-lwjgl
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(myFOV, ((float)myWidth) / ((float)myHeight), 0.01f, 5000f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glDepthFunc(GL_LEQUAL);
        glEnable(GL_DEPTH_TEST);
    }

    public void prepare2D(){ //see http://gamedev.stackexchange.com/questions/18468/making-a-hud-gui-with-opengl-lwjgl
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0, myWidth, myHeight, 0.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glTranslatef(0.375f, 0.375f, 0.0f); //?

        glDisable(GL_DEPTH_TEST);
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
        prepare3D();
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

        prepare2D();
        drawHud();

    }

    int hudTexture = -1;
    long lastHudUpdate = 0;
    long hudUpdatePeriod = 100;

    public void drawHud(){
        if(getTime() - lastHudUpdate > hudUpdatePeriod){
            hudTexture = TextureFactory.proceduralTexture("OpenGL FPS: "+myFPS + "\n" + myScene.myKCloud.statusString);

            lastHudUpdate = getTime();
        }
        glEnable (GL_BLEND);
        glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if(hudTexture!=-1)GeometryFactory.plane(hudTexture, 512);
    }


}