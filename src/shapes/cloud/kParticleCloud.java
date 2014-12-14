package shapes.cloud;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
/**
 * Created by user on 12/13/2014.
 */
public class kParticleCloud extends Kernel {
    static final int PARTICLES_MAX = 100000;
    static int numParticles=0;

    static final kParticle[] theParticles = new kParticle[PARTICLES_MAX];

    static final float neighborDistance = 1.0f;

    public kParticleCloud(int _numParticles){
        numParticles=min(_numParticles, PARTICLES_MAX);
        generateParticles();
    }

    public void generateParticles(){
        for(int i=0; i<numParticles; i++){
            theParticles[i] = new kParticle();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void update(){

        long time1=System.currentTimeMillis();

        this.setExplicit(true);


        //update data w/:
        //this.put(lineXY1).put(lineZS1).put(lineDI)
        //        .put(lineXY2).put(lineZS2);

        Range range = Range.create(numParticles);

        this.execute(range, 3);
        //this.get(pixels);

        System.out.println("ran in " + (System.currentTimeMillis()-time1)+"ms " + numParticles);
    }

    @Override
    public void run() {
        //findNeighbors
        //updateParticleVelocities();
        //updateParticlePositions();

        int i = getGlobalId(0);
        int pass = getPassId();
        kParticle theParticle = theParticles[i];

        switch (pass){
            case 0:
                findNeighbors(theParticle);
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    public void findNeighbors(kParticle theParticle){
        theParticle.clearNeighbors();
        for(int i=0; i<numParticles; i++){
            if(distance(theParticle, theParticles[i])<neighborDistance){
                theParticle.addNeighbor(i);
            }
        }
    }

    public float distance(kParticle p1, kParticle p2){
        float dx = p1.px - p2.px;
        float dy = p1.py - p2.py;
        float dz = p1.pz - p2.pz;
        return sqrt(dx*dx + dy*dy + dz*dz);
    }
}
