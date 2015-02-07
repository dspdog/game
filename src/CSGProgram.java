import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Sphere;

/**
 * Created by user on 2/7/2015.
 */
public class CSGProgram {
    CSG myCSG = new Sphere(25,5,5).toCSG();

    CSG toCSG(){
        return myCSG;
    }
}
