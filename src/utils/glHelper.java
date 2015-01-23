package utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluOrtho2D;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * Created by user on 1/23/2015.
 */
public class glHelper {

    public static Vector3f cameraXVector = new Vector3f(0,0,0);
    public static Vector3f cameraYVector = new Vector3f(0,0,0);
    public static Vector3f cameraZVector = new Vector3f(0,0,0);

    public static int sampleStencil(int x, int y){ //sample single pixel of Stencil Buffer
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        glReadPixels(x,y, 1, 1, GL_STENCIL_INDEX, GL_UNSIGNED_INT, ib);
        return ib.get(0);
    }

    public static void updateCamVectors(){//http://www.gamedev.net/topic/397751-how-to-get-camera-pos/
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

    public static void prepare3D(int width, int height, float fov){ //see http://gamedev.stackexchange.com/questions/18468/making-a-hud-gui-with-opengl-lwjgl
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(fov, ((float)width) / ((float)height), 0.01f, 5000f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        //glEnable(GL_DEPTH_TEST);
        //glDepthFunc(GL_LEQUAL);
        //glDepthFunc(GL_NEVER);
    }

    public static void prepare2D(int width, int height){ //see http://gamedev.stackexchange.com/questions/18468/making-a-hud-gui-with-opengl-lwjgl
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0, width, height, 0.0f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        //glTranslatef(0.375f, 0.375f, 0.0f); //?

        //glDisable(GL_DEPTH_TEST);
    }
}
