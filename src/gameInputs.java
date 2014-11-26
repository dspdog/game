import org.lwjgl.BufferUtils;
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

        float step = 1.1f* (System.currentTimeMillis()-lastPollTime);
        if(A_down){
            scene.cameraPosDesired.x+=scene.cameraXVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraXVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraXVector.z*step;
        }
        if(S_down){
            scene.cameraPosDesired.x-=scene.cameraZVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraZVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraZVector.z*step;
        }
        if(W_down){
            scene.cameraPosDesired.x+=scene.cameraZVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraZVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraZVector.z*step;
        }
        if(D_down){
            scene.cameraPosDesired.x-=scene.cameraXVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraXVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraXVector.z*step;
        }
        if(E_down){
            scene.cameraPosDesired.x+=scene.cameraYVector.x*step;
            scene.cameraPosDesired.y+=scene.cameraYVector.y*step;
            scene.cameraPosDesired.z+=scene.cameraYVector.z*step;
        }
        if(Q_down){
            scene.cameraPosDesired.x-=scene.cameraYVector.x*step;
            scene.cameraPosDesired.y-=scene.cameraYVector.y*step;
            scene.cameraPosDesired.z-=scene.cameraYVector.z*step;
        }

        lastPollTime=System.currentTimeMillis();
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
