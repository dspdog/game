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
    int numParticles=0;

    final float neighborDistance = 1.0f;
    final float densREF = 1000; // kg/m^3
    final float mu = 0.01f; // kg/ms (dynamical viscosity))
    final float c = 1.9f; // m/s speed of sound

    //PARTICLE PARAMS

    //velocity                                    //position
    final float[] vx = new float[PARTICLES_MAX];  final float[] px = new float[PARTICLES_MAX];
    final float[] vy = new float[PARTICLES_MAX];  final float[] py = new float[PARTICLES_MAX];
    final float[] vz = new float[PARTICLES_MAX];  final float[] pz = new float[PARTICLES_MAX];

    final float[] pd = new float[PARTICLES_MAX]; //density
    final float[] pm = new float[PARTICLES_MAX]; //mass
    final float[] pp = new float[PARTICLES_MAX]; //pressure

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

    public void initParticle(int particle){
        setVelocity(particle,0,0,0);
        setPosition(particle,0,0,0);
        setMass(particle,1f);
        setDensity(particle,1f);
        setPressure(particle,1f);
    }

    public void setVelocity(int particle, float x, float y, float z){vx[particle]=x; vy[particle]=y; vz[particle]=z;}
    public void setPosition(int particle, float x, float y, float z){px[particle]=x; py[particle]=y; pz[particle]=z;}
    public void setMass(int particle, float value){pm[particle]=value;}
    public void setDensity(int particle, float value){pd[particle]=value;}
    public void setPressure(int particle, float value){pp[particle]=value;}

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
        updateTime();

        long time1=System.currentTimeMillis();
        this.setExplicit(true);

        //update data w/:
        //this.put(lineXY1).put(lineZS1).put(lineDI)
        //        .put(lineXY2).put(lineZS2);

        Range range = Range.create(numParticles);

        this.execute(range, 4);
        //this.get(pixels);

        System.out.println(this.getExecutionMode() + " dt " + (dt*1000)+"ms parts" + numParticles + " exec" + (System.currentTimeMillis()-time1));
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
        /*
            Vector3f lower = sphCloud.lowerCorner;
            Vector3f upper = sphCloud.upperCorner;

            if(vel.lengthSquared()>speedlimit){
                vel.normalise().scale(speedlimit);} //speed limiter

            if((pos.x+ vel.x*dt > upper.x) || (pos.x+ vel.x*dt < lower.x))
                vel.x*=-1f;
            if((pos.y+ vel.y*dt > upper.y) || (pos.y+ vel.y*dt < lower.y))
                vel.y*=-1f;
            if((pos.x+ vel.z*dt > upper.z) || (pos.z+ vel.z*dt < lower.z))
                vel.z*=-1f;
            pos.set(pos.x+ vel.x*dt, pos.y+ vel.y*dt, pos.z+ vel.z*dt);

            pos.set(
                    Math.min(Math.max(lower.x, pos.x), upper.x),
                    Math.min(Math.max(lower.y, pos.y), upper.y),
                    Math.min(Math.max(lower.z, pos.z), upper.z)
            );

         */
    }

    public void updateVelocity(int particle){
        float accPressureX=0f;   float accViscX=0f;   float accInteractiveX=0f;   float accGravX=0f;// new Vector3f(p.pos.x-center.x,p.pos.y-center.y,p.pos.z-center.z); //suction source at origin
        float accPressureY=0f;   float accViscY=0f;   float accInteractiveY=0f;   float accGravY=0f;
        float accPressureZ=0f;   float accViscZ=0f;   float accInteractiveZ=0f;   float accGravZ=0f;

        float accPressScale=0;
        float accViscScale=0;

        int len = getNumberOfNeighbors(particle);

        for(int i=0; i<len; i++){
            /*
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
             */
        }

        /*
            if(gravityDown){
                accGravity = new Vector3f(0,1f,0);
            }

            accGravity.normalise().scale(9f);

            p.vel.translate(
                    accPressure.x+accVisc.x+accInteractive.x-accGravity.x,
                    accPressure.y+accVisc.y+accInteractive.y-accGravity.y,
                    accPressure.z+accVisc.z+accInteractive.z-accGravity.z);
            */
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
