package shapes.cloud;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayDeque;
import java.util.LinkedList;

public class particle {
    public Vector3f vel;
    public Vector3f pos;
    public Vector3f force;

    public float mass;
    public float density;
    //public float myNeighborsDensity;
    public float pressure;

    public static final float densREF = 1000; // kg/m^3
    public static final float mu = 0.001f; // kg/ms (dynamical viscosity))
    public static final float c = 1.9f; // m/s speed of sound

    public float radius=0f;

    public LinkedList<particle> myNeighbors = new LinkedList<>();

    public int myIndex;

    public static long time=0;
    public static long lastTime=0;
    public static float dt=0;

    public static float speedlimit = 0.5f;

    public particle(Vector3f lowerCorner, Vector3f upperCorner, int index){

        myIndex=index;

        mass = 0.01f; //kg
        density = 1f;
        pressure = 1f;

        vel = new Vector3f(
                (float)Math.random()-0.5f,
                (float)Math.random()-0.5f,
                (float)Math.random()-0.5f);

        vel.scale(0.1f);

        pos = new Vector3f(
                (float)Math.random()*(upperCorner.x - lowerCorner.x)+lowerCorner.x,
                (float)Math.random()*(upperCorner.y - lowerCorner.y)+lowerCorner.y,
                (float)Math.random()*(upperCorner.z - lowerCorner.z)+lowerCorner.z);
    }

    public static void updateTime(){
        lastTime=time;
        time = getTime();
        dt=(time-lastTime)*0.4f;
    }

    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void move(){
        Vector3f lower = sphCloud.lowerCorner;
        Vector3f upper = sphCloud.upperCorner;

        if(vel.lengthSquared()>speedlimit){
            vel.normalise().scale(speedlimit);} //speed limiter

        if((pos.x+ vel.x*dt > upper.x) || (pos.x+ vel.x*dt < lower.x))
            vel.x*=-1f;
        if((pos.y+ vel.y*dt > upper.y) || (pos.y+ vel.y*dt < lower.y))
            vel.y*=-1f;
        if((pos.x+ vel.z*dt > upper.z) || (pos.z+ vel.z*dt < lower.z))
            vel.z*=-1f;
        pos.set(pos.x+ vel.x*dt, pos.y+ vel.y*dt, pos.z+ vel.z*dt);

        pos.set(
                Math.min(Math.max(lower.x, pos.x), upper.x),
                Math.min(Math.max(lower.y, pos.y), upper.y),
                Math.min(Math.max(lower.z, pos.z), upper.z)
        );
    }

    public void findDensity(){/////////////////////////////////
        density=0f;
        if(myNeighbors.size()>0)
        for(particle neighbor : myNeighbors){
            density+=neighbor.mass*kernal(this.distanceTo(neighbor));
        }
    }

    public void findPressure(){
        pressure = c*c*(density-densREF);
    }

    public float kernal(float x){
        if(x>radius)return 0;
        return (1.0f - x*x)*1f;
    }

    public float kernald(float x){ //deriv of kernal
        if(x>radius)return 0;
        return -2f*x;
    }

    public int findNeighbors(float _radius){
        float dist;
        radius=_radius;
        myNeighbors = new LinkedList<>();
        ArrayDeque<particle> nearbyParticles = sphCloud.particlesNear(this.pos);
        for(particle otherParticle : nearbyParticles) {
            dist = this.distanceTo(otherParticle);
            if (dist < radius) {
                myNeighbors.add(otherParticle);
            }
        }

        int size = myNeighbors.size();

        myNeighbors.add(this);

        return (size+1);
    }

    public float distanceTo(particle p){
        return dist(p.pos.x - this.pos.x, p.pos.y - this.pos.y, p.pos.z - this.pos.z);
    }

    public float distanceTo(Vector3f position){
        return dist(position.x - this.pos.x, position.y - this.pos.y, position.z - this.pos.z);
    }

    public static float dist(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

}
