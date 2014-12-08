import factory.TextureFactory;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import shapes.cloud.sphCloud;
import shapes.geography.GeographyFactory;
import shapes.tree.tree;
import world.WorldObject;
import world.scene;

public class gameWorldWater implements Runnable {
    boolean running = false;

    scene myScene;

    long lastNeighborUpdate=0;

    public CubeMarcher cm = new CubeMarcher();
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
                if(getTime()-lastNeighborUpdate>100) {
                    sphCloud.findNeighbors();
                    lastNeighborUpdate = getTime();
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
