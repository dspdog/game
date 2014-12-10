package shapes.cloud;
import org.lwjgl.util.vector.Vector3f;
import world.WorldObject;

import java.util.ArrayDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by user on 12/8/2014.
 */
public class sphCloud {
    public static final int numParticles=4000;
    public static final particle[] theParticles = new particle[numParticles];
    public static CopyOnWriteArrayList<particle>[][][] particleGrid;
    public static final float gridSize=32f;
    public static boolean gridInited=false;

    //corners of the box bounding the cloud
    public static Vector3f lowerCorner = new Vector3f(0,0,0);
    public static Vector3f upperCorner = new Vector3f(10,10,10);

    public static Vector3f center = new Vector3f(5f,5f,5f);
    public static boolean neighborsFound = true;

    public static boolean gravityDown = false;

    public sphCloud(int total, WorldObject collisionObject){

        gridInited=false;
        int size = 16;

        lowerCorner = new Vector3f(gridSize*-size,gridSize*-size,gridSize*-size);
        upperCorner = new Vector3f(gridSize*size,gridSize*size,gridSize*size);
        center = new Vector3f(0,0,0);

        lowerCorner.translate(0,gridSize*10,0f);
        upperCorner.translate(0,gridSize*10,0f);
        center.translate(0,gridSize*10,0f);

        //numParticles=total;
        initParticleGrid();

        getRandomParticles();

        gridInited=true;
    }

    private void getRandomParticles(){
        particle p;
        for(int i=0; i<numParticles; i++){
            p = new particle(lowerCorner, upperCorner, i);
            addParticle(p);
            theParticles[i] = p;
        }
    }

    private void initParticleGrid(){
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);
        int x,y,z;

