package shapes.geography;

import utils.SimplexNoise;

/**
 * Created by user on 11/22/2014.
 */
public class GeographyFactory {
    public static  float geographyFunction(float x, float y){
        return  (float)(1f-(SimplexNoise.noise(x / 20f, y / 20f)+1f)*(SimplexNoise.noise(x/20f,y/20f)+1f))*10f;
    }
}
