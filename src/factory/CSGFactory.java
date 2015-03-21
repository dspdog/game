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

    public static CSG roundArrow(){
        double girth = 3;
        double length = 40;
        int slices = 8;
        CSG cone = new Cylinder(girth*2, 0.1, length/2, slices).toCSG().transformed(Transform.unity().translate(0,0,length));
        CSG shaft = new Cylinder(girth, girth, length, slices).toCSG();
        return cone.union(shaft);
    }

    public static CSG roundShaft(){
        double girth = 3;
        double length = 32;
        int slices = 4;
        //CSG cone = new Cylinder(girth*2, 0.1, length/2, slices).toCSG().transformed(Transform.unity().translate(0,0,length));
        CSG shaft = new Cylinder(girth, girth, length, slices).toCSG();
        return shaft;
    }

    public static CSG xyzVersions(CSG base){
        CSG base2 = base.transformed(Transform.unity().rot(0, -90, 0));
        CSG base3 = base.transformed(Transform.unity().rot(90, 0, 0));
        return base.union(base2, base3);
    }

    public static CSG boxedVersion(CSG _base){
        double scale = 32 * 2;
        CSG base = _base.transformed(Transform.unity().translate(-scale,-scale,-scale));

        CSG xEdge = base.transformed(Transform.unity().scaleX(-1)).union(base);
        CSG yEdge = xEdge.transformed(Transform.unity().scaleY(-1)).union(xEdge);
        CSG zEdge = yEdge.transformed(Transform.unity().scaleZ(-1)).union(yEdge);
        return zEdge;
    }

    public static CSG cornersBox(){
        return boxedVersion(xyzVersions(roundShaft())).transformed(Transform.unity().scale(0.1));
    }

    public static CSG xyzArrows(){
        return xyzVersions(roundArrow());
    }
}
