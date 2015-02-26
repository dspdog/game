package utils;

import org.lwjgl.Sys;

/**
 * Created by user on 1/23/2015.
 */
public class time {
    static public long lastI = getTime();
    static public double lastIf = getTimef();
    static public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
    static public double getTimef() { //returns milliseconds, using nano time
        return System.nanoTime()/1_000_000d;
    }
    static public long getDtMS(){
        long i = getTime()-lastI;
        lastI=getTime();
        return i;
    }
    static public double getDtMSf(){
        double i = getTimef()-lastIf;
        lastI=getTime();
        return i;
    }
}
