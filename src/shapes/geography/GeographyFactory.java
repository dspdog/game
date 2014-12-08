package shapes.geography;

import org.lwjgl.util.vector.Vector3f;
import utils.SimplexNoise;

/**
 * Created by user on 11/22/2014.
 */
public class GeographyFactory {

    public static float oceanWaves(float x, float y, float time){
        float total = 0;

        total+=SimplexNoise.noise(x / 20f, y / 10f, time)*3f;
        total+=SimplexNoise.noise(x / 50f + time/5f, y / 150f, time/5f)*10f;
        total+=SimplexNoise.noise(x / 250f + time/10f, y / 350f , time/25f)*10f;

        return (total+20f)/2f;
    }

    public static float island(float x, float y, float time){
        float total = 20f;

        total+=SimplexNoise.noise(x / 20f, y / 20f)*3f;
        total+=SimplexNoise.noise(x / 70f, y / 70f)*5f;
        total+=SimplexNoise.noise(x / 150f, y / 150f)*10f;
        total+=SimplexNoise.noise(x / 250f, y / 250f)*20f;

        return total-distance(x,y,0)/24f;
    }

    public static float bowl(float x, float y, float time){
        return (float)Math.cos(Math.max(Math.PI, distance(x,y,0)/20f))* 10f+10f;
    }

    public static float icebergs(float x, float y, float time){
        float total = 20f;

        total+=SimplexNoise.noise(x / 20f, y / 20f)*3f;
        total+=SimplexNoise.noise(x / 70f, y / 70f)*5f;
        total+=SimplexNoise.noise(x / 150f, y / 150f)*10f;
        total+=SimplexNoise.noise(x / 250f, y / 250f)*20f;

        return (float)Math.pow(total, 1.3f)-distance(x,y,0)/24f;
    }


    public static float distance(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }
}
