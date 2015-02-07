import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;

/**
 * Created by user on 2/7/2015.
 */
public class CSGProgram {
    long myIteration=0;
    CSG myCSG = new Sphere(25,5,5).toCSG();

    CSG toCSG(){
        return myCSG;
    }

    public void iterate(float dt){
        //build up the CSG...
        myIteration++;
    }
}
