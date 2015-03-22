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

    public static CSG cone(double girth, double length, int slices){
        CSG cone = new Cylinder(girth*2, 0.1, length/2, slices).toCSG();
        return cone;
    }

    public static CSG uncone(double girth, double length, int slices){
        CSG cone = new Cylinder(0.1, girth*2, length/2, slices).toCSG();
        return cone;
    }

    public static CSG roundArrow(){
        double girth = 3;
        double length = 40;
        int slices = 8;
        CSG cone = cone(girth,length,slices).transformed(Transform.unity().translate(0, 0, length));
        CSG shaft = new Cylinder(girth, girth, length, slices).toCSG();
        return cone.union(shaft);
    }

    public static CSG roundShaft(){
        double girth = 3;
        double length = 32;
        int slices = 4;
        CSG shaft = new Cylinder(girth, girth, length, slices).toCSG();
        return shaft;
    }

    public static CSG xyzVersions(CSG base){
        CSG base2 = base.transformed(Transform.unity().rot(0, -90, 0));
        CSG base3 = base.transformed(Transform.unity().rot(90, 0, 0));
        return base.union(base2, base3);
    }

    public static CSG boundingBox(CSG base, Bounds bounds){
        Vector3d max = bounds.getMax();
        Vector3d min = bounds.getMin();

        base = base.transformed(Transform.unity().scale(0.2));

        CSG UpperNorthEast = base.transformed(Transform.unity().translate(max.x, max.y, max.z).scale(1, 1, 1));
        CSG UpperNorthWest = base.transformed(Transform.unity().translate(min.x, max.y, max.z).scale(-1, 1, 1));
        CSG UpperSouthEast = base.transformed(Transform.unity().translate(max.x, min.y, max.z).scale(1, -1, 1));
        CSG UpperSouthWest = base.transformed(Transform.unity().translate(min.x, min.y, max.z).scale(-1, -1, 1));

        CSG LowerNorthEast = base.transformed(Transform.unity().translate(max.x, max.y, min.z).scale(1, 1, -1));
        CSG LowerNorthWest = base.transformed(Transform.unity().translate(min.x, max.y, min.z).scale(-1, 1, -1));
        CSG LowerSouthEast = base.transformed(Transform.unity().translate(max.x, min.y, min.z).scale(1, -1, -1));
        CSG LowerSouthWest = base.transformed(Transform.unity().translate(min.x, min.y, min.z).scale(-1, -1, -1));

        return UpperNorthEast.union(UpperNorthWest, UpperSouthEast, UpperSouthWest, LowerNorthEast, LowerNorthWest, LowerSouthEast, LowerSouthWest);
    }

    public static CSG pointyBoxBounds(Bounds bounds){
        double girth = 3;
        double length = 32;
        int slices = 8;
        CSG arrow = uncone(girth, length, slices).transformed(Transform.unity().rot(45,-45,0));
        return boundingBox(arrow, bounds);
    }

    public static CSG xyzArrows(){
        double s = 0.4;
        return xyzVersions(roundArrow()).transformed(Transform.unity().scale(s,-s,-s));
    }
}
