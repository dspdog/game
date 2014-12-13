package shapes.cloud;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import world.WorldObject;

import java.nio.FloatBuffer;
/**
 * Created by user on 12/8/2014.
 */
public class sphCloud {
    public static final int numParticles=1000;
    public static final particle[] theParticles = new particle[numParticles];
    public static limitedArray[][][] particleGrid;
    public static final float gridSize=32f;
    public static boolean gridInited=false;

    //corners of the box bounding the cloud
    public static Vector3f lowerCorner = new Vector3f(0,0,0);
    public static Vector3f upperCorner = new Vector3f(10,10,10);
    private static Vector3f lowerCornerBounds = new Vector3f(0,0,0);
    private static Vector3f upperCornerBounds = new Vector3f(10,10,10);
    public static Vector3f lowerCornerBoundsFinal = new Vector3f(0,0,0);
    public static Vector3f upperCornerBoundsFinal = new Vector3f(10,10,10);

    public static Vector3f center = new Vector3f(5f,5f,5f);

    public static boolean gravityDown = false;

    static final int vertex_size = 3;

    static final int NUM_LINES= 1024*1024; //MAX LINES
    public static final FloatBuffer vertex_data = BufferUtils.createFloatBuffer(NUM_LINES * vertex_size * 2); //XYZ1 XYZ2
    public static final FloatBuffer color_data = BufferUtils.createFloatBuffer(NUM_LINES * vertex_size * 2); //RGB1 RGB2


    public sphCloud(int total, WorldObject collisionObject){

        gridInited=false;
        int size = 16;

        lowerCorner = new Vector3f(gridSize*-size,gridSize*-size,gridSize*-size);
        upperCorner = new Vector3f(gridSize*size,gridSize*size,gridSize*size);
        lowerCornerBounds = new Vector3f(gridSize*-size/2,gridSize*-size/2,gridSize*-size/2);
        upperCornerBounds = new Vector3f(gridSize*size/2,gridSize*size/2,gridSize*size/2);
        center = new Vector3f(0,0,0);

        lowerCorner.translate(0,gridSize*10,0f);
        upperCorner.translate(0,gridSize*10,0f);
        lowerCornerBounds.translate(0,gridSize*10,0f);
        upperCornerBounds.translate(0,gridSize*10,0f);
        center.set((lowerCorner.x+upperCorner.x)/2f,(lowerCorner.y+upperCorner.y)/2f,(lowerCorner.z+upperCorner.z)/2f);

        initParticleGrid();
        getRandomParticles();

        gridInited=true;
    }

    private void getRandomParticles(){
        particle p;
        for(int i=0; i<numParticles; i++){
            p = new particle(lowerCorner, upperCorner, i);
            addParticleToGrid((int)p.gridPos.x, (int)p.gridPos.y, (int)p.gridPos.z, p);
            theParticles[i] = p;
        }
        theParticles[0] = theParticles[0].emptyParticle();
    }

