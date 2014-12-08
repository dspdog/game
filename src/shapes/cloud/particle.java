package shapes.cloud;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class particle {
    public Vector3f velocity;
    public Vector3f position;
    public Vector3f force;

    public float mass;
    public float density;
    public float pressure;

    boolean neighborsFound = false;
    public ArrayList<particle> myNeighbors;

    public particle(){

        neighborsFound=false;
        mass = 1f;
        density = 1f;
        pressure = 1f;

        float scale = 200f;
        position = new Vector3f((float)Math.random()*scale, (float)Math.random()*scale, (float)Math.random()*scale);
    }

    public int findNeighbors(sphCloud cloud, float cutoff){
        float dist;

        if(!neighborsFound){
            myNeighbors = new ArrayList<>();
            for(particle p : cloud.theParticles){
                dist = this.distanceTo(p);
                if(dist<cutoff){myNeighbors.add(p);}
            }
        }

        neighborsFound=true;
        return myNeighbors.size();
    }

    public float distanceTo(particle p){
        return dist(p.position.x-this.position.x, p.position.y-this.position.y, p.position.z-this.position.z);
    }

    public static float dist(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

}
