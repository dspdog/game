package shapes.cloud;
import org.lwjgl.util.vector.Vector3f;
import world.WorldObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by user on 12/8/2014.
 */
public class sphCloud {
    public static int numParticles=0;
    public static ArrayList<particle> theParticles = new ArrayList<>();
    public static ArrayList<particle>[][][] particleGrid;
    public static float gridSize=32f;

    //corners of the box bounding the cloud
    public static Vector3f lowerCorner;
    public static Vector3f upperCorner;

    public static boolean neighborsFound = true;

    public sphCloud(int total, WorldObject collisionObject){

        lowerCorner = new Vector3f(0,0,0);
        upperCorner = new Vector3f(gridSize*8,gridSize*8,gridSize*8);

        numParticles=total;
        initParticleGrid();
        getRandomParticles();
    }

    private void getRandomParticles(){
        for(int i=0; i<numParticles; i++){
            addParticle(new particle(lowerCorner, upperCorner, i));
        }
    }

    private void initParticleGrid(){
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);
        int x,y,z;

        particleGrid = new ArrayList[w+2][h+2][L+2];
        for(x=-1; x<w+1;x++){
            for(y=-1; y<h+1;y++){
                for(z=-1; z<L+1;z++){
                    particleGrid[x+1][y+1][z+1]=new ArrayList<>();
                }
            }
        }
        System.out.println("GRID size " + w + " " + h + " " + L);
    }

    public static void findNeighbors(){
        neighborsFound=false;
        if(numParticles>0){
            int numNeighbors=0;
            long time1 = System.currentTimeMillis();
            for(particle p : theParticles){
                numNeighbors += p.findNeighbors(gridSize/2f);
            }
            System.out.println("found "+numNeighbors+" neighbors in " + (System.currentTimeMillis() - time1) + "ms -- average " + numNeighbors/numParticles);
        }
        neighborsFound=true;
    }

    public boolean withinBounds(particle p){
        return p.position.x>lowerCorner.x+2 && p.position.x<upperCorner.x-2 &&
                p.position.y>lowerCorner.y+2 && p.position.y<upperCorner.y-2 &&
                 p.position.z>lowerCorner.z+2 && p.position.z<upperCorner.z-2;
    }

    public void addParticle(particle p){
        //if(withinBounds(p)){
            float gridX = ((p.position.x-lowerCorner.x)/gridSize) + 1;
            float gridY = ((p.position.z-lowerCorner.z)/gridSize) + 1;
            float gridZ = ((p.position.y-lowerCorner.y)/gridSize) + 1;
            particleGrid[(int)gridX][(int)gridY][(int)gridZ].add(p);
            theParticles.add(p);
        //}
    }

    public static LinkedList<particle> particlesNear(Vector3f position){
        float gridX = ((position.x-lowerCorner.x)/gridSize) + 1;
        float gridY = ((position.z-lowerCorner.z)/gridSize) + 1;
        float gridZ = ((position.y-lowerCorner.y)/gridSize) + 1;

        float gridXd = gridX - (int)gridX;
        float gridYd = gridY - (int)gridY;
        float gridZd = gridZ - (int)gridZ;

        LinkedList<particle> result = new LinkedList();
        result.addAll(particleGrid[(int)gridX][(int)gridY][(int)gridZ]);

        if(gridZd>0.5){//upper half
            result.addAll(particleGrid[(int)gridX][(int)gridY][(int)gridZ+1]);
            if(gridXd>0.5){ //E
                if(gridYd>0.5){//SE
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY+1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY+1][(int)gridZ+1]);

                }else{//NE
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY-1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY-1][(int)gridZ+1]);
                }
            }else{ //W
                if(gridYd>0.5){//SW
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY+1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY+1][(int)gridZ+1]);
                }else{//NW
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY-1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ+1]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY-1][(int)gridZ+1]);
                }
            }
        }else{ //lower half
            result.addAll(particleGrid[(int)gridX][(int)gridY][(int)gridZ-1]);
            if(gridXd>0.5){ //E
                if(gridYd>0.5){//SE
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY+1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY+1][(int)gridZ-1]);

                }else{//NE
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY-1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX+1][(int)gridY][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX+1][(int)gridY-1][(int)gridZ-1]);
                }
            }else{ //W
                if(gridYd>0.5){//SW
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY+1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY+1][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY+1][(int)gridZ-1]);
                }else{//NW
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY-1][(int)gridZ]);

                    result.addAll(particleGrid[(int)gridX-1][(int)gridY][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX][(int)gridY-1][(int)gridZ-1]);
                    result.addAll(particleGrid[(int)gridX-1][(int)gridY-1][(int)gridZ-1]);
                }
            }
        }

        return result;
    }
}
