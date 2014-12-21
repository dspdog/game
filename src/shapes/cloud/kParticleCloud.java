package shapes.cloud;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import world.scene;

/**
 * Created by user on 12/13/2014.
 */
public class kParticleCloud extends Kernel {

    final float S_PER_MS = 0.3f ; //seconds per milliseconds, make 0.001f for "realtime"(?)
    public boolean paused = false;

    //CLOUD PARAMS
        public static final int PARTICLES_MAX = 5000;
        long particleLifetime = 1000000;

    public int numParticles=0;

        final float neighborDistance = 5f;
        final float densREF = 0.0012f; // kg/m^3
        final float mu = 1f; // kg/ms (dynamical viscosity))
        final float c = 2.5f; // m/s speed of sound

        final float speedlimit = 0.75f;

        //bounding box
        final float boxSize = 200f;
        public final float lowerX = -boxSize;   public final float upperX = boxSize;
        public final float lowerY = -boxSize;   public final float upperY = boxSize;
        public final float lowerZ = -boxSize;   public final float upperZ = boxSize;

    //USER PARAMS
        final float cameraPos[] = new float[3];
        final float cameraDirXVec[] = new float[3];
        final float cameraDirYVec[] = new float[3];
        final float cameraDirZVec[] = new float[3];

    //PARTICLE PARAMS

        //velocity                                          //position                                                   //density, mass, pressure
        final float[] velocityX = new float[PARTICLES_MAX];  public final float[] positionX = new float[PARTICLES_MAX];  final float[] density = new float[PARTICLES_MAX];
        final float[] velocityY = new float[PARTICLES_MAX];  public final float[] positionY = new float[PARTICLES_MAX];  final float[] pmass = new float[PARTICLES_MAX];
        final float[] velocityZ = new float[PARTICLES_MAX];  public final float[] positionZ = new float[PARTICLES_MAX];  final float[] pressure = new float[PARTICLES_MAX];

        final long[] timestamp = new long[PARTICLES_MAX];

        public boolean neighborsReset=false; //smoother motion if set to false?

        final int MAX_NEIGHB_PER_PARTICLE = 20;
        final int[] neighborsList = new int[PARTICLES_MAX* MAX_NEIGHB_PER_PARTICLE]; //neighbors by index

        final int GRID_RES = 32;
        final int GRID_SLOTS = 32;
        final int GRID_TOTAL = GRID_RES*GRID_RES*GRID_RES*GRID_SLOTS;
        final int[] particleGrid = new int[GRID_RES*GRID_RES*GRID_RES*GRID_SLOTS];

        final float[] exports = new float[PARTICLES_MAX];

    public static boolean ready = false;

    public void setCameraData(){
        cameraPos[0] = scene.cameraPosDesired.x;
        cameraPos[1] = scene.cameraPosDesired.y;
        cameraPos[2] = scene.cameraPosDesired.z;
        cameraDirXVec[0] = scene.cameraXVector.x;
        cameraDirXVec[1] = scene.cameraXVector.y;
        cameraDirXVec[2] = scene.cameraXVector.z;
        cameraDirYVec[0] = scene.cameraYVector.x;
        cameraDirYVec[1] = scene.cameraYVector.y;
        cameraDirYVec[2] = scene.cameraYVector.z;
        cameraDirZVec[0] = scene.cameraZVector.x;
        cameraDirZVec[1] = scene.cameraZVector.y;
        cameraDirZVec[2] = scene.cameraZVector.z;
    }

    public kParticleCloud(int _numParticles){
        numParticles=min(_numParticles, PARTICLES_MAX);
        generateParticles();
    }

    public void clearGrid(){
        for(int i=0; i<particleGrid.length; i++){
            particleGrid[i]=-1;
        }
    }

