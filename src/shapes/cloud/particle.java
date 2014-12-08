package shapes.cloud;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;

public class particle {
    public Vector3f velocity;
    public Vector3f position;
    public Vector3f force;

    public float mass;
    public float density;
    public float pressure;

    public LinkedList<particle> myNeighbors;

    public int myIndex;

    public particle(Vector3f lowerCorner, Vector3f upperCorner, int index){

        myIndex=index;

        mass = 1f;
        density = 1f;
        pressure = 1f;

        position = new Vector3f(
                (float)Math.random()*(upperCorner.x - lowerCorner.x)+lowerCorner.x,
                (float)Math.random()*(upperCorner.y - lowerCorner.y)+lowerCorner.y,
                (float)Math.random()*(upperCorner.z - lowerCorner.z)+lowerCorner.z);
    }

    public int findNeighbors(float cutoff){
        float dist;
        myNeighbors = new LinkedList<>();
        LinkedList<particle> nearbyParticles = sphCloud.particlesNear(this.position);
        for(particle otherParticle : nearbyParticles) {
            dist = this.distanceTo(otherParticle);
            if (dist < cutoff) {
                myNeighbors.add(otherParticle);
            }
        }
        return myNeighbors.size();
    }

    public float distanceTo(particle p){
        return dist(p.position.x-this.position.x, p.position.y-this.position.y, p.position.z-this.position.z);
    }

    public static float dist(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

}
