import eu.mihosoft.vrl.v3d.*;
import utils.time;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by user on 2/7/2015.
 */
public class CSGProgram {
    long myIteration=0;
    long lifetimeMs = 1000;
    long minIterationPeriodMs = 100;
    long minSimplifyPeriodMs = 1000;

    worldObject myWorldObject= null;

    float radius = 25f;
    int quality = 8;

    Vector3d center = new Vector3d(0,0,0);
    CSG myCSG = new Sphere(center, radius,quality,quality).toCSG();

    long startTime = time.getTime();
    long lastBuildTime = 0;
    long lastSimplifyTime = 0;

    CSG toCSG(){
        return myCSG;
    }

    public void iterate(float dt){
        //build up the CSG...
        if( time.getTime() - startTime < lifetimeMs){
            if(time.getTime() - lastBuildTime > minIterationPeriodMs) {
                float scale = 20f;
                center = center.plus(new Vector3d(scale/2,scale/2,scale/2));
                CSG addition = new Sphere(center, radius, quality, quality).toCSG();
                myCSG = myCSG.union(addition);

                lastBuildTime = time.getTime();
                myIteration++;
            }
        }else{
            if(time.getTime() - lastSimplifyTime > minSimplifyPeriodMs) {
                //myCSG = old_SimplifyCSG.simplifyCSG(myCSG);
                lastSimplifyTime = time.getTime();
            }
        }

        if(myWorldObject instanceof worldObject){
            myWorldObject.getGeometryData();
        }
    }


}
