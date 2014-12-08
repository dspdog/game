import world.scene;

public class game {
    public static void main(String[] argv) {
        scene myScene = new scene();

        gameWorldLogic theLogic = new gameWorldLogic(myScene);
        gameWorldWater theWater = new gameWorldWater(myScene);
        gameWorldRender theWorld = new gameWorldRender(theLogic);

        (new Thread(theLogic)).start();
        (new Thread(theWater)).start();
        theWorld.start();
    }
}
