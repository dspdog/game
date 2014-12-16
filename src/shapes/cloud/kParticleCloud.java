package shapes.cloud;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import org.lwjgl.Sys;

/**
 * Created by user on 12/13/2014.
 */
public class kParticleCloud extends Kernel {
    //CLOUD PARAMS
        public static final int PARTICLES_MAX = 10_000;
        public int numParticles=0;

        final float neighborDistance = 5f;
        final float densREF = 0.0099f; // kg/m^3
        final float mu = 0.01f; // kg/ms (dynamical viscosity))
        final float c = 1.9f; // m/s speed of sound

        final float speedlimit = 2f;

        //bounding box
        final float boxSize = 50f;
        public final float lowerX = -boxSize;   public final float upperX = boxSize;
        public final float lowerY = -boxSize;   public final float upperY = boxSize;
        public final float lowerZ = -boxSize;   public final float upperZ = boxSize;

    //PARTICLE PARAMS
        //velocity                                    //position                                    //density, mass, pressure
        final float[] velocityX = new float[PARTICLES_MAX];  public final float[] positionX = new float[PARTICLES_MAX];  final float[] pd = new float[PARTICLES_MAX];
        final float[] velocityY = new float[PARTICLES_MAX];  public final float[] positionY = new float[PARTICLES_MAX];  final float[] pm = new float[PARTICLES_MAX];
        final float[] velocityZ = new float[PARTICLES_MAX];  public final float[] positionZ = new float[PARTICLES_MAX];  final float[] pp = new float[PARTICLES_MAX];

        final int MAX_NEIGHBORS = 64;
        final int[] pn = new int[PARTICLES_MAX*MAX_NEIGHBORS]; //neighbors by index
        final int[] pnn = new int[PARTICLES_MAX]; //neighbors totals by index

        final int GRID_RES = 10;
        final int GRID_SLOTS = 50;
        final int[] particleGrid = new int[GRID_RES*GRID_RES*GRID_RES * GRID_SLOTS];
        final int[] particleGridTotal = new int[GRID_RES*GRID_RES*GRID_RES];

    public static boolean ready = false;

    public kParticleCloud(int _numParticles){
        numParticles=min(_numParticles, PARTICLES_MAX);
        generateParticles();
    }

    public void clearGrid(){

        for(int i=0; i<particleGrid.length; i++){
            particleGrid[i]=-1;
        }

        for(int i=0; i<particleGridTotal.length; i++){
            particleGridTotal[i]=0;
        }
    }

