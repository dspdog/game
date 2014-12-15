package shapes.cloud;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayDeque;


public class particle {
    public Vector3f vel;
    public Vector3f pos;
    public Vector3f force;

    public Vector3f gridPos = new Vector3f(0,0,0);

    public float mass;
    public float density;
    //public float myNeighborsDensity;
    public float pressure;

    public static final float densREF = 1000; // kg/m^3
    public static final float mu = 0.01f; // kg/ms (dynamical viscosity))
    public static final float c = 1.9f; // m/s speed of sound

    public float radius=0f;

    public sphCloud.limitedArray myNeighbors = new sphCloud.limitedArray();

    public Integer myIndex;

    public static long time=0;
    public static long lastTime=0;
    public static float dt=0;

    public static float speedlimit = c/2f;

    public particle(Vector3f lowerCorner, Vector3f upperCorner, int index){
        myIndex=index;

        mass = 0.01f; //kg
        density = 1f;
        pressure = 1f;

        vel = new Vector3f(0,0,0);

        pos = new Vector3f(
                (float)Math.random()*(upperCorner.x - lowerCorner.x)+lowerCorner.x,
                (float)Math.random()*(upperCorner.y - lowerCorner.y)+lowerCorner.y,
                (float)Math.random()*(upperCorner.z - lowerCorner.z)+lowerCorner.z);

        updateGridPos();
    }

    public particle(){
        vel = new Vector3f(0,0,0);
        pos = new Vector3f(0,0,0);
    }

    public particle emptyParticle(){
        myIndex=0;

        mass = 0.0f; //kg
        density = 0f;
        pressure = 0f;

        vel = new Vector3f(0,0,0);
        pos = new Vector3f(-10000f,-10000f,-10000f);
        return this;
    }

    public particle(particle p){
        myIndex=p.myIndex;

        mass = p.mass; //kg
        density = p.density;
        pressure = p.pressure;

        vel = new Vector3f(p.vel.x,p.vel.y,p.vel.z);
        pos = new Vector3f(p.pos.x,p.pos.y,p.pos.z);
    }

    public static void updateTime(){
        lastTime=time;
        time = getTime();
        dt=(time-lastTime)*0.1f;
    }

    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void move(){
        if(myIndex!=0){
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

        updateGridPos();
    }

    public void updateGridPos(){



        gridPos.set( 1f*((pos.x-sphCloud.lowerCorner.x)/sphCloud.gridSize) + 1
                    ,1f*((pos.z-sphCloud.lowerCorner.z)/sphCloud.gridSize) + 1
                    ,1f*((pos.y-sphCloud.lowerCorner.y)/sphCloud.gridSize) + 1);


    }

    public void findDensity(){/////////////////////////////////
        density=0f;
        int len = myNeighbors.getEnd();
        for(int i=0; i<len; i++){
            if(myNeighbors.ints[i]!=0){
                particle neighbor = sphCloud.theParticles[myNeighbors.ints[i]];
                density+=neighbor.mass*kernal(this.distanceTo(neighbor));
            }
        }
    }

    public void findPressure(){
        pressure = c*c*(density-densREF);
    }

    public float kernal(float x){
        x*=1f; //TODO make this a param
        if(x>radius)return 0;
        return (1.0f - x*x)*1f;
    }

    public float kernald(float x){ //deriv of kernal
        x*=1f; //TODO make this a param
        if(x>radius)return 0;
        return -2f*x;
    }

    public int findNeighbors(float _radius){
        float dist;
        radius=_radius;

        myNeighbors = new sphCloud.limitedArray();
        sphCloud.limitedArray nearbyParticles = sphCloud.particlesNear(this);

        int len = nearbyParticles.getEnd();

        for(int i=0; i<len; i++) {
            if(nearbyParticles.ints[i]!=0){
                particle otherParticle = sphCloud.theParticles[nearbyParticles.ints[i]];
                dist = this.distanceTo(otherParticle);
                if (dist < radius) {
                    myNeighbors.add(otherParticle.myIndex);
                }
            }
        }

        int size = myNeighbors.getEnd();

        myNeighbors.add(this.myIndex);

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
