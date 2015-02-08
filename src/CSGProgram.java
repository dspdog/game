import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.lwjgl.util.vector.Vector3f;
import simplify.Simplify;
import simplify.SimplifyCSG;
import utils.RandomHelper;
import utils.time;

/**
 * Created by user on 2/7/2015.
 */
public class CSGProgram {
    long myIteration=0;
    long lifetimeMs = 2000;
    long minIterationPeriodMs = 100;
    long minSimplifyPeriodMs = 1000;

    float radius = 25f;
    int quality = 4;

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

                CSG addition = new Sphere(center.plus(new Vector3d(myIteration*5,0,0)), radius, quality, quality).toCSG();
                myCSG = myCSG.union(addition);
                lastBuildTime = time.getTime();
                myIteration++;

            }
        }else{
            if(time.getTime() - lastSimplifyTime > minSimplifyPeriodMs) {
                SimplifyCSG.simplifyCSG(myCSG);
                lastSimplifyTime = time.getTime();
            }

        }

    }
}
