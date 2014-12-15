import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector3f;
import shapes.cloud.kParticleCloud;
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
                if(getTime()-lastPosUpdate>1) {
                    scene.myKCloud.update();
                    lastPosUpdate = getTime();
                }

                if(getTime()-lastSurfaceUpdate>100) {
                    //getSurface();
                    lastSurfaceUpdate = getTime();
                }

                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static float minDens;
    public static float maxDens;
/*
    public static void getSurface(){

        CubeMarcher.analyzer a= new CubeMarcher.analyzer() {
            @Override
            public double getStep() {
                return 32f;
            }

            @Override
            public double potential(double x, double y, double z) {

                if(sphCloud.particleGrid!=null){
                    double dens = sphCloud.densityAt(new Vector3f((float)x,(float)y,(float)z));
                    minDens=Math.min(minDens,(float)dens);
                    maxDens=Math.max(maxDens, (float) dens);
                    //System.out.println("pot!");
                    return dens;
                }

                return 0.0;
            }
        };

        minDens=100f;
        maxDens=-100f;

        CubeMarcher cm = new CubeMarcher();
        ArrayList<CubeMarcher.Triangle> theTriangles = new ArrayList<>();
        int num_tri=0;
        long time1 = System.currentTimeMillis();
        float step = (float)a.getStep();
        int i=0;
        for(float x=sphCloud.lowerCornerBoundsFinal.x; x<sphCloud.upperCornerBoundsFinal.x; x+=step){
            for(float y=sphCloud.lowerCornerBoundsFinal.y; y<sphCloud.upperCornerBoundsFinal.y; y+=step){
                for(float z=sphCloud.lowerCornerBoundsFinal.z; z<sphCloud.upperCornerBoundsFinal.z; z+=step){
                    //System.out.println("OK?");
                    i++;
                    int new_tris = cm.Polygonise(x,y,z, 0.5, a);

                    num_tri += new_tris;
                    for(int t=0; t<new_tris; t++){
                        theTriangles.add(cm.temp_triangles[t].copy());
                        sphCloud.vertex_data.put(cm.temp_triangles[t].asFloatArray());
                        sphCloud.color_data.put(cm.temp_triangles[t].asFloatArray(0.005f));
                    }

                }
            }
        }
        System.out.println("got surface " + (System.currentTimeMillis()-time1)+"ms " + num_tri + " tri min" +minDens + " max" + maxDens + " steps " + i);
        System.out.println((sphCloud.upperCornerBoundsFinal.x-sphCloud.lowerCornerBoundsFinal.x) + "x" +
                            (sphCloud.upperCornerBoundsFinal.y-sphCloud.lowerCornerBoundsFinal.y) + "x" +
                            (sphCloud.upperCornerBoundsFinal.z-sphCloud.lowerCornerBoundsFinal.z));
    }
*/
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
