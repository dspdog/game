package shapes.cloud;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import org.lwjgl.Sys;

/**
 * Created by user on 12/13/2014.
 */
public class kParticleCloud extends Kernel {
    //CLOUD PARAMS
        final int PARTICLES_MAX = 100000;
        public int numParticles=0;

        final float neighborDistance = 2.0f;
        final float densREF = 1000; // kg/m^3
        final float mu = 0.01f; // kg/ms (dynamical viscosity))
        final float c = 1.9f; // m/s speed of sound

        final float speedlimit = 2f;

        //bounding box
        final float boxSize = 200f;
        public final float lowerX = -boxSize;   public final float upperX = boxSize;
        public final float lowerY = -boxSize;   public final float upperY = boxSize;
        public final float lowerZ = -boxSize;   public final float upperZ = boxSize;

    //PARTICLE PARAMS
        //velocity                                    //position                                    //density, mass, pressure
        final float[] vx = new float[PARTICLES_MAX];  final float[] px = new float[PARTICLES_MAX];  final float[] pd = new float[PARTICLES_MAX];
        final float[] vy = new float[PARTICLES_MAX];  final float[] py = new float[PARTICLES_MAX];  final float[] pm = new float[PARTICLES_MAX];
        final float[] vz = new float[PARTICLES_MAX];  final float[] pz = new float[PARTICLES_MAX];  final float[] pp = new float[PARTICLES_MAX];

        final int MAX_NEIGHBORS = 50;
        final int[] pn = new int[PARTICLES_MAX*MAX_NEIGHBORS]; //neighbors by index
        final int[] pnn = new int[PARTICLES_MAX]; //neighbors totals by index

        final int GRID_RES = 32;
        final int GRID_SLOTS = 100;
        final int[] particleGrid = new int[GRID_RES*GRID_RES*GRID_RES * GRID_SLOTS];
        final int[] particleGridTotal = new int[GRID_RES*GRID_RES*GRID_RES];

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

