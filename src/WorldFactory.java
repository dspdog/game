import factory.CSGFactory;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/15/2015.
 */
public class WorldFactory {
    static void buildWorld(){
        gameScene.addWorldObject(new worldObject(CSGFactory.arrow()));

        for(int i=0; i<1000; i++){
            gameScene.addWorldObject(new worldObject(CSGFactory.arrow()).setPos(new Vector3f((i%25)*10,0,i)));
        }

        CSGProgram myProg = new CSGProgram();
        gameScene.addWorldObject(new worldObject(myProg));
    }
}