    public boolean noSkip(float x, float y, float z){
        float gridX = (GRID_RES*(x-lowerX)/(upperX-lowerX));
        float gridY = (GRID_RES*(y-lowerY)/(upperY-lowerY));
        float gridZ = (GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

        float gxd = gridX-floor(gridX);
        float gyd = gridY-floor(gridY);
        float gzd = gridZ-floor(gridZ);

        float marginX = GRID_RES/(upperX-lowerX)/2 - neighborDistance;
        float marginY = GRID_RES/(upperY-lowerY)/2 - neighborDistance;
        float marginZ = GRID_RES/(upperZ-lowerZ)/2 - neighborDistance;
        //gridX = max(min(GRID_RES-1, gridX), 1);
        //gridY = max(min(GRID_RES-1, gridY), 1);
        //gridZ = max(min(GRID_RES-1, gridZ), 1);

        return abs(gxd-0.5)<marginX && abs(gyd-0.5)<marginY && abs(gyd-0.5)<marginZ ;
    }

    public int getGridPos(float x, float y, float z){
        int gridX = (int)(GRID_RES*(x-lowerX)/(upperX-lowerX));
        int gridY = (int)(GRID_RES*(y-lowerY)/(upperY-lowerY));
        int gridZ = (int)(GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

        gridX = max(min(GRID_RES-1, gridX), 1);
        gridY = max(min(GRID_RES-1, gridY), 1);
        gridZ = max(min(GRID_RES-1, gridZ), 1);

        return gridZ*GRID_RES*GRID_RES* GRID_SLOTS + gridY*GRID_RES* GRID_SLOTS + gridX* GRID_SLOTS;
    }

    public void addToGrid(int particle){
        int gridPos = getGridPos(positionX[particle],positionY[particle],positionZ[particle]);
        int gridRndPos = prand()%GRID_SLOTS; //this must be random to work!

        particleGrid[gridPos + gridRndPos] = particle;
    }

    public void generateParticles(){
        for(int i=0; i<PARTICLES_MAX; i++){
            resetParticle(i);
        }
        exportData();
    }

    public void importData(){ //TODO are most of these needed?
        this.put(particleGrid)
            .put(cameraDirXVec).put(cameraDirYVec).put(cameraDirZVec).put(cameraPos).put(timestamp);
    }

    public void importData_firstTime(){ //TODO are most of these needed?
        this.put(positionX).put(positionY).put(positionZ)
                .put(velocityX).put(velocityY).put(velocityZ)
                .put(pmass)
                .put(particleGrid)
                .put(cameraDirXVec).put(cameraDirYVec).put(cameraDirZVec).put(cameraPos).put(timestamp);
    }

    public void exportData(){
        this.get(positionX).get(positionY).get(positionZ)
            .get(velocityX).get(velocityY).get(velocityZ)
            .get(density).get(pressure).get(neighborsList)
            .get(particleGrid).get(exports).get(timestamp);
    }

    private int seed = 123456789;
    int randInt() { //random positive or negative integers
        final int a = 1103515245;
        final int c = 12345;
        seed = (a * seed + c);
        return seed;
    }

    float rand(){ //random float from 0 to 1
        return (randInt()/(1f*Integer.MAX_VALUE)+1f)/2f;
    }

    public void resetParticle(int particle){
        velocityX[particle]=0;
        velocityY[particle]=0;
        velocityZ[particle]=0;

        positionX[particle]= (prand()%(int)(upperX-lowerX))+lowerX;
        positionY[particle]= (prand()%(int)(upperY-lowerY))+lowerY;
        positionZ[particle]= (prand()%(int)(upperZ-lowerZ))+lowerZ;

        pmass[particle]=0.01f;
        density[particle]=1f;
        pressure[particle]=1f;
        timestamp[particle]=time+(int)(prand()%particleLifetime);

        resetNeighbors(particle);
   }

    public void resetNeighbors(int particle){
        for(int i=0; i<MAX_NEIGHB_PER_PARTICLE; i++){//reset existing neighbors
            neighborsList[particle*MAX_NEIGHB_PER_PARTICLE + i]=-1;
        }
    }

    public void limitVelocity(int particle, float max){
        float mag = sqrt(velocityX[particle]* velocityX[particle] + velocityY[particle]* velocityY[particle] + velocityZ[particle]* velocityZ[particle]);
        if(mag>max){
            float scale = max/mag;
            velocityX[particle]*=scale;
            velocityY[particle]*=scale;
            velocityZ[particle]*=scale;
        }
    }

    public final void addNeighbor(int particle, int neighbor){
        neighborsList[particle*MAX_NEIGHB_PER_PARTICLE+(neighbor%MAX_NEIGHB_PER_PARTICLE)]=neighbor;
        //if(!alreadyNeighbors(particle,neighbor)){
        //int neighBPos = neighbor%MAX_NEIGHB_PER_PARTICLE;
        //if(neighborsList[particle* MAX_NEIGHB_PER_PARTICLE +neighBPos]!=neighbor){
            //neighborsList[particle* MAX_NEIGHB_PER_PARTICLE + neighBPos]=neighbor;
        //}
    }

    public boolean alreadyNeighbors(int particle, int neighbor){// return false;
        if(neighbor==-1 || particle ==-1)return true;
        for(int neighborNo=0; neighborNo<MAX_NEIGHB_PER_PARTICLE; neighborNo++){
            if(neighborsList[particle* MAX_NEIGHB_PER_PARTICLE +neighborNo]==neighbor)return true;
        }
        return false;
    }

    public float distance(int particle, int neighbor){
        float x = positionX[particle]- positionX[neighbor];
        float y = positionY[particle]- positionY[neighbor];
        float z = positionZ[particle]- positionZ[neighbor];
        return sqrt(x*x+y*y+z*z);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateTime(){
        lastTime=time;
        time = getTime();
        dt=paused?0:(time-lastTime)*S_PER_MS;
    }

    public float dt;
    public long time=getTime();
    public long lastTime;
    public boolean gravityDown = false;
    public long lastShot = 0;
    public long runNo=0;

    private static long getTime() {
       return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getTotalGridMembers(){
        int total=0;
        for(int i=0; i<particleGrid.length; i++){
            if(particleGrid[i]>=0 && particleGrid[i]<numParticles){
                total++;
            }
        }
        return total;
    }

    public float numberBad = 0f;
    public float averageD = 0.10f;
    public float averageP = 0.10f;
    public int maxG = 0;
    public int maxN = 0;
    public float averageNeighbors = 0.0f;

    static long lastPrint = 0;
    public static long statusUpdateInterval = 50;

    public String statusString = "";
    public static boolean firstTime = true;

    public void update(){

        ready=false;
        float gridStep = (upperX-lowerX)/GRID_RES;
        if(neighborDistance>gridStep/2){ //TODO is divide by 2 necessary?
            System.out.println("bad grid step!");
        }

        long time1=System.currentTimeMillis();
        setCameraData();
        updateTime();
        this.setExplicit(true);
        Range range = Range.create(numParticles);
        clearGrid();
        if(firstTime){
            importData_firstTime();
            firstTime=false;
        }else{
            importData();
        }

        this.execute(range, 5);
        exportData();

        runNo++;
        long time2=System.currentTimeMillis();
        getAverages();
        long time3=System.currentTimeMillis();

        if(getTime() - lastPrint > statusUpdateInterval){
            statusString=
                    "Particles " + numParticles + "\n"+
                    "dt " + dt*1000+"ms\n" +
                    this.getExecutionMode() + " Exec " + (time2-time1) + "ms CPU " +  (time3-time2) + "ms\n"+
                    "avDens " + averageD + "\n" +
                    "avPres " + averageP + "\n" +
                    "avSibs " + averageNeighbors + "\n"+
                    "maxSibs " + maxN + "\n"+
                    "gridFound " + getTotalGridMembers() + "\n" +
                    "gridMAX " + maxG + "\n" +
                    "LocalSz " + range.getLocalSize(0) + " Grps " + range.getNumGroups(0)+ "\n" +
                    "Bad " + numberBad + "\n" +
                    "Arm " + armLen + "\n" +
                    "";
            lastPrint=getTime();
        }

        ready=true;
    }

    public float getTotalExports(){
        float total =0;
        for(int i=0; i<numParticles; i++){
            total+=exports[i];
        }
        return total;
    }

    public Vector3f lowerBounds = new Vector3f(0,0,0);
    public Vector3f upperBounds = new Vector3f(0,0,0);

    public Vector3f lowerBoxBounds = new Vector3f(0,0,0);
    public Vector3f upperBoxBounds = new Vector3f(0,0,0);

    public Vector3f lowerDenseBounds = new Vector3f(0,0,0);
    public Vector3f upperDenseBounds = new Vector3f(0,0,0);

    public void getAverages(){
        float totalN=0f;
        float totalP=0f;
        float totalD=0f;
        float totalBad =0f;
        int neighborsMax=0;
        lowerBounds = new Vector3f(1000000,1000000,1000000);
        upperBounds = new Vector3f(-1000000,-1000000,-1000000);
        lowerBoxBounds = new Vector3f(lowerX,lowerY,lowerZ);
        upperBoxBounds = new Vector3f(upperX,upperY,upperZ);
        lowerDenseBounds = new Vector3f(1000000,1000000,1000000);
        upperDenseBounds = new Vector3f(-1000000,-1000000,-1000000);

        int neighbors=0;

        for(int i=0; i<numParticles; i++){
            neighbors=getTotalNeighbors(i);
            updateLife(i);
            totalN+=neighbors;
            neighborsMax=max(neighborsMax,neighbors);
            //if(!Float.isNaN(pressure[i]))totalP+=pressure[i];
            //if(!Float.isNaN(density[i]))totalD+=density[i];
            totalP+=pressure[i];
            totalD+=density[i];
            lowerBounds.set(min(lowerBounds.x,positionX[i]), min(lowerBounds.y,positionY[i]), min(lowerBounds.z,positionZ[i]));
            upperBounds.set(max(upperBounds.x, positionX[i]), max(upperBounds.y, positionY[i]), max(upperBounds.z, positionZ[i]));

            if(neighbors>1){
                lowerDenseBounds.set(min(lowerDenseBounds.x,positionX[i]),  min(lowerDenseBounds.y,positionY[i]),  min(lowerDenseBounds.z, positionZ[i]));
                upperDenseBounds.set(max(upperDenseBounds.x, positionX[i]), max(upperDenseBounds.y, positionY[i]), max(upperDenseBounds.z, positionZ[i]));
            }

            if(badPosition(i)){
                totalBad++;
            }
        }

        int slotMax=0;
        int totalMax=0;
        for(int i=0; i<GRID_TOTAL; i++){
            if(i%GRID_SLOTS==0){
                totalMax=max(slotMax,totalMax);
                slotMax=0;
            }
            if(particleGrid[i]!=-1){
                slotMax++;
            }
        }

        maxG=totalMax;
        maxN=neighborsMax;
        averageNeighbors = totalN/numParticles;
        averageP = totalP/numParticles;
        averageD = totalD/numParticles;
        numberBad=totalBad;
    }

    public boolean badPosition(int particle){
        final float edgeThresh = 0.001f;
        return  abs(positionX[particle]-lowerX)<edgeThresh || abs(positionX[particle]-upperX)<edgeThresh ||
                abs(positionY[particle]-lowerY)<edgeThresh || abs(positionY[particle]-upperY)<edgeThresh||
                abs(positionZ[particle]-lowerZ)<edgeThresh || abs(positionZ[particle]-upperZ)<edgeThresh;
    }

    public int getTotalNeighbors(int particle){
        int total = 0;
        for(int i=0; i<MAX_NEIGHB_PER_PARTICLE; i++){
            if(neighborsList[particle* MAX_NEIGHB_PER_PARTICLE + i]!=-1){
                total++;
            }
        }
        return total;
    }

    final int LOCALSIZE=250;
    @Local long[] locals = new long[LOCALSIZE];
    @Local long[] rndSeed = {123456789};

    @Override
    public void run() {
        int particle = getGlobalId(0);
        int pass = getPassId();

        if(pass==0){
            addToGrid(particle);
        }else if(pass==1){
            findNeighbors(particle);
        }else if(pass==2){
            updateDensity(particle);
            updatePressure(particle);
        }else if(pass==3){
            updateVelocity(particle);
        }else if(pass==4){
            updatePosition(particle);

        }/*else if(pass==5){
            locals[particle%LOCALSIZE]=1;
            passFromLocal(particle%LOCALSIZE);
        }*/

        //localBarrier();


        /*   //single pass mode -- sloppy but maybe faster?
            addToGrid(particle);
            findNeighbors(particle);
            updateDensity(particle);
            updatePressure(particle);
            updateVelocity(particle);
            updatePosition(particle);
         */
    }

    void updateLife(int particle){
        if(time-timestamp[particle]>particleLifetime){
            resetParticle(particle);
        }
    }

    int prand() { //parallel random positive #
        int particle = getGlobalId(0)+1;
        int pass = getPassId()+1;
        int entropy = (int)(particle*time*pass);

        final int a = 1103515245;
        final int c = 12345;
        rndSeed[0] = (a * rndSeed[0] + c);

        final int rollScale = 1000000;

        return (int)(abs(rndSeed[0]*entropy)%rollScale);
    }

    public void passFromLocal(int particle){
        exports[particle]=locals[particle];
    }

    public final void addNeighborsFromGrid(int particle, int gridPos){
        for(int gridMemberNo=0; gridMemberNo<GRID_SLOTS; gridMemberNo++){
            int gridMember=particleGrid[gridPos + gridMemberNo];
            if(gridMember!=-1){
                if(distance(gridMember,particle)<neighborDistance)
                    addNeighbor(particle,gridMember);
            }
        }
    }

    public final void findNeighbors(int particle){
        if(neighborsReset){
            resetNeighbors(particle);
        }

        float x = positionX[particle];
        float y = positionY[particle];
        float z = positionZ[particle];

        int _prevGridPos;

        int gridPos = getGridPos(x, y, z);
        int _gridPos = gridPos; //record first grid location to avoid repeated queries

        addNeighborsFromGrid(particle,gridPos); //center of box

        boolean noSkip = noSkip(x,y,z);

        if(!noSkip){
            //top NW
            gridPos = getGridPos(x-neighborDistance,y-neighborDistance,z-neighborDistance);
            if(gridPos!=_gridPos)
                addNeighborsFromGrid(particle,gridPos);

            //top NE
            _prevGridPos = gridPos;
            gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z - neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //top SW
            _prevGridPos = gridPos;
            gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z - neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //top SE
            _prevGridPos = gridPos;
            gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z - neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //bottom NW
            _prevGridPos = gridPos;
            gridPos = getGridPos(x - neighborDistance, y - neighborDistance, z + neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //bottom NE
            _prevGridPos = gridPos;
            gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z + neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //bottom SW
            _prevGridPos = gridPos;
            gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z + neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);

            //bottom SE
            _prevGridPos = gridPos;
            gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z + neighborDistance);
            if(gridPos!=_gridPos && gridPos!=_prevGridPos)
                addNeighborsFromGrid(particle, gridPos);
        }

        addNeighbor(particle,particle); //always include self
    }

    public void updatePosition(int particle){
        limitVelocity(particle, speedlimit);

        float flipScale = -1f;
        boolean sticky = false;
        if(sticky)flipScale=0f;

        //flip velocities leaving box
        if((positionX[particle]+velocityX[particle]*dt > upperX)||(positionX[particle]+velocityX[particle]*dt < lowerX)){velocityX[particle]*=flipScale;}
        if((positionY[particle]+velocityY[particle]*dt > upperY)||(positionY[particle]+velocityY[particle]*dt < lowerY)){velocityY[particle]*=flipScale;}
        if((positionZ[particle]+velocityZ[particle]*dt > upperZ)||(positionZ[particle]+velocityZ[particle]*dt < lowerZ)){velocityZ[particle]*=flipScale;}

        //set position, restrict position to box
        positionX[particle]=min(max(lowerX, positionX[particle]+velocityX[particle]*dt), upperX);
        positionY[particle]=min(max(lowerY, positionY[particle]+velocityY[particle]*dt), upperY);
        positionZ[particle]=min(max(lowerZ, positionZ[particle]+velocityZ[particle]*dt), upperZ);
        if(badPosition(particle))resetParticle(particle);
    }

    public float armLen = 300f;

    public void updateVelocity(int particle){
        float accPressureX=0f;   float accViscX=0f;   float accInteractiveX=0f;   float accGravX=0f;
        float accPressureY=0f;   float accViscY=0f;   float accInteractiveY=0f;   float accGravY=0f;
        float accPressureZ=0f;   float accViscZ=0f;   float accInteractiveZ=0f;   float accGravZ=0f;

        float accPressScale=0;
        float accViscScale=0;

        int neighbor=0;
        float weightVal=0;
        float weightVal_d=0;

        for(int neighborNo=0; neighborNo<MAX_NEIGHB_PER_PARTICLE; neighborNo++){
            if(neighbor!=particle){
                neighbor = neighborsList[particle* MAX_NEIGHB_PER_PARTICLE +neighborNo];
                if(neighbor!=-1){
                    float dist = distance(particle,neighbor);
                    if(dist<neighborDistance){
                        weightVal=weight(dist/neighborDistance);
                        weightVal_d=weight_deriv(dist/neighborDistance);

                        accPressScale = -1.0f * pmass[neighbor] * (pressure[particle] / (density[particle] * density[particle]) +
                                pressure[neighbor] / (density[neighbor] * density[neighbor])) * weightVal;
                        accViscScale = mu * pmass[neighbor] / density[neighbor] / density[particle] * weightVal_d;

                        accViscX+=accViscScale*(velocityX[neighbor] - velocityX[particle]);
                        accViscY+=accViscScale*(velocityY[neighbor] - velocityY[particle]);
                        accViscZ+=accViscScale*(velocityZ[neighbor] - velocityZ[particle]);

                        accPressureX+=accPressScale*(positionX[neighbor] - positionX[particle]);
                        accPressureY+=accPressScale*(positionY[neighbor] - positionY[particle]);
                        accPressureZ+=accPressScale*(positionZ[neighbor] - positionZ[particle]);
                    }
                }
            }
        }

        float gravScale = dt/30f;
        if(gravityDown){
            accGravX=0f;
            accGravY=-gravScale;
            accGravZ=0f;

            float shotTimeMS = 500;
            float shotStrength = 5f;
            if(time-lastShot<shotTimeMS){
                //float strength = shotStrength;
                float strength = shotStrength*max(0,1f*(shotTimeMS-(time-lastShot))/shotTimeMS);
                accInteractiveX = -cameraDirZVec[0]*strength;
                accInteractiveY = -cameraDirZVec[1]*strength;
                accInteractiveZ = -cameraDirZVec[2]*strength;
            }

        }else{

            float xd=positionX[particle]-(upperX-cameraPos[0]+lowerX)+cameraDirZVec[0] * armLen;
            float yd=positionY[particle]-(upperY-cameraPos[1]+ lowerY)+cameraDirZVec[1] * armLen;
            float zd=positionZ[particle]-(upperZ-cameraPos[2]+lowerZ)+cameraDirZVec[2] * armLen;

            float mag = sqrt(xd*xd+yd*yd+zd*zd);

            accGravX = -0.5f*gravScale*xd/mag;
            accGravY = -0.5f*gravScale*yd/mag;
            accGravZ = -0.5f*gravScale*zd/mag;
        }

         velocityX[particle]+=accPressureX+accViscX+accInteractiveX+accGravX;
         velocityY[particle]+=accPressureY+accViscY+accInteractiveY+accGravY;
         velocityZ[particle]+=accPressureZ+accViscZ+accInteractiveZ+accGravZ;
    }

    public void updateDensity(int particle){/////////////////////////////////
        float _density=0;
        for(int neighborNo=0; neighborNo<MAX_NEIGHB_PER_PARTICLE; neighborNo++){
            int neighborParticle = neighborsList[particle* MAX_NEIGHB_PER_PARTICLE +neighborNo];
            if(neighborParticle!=-1){
                float dist = distance(particle,neighborParticle);
                _density+=pmass[neighborParticle]*weight(dist/neighborDistance);
            }
        }
        density[particle]=_density;
    }

    public void updatePressure(int particle){
        float _pressure = c*c*(density[particle]-densREF);
        pressure[particle]=_pressure;
    }

    public float weight(float x){
        return max(0,1.0f - x*x*x*x);
    }

    public float weight_deriv(float x){
        return max(0,1.0f - x*x*x*x);
    }
}