    public int getGridPos(float x, float y, float z){
        int gridX = (int)(1f*GRID_RES*(x-lowerX)/(upperX-lowerX));
        int gridY = (int)(1f*GRID_RES*(y-lowerY)/(upperY-lowerY));
        int gridZ = (int)(1f*GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

        gridX = max(min(GRID_RES-1, gridX), 1);
        gridY = max(min(GRID_RES-1, gridY), 1);
        gridZ = max(min(GRID_RES-1, gridZ), 1);

        return gridZ*GRID_RES*GRID_RES* GRID_SLOTS + gridY*GRID_RES* GRID_SLOTS + gridX* GRID_SLOTS;
    }

    public int getGridTotalsPos(float x, float y, float z){
        int gridX = (int)(1f*GRID_RES*(x-lowerX)/(upperX-lowerX));
        int gridY = (int)(1f*GRID_RES*(y-lowerY)/(upperY-lowerY));
        int gridZ = (int)(1f*GRID_RES*(z-lowerZ)/(upperZ-lowerZ));

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
        int gridPos = getGridPos(getPositionX(particle), getPositionY(particle), getPositionZ(particle));
        int gridTotalsPos = getGridTotalsPos(getPositionX(particle), getPositionY(particle), getPositionZ(particle));

        if(!alreadyInGrid(particle)){
            particleGrid[gridPos + particleGridTotal[gridTotalsPos]] = particle;
            particleGridTotal[gridTotalsPos]++;

            particleGridTotal[gridTotalsPos]=min(particleGridTotal[gridTotalsPos], GRID_SLOTS -1);

        }
    }

    public boolean alreadyInGrid(int particle){
        int gridPos = getGridPos(getPositionX(particle), getPositionY(particle), getPositionZ(particle));
        int gridTotalsPos = getGridTotalsPos(getPositionX(particle), getPositionY(particle), getPositionZ(particle));

        for(int gridMemberNo=0; gridMemberNo<particleGridTotal[gridTotalsPos]; gridMemberNo++){
            if(getGridMember(gridPos, gridMemberNo)==particle)return true;
        }
        return false;
    }

    public void generateParticles(){
        for(int i=0; i<PARTICLES_MAX; i++){
            initParticle(i);
            resetNeighbors(i);
        }
        exportAll();
    }

    public void importData(){ //TODO are most of these needed?
        this.put(px).put(py).put(pz)
            .put(vx).put(vy).put(vz)
            .put(pd).put(pm).put(pp)
            .put(pn).put(pnn).put(particleGrid).put(particleGridTotal);
    }

    public void exportAll(){
        this.get(px).get(py).get(pz);
        this.get(vx).get(vy).get(vz).get(pd).get(pm).get(pp).get(pn).get(pnn).get(particleGrid).get(particleGridTotal);
    }

    public particle getParticle(int particle){
        particle p = new particle();
        p.pos.x=getPositionX(particle);
        p.pos.y=getPositionY(particle);
        p.pos.z=getPositionZ(particle);
        return p;
    }

    int seed = 123456789;
    int randInt() { //random positive or negative integers
        int a = 1103515245;
        int c = 12345;
        seed = (a * seed + c);
        return seed;
    }

    float rand(){ //random float from 0 to 1
        return (randInt()/(1f*Integer.MAX_VALUE)+1f)/2f;
    }

    float randZero(){ //random float from -1 to 1
        return (randInt()/(1f*Integer.MAX_VALUE));
    }

    public void initParticle(int particle){
        float velocityScale = 1f;

        setVelocity(particle,
                velocityScale * randZero() ,
                velocityScale * randZero() ,
                velocityScale * randZero());

        setPosition(particle,
                rand()*(upperX-lowerX)+lowerX,
                rand()*(upperY-lowerY)+lowerY,
                rand()*(upperZ-lowerZ)+lowerZ);

        setMass(particle,0.01f);
        setDensity(particle,1f);
        setPressure(particle,1f);
    }

    public void setVelocity(int particle, float x, float y, float z){vx[particle]=x; vy[particle]=y; vz[particle]=z;}
    public void setPosition(int particle, float x, float y, float z){px[particle]=x; py[particle]=y; pz[particle]=z;}
    public void setMass(int particle, float value){pm[particle]=value;}
    public void setDensity(int particle, float value){pd[particle]=value;}
    public void setPressure(int particle, float value){pp[particle]=value;}

    public float getVelocityX(int particle){return vx[particle];}
    public float getVelocityY(int particle){return vy[particle];}
    public float getVelocityZ(int particle){return vz[particle];}

    public void flipVelocityX(int particle){vx[particle]*=-1f;}
    public void flipVelocityY(int particle){vy[particle]*=-1f;}
    public void flipVelocityZ(int particle){vz[particle]*=-1f;}

    public void translateVelocity(int particle, float x, float y, float z){vx[particle]+=x; vy[particle]+=y; vz[particle]+=z;}
    public void translatePosition(int particle, float x, float y, float z){px[particle]+=x; py[particle]+=y; pz[particle]+=z;}

    public void limitVelocity(int particle, float max){
        float mag = sqrt(vx[particle]*vx[particle] + vy[particle]*vy[particle] + vz[particle]*vz[particle]);
        if(mag>max){
            float scale = max/mag;
            setVelocity(particle, vx[particle]*scale, vy[particle]*scale, vz[particle]*scale);
        }
    }

    public float getPositionX(int particle){return px[particle];}
    public float getPositionY(int particle){return py[particle];}
    public float getPositionZ(int particle){return pz[particle];}

    public float getMass(int particle){return pm[particle];}
    public float getDensity(int particle){return pd[particle];}
    public float getPressure(int particle){return pp[particle];}
    public int getNumberOfNeighbors(int particle){return pnn[particle];}

    public void resetNeighbors(int i){
        pnn[i]=0;
        for(int n=0; n<MAX_NEIGHBORS; n++){
            pn[i*MAX_NEIGHBORS+n]=0;
        }
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

    public float distance(int i, int o){
        float x = px[i]-px[o];
        float y = py[i]-py[o];
        float z = pz[i]-pz[o];
        return sqrt(x*x+y*y+z*z);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateTime(){
        lastTime=time;
        time = getTime();
        dt=(time-lastTime)*0.21f;
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

    public void update(){

        float gridStep = (upperX-lowerX)/GRID_RES;
        if(neighborDistance>gridStep/2){
            System.out.println("bad grid step");
        }

        long time1=System.currentTimeMillis();

        updateTime();
        this.setExplicit(true);
        Range range = Range.create(numParticles);

        clearGrid();

        importData();
        this.execute(range, 5);
        exportAll();

        runNo++;
        float averageNeighbors = getAverageNeighbors();

        limitedPrint(this.getExecutionMode() + " dt " + (dt*1000)+"ms parts " + numParticles + " exec"
                    + (System.currentTimeMillis()-time1) + " avN " + averageNeighbors + " grid " + getTotalGridMembers() + " max " + getGridMax());
    }

    public float getAverageNeighbors(){
        float total=0;
        for(int i=0; i<numParticles; i++){
            total+=getNumberOfNeighbors(i);
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
            //if(runNo%1==0)
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

        float x = getPositionX(particle);
        float y = getPositionY(particle);
        float z = getPositionZ(particle);

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
        if((getPositionX(particle)+getVelocityX(particle)*dt > upperX)||(getPositionX(particle)+getVelocityX(particle)*dt < lowerX)){flipVelocityX(particle);}
        if((getPositionY(particle)+getVelocityY(particle)*dt > upperY)||(getPositionY(particle)+getVelocityY(particle)*dt < lowerY)){flipVelocityY(particle);}
        if((getPositionZ(particle)+getVelocityZ(particle)*dt > upperZ)||(getPositionZ(particle)+getVelocityZ(particle)*dt < lowerZ)){flipVelocityZ(particle);}

        //update positions
        translatePosition(particle, getVelocityX(particle) * dt, getVelocityY(particle) * dt, getVelocityZ(particle) * dt);

        //restrict position to box
        setPosition(particle,
                min(max(lowerX, getPositionX(particle)), upperX),
                min(max(lowerY, getPositionY(particle)), upperY),
                min(max(lowerZ, getPositionZ(particle)), upperZ)
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
            neighbor = getNeighbor(particle, neighborNo);
            weightVal=weight(distance(particle,neighbor));
            weightVal_d=weight_deriv(distance(particle, neighbor));

            accPressScale = -1.0f * getMass(neighbor) * (getPressure(particle) / (getDensity(particle) * getDensity(particle)) +
                                                         getPressure(neighbor) / (getDensity(neighbor) * getDensity(neighbor))) * weightVal;
            accViscScale = mu * getMass(neighbor) / getDensity(neighbor) / getDensity(particle) * weightVal_d;

            accViscX+=accViscScale*(getVelocityX(neighbor) - getVelocityX(particle));
            accViscY+=accViscScale*(getVelocityY(neighbor) - getVelocityY(particle));
            accViscZ+=accViscScale*(getVelocityZ(neighbor) - getVelocityZ(particle));

            accPressureX+=accPressScale*(getPositionX(neighbor) - getPositionX(particle));
            accPressureY+=accPressScale*(getPositionY(neighbor) - getPositionY(particle));
            accPressureZ+=accPressScale*(getPositionZ(neighbor) - getPositionZ(particle));
        }

        float gravScale = dt/30f;
        if(gravityDown){
            accGravX=0f;
            accGravY=gravScale;
            accGravZ=0f;
        }else{
            float mag = sqrt(
                    getPositionX(particle)*getPositionX(particle) +
                    getPositionY(particle)*getPositionY(particle) +
                    getPositionZ(particle)*getPositionZ(particle));
            accGravX = gravScale*getPositionX(particle)/mag;
            accGravY = gravScale*getPositionY(particle)/mag;
            accGravZ = gravScale*getPositionZ(particle)/mag;
        }

        translateVelocity(particle,
                            accPressureX+accViscX+accInteractiveX-accGravX,
                            accPressureY+accViscY+accInteractiveY-accGravY,
                            accPressureZ+accViscZ+accInteractiveZ-accGravZ);
    }

    public void updateDensity(int particle){/////////////////////////////////
        float density=getDensity(particle);
        int len = getNumberOfNeighbors(particle);
        for(int neighborNo=0; neighborNo<len; neighborNo++){
            int neighborParticle = getNeighbor(particle,neighborNo);
            density+=getMass(neighborParticle)*weight(distance(particle,neighborParticle));
        }
        setDensity(particle,density);
    }

    public void updatePressure(int particle){
        float pressure = c*c*(getDensity(particle)-densREF);
        setPressure(particle, pressure);
    }

    public float weight(float x){
        if(x>neighborDistance)return 0;
        x/=neighborDistance;
        return max(0,(1.0f - x*x)*1f);
    }

    public float weight_deriv(float x){
        if(x>neighborDistance)return 0;
        x/=neighborDistance;
        return -2f*x;
    }
}
