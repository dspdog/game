import eu.mihosoft.vrl.v3d.CSG;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import shapes.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.util.glu.GLU.*;

public class gameWorldRender {

    public static Vector3f cameraXVector = new Vector3f(0,0,0);
    public static Vector3f cameraYVector = new Vector3f(0,0,0);
    public static Vector3f cameraZVector = new Vector3f(0,0,0);

    private int fps;
    private long lastFPS;
    private long startTime;
    public int myFPS = 0;
    public int myWidth, myHeight;
    public float myFOV;

    long lastPrint=0;

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
        prepare3D();

        try {
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        myScene = new scene();
        //myScene.addWorldObject(new WorldObject(myTexture));
        //myLogic.theTree.updateCSG();
        myScene.addWorldObject(new WorldObject(myLogic.theTree));
        //myScene.addWorldObject(new WorldObject((x, y) -> (float)(1f-(SimplexNoise.noise(x/20f,y/20f)+1f)*(SimplexNoise.noise(x/20f,y/20f)+1f))*10f));
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



        //glClear(GL_ACCUM_BUFFER_BIT);

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



        /*glDrawBuffer(GL_FRONT);
        glAccum(GL_ACCUM, 1f);
        glDrawBuffer(GL_BACK);

        glAccum(GL_RETURN, 0.1f);//push bach to draw buffer
        */



        //sampling:

        IntBuffer ib = BufferUtils.createIntBuffer(1);
        //FloatBuffer fb = BufferUtils.createFloatBuffer(1);

        glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL_STENCIL_INDEX, GL_UNSIGNED_INT, ib);

        if(getTime() - lastPrint > 1000){
            lastPrint = getTime();
            if(myScene.idsMap.containsKey(ib.get(0)+"")){
                System.out.println("SELECTED " + myScene.idsMap.get(ib.get(0)+"").name );
            }else{
                System.out.println("SELECTED NONE ");
            }
        }


        //glReadPixels(Mouse.getX(), Mouse.getY() - 1, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, fb);
        //System.out.println("depth?" +fb.get(0));
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


    public static void getCamVectors(){//http://www.gamedev.net/topic/397751-how-to-get-camera-pos/
        FloatBuffer mdl = BufferUtils.createFloatBuffer(16);
        // save the current modelview matrix
        //glPushMatrix();
        // get the current modelview matrix
        GL11.glGetFloat(GL_MODELVIEW_MATRIX, mdl);

        cameraXVector = new Vector3f(mdl.get(0),mdl.get(4),mdl.get(8));
        cameraYVector = new Vector3f(mdl.get(1),mdl.get(5),mdl.get(9));
        cameraZVector = new Vector3f(mdl.get(2),mdl.get(6),mdl.get(10));

        cameraXVector.normalise();
        cameraYVector.normalise();
        cameraZVector.normalise();
    }

    class scene{
        private ArrayList<WorldObject> objs;
        public Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

        public void drawScene(){
            getCamVectors();
            for(WorldObject wo : objs){
                glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
                if(wo.isCSG){
                    GeometryFactory.drawTrisByVBOHandles(wo.myCSG.numTriangles, wo.VBOHandles);
                }else if(wo.isGrid){
                    GeometryFactory.drawTrisByVBOHandles(256*256*2, wo.VBOHandles);
                    //GeometryFactory.drawFunctionGrid(wo.myFunction);
                }else if (wo.isPlane){
                    GeometryFactory.plane(wo.myTexture);
                }else if (wo.isTree){
                    if(myLogic.lastGameLogic - lastVBOUpdate > 1){
                        wo.updateVBOs();
                        lastVBOUpdate=getTime();
                    }
                    GeometryFactory.drawQuadsByVBOHandles(wo.vertices, wo.VBOHandles);
                }
            }
        }

        public scene(){
            objs = new ArrayList<>();
        }

        public void addWorldObject(WorldObject wo){
            objs.add(wo);
            System.out.println("ADDING " + (wo.stencilId+1));
            idsMap.put((wo.stencilId+1)+"", wo);
        }
    }

    public class WorldObject{ //have this handle all the interactions w/ geometryfactory...
        CSG myCSG;
        tree myTree;
        Texture myTexture;
        GeometryFactory.gridFunction myFunction;
        int[] VBOHandles;

        String name="";

        int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
        int vertices = 0;
        boolean isCSG = false;
        boolean isGrid = false;
        boolean isPlane = false;
        boolean isTree = false;

        public WorldObject(CSG csg){
            name="CSG_" + stencilId;
            isCSG=true;
            myCSG = csg;
            csg.getTriangles();
            VBOHandles = GeometryFactory.csgVBOHandles(csg);
        }

        public WorldObject(tree tree){
            name="TREE_" + stencilId;
            isTree=true;
            myTree = tree;
            VBOHandles = GeometryFactory.treeVBOLineHandles(tree);
        }

        public void updateVBOs(){
            if(isTree){
                VBOHandles = GeometryFactory.treeVBOQuadHandles(myTree);
                vertices = myTree.vertices;
            }
        }

        public WorldObject(Texture texture){
            name="TEX_" + stencilId;
            isPlane = true;
            myTexture = texture;
        }

        public WorldObject(GeometryFactory.gridFunction d){
            name="GRID_" + stencilId;
            isGrid=true;
            myFunction = d;
            VBOHandles = GeometryFactory.gridVBOHandles(d);
        }
    }
}