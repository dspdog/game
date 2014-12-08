package shapes.cloud;
import org.lwjgl.util.vector.Vector3f;
import world.WorldObject;

import java.util.ArrayList;

/**
 * Created by user on 12/8/2014.
 */
public class sphCloud {
    public int numParticles=0;
    public ArrayList<particle> theParticles = new ArrayList<>();

    public sphCloud(int total, WorldObject collisionObject){
        numParticles=total;
        getRandomParticles();
    }

    private void getRandomParticles(){
        for(int i=0; i<numParticles; i++){
            addParticle(new particle());
        }

        findNeighbors();
    }

    public void findNeighbors(){
        int numNeighbors=0;
        long time1 = System.currentTimeMillis();
        for(particle p : theParticles){
            numNeighbors += p.findNeighbors(this, 200f);
        }
        System.out.println("found "+numNeighbors+" neighbors in " + (System.currentTimeMillis() - time1) + "ms");
    }

    public void addParticle(particle p){
        theParticles.add(p);
    }
}
