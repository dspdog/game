package shapes.cloud;

import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 12/8/2014.
 */
public class particle {
    public Vector3f velocity;
    public Vector3f position;
    public Vector3f force;

    public float mass;
    public float density;
    public float pressure;

    public particle(){
        mass = 1f;
        density = 1f;
        pressure = 1f;

        float scale = 200f;
        position = new Vector3f((float)Math.random()*scale, (float)Math.random()*scale, (float)Math.random()*scale);
    }
}
