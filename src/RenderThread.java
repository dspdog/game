import factory.CSGFactory;
import factory.GeometryFactory;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import utils.glHelper;
import utils.time;


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.ARBPixelBufferObject.*;
public class RenderThread {

    public static int fps;
    public static long lastFPS;
    public static long startTime;
    public static int mySyncFPS;
    public static int myFPS = 0;
    public static int myWidth;
    public static int myHeight;

    public static worldObject cameraObj;

    public static boolean doFBOPass;
    public static boolean doProcessPixels;
    public static boolean doShaderOverlay;
    public static boolean doWireFrame;

    public static boolean doOrbitCamera;

    private int myFBOWidth;
    private int myFBOHeight;

    public static float myFOV;
    public static boolean useOrtho;


    // "index" is used to read pixels from framebuffer to a PBO
    // "nextIndex" is used to update pixels in the other PBO
    private int frame_index;// = (index + 1) % 2;
    private int frame_nextIndex;// = (index + 1) % 2;

    public static float rotationx = 0;
    public static float rotationy = 0;
    public static float rotationz = 0;

    static Vector3f cameraPos = new Vector3f(50,50,50);


    public static long lastVBOUpdate=0;

    private LogicThread myLogic;

    private float scrollPos = 1.0f;

    DecimalFormat addCommas = new DecimalFormat( "#,###,###,##0" );

    public RenderThread(LogicThread gl){
        myFOV = 75;
        myWidth = 1024;
        myHeight = 1024;

        useOrtho=false;
        doOrbitCamera=true;
        doProcessPixels=false;
        doShaderOverlay=false;
        doFBOPass=false;
        doWireFrame=false;

        myFBOHeight = 512;
        myFBOWidth = 512;

        myLogic=gl;

        startTime=time.getTime();

        GeometryFactory.doCSGSparkle = true;
    }

    void updateFPS() {
        if (time.getTime() - lastFPS > 1000) {
            myFPS = fps;
            fps = 0;
            lastFPS = time.getTime();
        }

        fps++;
    }

    private float mySurfaceTotal = 0;