    private void initParticleGrid(){
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);
        int x,y,z;
        particleGrid = new limitedArray[w+2][h+2][L+2];
        for(x=-1; x<w+1;x++){
            for(y=-1; y<h+1;y++){
                for(z=-1; z<L+1;z++){
                    particleGrid[x+1][y+1][z+1]=new limitedArray();
                }
            }
        }
        System.out.println("GRID getEnd " + w + " " + h + " " + L);
    }

    static long lastOutput = 0;
    public static void findNeighbors(){
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

    public static void updateParticleVelocities(){
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

            int len = p.myNeighbors.getEnd();

            for(int i=0; i<len; i++){
                if(p.myNeighbors.ints[i]!=0) {
                    particle n = theParticles[p.myNeighbors.ints[i]];
                    float kernalVal = n.kernal(n.distanceTo(p));
                    float kernalVald = n.kernald(n.distanceTo(p));

                    accPressScale = -1.0f * n.mass * (p.pressure / (p.density * p.density) + n.pressure / (n.density * n.density)) * kernalVal;
                    accViscScale = p.mu * n.mass / n.density / p.density * kernalVald;

                    accVisc.translate(
                            accViscScale * (n.vel.x - p.vel.x),
                            accViscScale * (n.vel.y - p.vel.y),
                            accViscScale * (n.vel.z - p.vel.z)
                    );

                    accPressure.translate(
                            accPressScale * (n.pos.x - p.pos.x),
                            accPressScale * (n.pos.y - p.pos.y),
                            accPressScale * (n.pos.z - p.pos.z)
                    );
                }
            }

            Vector3f accInteractive = new Vector3f(0,0,0);
            Vector3f accGravity = new Vector3f(p.pos.x-center.x,p.pos.y-center.y,p.pos.z-center.z); //suction source at origin

            if(gravityDown){
                accGravity = new Vector3f(0,1f,0);
            }

            accGravity.normalise().scale(9f);

            p.vel.translate(
                    accPressure.x+accVisc.x+accInteractive.x-accGravity.x,
                    accPressure.y+accVisc.y+accInteractive.y-accGravity.y,
                    accPressure.z+accVisc.z+accInteractive.z-accGravity.z);

        }}
    }

    public static void updateParticlePositions(){
        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);
        int x,y,z;

        int _gridX, _gridY, _gridZ, gridX, gridY, gridZ;

        float scale = 0.8f;
        //Vector3f lowerCornerMini = new Vector3f(lowerCorner.x*scale, lowerCorner.y*scale, lowerCorner.z*scale);
        //Vector3f upperCornerMini = new Vector3f(upperCorner.x*scale, upperCorner.y*scale, upperCorner.z*scale);
        lowerCornerBounds.set(upperCorner.x,upperCorner.y,upperCorner.z);
        upperCornerBounds.set(lowerCorner.x,lowerCorner.y,lowerCorner.z);
        if(gridInited)
            for(x=0; x<w+2;x++){
                for(y=0; y<h+2;y++){
                    for(z=0; z<L+2;z++){
                       // particle[] particles = particlesArrayFromIndex(particleGrid[x][y][z]);
                        int numP = particleGrid[x][y][z].getEnd();

                        for(int i=0; i<numP; i++){
                            if(particleGrid[x][y][z].ints[i]!=0){
                                particle p = theParticles[particleGrid[x][y][z].ints[i]];

                                _gridX = (int)p.gridPos.x;
                                _gridY = (int)p.gridPos.y;
                                _gridZ = (int)p.gridPos.z;
                                p.move();

                                lowerCornerBounds.set(
                                        Math.min(lowerCornerBounds.x, p.pos.x),
                                        Math.min(lowerCornerBounds.y, p.pos.y),
                                        Math.min(lowerCornerBounds.z, p.pos.z));

                                upperCornerBounds.set(
                                        Math.max(upperCornerBounds.x, p.pos.x),
                                        Math.max(upperCornerBounds.y, p.pos.y),
                                        Math.max(upperCornerBounds.z, p.pos.z));

                                gridX = (int)p.gridPos.x;
                                gridY = (int)p.gridPos.y;
                                gridZ = (int)p.gridPos.z;

                                if(gridX!=_gridX || gridY!=_gridY || gridZ!=_gridZ){
                                    _gridX = Math.max(1, Math.min(w - 1, _gridX));
                                    _gridY = Math.max(1, Math.min(h - 1, _gridY));
                                    _gridZ = Math.max(1, Math.min(L - 1, _gridZ));

                                    gridX = Math.max(1, Math.min(w - 1, gridX));
                                    gridY = Math.max(1, Math.min(h - 1, gridY));
                                    gridZ = Math.max(1, Math.min(L - 1, gridZ));

                                    removeParticleFromGrid(_gridX,_gridY,_gridZ, p);
                                    addParticleToGrid(gridX,gridY,gridZ, p);
                                }
                            }
                        }
                    }
                }
            }
        lowerCornerBoundsFinal.set(lowerCornerBounds.x,lowerCornerBounds.y,lowerCornerBounds.z);
        upperCornerBoundsFinal.set(upperCornerBounds.x,upperCornerBounds.y,upperCornerBounds.z);
    }

    private static void addParticleToGrid(int _gridX, int _gridY, int _gridZ, particle p){
        particleGrid[_gridX][_gridY][_gridZ].add(p.myIndex);
    }
    private static void removeParticleFromGrid(int _gridX, int _gridY, int _gridZ, particle p){
        particleGrid[_gridX][_gridY][_gridZ].remove(p.myIndex); //we are removing the Integer, not at the index represented by the Integer
    }

    public boolean withinBounds(particle p){
        return p.pos.x>lowerCorner.x+2 && p.pos.x<upperCorner.x-2 &&
                p.pos.y>lowerCorner.y+2 && p.pos.y<upperCorner.y-2 &&
                p.pos.z>lowerCorner.z+2 && p.pos.z<upperCorner.z-2;
    }



    public static limitedArray particlesNear(particle p){
        float gridX = p.gridPos.x;
        float gridY = p.gridPos.y;
        float gridZ = p.gridPos.z;

        int w=(int)((upperCorner.x-lowerCorner.x)/gridSize);
        int h=(int)((upperCorner.z-lowerCorner.z)/gridSize);
        int L=(int)((upperCorner.y-lowerCorner.y)/gridSize);

        float gridXd = gridX - (int)gridX;
        float gridYd = gridY - (int)gridY;
        float gridZd = gridZ - (int)gridZ;

        gridX = Math.max(1, Math.min(w - 1, (int)gridX));
        gridY = Math.max(1, Math.min(h - 1, (int)gridY));
        gridZ = Math.max(1, Math.min(L - 1, (int)gridZ));

        limitedArray result = new limitedArray();

        addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY][(int) gridZ]));
        if(gridZd>0.5){//upper half
            addToArray(result, particlesFromIndex(particleGrid[(int)gridX][(int)gridY][(int)gridZ+1]));
            if(gridXd>0.5){ //E
                if(gridYd>0.5){//SE
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY + 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY + 1][(int) gridZ + 1]));
                }else{//NE
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY - 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY - 1][(int) gridZ + 1]));
                }
            }else{ //W
                if(gridYd>0.5){//SW
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY + 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY + 1][(int) gridZ + 1]));
                }else{//NW
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY - 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ + 1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY - 1][(int) gridZ + 1]));
                }
            }
        }else{ //lower half
            addToArray(result, particlesFromIndex(particleGrid[(int)gridX][(int)gridY][(int)gridZ-1]));
            if(gridXd>0.5){ //E
                if(gridYd>0.5){//SE
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY + 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY + 1][(int) gridZ-1]));
                }else{//NE
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY - 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX + 1][(int) gridY - 1][(int) gridZ-1]));
                }
            }else{ //W
                if(gridYd>0.5){//SW
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY + 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY + 1][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY + 1][(int) gridZ-1]));
                }else{//NW
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY - 1][(int) gridZ]));

                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX][(int) gridY - 1][(int) gridZ-1]));
                    addToArray(result, particlesFromIndex(particleGrid[(int) gridX - 1][(int) gridY - 1][(int) gridZ-1]));
                }
            }
        }

        return result;
    }

    public static limitedArray particlesFromIndex(limitedArray arr){
        limitedArray res = new limitedArray();
        int len = res.getLen();
        //CopyOnWriteArrayList<particle> res = new CopyOnWriteArrayList<>();
        for(int i=0; i<len; i++){
            res.add(arr.ints[i]);
        }
        return res;
    }

    public static void addToArray( limitedArray result, limitedArray parts){
        int len = parts.getEnd();
        for(int i=0; i<len; i++){
            result.add(parts.ints[i]);
        }
    }

    public static class limitedArray{
        final int pPerBrick = 25;
        final int TOTAL = pPerBrick*8+2;
        public int ints[] = new int[TOTAL];
        boolean changed = false;

        public limitedArray(){
            for(int i=0; i<TOTAL; i++){
                ints[i]=0;
            }
            setLen(TOTAL-5);
            setFZ(0);
        }

        public void add(int x){
            if(!alreadyHere(x)){
                changed=true;
                int fz = getFZ();
                ints[fz] = x;
                int len = getLen();
                for(int i=fz+1; i<len; i++){
                    if(ints[i]==0){
                        setFZ(i);
                        break;
                    }
                }
            }
        }

        private int lastSize = 0;
        public int getEnd(){
            if(!changed){return lastSize;}
            else{
                int size=0;
                int len = getLen();
                for(int i=0; i<len; i++){
                    if(ints[i]!=0)size=i;
                }
                changed=false;
                lastSize = size+1;
                return lastSize;
            }
        }

        public boolean alreadyHere(int x){
            int len = getEnd();
            for(int i=0; i<len; i++){
                if(ints[i]==x){
                    return true;
                }
            }
            return false;
        }

        public void remove(int x){
            int len = getEnd();
            boolean fzSet=false;
            for(int i=0; i<len; i++){
                if(ints[i]==x){
                    changed=true;
                    ints[i]=0;
                    if(!fzSet) {
                        setFZ(i); fzSet =true;
                    }
                }
            }
        }

        private int getLen(){return ints[TOTAL-1];}
        private int setLen(int len){return ints[TOTAL-1] = len;}
        private int getFZ(){return ints[TOTAL-2];}
        private int setFZ(int fz){return ints[TOTAL-2] = fz;}
    }
}
