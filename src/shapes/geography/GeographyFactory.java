package shapes.geography;

import utils.SimplexNoise;

/**
 * Created by user on 11/22/2014.
 */
public class GeographyFactory {
    public static  float geographyFunction(float x, float y){
        float total = 0;

        total+=SimplexNoise.noise(x / 10f, y / 10f)*3f;
        total+=SimplexNoise.noise(x / 20f, y / 20f)*10f;
        total+=SimplexNoise.noise(x / 80f, y / 80f)*10f;
        total+=SimplexNoise.noise(x / 160f, y / 160f)*20f;
        return (total+20f)/2f;
    }
}
