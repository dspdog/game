import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.theme.ThemeManager;
import factory.GeometryFactory;
import factory.TextureFactory;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import shapes.cloud.kParticleCloud;
import utils.ShaderHelper;
import world.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    private LWJGLRenderer renderer;
    private ThemeManager theme;
    private GUI gui;
    private Widget root;

    private int fps;
    private long lastFPS;
    private long startTime;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;
    public gameInputs myInput;

    scene myScene;

    gameWorldLogic myLogic;

    public static float scrollPos = 1.0f;

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

        //WorldObject theGround = new WorldObject((float x, float y, float t) -> GeographyFactory.bowl(x, y, t)).setColor(0,1.0f,0);
        //WorldObject theWaves = new WorldObject((float x, float y, float t) -> GeographyFactory.oceanWaves(x, y, t)).setUpdateInterval(10).setColor(0,0,1.0f);

        WorldObject kCloud = new WorldObject(kParticleCloud.PARTICLES_MAX).setUpdateInterval(5);

        myScene.myKCloud = kCloud.myKCloud;

        //myScene.addWorldObject(new WorldObject(myLogic.theTree.getUpdatedCSG()));

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
        setupGui();
        //bindShaders();
        while (!Display.isCloseRequested()) {
            update();

            myScene.myKCloud.armLen = scrollPos * 100f;
            renderGL();
            myInput.pollInput();
            Display.update();
            Display.sync(60); // cap fps to 60fps
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

        //glEnable(GL_DEPTH_TEST);
        //glDepthFunc(GL_LEQUAL);
        //glDepthFunc(GL_NEVER);
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

    public void setupGui(){
        try {
            renderer = new LWJGLRenderer();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        try {
            File theThemeFile = new File("./res/simple_demo.xml");
            theme = ThemeManager.createThemeManager(theThemeFile.toURI().toURL(), renderer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        root = new Widget();
        root.setTheme("");

        gui = new GUI(root, renderer);
        gui.setSize();
        gui.applyTheme(theme);


        addTestAlert(100, 100, "ASDASDASDASDASDASD");

    }

    private void addTestAlert(int x, int y, String text) {
        Alert alert = new Alert(text);
        alert.addButton("OK");
        alert.addButton("Cancel");
        alert.setPosition(x, y);
        root.add(alert);
        alert.adjustSize();
    }


    public class Alert extends ResizableFrame {

        private DialogLayout.Group buttonGroupH, buttonGroupV;
        private TextArea textArea;
        private ScrollPane scrollPane;

        public Alert(String text) {
            setTheme("/resizableframe");

            final HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel(text);
            textArea = new TextArea(textAreaModel);

            scrollPane = new ScrollPane(textArea);
            scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

            DialogLayout layout = new DialogLayout();

            buttonGroupH = layout.createSequentialGroup();
            buttonGroupH.addGap();
            buttonGroupV = layout.createParallelGroup();

            layout.setTheme("/alertbox");
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .addWidget(scrollPane)
                    .addGroup(buttonGroupH));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addWidget(scrollPane)
                    .addGroup(buttonGroupV));
            add(layout);
        }

        public void addButton(String text) {
            Button button = new Button(text);
            buttonGroupH.addWidget(button);
            buttonGroupV.addWidget(button);
        }
    }

    public void renderGL() {

        double rotationx = 90f- 180f * Mouse.getY()/myHeight;
        double rotationy = Mouse.getX();

        int window_width = Display.getWidth(); //glutGet(GLUT_WINDOW_WIDTH);
        int window_height = Display.getHeight();
        int index = 0;

        int scroll = Mouse.getDWheel();
        if(scroll<0){
            scrollPos*=0.95f;
        }else if(scroll>0){
            scrollPos*=1.05f;
        }

        float zoom = 5f*scrollPos;
        glClearColor(0.5f,0.5f,0.75f,1f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        //glEnable(GL_DEPTH_TEST);
        //glClear(GL_ACCUM_BUFFER_BIT);

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

        //gui.update(); //uncomment to see TWL gui

        /*glDrawBuffer(GL_FRONT);
        glAccum(GL_ACCUM, 1f);
        glDrawBuffer(GL_BACK);

        glAccum(GL_RETURN, 0.1f);//push bach to draw buffer
        */
    }

    int hudTexture = -1;
    long lastHudUpdate = 0;

    public void drawHud(){
        if(getTime() - lastHudUpdate > kParticleCloud.statusUpdateInterval){
            hudTexture = TextureFactory.proceduralTexture(
                    "Logic FPS: " + gameWorldLogic.myFPS  +
                    "\nOpenGL FPS: "+myFPS +
                    "\n" + myScene.myKCloud.statusString+
                    "\nParam1: " + gameInputs.param1+
                    "\nParam2: " + gameInputs.param2+
                    "\nParam3: " + gameInputs.param3+
                    "\nParam4: " + gameInputs.param4+
                    "\nParam5: " + gameInputs.param5+
                    "\nParam6: " + gameInputs.param6+
                    "\nParam7: " + gameInputs.param7+
                    "\nParam8: " + gameInputs.param8+
                    "\nParam9: " + gameInputs.param9);
            lastHudUpdate = getTime();
        }
        glEnable (GL_BLEND);
        glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if(hudTexture!=-1)GeometryFactory.plane(hudTexture, 512);
    }


}