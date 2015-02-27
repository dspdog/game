import eu.mihosoft.vrl.v3d.*;
import simplify.SimplifyCSG;
import utils.RandomHelper;
import utils.time;

/**
 * Created by user on 2/7/2015.
 */
public class CSGProgram {
    long myIteration=0;
    long lifetimeMs = 10000;
    long minIterationPeriodMs = 10;
    long minSimplifyPeriodMs = 100;

    worldObject myWorldObject= null;

    float radius = 10f;
    int quality = 6;

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
        if( time.getTime() - startTime < lifetimeMs){ //living
            if(time.getTime() - lastBuildTime > minIterationPeriodMs) {
                float scalex = (float)Math.random()*20f-10f;
                float scaley = (float)Math.random()*20f-10f;
                float scalez = (float)Math.random()*20f-10f;
                center = center.plus(new Vector3d(scalex,scaley,scalez));
                CSG addition = new Sphere(center, radius, quality, quality).toCSG();
                CSG newCSG = myCSG.union(addition);
                myCSG = newCSG;//.union(addition);

                lastBuildTime = time.getTime();
                myIteration++;
                if(myWorldObject instanceof worldObject){
                    myWorldObject.updateGeometryData();
                }
            }
        }else{ //dead

            if(time.getTime() - lastBuildTime > minSimplifyPeriodMs) {
                System.out.println("SIMPLIFY!");
                lastBuildTime = time.getTime();

                //if(time.getTime() - lastSimplifyTime > minSimplifyPeriodMs) {
                    SimplifyCSG.loadCSG(myCSG);
                    if(myWorldObject instanceof worldObject){
                       myWorldObject.setCSG(SimplifyCSG.simplifyCSG(myCSG));
                    }
                    lastSimplifyTime = time.getTime();
                //}

                if(myWorldObject instanceof worldObject){
                    myWorldObject.updateGeometryData();
                }
            }
        }


    }


}