    public int getGridPosI(int particle){
        float x=positionX[particle];
        float y=positionY[particle];
        float z=positionZ[particle];
        return getGridPos(x,y,z);
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

    public int getGridTotalsPos(float x, float y, float z){
        int gridX = (int)(GRID_RES*(x-lowerX)/(upperX-lowerX));
        int gridY = (int)(GRID_RES*(y-lowerY)/(upperY-lowerY));
        int gridZ = (int)(GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

        gridX = max(min(GRID_RES-1, gridX), 1);
        gridY = max(min(GRID_RES-1, gridY), 1);
        gridZ = max(min(GRID_RES-1, gridZ), 1);

        return gridZ*GRID_RES*GRID_RES + gridY*GRID_RES + gridX;
    }

    public int getGridMember(int gridPos, int member){
        member = min(member, GRID_SLOTS -1);
        return particleGrid[gridPos + member];
    }

    public void addToGrid(int particle){
        int gridPos = getGridPos(positionX[particle],positionY[particle],positionZ[particle]);
        int gridTotalsPos = getGridTotalsPos(positionX[particle],positionY[particle],positionZ[particle]);
        particleGrid[gridPos + particleGridTotal[gridTotalsPos]] = particle;
        particleGridTotal[gridTotalsPos]=min(particleGridTotal[gridTotalsPos]+1, GRID_SLOTS -1);
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
            .put(pd).put(pm).put(pp)
            .put(pn).put(pnn).put(particleGrid).put(particleGridTotal);
    }

    public void exportData(){
        this.get(positionX).get(positionY).get(positionZ);
        this.get(velocityX).get(velocityY).get(velocityZ).get(pd).get(pm).get(pp).get(pn).get(pnn).get(particleGrid).get(particleGridTotal);
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
        setVelocity(particle,0,0,0);
        setPosition(particle,
                rand()*(upperX-lowerX)+lowerX,
                rand()*(upperY-lowerY)+lowerY,
                rand()*(upperZ-lowerZ)+lowerZ);
        setMass(particle,0.01f);
        setDensity(particle,1f);
        setPressure(particle,1f);
    }

    public void setVelocity(int particle, float x, float y, float z){
        velocityX[particle]=x; velocityY[particle]=y; velocityZ[particle]=z;}
    public void setPosition(int particle, float x, float y, float z){
        positionX[particle]=x; positionY[particle]=y; positionZ[particle]=z;}
    public void setMass(int particle, float value){pm[particle]=value;}
    public void setDensity(int particle, float value){pd[particle]=value;}
    public void setPressure(int particle, float value){pp[particle]=value;}

    public void translateVelocity(int particle, float x, float y, float z){
        velocityX[particle]+=x; velocityY[particle]+=y; velocityZ[particle]+=z;}
    public void translatePosition(int particle, float x, float y, float z){
        positionX[particle]+=x; positionY[particle]+=y; positionZ[particle]+=z;}

    public void limitVelocity(int particle, float max){
        float mag = sqrt(velocityX[particle]* velocityX[particle] + velocityY[particle]* velocityY[particle] + velocityZ[particle]* velocityZ[particle]);
        if(mag>max){
            float scale = max/mag;
            setVelocity(particle, velocityX[particle]*scale, velocityY[particle]*scale, velocityZ[particle]*scale);
        }
    }

    public float getMass(int particle){return pm[particle];}
    public float getDensity(int particle){return pd[particle];}
    public float getPressure(int particle){return pp[particle];}
    public int getNumberOfNeighbors(int particle){return pnn[particle];}

    public void resetNeighbors(int i){
        pnn[i]=0;
    }

    public void addNeighbor(int particle, int neighbor){
        if(pnn[particle] < MAX_NEIGHBORS && !alreadyNeighbors(particle,neighbor)){
            pnn[particle]++;
            pn[particle*MAX_NEIGHBORS+pnn[particle]]=neighbor;
        }
    }

    public int getNeighbor(int particle, int neighborNo){
            return pn[particle*MAX_NEIGHBORS+neighborNo];
    }

    public boolean alreadyNeighbors(int particle, int neighbor){
        int len = getNumberOfNeighbors(particle);
        for(int neighborNo=0; neighborNo<len; neighborNo++){
            if(getNeighbor(particle,neighborNo)==neighbor)return true;
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
        dt=(time-lastTime)*0.0151f;
    }

    public float dt;
    public long time=getTime();
    public long lastTime;
    public boolean gravityDown = false;
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

    public int getGridMax(){
        int max=0;
        for(int i=0; i<particleGridTotal.length; i++){
            max = max(max, particleGridTotal[i]);
        }
        return max;
    }

    public float averageD = 0.10f;
    public float averageP = 0.10f;

    public void update(){
        ready=false;
        float gridStep = (upperX-lowerX)/GRID_RES;
        if(neighborDistance>gridStep/2){ //TODO is divide by 2 necessary?
            System.out.println("bad grid step!");
        }

        long time1=System.currentTimeMillis();

        updateTime();
        this.setExplicit(true);
        Range range = Range.create(numParticles);

        clearGrid();

        importData();
        this.execute(range, 5);
        exportData();

        runNo++;
        float averageNeighbors = getAverageNeighbors();

        averageD = getAverageDensity();
        averageP = getAveragePressure();

        limitedPrint(" " +this.getExecutionMode() + " dt " + (dt*1000)+"ms parts " + numParticles + " exec" + (System.currentTimeMillis()-time1) +
                    "\n avN " + averageNeighbors + " avD " + averageD + " avP " + averageP+
                    "\n grid " + getTotalGridMembers() + " max " + getGridMax());
        ready=true;
    }

    public float getAverageNeighbors(){
        float total=0;
        for(int i=0; i<numParticles; i++){
            total+=getNumberOfNeighbors(i);
        }
        return total/numParticles;
    }

    public float getAverageDensity(){
        float total=0;
        for(int i=0; i<numParticles; i++){
            total+=getDensity(i);
        }
        return total/numParticles;
    }

    public float getAveragePressure(){
        float total=0;
        for(int i=0; i<numParticles; i++){
            total+=getPressure(i);
        }
        return total/numParticles;
    }

    static long lastPrint = 0;
    public void limitedPrint(String s){
        if(getTime() - lastPrint > 1000){
            System.out.println(s);
            lastPrint=getTime();
        }
    }

    @Override
    public void run() {
        int particle = getGlobalId(0);
        int pass = getPassId();

        if(pass==0){
            addToGrid(particle);
        }if(pass==1){
            findNeighbors(particle);
        }else if(pass==2){
            updateDensity(particle);
            updatePressure(particle);
        }else if(pass==3){
            updateVelocity(particle);
        }else if(pass==4){
            updatePosition(particle);
        }
    }

    public void addNeighborsFromGrid(int particle, int gridPos, int gridTotalPos){
        for(int gridMemberNo=0; gridMemberNo<particleGridTotal[gridTotalPos]; gridMemberNo++){
            int gridMember=getGridMember(gridPos, gridMemberNo);
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

        int _gridTotalPos = getGridTotalsPos(x, y, z); //record first grid location to avoid repeated queries

        int gridTotalPos = getGridTotalsPos(x,y,z);
        int gridPos = getGridPos(x, y, z);

        addNeighborsFromGrid(particle,gridPos,gridTotalPos); //center of box

        //top NW
        gridTotalPos = getGridTotalsPos(x-neighborDistance,y-neighborDistance,z-neighborDistance);
        if(gridTotalPos!=_gridTotalPos){
            gridPos = getGridPos(x-neighborDistance,y-neighborDistance,z-neighborDistance);
            addNeighborsFromGrid(particle,gridPos,gridTotalPos);
        }

        //top NE
        gridTotalPos = getGridTotalsPos(x+neighborDistance,y-neighborDistance,z-neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z - neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //top SW
        gridTotalPos = getGridTotalsPos(x-neighborDistance,y+neighborDistance,z-neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z - neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //top SE
        gridTotalPos = getGridTotalsPos(x+neighborDistance,y+neighborDistance,z-neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z - neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //bottom NW
        gridTotalPos = getGridTotalsPos(x-neighborDistance,y-neighborDistance,z+neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x - neighborDistance, y - neighborDistance, z + neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //bottom NE
        gridTotalPos = getGridTotalsPos(x+neighborDistance,y-neighborDistance,z+neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x + neighborDistance, y - neighborDistance, z + neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //bottom SW
        gridTotalPos = getGridTotalsPos(x-neighborDistance,y+neighborDistance,z+neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x - neighborDistance, y + neighborDistance, z + neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        //bottom SE
        gridTotalPos = getGridTotalsPos(x+neighborDistance,y+neighborDistance,z+neighborDistance);
        if(gridTotalPos!=_gridTotalPos) {
            gridPos = getGridPos(x + neighborDistance, y + neighborDistance, z + neighborDistance);
            addNeighborsFromGrid(particle, gridPos, gridTotalPos);
        }

        addNeighbor(particle,particle); //always include self

        //TODO check grid resolution is low enough...
    }

    public void updatePosition(int particle){
        limitVelocity(particle, speedlimit);

        //flip velocities leaving box
        if((positionX[particle]+velocityX[particle]*dt > upperX)||(positionX[particle]+velocityX[particle]*dt < lowerX)){velocityX[particle]*=-1f;}
        if((positionY[particle]+velocityY[particle]*dt > upperY)||(positionY[particle]+velocityY[particle]*dt < lowerY)){velocityY[particle]*=-1f;}
        if((positionZ[particle]+velocityZ[particle]*dt > upperZ)||(positionZ[particle]+velocityZ[particle]*dt < lowerZ)){velocityZ[particle]*=-1f;}

        //update positions
        translatePosition(particle, velocityX[particle] * dt, velocityY[particle] * dt, velocityZ[particle] * dt);

        //restrict position to box
        setPosition(particle,
                min(max(lowerX, positionX[particle]), upperX),
                min(max(lowerY, positionY[particle]), upperY),
                min(max(lowerZ, positionZ[particle]), upperZ)
        );
    }

    public void updateVelocity(int particle){
        float accPressureX=0f;   float accViscX=0f;   float accInteractiveX=0f;   float accGravX=0f;
        float accPressureY=0f;   float accViscY=0f;   float accInteractiveY=0f;   float accGravY=0f;
        float accPressureZ=0f;   float accViscZ=0f;   float accInteractiveZ=0f;   float accGravZ=0f;

        float accPressScale=0;
        float accViscScale=0;

        int len = getNumberOfNeighbors(particle);
        int neighbor=0;
        float weightVal=0;
        float weightVal_d=0;

        for(int neighborNo=0; neighborNo<len; neighborNo++){
            if(neighbor!=particle){
                neighbor = getNeighbor(particle, neighborNo);
                float dist = distance(particle,neighbor);
                if(dist<neighborDistance){
                    weightVal=weight(dist/neighborDistance);
                    weightVal_d=weight_deriv(dist/neighborDistance);

                    accPressScale = -1.0f * getMass(neighbor) * (getPressure(particle) / (getDensity(particle) * getDensity(particle)) +
                                                                 getPressure(neighbor) / (getDensity(neighbor) * getDensity(neighbor))) * weightVal;
                    accViscScale = mu * getMass(neighbor) / getDensity(neighbor) / getDensity(particle) * weightVal_d;

                    accViscX+=accViscScale*(velocityX[neighbor] - velocityX[particle]);
                    accViscY+=accViscScale*(velocityY[neighbor] - velocityY[particle]);
                    accViscZ+=accViscScale*(velocityZ[neighbor] - velocityZ[particle]);

                    accPressureX+=accPressScale*(positionX[neighbor] - positionX[particle]);
                    accPressureY+=accPressScale*(positionY[neighbor] - positionY[particle]);
                    accPressureZ+=accPressScale*(positionZ[neighbor] - positionZ[particle]);
                }
            }
        }

        float gravScale = dt/30f;
        if(gravityDown){
            accGravX=0f;
            accGravY=-gravScale;
            accGravZ=0f;
        }else{
            float mag = sqrt(
                    positionX[particle]*positionX[particle] +
                    positionY[particle]*positionY[particle] +
                    positionZ[particle]*positionZ[particle]);
            accGravX = -gravScale*positionX[particle]/mag;
            accGravY = -gravScale*positionY[particle]/mag;
            accGravZ = -gravScale*positionZ[particle]/mag;
        }

        translateVelocity(particle,
                            accPressureX+accViscX+accInteractiveX+accGravX,
                            accPressureY+accViscY+accInteractiveY+accGravY,
                            accPressureZ+accViscZ+accInteractiveZ+accGravZ);
    }

    public void updateDensity(int particle){/////////////////////////////////
        float density=0;//getDensity(particle);
        addNeighbor(particle,particle); //make sure to include self
        int len = getNumberOfNeighbors(particle);
        for(int neighborNo=0; neighborNo<len; neighborNo++){
            int neighborParticle = getNeighbor(particle,neighborNo);
            float dist = distance(particle,neighborParticle);
            density+=getMass(neighborParticle)*weight(dist/neighborDistance);
        }
        setDensity(particle,density);
    }

    public void updatePressure(int particle){
        float pressure = c*c*(getDensity(particle)-densREF);
        setPressure(particle, pressure);
    }

    public float weight(float x){
        return max(0,1.0f - x*x);
    }

    public float weight_deriv(float x){
        return max(0,1.0f - x*x);
    }
}
