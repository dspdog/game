package shapes.cloud;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 12/13/2014.
 */
public class kParticleCloud extends Kernel {
    //CLOUD PARAMS
        final int PARTICLES_MAX = 100000;
        public int numParticles=0;

        final float neighborDistance = 5.0f;
        final float densREF = 1000; // kg/m^3
        final float mu = 0.01f; // kg/ms (dynamical viscosity))
        final float c = 1.9f; // m/s speed of sound

        final float speedlimit = 1f;

        //bounding box
        final float boxSize = 200f;
        public float lowerX = -boxSize;   public float upperX = boxSize;
        public float lowerY = -boxSize;   public float upperY = boxSize;
        public float lowerZ = -boxSize;   public float upperZ = boxSize;

    //PARTICLE PARAMS
        //velocity                                    //position                                    //density, mass, pressure
        final float[] vx = new float[PARTICLES_MAX];  final float[] px = new float[PARTICLES_MAX];  final float[] pd = new float[PARTICLES_MAX];
        final float[] vy = new float[PARTICLES_MAX];  final float[] py = new float[PARTICLES_MAX];  final float[] pm = new float[PARTICLES_MAX];
        final float[] vz = new float[PARTICLES_MAX];  final float[] pz = new float[PARTICLES_MAX];  final float[] pp = new float[PARTICLES_MAX];

        final int MAX_NEIGHBORS = 20;
        final int[] pn = new int[PARTICLES_MAX*MAX_NEIGHBORS]; //neighbors by index
        final int[] pnn = new int[PARTICLES_MAX]; //neighbors totals by index

    public kParticleCloud(int _numParticles){
        numParticles=min(_numParticles, PARTICLES_MAX);
        generateParticles();
    }

    public void generateParticles(){
        for(int i=0; i<numParticles; i++){
            initParticle(i);
            resetNeighbors(i);
        }
    }

    public void importData(){ //TODO are most of these needed?
        this.put(px).put(py).put(pz)
            .put(vx).put(vy).put(vz)
            .put(pd).put(pm).put(pp)
            .put(pn).put(pnn);
    }

    public void exportPositions(){
        this.get(px).get(py).get(pz);
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

    public void initParticle(int particle){
        float velocityScale = 0.01f;

        setVelocity(particle,
                velocityScale * rand() ,
                velocityScale * rand() ,
                velocityScale * rand());

        setPosition(particle,
                rand()*(upperX-lowerX)+lowerX,
                rand()*(upperY-lowerY)+lowerY,
                rand()*(upperZ-lowerZ)+lowerZ);

        setMass(particle,1f);
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
        dt=(time-lastTime)*0.001f;
    }

    public float dt;
    public long time;
    public long lastTime;

    private static long getTime() {
       return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void update(){
        long time1=System.currentTimeMillis();

        updateTime();
        this.setExplicit(true);
        Range range = Range.create(numParticles);

        importData();
        this.execute(range, 4);
        exportPositions();

        limitedPrint(this.getExecutionMode() + " dt " + (dt*1000)+"ms parts" + numParticles + " exec" + (System.currentTimeMillis()-time1));
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
            findNeighbors(particle);
        }else if(pass==1){
            updateDensity(particle);
            updatePressure(particle);
        }else if(pass==2){
            updateVelocity(particle);
        }else if(pass==3){
            updatePosition(particle);
        }
    }

    public void findNeighbors(int particle){
        resetNeighbors(particle);
        for(int o=0; o<numParticles; o++){
            if(distance(o,particle)<neighborDistance){
                addNeighbor(particle,o);
            }
        }
    }

    public void updatePosition(int particle){
        //limitVelocity(particle, speedlimit);

        //flip velocities leaving box
        if((getPositionX(particle)+getVelocityX(particle)*dt > upperX)||(getPositionX(particle)+getVelocityX(particle)*dt < lowerX))flipVelocityX(particle);
        if((getPositionY(particle)+getVelocityY(particle)*dt > upperY)||(getPositionY(particle)+getVelocityY(particle)*dt < lowerY))flipVelocityY(particle);
        if((getPositionZ(particle)+getVelocityZ(particle)*dt > upperZ)||(getPositionZ(particle)+getVelocityZ(particle)*dt < lowerZ))flipVelocityZ(particle);

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
        float accPressureX=0f;   float accViscX=0f;   float accInteractiveX=0f;   float accGravX=0f;// new Vector3f(p.pos.x-center.x,p.pos.y-center.y,p.pos.z-center.z); //suction source at origin
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
        //if(gravityDown){
        //    accGravity = new Vector3f(0,1f,0);
        //}
        //accGravity.normalise().scale(9f);

        translateVelocity(particle,
                            accPressureX+accViscX+accInteractiveX-accGravX,
                            accPressureY+accViscY+accInteractiveY-accGravY,
                            accPressureZ+accViscZ+accInteractiveZ-accGravZ);
    }

    public void updateDensity(int particle){/////////////////////////////////
        float density=0f;
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
        //x*=1f;
        return max(0,(1.0f - x*x)*1f);
    }

    public float weight_deriv(float x){
        return -2f*x;
    }


}
