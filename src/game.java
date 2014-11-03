
public class game {
    public static void main(String[] argv) {
        gameWorldLogic theLogic = new gameWorldLogic();
        gameWorld theWorld = new gameWorld(theLogic);

        (new Thread(theLogic)).start();
        theWorld.start();
    }
}
