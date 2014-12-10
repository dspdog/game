package shapes.cloud;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;

public class particle {
    public Vector3f velocity;
    public Vector3f position;
    public Vector3f force;

    public float mass;
    public float density;
    public float pressure;

    public float densREF = 1000; // kg/m^3
    public float mu = 1f; // kg/ms (viscosity))
    public float c = 5f; // m/s speed of sound

    public float radius=0f;

    public LinkedList<particle> myNeighbors = new LinkedList<>();

    public int myIndex;

    public static long time=0;
    public static long lastTime=0;
    public static float dt=0;

    public static float speedlimit = 0.2f;

    public particle(Vector3f lowerCorner, Vector3f upperCorner, int index){

        myIndex=index;

        mass = 0.01f; //kg
        density = 1f;
        pressure = 1f;

        velocity = new Vector3f(
                (float)Math.random()-0.5f,
                (float)Math.random()-0.5f,
                (float)Math.random()-0.5f);

        velocity.scale(1f);

        position = new Vector3f(
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

    public void move(Vector3f lower, Vector3f upper){

        if(velocity.lengthSquared()>speedlimit){velocity.normalise().scale(speedlimit);} //speed limiter

        if((position.x+velocity.x*dt > upper.x) || (position.x+velocity.x*dt < lower.x))
            velocity.x*=-1f;

        if((position.y+velocity.y*dt > upper.y) || (position.y+velocity.y*dt < lower.y))
            velocity.y*=-1f;

        if((position.x+velocity.z*dt > upper.z) || (position.z+velocity.z*dt < lower.z))
            velocity.z*=-1f;
        position.set(position.x+velocity.x*dt,position.y+velocity.y*dt,position.z+velocity.z*dt);

        position.set(
                Math.min(Math.max(lower.x, position.x), upper.x),
                Math.min(Math.max(lower.y, position.y), upper.y),
                Math.min(Math.max(lower.z, position.z), upper.z)
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
        return 1.0f - x*x;
    }

    public int findNeighbors(float cutoff){
        float dist;
        radius=cutoff;
        myNeighbors = new LinkedList<>();
        ArrayDeque<particle> nearbyParticles = sphCloud.particlesNear(this.position);
        for(particle otherParticle : nearbyParticles) {
            dist = this.distanceTo(otherParticle);
            if (dist < cutoff) {
                //otherParticle._tempDist = dist;
                myNeighbors.add(otherParticle);
            }
        }
        myNeighbors.add(this);
        return myNeighbors.size();
    }

    public float distanceTo(particle p){
        return dist(p.position.x-this.position.x, p.position.y-this.position.y, p.position.z-this.position.z);
    }

    public static float dist(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

}
