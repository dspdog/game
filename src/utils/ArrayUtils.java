package utils;

import java.util.ArrayList;

/**
 * Created by user on 2/11/2015.
 */
public class ArrayUtils {
    public static void resize(ArrayList E, int size){
        E.subList(size, E.size()).clear(); //E.resize(dst); //remove everything after dst
    }
}
