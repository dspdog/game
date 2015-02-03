/**
 * Created by user on 1/31/2015.
 */
public class gameCommands {

    static String defaultHelpMsg = "Type HELP to see a list of commands.";
    static String commandString =  defaultHelpMsg + "\n";

    static final String errorCode = "<ERROR>";
    static final String confirmCode = "<CONFIRM>";
    static final String submitCode = "<SUBMITTED>";


    //TODO tree presets - perturb strength - rnd shapes...
    //TODO autosave

    static public String theCmds[][] = {
            {"persp", "perspective mode", "Perspective mode enabled! Use FOV to change field of view."},
            {"ortho", "orthogonal mode", "Ortho mode enabled! Use PERSP to enable perspective mode."},
            {"fov",   "field of view", "Field of view now __X"},
            {"sync", "frame limiter", "Frame limit now __X. Use 0 to remove limit."},
            {"overlay", "shader overlay on/off", "shader overlay __X"},
            {"pixels", "pixel integrator on/off", "pixel integrator __X"},
            {"fbo", "fbo pass on/off", "fbo pass __X"},
            {"wireframe", "wireframe mode on/off", "wireframe mode __X"},


            {"orbit", "orbit camera mode", "Orbit camera mode enabled"},
            {"fly", "flying camera mode", "Flying camera mode enabled"},
            //next 3 require "fly" mode
            {"top", "top view", "Now viewing top view"},
            {"side", "side view", "Now viewing side view"},
            {"front", "front view", "Now viewing front view"},
            {"speed", "camera speed", "Camera speed now __X"},
            {"timescale", "time multiplier", "time speed now __Xx"},
            {"save", "save state", "State saved."},
            {"load", "load state", "State loaded."},
            {"shader", "shader edges/normals/dof/plain", "Now using __X shader"},

            {"details", "toggle render details", "Render details display toggled"},
            {"reset", "reset state", "Everything reset."},
            {"help", "help file", "help file"}
    };

    public static void runCmd(String cmd, String param){
        switch(cmd){
            case "persp":
                RenderThread.useOrtho = false;
                break;
            case "ortho":
                RenderThread.useOrtho = true;
                break;
            case "fov":
                RenderThread.myFOV=Float.valueOf(param);
                break;
            case "sync":
                RenderThread.mySyncFPS=Integer.valueOf(param);
                break;
            case "overlay":
                RenderThread.doShaderOverlay= param.equalsIgnoreCase("on");
                System.out.println(RenderThread.doShaderOverlay);
                break;
            case "pixels":
                RenderThread.doProcessPixels= param.equalsIgnoreCase("on");
                System.out.println(RenderThread.doProcessPixels);
                break;
            case "fbo":
                RenderThread.doFBOPass= param.equalsIgnoreCase("on");
                System.out.println(RenderThread.doFBOPass);
                break;
            case "wireframe":
                RenderThread.doWireFrame= param.equalsIgnoreCase("on");
                System.out.println(RenderThread.doWireFrame);
                break;
        }
    }

    public static void submitCommand(String cmd){
        commandString+= submitCode + ">" + cmd+"\n";
        commandString+=parseCommand(cmd) + "\n";
    }

    private static String parseCommand(String cmd){
        String[] splitData = cmd.split(" ");

        String theCmd = cmd.toLowerCase();
        String theParam = " ";

        if(splitData.length>1){ //multi-arg commands
            theCmd = splitData[0];
            theParam = splitData[1];
        }

        for(int i=0; i<theCmds.length; i++){
            String cmdTitle = theCmds[i][0];
            String cmdDesc = theCmds[i][1];
            String cmdConfirmation = theCmds[i][2].replace("__X", theParam);

            if(theCmd.equals(cmdTitle)){

                if(theParam.equals(" ") && theCmds[i][2].contains("__X")){ //if user types wrong number of params
                    return "Missing parameter. Use " + cmdTitle + " X to set " + cmdDesc + " to X";
                }

                try{
                    runCmd(theCmd, theParam);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return confirmCode + cmdConfirmation;
            }
        }

        return errorCode + "Unrecognized command: " + cmd + ". " + defaultHelpMsg;
    }

}