        particleGrid = new CopyOnWriteArrayList[w+2][h+2][L+2];
        for(x=-1; x<w+1;x++){
            for(y=-1; y<h+1;y++){
                for(z=-1; z<L+1;z++){
                    particleGrid[x+1][y+1][z+1]=new CopyOnWriteArrayList<particle>();
                }
            }
        }
        System.out.println("GRID size " + w + " " + h + " " + L);
    }

    static long lastOutput = 0;
    public static void findNeighbors(){
        if(numParticles>0 && gridInited){
            int numNeighbors=0;
            long time1 = System.currentTimeMillis();
            for(particle p : theParticles){
                if(p!=null)
                numNeighbors += p.findNeighbors(gridSize/2f);
            }


            if(System.currentTimeMillis()-lastOutput>1000){
                System.out.println("found "+numNeighbors+" neighbors in " + (System.currentTimeMillis() - time1) + "ms -- average " + numNeighbors/numParticles);
                lastOutput=System.currentTimeMillis();
            }

        }
    }

    public static void updateParticleVelocities(){
        if(gridInited){
            for(particle p : theParticles){
                if(p!=null){
                    p.findDensity();
                    p.findPressure();
                }

            }

            for(particle p : theParticles){if(p!=null){
                Vector3f accPressure = new Vector3f(0,0,0);
                Vector3f accVisc = new Vector3f(0,0,0);
                float accPressScale=0;
                float accViscScale=0;

                for(particle n : p.myNeighbors){
                    float kernalVal = n.kernal(n.distanceTo(p));

                    accPressScale = -1.0f*n.mass*(p.pressure/(p.density*p.density) + n.pressure/(n.density*n.density)) * kernalVal;
                    accViscScale = p.mu * n.mass / n.density / p.density * kernalVal; //second gradient?

                    accVisc.translate(
                            accViscScale*(n.velocity.x-p.velocity.x),
                            accViscScale*(n.velocity.y-p.velocity.y),
                            accViscScale*(n.velocity.z-p.velocity.z)
                    );

                    accPressure.translate(
                            accPressScale*(n.position.x-p.position.x),
                            accPressScale*(n.position.y-p.position.y),
                            accPressScale*(n.position.z-p.position.z)
                    );
                }

                Vector3f accInteractive = new Vector3f(0,0,0);
                Vector3f accGravity = new Vector3f(p.position.x-center.x,p.position.y-center.y,p.position.z-center.z); //suction source at origin

                if(gravityDown){
                    accGravity = new Vector3f(0,1f,0);
                }

                accGravity.normalise().scale(5f);

                p.velocity.translate(
                        accPressure.x+accVisc.x+accInteractive.x-accGravity.x,
                        accPressure.y+accVisc.y+accInteractive.y-accGravity.y,
                        accPressure.z+accVisc.z+accInteractive.z-accGravity.z);

            }}
        }
    }

    public static void updateParticlePositions(){
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);
        int x,y,z;
        //particleGrid = new ArrayDeque[w+2][h+2][L+2];

        int _gridX, _gridY, _gridZ, gridX, gridY, gridZ;

        float scale = 0.8f;
        //Vector3f lowerCornerMini = new Vector3f(lowerCorner.x*scale, lowerCorner.y*scale, lowerCorner.z*scale);
        //Vector3f upperCornerMini = new Vector3f(upperCorner.x*scale, upperCorner.y*scale, upperCorner.z*scale);

        if(gridInited)
        for(x=0; x<w+2;x++){
            for(y=0; y<h+2;y++){
                for(z=0; z<L+2;z++){
                    int index=0;
                    //if(particleGrid[x][y][z] != null)
                    for(particle p:particleGrid[x][y][z]){

                        _gridX = (int)(((p.position.x-lowerCorner.x)/gridSize) + 1);
                        _gridY = (int)(((p.position.z-lowerCorner.z)/gridSize) + 1);
                        _gridZ = (int)(((p.position.y-lowerCorner.y)/gridSize) + 1);
                        p.move();
                        gridX = (int)(((p.position.x-lowerCorner.x)/gridSize) + 1);
                        gridY = (int)(((p.position.z-lowerCorner.z)/gridSize) + 1);
                        gridZ = (int)(((p.position.y-lowerCorner.y)/gridSize) + 1);

                        if(gridX!=_gridX || gridY!=_gridY || gridZ!=_gridZ){

                            _gridX = Math.max(1, Math.min(w - 1, _gridX));
                            _gridY = Math.max(1, Math.min(h - 1, _gridY));
                            _gridZ = Math.max(1, Math.min(L - 1, _gridZ));

                            gridX = Math.max(1, Math.min(w - 1, gridX));
                            gridY = Math.max(1, Math.min(h - 1, gridY));
                            gridZ = Math.max(1, Math.min(L - 1, gridZ));


                            particleGrid[_gridX][_gridY][_gridZ].remove(p);
                            particleGrid[gridX][gridY][gridZ].add(p);
                        }

                        index++;
                    }


                    //particleGrid[x][y][z]=new ArrayDeque<>(32);

                }
            }
        }

    }

    public boolean withinBounds(particle p){
        return p.position.x>lowerCorner.x+2 && p.position.x<upperCorner.x-2 &&
                p.position.y>lowerCorner.y+2 && p.position.y<upperCorner.y-2 &&
                 p.position.z>lowerCorner.z+2 && p.position.z<upperCorner.z-2;
    }

    public static void addParticle(particle p){
        //if(withinBounds(p)){
            float gridX = ((p.position.x-lowerCorner.x)/gridSize) + 1;
            float gridY = ((p.position.z-lowerCorner.z)/gridSize) + 1;
            float gridZ = ((p.position.y-lowerCorner.y)/gridSize) + 1;
            particleGrid[(int)gridX][(int)gridY][(int)gridZ].add(p);
            //theParticles.add(p);
        //}
    }

    public static float densityAt(Vector3f position){
        ArrayDeque<particle> nearbyParticles = particlesNear(position);
        float d = 0;
        for(particle otherParticle : nearbyParticles) {
            d+=otherParticle.kernal(otherParticle.distanceTo(position));
        }
        return d;
    }

    public static ArrayDeque<particle> particlesNear(Vector3f position){
        float gridX = ((position.x-lowerCorner.x)/gridSize) + 1;
        float gridY = ((position.z-lowerCorner.z)/gridSize) + 1;
        float gridZ = ((position.y-lowerCorner.y)/gridSize) + 1;

        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);

        float gridXd = gridX - (int)gridX;
        float gridYd = gridY - (int)gridY;
        float gridZd = gridZ - (int)gridZ;

        gridX = (int)Math.max(1, Math.min(w - 1, (int)gridX));
        gridY = (int)Math.max(1, Math.min(h - 1, (int)gridY));
        gridZ = (int)Math.max(1, Math.min(L - 1, (int)gridZ));

        ArrayDeque<particle> result = new ArrayDeque(8*20);
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
