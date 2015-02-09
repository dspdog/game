import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;
import eu.mihosoft.vrl.v3d.Transform;
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
    long lifetimeMs = 10000;
    long minIterationPeriodMs = 100;
    long minSimplifyPeriodMs = 1000;

    float radius = 25f;
    int quality = 5;

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
                center = center.plus(RandomHelper.randomBrownian(scale).minus(new Vector3d(scale/2,scale/2,scale/2)));
                CSG addition = new Sphere(center, radius, quality, quality).toCSG();
                myCSG = myCSG.union(addition);

                lastBuildTime = time.getTime();
                myIteration++;

            }
        }else{
            if(time.getTime() - lastSimplifyTime > minSimplifyPeriodMs) {
                //myCSG = SimplifyCSG.simplifyCSG(myCSG);
                lastSimplifyTime = time.getTime();
            }

        }

    }
}
