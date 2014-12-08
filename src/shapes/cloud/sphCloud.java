package shapes.cloud;
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
            theParticles.add(new particle());
        }
    }
}
