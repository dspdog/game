package utils;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/3/2015.
 */
public class RandomHelper {
    static public Vector3f randomPosition(float scale){
        return new Vector3f(
                (float)(Math.random()*scale),
                (float)(Math.random()*scale),
                ((float)Math.random()*scale));
    }

    static public Vector3f randomRotation(){
        float scale = 2f*(float)Math.PI;
        return new Vector3f(
                (float)(Math.random()*scale),
                (float)(Math.random()*scale),
                ((float)Math.random()*scale));
    }
}
