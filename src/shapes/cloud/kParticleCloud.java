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

    //CLOUD PARAMS
        public static final int PARTICLES_MAX = 10000;
        long particleLifetime = 10000;

    public int numParticles=0;

        final float neighborDistance = 3f;
        final float densREF = 0.0012f; // kg/m^3
        final float mu = 1f; // kg/ms (dynamical viscosity))
        final float c = 2.5f; // m/s speed of sound

        final float speedlimit = 0.75f;

        //bounding box
        final float boxSize = 400f;
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

        final int MAX_NEIGHB_PER_PARTICLE = 64;
        final int[] neighborsList = new int[PARTICLES_MAX* MAX_NEIGHB_PER_PARTICLE]; //neighbors by index
        final int[] neighborTotals = new int[PARTICLES_MAX]; //neighbors totals by index

        final int GRID_RES = 32;
        final int GRID_SLOTS = 200;
        final int[] particleGrid = new int[GRID_RES*GRID_RES*GRID_RES * GRID_SLOTS];

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

    public int getGridPos(float x, float y, float z){
        int gridX = (int)(GRID_RES*(x-lowerX)/(upperX-lowerX));
        int gridY = (int)(GRID_RES*(y-lowerY)/(upperY-lowerY));
        int gridZ = (int)(GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

        gridX = max(min(GRID_RES-1, gridX), 1);
        gridY = max(min(GRID_RES-1, gridY), 1);
        gridZ = max(min(GRID_RES-1, gridZ), 1);

        return gridZ*GRID_RES*GRID_RES* GRID_SLOTS + gridY*GRID_RES* GRID_SLOTS + gridX* GRID_SLOTS;
    }

    public int getGridMember(int gridPos, int member){
        //member = min(member, GRID_SLOTS);
        return particleGrid[gridPos + member];
    }

    public void addToGrid(int particle){
        int gridPos = getGridPos(positionX[particle],positionY[particle],positionZ[particle]);
        int gridRndPos = prand()%GRID_SLOTS;

        particleGrid[gridPos + gridRndPos] = particle;
    }

    public void generateParticles(){
        for(int i=0; i<PARTICLES_MAX; i++){
            initParticle(i);
            resetNeighbors(i);
        }
        exportData();
    }

    public void importData(){ //TODO are most of these needed?
        this.put(positionX).put(positionY).put(positionZ)
            .put(velocityX).put(velocityY).put(velocityZ)
            .put(density).put(pmass).put(pressure)
            .put(neighborsList).put(neighborTotals).put(particleGrid)
            .put(cameraDirXVec).put(cameraDirYVec).put(cameraDirZVec).put(cameraPos).put(timestamp);
    }

    public void exportData(){
        this.get(positionX).get(positionY).get(positionZ)
            .get(velocityX).get(velocityY).get(velocityZ)

            .get(density).get(pressure).get(neighborsList)
            .get(neighborTotals).get(particleGrid).get(exports).get(timestamp);
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

    public void initParticle(int particle){
        resetParticle(particle);
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

    public void resetNeighbors(int particle){
        neighborTotals[particle]=0;
        for(int i=0; i<MAX_NEIGHB_PER_PARTICLE; i++){
            neighborsList[particle*MAX_NEIGHB_PER_PARTICLE + i]=-1;
        }
    }

    public void addNeighbor(int particle, int neighbor){
        if(neighborTotals[particle] < MAX_NEIGHB_PER_PARTICLE && !alreadyNeighbors(particle,neighbor)){
            neighborTotals[particle]++;
            neighborsList[particle* MAX_NEIGHB_PER_PARTICLE + neighborTotals[particle]]=neighbor;
        }
    }

    public boolean alreadyNeighbors(int particle, int neighbor){// return false;
        if(neighbor==-1)return true;
        int len = neighborTotals[particle];
        for(int neighborNo=0; neighborNo<len; neighborNo++){
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
        dt=(time-lastTime)*S_PER_MS;
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
            if(particleGrid[i]!=-1){
                total++;
            }
        }
        return total;
    }

    public float numberBad = 0f;
    public float averageD = 0.10f;
    public float averageP = 0.10f;
    public float averageNeighbors = 0.0f;

    static long lastPrint = 0;
    public static long updateInterval = 200;

    public String statusString = "";

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

        importData();
        this.execute(range, 6);
        exportData();

        runNo++;

        getAverages();

        if(getTime() - lastPrint > updateInterval){
            statusString=
                    "Particles " + numParticles + "\n"+
                    "dt " + dt*1000+"ms\n" +
                    this.getExecutionMode() + " Exec " + (System.currentTimeMillis()-time1) + "ms\n"+
                    "avDens " + averageD + "\n" +
                    "avPres " + averageP + "\n" +
                    "avSibs " + averageNeighbors + "\n"+
                    "gridFound " + getTotalGridMembers() + "\n" +
                    "LocalSz " + range.getLocalSize(0) + " Grps " + range.getNumGroups(0)+ "\n" +
                    "Bad " + numberBad;
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

    public void getAverages(){
        float totalN=0f;
        float totalP=0f;
        float totalD=0f;
        float totalBad =0f;
        lowerBounds = new Vector3f(1000000,1000000,1000000);
        upperBounds = new Vector3f(-1000000,-1000000,-1000000);
        lowerBoxBounds = new Vector3f(lowerX,lowerY,lowerZ);
        upperBoxBounds = new Vector3f(upperX,upperY,upperZ);

        for(int i=0; i<numParticles; i++){
            updateLife(i);
            totalN+=getTotalNeighbors(i);
            //if(!Float.isNaN(pressure[i]))totalP+=pressure[i];
            //if(!Float.isNaN(density[i]))totalD+=density[i];
            totalP+=pressure[i];
            totalD+=density[i];
            lowerBounds.set(min(lowerBounds.x,positionX[i]), min(lowerBounds.y,positionY[i]), min(lowerBounds.z,positionZ[i]));
            upperBounds.set(max(upperBounds.x, positionX[i]), max(upperBounds.y, positionY[i]), max(upperBounds.z, positionZ[i]));

            if(badPosition(i)){
                totalBad++;
            }
        }

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
        }else if(pass==5){
            locals[particle%LOCALSIZE]=1;
            passFromLocal(particle%LOCALSIZE);
        }

        localBarrier();
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

        return (int)((rndSeed[0]*entropy)%rollScale+rollScale)%rollScale;
    }

    public void passFromLocal(int particle){
        exports[particle]=locals[particle];
    }

    public void addNeighborsFromGrid(int particle, int gridPos){
        for(int gridMemberNo=0; gridMemberNo<GRID_SLOTS; gridMemberNo++){
            int gridMember=getGridMember(gridPos, gridMemberNo);
            if(gridMember!=-1)
            if(distance(gridMember,particle)<neighborDistance){
                addNeighbor(particle,gridMember);
            }
        }
    }

    public void findNeighbors(int particle){
        resetNeighbors(particle);

        float x = positionX[particle];
        float y = positionY[particle];
        float z = positionZ[particle];

        int _prevGridPos;

        int gridPos = getGridPos(x, y, z);
        int _gridPos = gridPos; //record first grid location to avoid repeated queries

        addNeighborsFromGrid(particle,gridPos); //center of box

        //top NW
        gridPos = getGridPos(x-neighborDistance,y-neighborDistance,z-neighborDistance);
        if(gridPos!=_gridPos){
            addNeighborsFromGrid(particle,gridPos);
        }

        //top NE
        _prevGridPos = gridPos;
        gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z - neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //top SW
        _prevGridPos = gridPos;
        gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z - neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //top SE
        _prevGridPos = gridPos;
        gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z - neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //bottom NW
        _prevGridPos = gridPos;
        gridPos = getGridPos(x - neighborDistance, y - neighborDistance, z + neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //bottom NE
        _prevGridPos = gridPos;
        gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z + neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //bottom SW
        _prevGridPos = gridPos;
        gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z + neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
            addNeighborsFromGrid(particle, gridPos);
        }

        //bottom SE
        _prevGridPos = gridPos;
        gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z + neighborDistance);
        if(gridPos!=_gridPos && gridPos!=_prevGridPos) {
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
        if(badPosition(particle)){
            resetParticle(particle);
            //positionX[particle]=(prand()%(int)(upperX-lowerX))+lowerX;
            //positionY[particle]=(prand()%(int)(upperY-lowerY))+lowerY;
            //positionZ[particle]=(prand()%(int)(upperZ-lowerZ))+lowerZ;

            //velocityX[particle]=0;
            //velocityY[particle]=0;
            //velocityZ[particle]=0;

            //positionX[particle]=prand()%(upperX-lowerX)+lowerX;
            //positionY[particle]=prand()%(upperY-lowerY)+lowerY;
            //positionZ[particle]=prand()%(upperZ-lowerZ)+lowerZ;

            //pmass[particle]=0.01f;
            //density[particle]=1f;
            //pressure[particle]=1f;
            //timestamp[particle]=time+(int)(prand()*particleLifetime);

        }


    }

    public float armLen = 300f;

    public void updateVelocity(int particle){
        float accPressureX=0f;   float accViscX=0f;   float accInteractiveX=0f;   float accGravX=0f;
        float accPressureY=0f;   float accViscY=0f;   float accInteractiveY=0f;   float accGravY=0f;
        float accPressureZ=0f;   float accViscZ=0f;   float accInteractiveZ=0f;   float accGravZ=0f;

        float accPressScale=0;
        float accViscScale=0;

        int len = neighborTotals[particle];
        int neighbor=0;
        float weightVal=0;
        float weightVal_d=0;

        for(int neighborNo=0; neighborNo<len; neighborNo++){
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
        float _density=0;//getDensity(particle);
        int len = neighborTotals[particle];
        for(int neighborNo=0; neighborNo<len; neighborNo++){
            int neighborParticle = neighborsList[particle* MAX_NEIGHB_PER_PARTICLE +neighborNo];
            float dist = distance(particle,neighborParticle);
            _density+=pmass[neighborParticle]*weight(dist/neighborDistance);
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
