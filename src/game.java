
public class game {
    public static void main(String[] argv) {
        gameWorldLogic theLogic = new gameWorldLogic();
        gameWorldRender theWorld = new gameWorldRender(theLogic);

        (new Thread(theLogic)).start();
        theWorld.start();
    }
}
