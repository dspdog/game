
public class Main {
    public static void main(String[] argv) {
        LogicThread theLogic = new LogicThread();
        RenderThread theWorld = new RenderThread(theLogic);

        theLogic.myScene = theWorld.myScene;

        (new Thread(theLogic)).start();
        theWorld.start();
    }
}
