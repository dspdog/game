import factory.CSGFactory;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by user on 2/15/2015.
 */
public class WorldFactory {
    static void buildWorld(){

        for(int i=0; i<32; i++){
            GameScene.addWorldObject(new WorldObject(CSGFactory.house()).setPos(new Vector3f((i % 25) * 10, 0, i)));
        }

        //new WorldObject(CSGFactory.cornersBox())
        GameScene.setPointerObject(new WorldObject(CSGFactory.uncone(1, 1, 3)));
        GameScene.setSelectionObject(new WorldObject(CSGFactory.uncone(1, 1, 3)));
        CSGProgram myProg = new CSGProgram();
      //  gameScene.addWorldObject(new WorldObject(myProg));
    }
}
