package shapes.geography;

import utils.SimplexNoise;

/**
 * Created by user on 11/22/2014.
 */
public class GeographyFactory {
    public static  float geographyFunction(float x, float y, float time){
        float total = 0;

        total+=SimplexNoise.noise(x / 20f, y / 10f, time)*3f;
        total+=SimplexNoise.noise(x / 50f + time/5f, y / 150f, time/5f)*10f;
        total+=SimplexNoise.noise(x / 250f + time/10f, y / 350f , time/25f)*10f;

        return (total+20f)/2f;
    }
}
