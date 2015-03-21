import factory.CSGFactory;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/15/2015.
 */
public class WorldFactory {
    static void buildWorld(){

        for(int i=5; i<100; i++){
            GameScene.addWorldObject(new WorldObject(CSGFactory.arrow()).setPos(new Vector3f((i % 25) * 10, 0, i)));
        }

        GameScene.addWorldObject(new WorldObject(CSGFactory.cornersBox()));

        CSGProgram myProg = new CSGProgram();
      //  gameScene.addWorldObject(new WorldObject(myProg));
    }
}
