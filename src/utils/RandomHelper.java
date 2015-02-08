package utils;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/3/2015.
 */
public class RandomHelper {

    static public Vector3d randomBrownian(float scale){
        return new Vector3d(
                (float)(Math.random()*scale),
                (float)(Math.random()*scale),
                ((float)Math.random()*scale));
    }

    static public Vector3f randomPosition(float scale){
        return new Vector3f(
                (float)(Math.random()*scale),
                (float)(Math.random()*scale),
                ((float)Math.random()*scale));
    }

    static public Vector3f randomRotation(){
        float scale = 360; //2f*(float)Math.PI;
        return new Vector3f(
                (float)(Math.random()*scale),
                (float)(Math.random()*scale),
                ((float)Math.random()*scale));
    }
}
