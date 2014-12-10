import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import shapes.cloud.particle;
import shapes.cloud.sphCloud;
import world.scene;

public class gameWorldWater implements Runnable {
    boolean running = false;

    scene myScene;

    long lastNeighborUpdate=0;
    long lastPosUpdate=0;
    long lastSurfaceUpdate=0;

    public gameWorldWater(scene _myScene){
        myScene = _myScene;
    }

    public void end(){
        running=false;
    }

    @Override
    public void run() {
        running=true;
        while(running){

            try {
                if(getTime()-lastNeighborUpdate>1) {
                    sphCloud.findNeighbors();
                    lastNeighborUpdate = getTime();
                }

                if(getTime()-lastPosUpdate>1) {
                    sphCloud.updateParticleVelocities();
                    sphCloud.updateParticlePositions();
                    particle.updateTime();
                    lastPosUpdate = getTime();
                }

                if(getTime()-lastSurfaceUpdate>100) {
                    //sphCloud.updateParticleVelocities();
                    //sphCloud.updateParticlePositions();
                    //particle.updateTime();
                    getSurface();
                    lastSurfaceUpdate = getTime();
                }

                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getSurface(){
        CubeMarcher.analyzer a= new CubeMarcher.analyzer() {
            @Override
            public double getStep() {
                return 16f;
            }

            @Override
            public double potential(double x, double y, double z) {
                if(sphCloud.particleGrid!=null)
                return sphCloud.densityAt(new Vector3f((float)x,(float)y,(float)z));
                return 0.0;
            }
        };
        CubeMarcher cm = new CubeMarcher();


        float step = (float)a.getStep();
        for(float x=sphCloud.lowerCornerBoundsFinal.x; x<sphCloud.upperCornerBoundsFinal.x; x+=step){
            for(float y=sphCloud.lowerCornerBoundsFinal.y; y<sphCloud.upperCornerBoundsFinal.y; y+=step){
                for(float z=sphCloud.lowerCornerBoundsFinal.z; z<sphCloud.upperCornerBoundsFinal.z; z+=step){
                    cm.Polygonise(x,y,z, 0.5, a);
                }
            }
        }

    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
