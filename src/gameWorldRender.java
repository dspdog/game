import eu.mihosoft.vrl.v3d.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import utils.SimplexNoise;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

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
            pollInput();
            Display.update();
            //Display.sync(60); // cap fps to 60fps
        }

        myTexture.release();
        myLogic.end();
        Display.destroy();
        System.exit(1);
    }

    public void pollInput() {
        if (Mouse.isButtonDown(0)) {
            onMouseDown();
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
        myScene.addWorldObject(new WorldObject(TextureFactory.proceduralTexture()));
        myLogic.theTree.updateCSG();
        myScene.addWorldObject(new WorldObject(myLogic.theTree.myCSG));
        myScene.addWorldObject(new WorldObject((x, y) -> (float)(1f-(SimplexNoise.noise(x/20f,y/20f)+1f)*(SimplexNoise.noise(x/20f,y/20f)+1f))*10f));
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
        gluLookAt(myScene.cameraPos.x,myScene.cameraPos.y,myScene.cameraPos.z,myScene.focalPos.x,myScene.focalPos.y,myScene.focalPos.z,0,1,0);
        glPushMatrix();

            glTranslatef(myScene.focalPos.x, myScene.focalPos.y, myScene.focalPos.z);glScalef(zoom, zoom, zoom);
        glRotatef((float) rotationy, 0f, 1f, 0f);
            glRotatef((float) rotationx, 1f, 0f, 0f);
            glTranslatef(-myScene.focalPos.x, -myScene.focalPos.y, -myScene.focalPos.z);

            myScene.drawScene();

        glPopMatrix();
    }


    class scene{
        private ArrayList<WorldObject> objs;
        public Map<String, WorldObject> idsMap = new HashMap<String, WorldObject>();

        public Vector3f focalPos = new Vector3f(0,0,0);
        public Vector3f cameraPos = new Vector3f(500,500,500);

        public void drawScene(){
            for(WorldObject wo : objs){
                glStencilFunc(GL_ALWAYS, wo.stencilId + 1, -1);
                if(wo.isCSG || wo.isGrid){
                    GeometryFactory.drawTrisByVBOHandles(wo.triangles, wo.VBOHandles);
                }else if (wo.isPlane){
                    GeometryFactory.billboardCheatSphericalBegin();
                    GeometryFactory.plane(wo.myTextureId);
                    GeometryFactory.billboardEnd();
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

        public void focusOn(WorldObject wo){
            focalPos = wo.getCenter();
        }
    }

    public class WorldObject{ //have this handle all the interactions w/ geometryfactory...
        CSG myCSG;
        int myTextureId;
        GeometryFactory.gridFunction myFunction;
        GeometryFactory.gridFunction3d myFunction3d;
        int[] VBOHandles;
        int triangles = 0;
        String name="";

        int stencilId = (int)(System.currentTimeMillis()%255); //for stencil buffer
        boolean isCSG = false;
        boolean isGrid = false;
        boolean isPlane = false;

        public WorldObject(CSG csg){
            name="CSG_" + stencilId;
            isCSG=true;
            myCSG = csg;
            triangles = GeometryFactory.getTriangles(csg);
            VBOHandles = GeometryFactory.VBOHandles(GeometryFactory.getCSGVertexData(csg, triangles));
        }

        public WorldObject(Texture texture){
            name="TEX_" + stencilId;
            isPlane = true;
            myTextureId = texture.getTextureID();
        }

        public WorldObject(int textureId){
            name="TEX_" + stencilId;
            isPlane = true;
            myTextureId = textureId;
        }

        public WorldObject(GeometryFactory.gridFunction d){
            name="GRID_" + stencilId;
            isGrid=true;
            myFunction = d;
            VBOHandles = GeometryFactory.VBOHandles(GeometryFactory.functionGridVertexData(d));
            triangles = GeometryFactory.gridSize*GeometryFactory.gridSize*2;
        }

        public Vector3f getCenter(){
            if(isGrid){
                return new Vector3f(GeometryFactory.gridSize/2, 0, GeometryFactory.gridSize/2);
            }else if(isCSG){
                Vector3d center = myCSG.getBounds().getCenter();
                return new Vector3f((float)center.x, (float)center.y, (float)center.z);
            }
            return new Vector3f(GeometryFactory.gridSize/2, 0, GeometryFactory.gridSize/2);
        }
    }

}