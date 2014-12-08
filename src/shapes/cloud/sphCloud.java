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

    public ArrayList<particle>[][] particleGrid;

    //corners of the box bounding the cloud
    public Vector3f lowerCorner;
    public Vector3f upperCorner;

    public sphCloud(int total, WorldObject collisionObject){

        lowerCorner = new Vector3f(0,0,0);
        upperCorner = new Vector3f(200,200,200);

        numParticles=total;
        initParticleGrid();
        getRandomParticles();
    }

    private void getRandomParticles(){
        for(int i=0; i<numParticles; i++){
            addParticle(new particle(lowerCorner, upperCorner));
        }

        findNeighbors();
    }

    private void initParticleGrid(){
        float w=upperCorner.x-lowerCorner.x;
        float h=upperCorner.z-lowerCorner.z;
        int x,y;

        particleGrid = new ArrayList[(int)w][(int)h];
        for(x=0; x<w;x++){
            for(y=0; y<h;y++){
                particleGrid[x][y]=new ArrayList<>();
            }
        }
    }

    public void findNeighbors(){
        int numNeighbors=0;
        long time1 = System.currentTimeMillis();
        for(particle p : theParticles){
            numNeighbors += p.findNeighbors(this, 20f);
        }
        System.out.println("found "+numNeighbors+" neighbors in " + (System.currentTimeMillis() - time1) + "ms");
    }

    public boolean withinBounds(particle p){
        return p.position.x>lowerCorner.x && p.position.x<upperCorner.x &&
                p.position.x>lowerCorner.y && p.position.x<upperCorner.y &&
                 p.position.x>lowerCorner.z && p.position.x<upperCorner.z;
    }

    public void addParticle(particle p){
        if(withinBounds(p)){
            particleGrid[(int)p.position.x][(int)p.position.y].add(p);
            theParticles.add(p);
        }
    }
}
