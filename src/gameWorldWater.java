import org.lwjgl.Sys;
import shapes.cloud.particle;
import shapes.cloud.sphCloud;
import world.scene;

public class gameWorldWater implements Runnable {
    boolean running = false;

    scene myScene;

    long lastNeighborUpdate=0;
    long lastPosUpdate=0;

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

                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