    public void start() {
        frame_index = 0;
        frame_nextIndex = 0;
        lastFPS = time.getTime(); //initialise lastFPS by setting to current Time

        try {
            Display.setDisplayMode(new DisplayMode(myWidth, myHeight));
            //Display.create(new PixelFormat(0, 16, 8, 16)); //anti aliasing 16x max
            Display.create(new PixelFormat(0, 16, 8));
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        WorldFactory.buildWorld();
        initScreenCapture();

        while (!Display.isCloseRequested()) {
            update();
            renderGL();
            Display.update();
            Display.sync(mySyncFPS); // cap fps to 60fps
        }

        myLogic.end();
        Display.destroy();
        System.exit(1);
    }

    void update() {
        updateFPS();
    }

    private Texture myTexture;

    private int framebufferID;
    private int colorTextureID;
    private int depthRenderBufferID;

    static int stencilValue;

    private void initScreenCapture(){
        //http://wiki.lwjgl.org/index.php?title=Render_to_Texture_with_Frame_Buffer_Objects_%28FBO%29
        // init our fbo

        framebufferID = glGenFramebuffersEXT();                                         // create a new framebuffer
        colorTextureID = glGenTextures();                                               // and a new texture used as a color buffer
        depthRenderBufferID = glGenRenderbuffersEXT();                                  // And finally a new depthbuffer

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID);                        // switch to the new framebuffer

        // initialize color texture
        glBindTexture(GL_TEXTURE_2D, colorTextureID);                                   // Bind the colorbuffer texture
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);               // make it linear filterd
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, myFBOWidth, myFBOHeight, 0,GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);  // Create the texture data
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT,GL_COLOR_ATTACHMENT0_EXT,GL_TEXTURE_2D, colorTextureID, 0); // attach it to the framebuffer


        // initialize depth renderbuffer
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthRenderBufferID);                // bind the depth renderbuffer
        glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, myFBOWidth, myFBOHeight); // get the data space for it
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT,GL_DEPTH_ATTACHMENT_EXT,GL_RENDERBUFFER_EXT, depthRenderBufferID); // bind it to the renderbuffer

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);                                    // Swithch back to normal framebuffer rendering

        ////////////////////////////////////////////////////////

        glGenBuffersARB(pboIds);

        glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, pboIds.get(0));
        glBufferDataARB(GL_PIXEL_PACK_BUFFER_ARB, 512*512*3, GL_STREAM_READ_ARB);
        glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, pboIds.get(1));
        glBufferDataARB(GL_PIXEL_PACK_BUFFER_ARB, 512*512*3, GL_STREAM_READ_ARB);

        glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, 0);
    }

    private IntBuffer pboIds = BufferUtils.createIntBuffer(2);
    private ByteBuffer pixels = BufferUtils.createByteBuffer(myFBOWidth * myFBOHeight * 3);

    void renderGL() {

        frame_index = (frame_index + 1) % 2;
        frame_nextIndex = (frame_index + 1) % 2;

        glEnable(GL_DEPTH_TEST);
        glHelper.prepare3D(myWidth, myHeight, myFOV, useOrtho);
        glBindTexture(GL_TEXTURE_2D, 0);

        if(doFBOPass){
            // FBO render pass
            glViewport (0, 0, myFBOWidth, myFBOHeight);                                    // set The Current Viewport to the fbo size
            glBindTexture(GL_TEXTURE_2D, 0);                                // unlink textures because if we dont it all is gonna fail
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, framebufferID);        // switch to rendering on our FBO
            glClearColor (0.0f, 0.0f, 1.0f, 1.0f);
            glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);            // Clear Screen And Depth Buffer on the fbo to red
            cameraTransform();
            gameScene.drawScene();
        }

        // Normal render pass, draw cube with texture
        glViewport(0, 0, myWidth, myHeight);
        glEnable(GL_TEXTURE_2D);                                        // enable texturing
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);                    // switch to rendering on the framebuffer
        glClearColor(0.5f, 0.5f, 0.5f, 1f);

        //glBindTexture(GL_TEXTURE_2D, colorTextureID);                   // bind our FBO texture

        //normal render pass

        //glClear(GL_ACCUM_BUFFER_BIT);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        if(doWireFrame){glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );}
        cameraTransform();
        gameScene.drawScene();
        glPopMatrix();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        if(doShaderOverlay){
            GeometryFactory.shaderOverlay(colorTextureID, myWidth, myHeight);
        }

        /*glDrawBuffer(GL_BACK);
        glAccum(GL_ACCUM, 0.10f); //adding the current frame to the buffer
        glAccum(GL_RETURN, 0.490f); //Drawing last frame, saved in buffer
        glAccum(GL_MULT, 0.79f ); //make current frame in buffer dim
*/

        //glDrawBuffer(GL_FRONT);
        //glAccum(GL_ACCUM, 0.1f);
        //glDrawBuffer(GL_BACK);

        //glAccum(GL_RETURN, 0.01f);//push bach to draw buffer


        if(doProcessPixels){

            // set the target framebuffer to read
            glReadBuffer(GL_FRONT);

            // read pixels from framebuffer to PBO
            // glReadPixels() should return immediately.
            glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, pboIds.get(frame_index));
            glReadPixels(0, 0, 512, 512, GL_RGB, GL_UNSIGNED_BYTE, 0); //anchor point for coords is LOWER LEFT

            // map the PBO to process its data by CPU
            glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, pboIds.get(frame_nextIndex));
            pixels = ARBBufferObject.glMapBufferARB(GL_PIXEL_PACK_BUFFER_ARB, GL_READ_ONLY_ARB, 512*512*3, null);
            processPixels();
            glUnmapBufferARB(GL_PIXEL_PACK_BUFFER_ARB);


            // back to conventional pixel operation
            glBindBufferARB(GL_PIXEL_PACK_BUFFER_ARB, 0);
        }

        stencilValue = glHelper.sampleStencil((int)gameInputs.mouseX, (int)gameInputs.mouseY);

        updateConsole();
    }

    void updateConsole(){
        worldObject objectUnderMouse = worldObject_AtMouse();
        boolean somethingIsSelected = objectUnderMouse instanceof worldObject;

        String consoleStatus ="RENDER FPS: " + myFPS +
                "\nLOGIC FPS: " + myLogic.myFPS +
                "\nTRIS: " + (somethingIsSelected ? addCommas.format(objectUnderMouse.numTris) : "---") + //gameScene.getSelectedWorldObject().numTris+
                "\nPOLYS: " + (somethingIsSelected ? addCommas.format(objectUnderMouse.numPolys) : "---") +
                "\nVERTS: " + (somethingIsSelected ? addCommas.format(objectUnderMouse.numVerts) : "---") +
                "\nUniqVERTS: " + (somethingIsSelected ? addCommas.format(objectUnderMouse.numVertsUnique) : "---") +
                "\nSURFACE: " + (somethingIsSelected ? "???" : "---") +
                "\nVOLUME: " + (somethingIsSelected ? "???" : "---") +
                "\nNAME: " + (somethingIsSelected ? objectUnderMouse.name : "---");
        gameConsole.setStatusString(consoleStatus);
        gameConsole.draw(myWidth, myHeight, myWidth, 1024, 0, 0, 0.1f, 0.25f);
    }

    void cameraTransform(){//TODO camera obj --> set POV
        gameScene.poi = cameraPos;

        rotationx = myLogic.rotationx;
        rotationy = myLogic.rotationy;
        rotationz = myLogic.rotationz;

        glLoadIdentity();
        glPushMatrix();

        glRotatef((float) rotationz, 0f, 0f, 1f);
        glRotatef((float) rotationy, 0f, 1f, 0f);
        glRotatef((float) rotationx, 1f, 0f, 0f);

        glTranslatef(gameScene.poi.x, gameScene.poi.y, gameScene.poi.z);
    }

    public static worldObject worldObject_AtMouse(){
        if(gameScene.idsMap.containsKey(stencilValue+"")){
            return gameScene.idsMap.get(stencilValue+"");
        }else{
            return null;
        }
    }

    void processPixels(){
        long _total = 0;
        if(pixels.remaining()==0)pixels.flip();
        for(int x=0; x<512*512; x++){
            int r,g,b;
            r=pixels.get();//R
            g=pixels.get();//G
            b=pixels.get();//B
            _total+=(r+g+b);
        }
        mySurfaceTotal = (_total / 3_000_000f);
    }
}