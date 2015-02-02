package utils;

import org.lwjgl.Sys;

/**
 * Created by user on 1/23/2015.
 */
public class time {
    static public long lastI = getTime();
    static public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
    static public long getDtMS(){
        long i = getTime()-lastI;
        lastI=getTime();
        return i;
    }
}
