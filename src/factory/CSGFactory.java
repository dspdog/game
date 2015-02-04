package factory;

import eu.mihosoft.vrl.v3d.*;

/**
 * Created by user on 2/4/2015.
 */
public class CSGFactory {

    public static CSG testSet(){
        // we use cube and sphere as base geometries
        CSG cube = new Cube(2).toCSG();
        CSG sphere = new Sphere(1.25).toCSG();

        // perform union, difference and intersection
        CSG cubePlusSphere = cube.union(sphere);
        CSG cubeMinusSphere = cube.difference(sphere);
        CSG cubeIntersectSphere = cube.intersect(sphere);

        // translate geometries to prevent overlapping
        CSG union = cube.
                union(sphere.transformed(Transform.unity().translateX(3))).
                union(cubePlusSphere.transformed(Transform.unity().translateX(6))).
                union(cubeMinusSphere.transformed(Transform.unity().translateX(9))).
                union(cubeIntersectSphere.transformed(Transform.unity().translateX(12)));

        return union;
    }

    public static CSG arrow(){
        float depth = 0.2f;
        float size = 12f;
        CSG base = Extrude.points(new Vector3d(0,0,depth),new Vector3d(1,1,0), new Vector3d(1,0,0), new Vector3d(0,0,0), new Vector3d(0,1,0)).transformed(Transform.unity().scale(size));
        CSG top = Extrude.points(new Vector3d(0,0,depth),new Vector3d(0,0,0), new Vector3d(-1,1,0), new Vector3d(1,1,0)).transformed(Transform.unity().scale(size).translate(0.5, -1,0));
        CSG sphere = new Sphere().toCSG().transformed(Transform.unity().translate(size/2,0,0));
        CSG finalShape = base.union(top).union(sphere);
        return finalShape;
    }

}
