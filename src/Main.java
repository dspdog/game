
public class Main {
    public static void main(String[] argv) {
        LogicThread theLogic = new LogicThread();
        RenderThread theWorld = new RenderThread(theLogic);

        (new Thread(theLogic)).start();
        theWorld.start();
    }
}
