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
    public final float gridSize=4f;

    //corners of the box bounding the cloud
    public Vector3f lowerCorner;
    public Vector3f upperCorner;

    public sphCloud(int total, WorldObject collisionObject){

        lowerCorner = new Vector3f(0,0,0);
        upperCorner = new Vector3f(20,20,20);

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
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int x,y;

        particleGrid = new ArrayList[w+2][h+2];
        for(x=-1; x<w+1;x++){
            for(y=-1; y<h+1;y++){
                particleGrid[x+1][y+1]=new ArrayList<>();
            }
        }
        System.out.println("GRID size " + w + " " + h);
    }

    public void findNeighbors(){
        int numNeighbors=0;
        long time1 = System.currentTimeMillis();
        for(particle p : theParticles){
            numNeighbors += p.findNeighbors(this, gridSize/2f);
        }
        System.out.println("found "+numNeighbors+" neighbors in " + (System.currentTimeMillis() - time1) + "ms");
    }

    public boolean withinBounds(particle p){
        return p.position.x>lowerCorner.x+2 && p.position.x<upperCorner.x-2 &&
                p.position.y>lowerCorner.y+2 && p.position.y<upperCorner.y-2 &&
                 p.position.z>lowerCorner.z+2 && p.position.z<upperCorner.z-2;
    }

    public void addParticle(particle p){
        if(withinBounds(p)){
            float gridX = ((p.position.x-lowerCorner.x)/gridSize) + 1;
            float gridY = ((p.position.z-lowerCorner.z)/gridSize) + 1;
            particleGrid[(int)gridX][(int)gridY].add(p);
            theParticles.add(p);
        }
    }

    public ArrayList<particle> particlesNear(Vector3f position){
        float gridX = ((position.x-lowerCorner.x)/gridSize) + 1;
        float gridY = ((position.z-lowerCorner.z)/gridSize) + 1;

        float gridXd = gridX - (int)gridX;
        float gridYd = gridY - (int)gridY;

        ArrayList<particle> result = new ArrayList();
        result.addAll(particleGrid[(int)gridX][(int)gridY]);

        if(gridXd>0.5){ //E
            if(gridYd>0.5){//SE
                result.addAll(particleGrid[(int)gridX+1][(int)gridY]);
                result.addAll(particleGrid[(int)gridX][(int)gridY+1]);
                result.addAll(particleGrid[(int)gridX+1][(int)gridY+1]);
            }else{//NE
                result.addAll(particleGrid[(int)gridX+1][(int)gridY]);
                result.addAll(particleGrid[(int)gridX][(int)gridY-1]);
                result.addAll(particleGrid[(int)gridX+1][(int)gridY-1]);
            }
        }else{ //W
            if(gridYd>0.5){//SW
                result.addAll(particleGrid[(int)gridX-1][(int)gridY]);
                result.addAll(particleGrid[(int)gridX][(int)gridY+1]);
                result.addAll(particleGrid[(int)gridX-1][(int)gridY+1]);
            }else{//NW
                result.addAll(particleGrid[(int)gridX-1][(int)gridY]);
                result.addAll(particleGrid[(int)gridX][(int)gridY-1]);
                result.addAll(particleGrid[(int)gridX-1][(int)gridY-1]);
            }
        }

        return result;
    }
}
