import utils.time;

public class ComputationThread implements Runnable {

    public static int fps = 0;
    public static float lastFPS = 0;
    public static int myFPS = 0;

    private boolean running = false;
    public static float lastGameLogic;

    long frame=0;

    RenderThread myRenderThread;

    public ComputationThread(RenderThread rt){
        myRenderThread = rt;
    }

    void updateFPS() {
        if (time.getTime() - lastFPS > 1000) {
            myFPS = fps;
            fps = 0;
            lastFPS = time.getTime();
        }
        fps++;
    }

    void updateComputations(){
        frame++;
        float dt = time.getDtMS()*0.1f;
        GameScene.logicScene(dt);
        lastGameLogic = time.getTime();
        updateFPS();
    }

    public void end(){
        running=false;
    }

    void init(){

    }

    @Override
    public void run() {
        long frame=0;
        init();
        running=true;

        while(running){
            try {
                frame++;
                updateComputations();
                //if(frame%10==0) //faster framerates
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
