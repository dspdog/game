
public class Main {
    public static void main(String[] argv) {
        MovementThread theMovement = new MovementThread();
        RenderThread theWorld = new RenderThread(theMovement);
        ComputationThread theComputer = new ComputationThread(theWorld);
        theWorld.myComputeThread = theComputer;

        (new Thread(theMovement)).start();
        (new Thread(theComputer)).start();
        theWorld.start();
    }
}
