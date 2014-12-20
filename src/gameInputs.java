import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import world.scene;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_STENCIL_INDEX;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glReadPixels;

/**
 * Created by user on 11/26/2014.
 */
public class gameInputs {

    scene myScene;

    public gameInputs(scene _scene){
        myScene = _scene;
    }

    long lastPollTime = System.currentTimeMillis();

    boolean SPACE_down = false;

    static float param1 = 1.0f;
    static float param2 = 1.0f;
    static float param3 = 1.0f;
    static float param4 = 1.0f;
    static float param5 = 1.0f;
    static float param6 = 1.0f;
    static float param7 = 1.0f;
    static float param8 = 1.0f;
    static float param9 = 1.0f;

    public void pollInput() {
        if (Mouse.isButtonDown(0)) {
            onMouseDown();
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                    SPACE_down=true;
                    scene.myKCloud.gravityDown = !scene.myKCloud.gravityDown;
                    if(scene.myKCloud.gravityDown){
                        scene.myKCloud.lastShot=getTime();
                    }
                }
            } else {
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                    SPACE_down=false;
                }
            }
        }

        float step = 0.3f* (System.currentTimeMillis()-lastPollTime);
        float multiplier=1.0f;

        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
            step*=2.5f;
            multiplier*=-1f;
        }

        if(Keyboard.isKeyDown(Keyboard.KEY_1)){param1*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_2)){param2*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_3)){param3*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_4)){param4*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_5)){param5*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_6)){param6*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_7)){param7*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_8)){param8*=(1.0f+multiplier*0.01f);}
        if(Keyboard.isKeyDown(Keyboard.KEY_9)){param9*=(1.0f+multiplier*0.01f);}

        if(Keyboard.isKeyDown(Keyboard.KEY_A)){
            scene.cameraPosDesired.x+=scene.cameraXVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraXVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraXVector.z*step;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_S)){
            scene.cameraPosDesired.x-=scene.cameraZVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraZVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraZVector.z*step;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_W)){
            scene.cameraPosDesired.x+=scene.cameraZVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraZVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraZVector.z*step;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_D)){
            scene.cameraPosDesired.x-=scene.cameraXVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraXVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraXVector.z*step;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_E)){
            scene.cameraPosDesired.x+=scene.cameraYVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraYVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraYVector.z*step;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
            scene.cameraPosDesired.x-=scene.cameraYVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraYVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraYVector.z*step;
        }

        lastPollTime=System.currentTimeMillis();
    }
    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
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
}
