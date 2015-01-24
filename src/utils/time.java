package utils;

import org.lwjgl.Sys;

/**
 * Created by user on 1/23/2015.
 */
public class time {
    static public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
