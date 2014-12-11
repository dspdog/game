import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import shapes.cloud.particle;
import shapes.cloud.sphCloud;
import world.scene;

import java.util.ArrayList;

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
                    getSurface();

                    lastSurfaceUpdate = getTime();
                }

                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static final CubeMarcher.Triangle[] temp_triangles = new CubeMarcher.Triangle[12];
    public static void getSurface(){
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
        ArrayList<CubeMarcher.Triangle> theTriangles = new ArrayList<>();
        int num_tri=0;
        long time1 = System.currentTimeMillis();
        float step = (float)a.getStep();
        int i=0;
        for(float x=sphCloud.lowerCornerBoundsFinal.x; x<sphCloud.upperCornerBoundsFinal.x; x+=step){
            for(float y=sphCloud.lowerCornerBoundsFinal.y; y<sphCloud.upperCornerBoundsFinal.y; y+=step){
                for(float z=sphCloud.lowerCornerBoundsFinal.z; z<sphCloud.upperCornerBoundsFinal.z; z+=step){
                    int new_tris = cm.Polygonise(x,y,z, 0.5, a);

                    num_tri += new_tris;
                    for(int t=0; t<new_tris; t++){
                        theTriangles.add(temp_triangles[t].copy());
                        sphCloud.vertex_data.put(temp_triangles[t].asFloatArray());
                        sphCloud.color_data.put(temp_triangles[t].asFloatArray(0.005f));
                    }

                }
            }
        }
        System.out.println("got surface " + (System.currentTimeMillis()-time1)+"ms " + num_tri + " tri");
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
