package shapes;

import utils.Quaternion;

import java.util.Random;

public final class treePt implements java.io.Serializable{

    public float x, y, z;
    public float scale;
    public float degreesYaw;
    public float degreesPitch;
    public float degreesRoll;
    public float radius;
    public float rotationYaw;
    public float rotationPitch;
    public float rotationRoll;
    public float opacity;

    public treePt savedPt;

    public Quaternion rotationQ;

    static treePt X_UNIT = new treePt(1.0f,0,0);
    static treePt Y_UNIT = new treePt(0,1.0f,0);
    static treePt Z_UNIT = new treePt(0,0,1.0f);

    public treePt(){
        rotationQ = new Quaternion(0,0,0,0);
        x = 0f; y = 0f; z = 0f;
        scale = 1f;
        rotationYaw = 0.0f;
        rotationPitch = 0.0f;
        rotationRoll = 0.0f;
        degreesYaw = 0f;
        degreesPitch = 0f;
        degreesRoll = 0f;
        radius = 1f;
        opacity = 1f;
    }

    public String coordString(){
        return this.x + " " + this.y + " " + this.z;
    }

    public treePt(treePt pt){
        x=pt.x;
        y=pt.y;
        z=pt.z;
    }

    public treePt(treePt pt, boolean full){
        this.x=pt.x;
        this.y=pt.y;
        this.z=pt.z;

        if(full){
            this.scale = pt.scale;
            this.rotationYaw = pt.rotationYaw;
            this.rotationPitch = pt.rotationPitch;
            this.degreesYaw = pt.degreesYaw;
            this.degreesPitch = pt.degreesPitch;
            this.degreesRoll = pt.degreesRoll;
            this.radius = pt.radius;
            this.opacity = pt.opacity;
            this.savedPt = pt.savedPt;
        }
    }

    public treePt(float _x, float _y, float _z){
        x = _x; y = _y; z = _z;
    }

    public treePt(float _x, float _y, float _z, float _pitch, float _yaw, float _roll){
        x = _x; y = _y; z = _z;
        rotationPitch = _pitch; rotationRoll = _roll; rotationYaw = _yaw;
    }

    public treePt(double _x, double _y, double _z){
        x = (float)_x; y = (float)_y; z = (float)_z;
    }

    public treePt intensify(float x){
        treePt pt= new treePt(this, true);
        pt.x*=x;pt.y*=x;pt.z*=x;
        pt.scale*=x;
        pt.rotationPitch*=x;
        pt.rotationRoll*=x;
        pt.rotationYaw*=x;
        return pt;
    }

    public void perturb(treePt mutation, long seed){
        Random rnd = new Random();
        if(seed>=0){
            rnd.setSeed(seed);
        }

        //change all the properties slightly...
        scale*=(1+rnd.nextGaussian()*mutation.scale);

        rotationPitch+=rnd.nextGaussian()*mutation.rotationPitch/180*Math.PI;
        rotationYaw+=rnd.nextGaussian()*mutation.rotationYaw/180*Math.PI;
        rotationRoll+=rnd.nextGaussian()*mutation.rotationRoll/180*Math.PI;

        x+=rnd.nextGaussian()*mutation.x;
        y+=rnd.nextGaussian()*mutation.y;
        z+=rnd.nextGaussian()*mutation.z;
    }

    public float magnitude(){
        return (float)dist(this.x, this.y, this.z);
    }

    public treePt add(treePt pt){
        return new treePt(this.x+pt.x, this.y+pt.y, this.z+pt.z);
    }

    public void _add(treePt rpt){
        this.x+=rpt.x;
        this.y+=rpt.y;
        this.z+=rpt.z;
    }

    public treePt subtract(treePt pt){
        return new treePt(this.x-pt.x, this.y-pt.y, this.z-pt.z);
    }

    public treePt scale(float s){
        return new treePt(this.x*s, this.y*s, this.z*s);
    }

    public double distanceXY(treePt pt){
        return dist(pt.x - this.x, pt.y - this.y, 0);
    }

    public static double dist(float x, float y, float z){
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double distTo(treePt p){
        return Math.sqrt((p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y) + (p.z - this.z) * (p.z - this.z));
    }

    public treePt interpolateTo(treePt dest, float factor, float outerScale, float outerRotation){
        //go from this to dest as factor goes from 0 to 1
        treePt res = this.add(dest.subtract(this).scale(factor));

        res.radius = this.radius + (dest.radius-this.radius)*factor;
        res.scale = this.scale + (dest.scale-this.scale)*factor + outerScale;// + (float)Math.cos(factor*10)/5;

        res.rotationPitch = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor;
        res.rotationYaw = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor;
        res.rotationRoll = this.rotationPitch + (dest.rotationPitch-this.rotationPitch)*factor + outerRotation;

        return res;
    }

    public treePt getRotatedPt(treePt rpt){
        return this.qRotate(treePt.X_UNIT, rpt.x).qRotate(treePt.Y_UNIT, rpt.y).qRotate(treePt.Z_UNIT,rpt.z);
    }

    public treePt getRotation(){
        return new treePt(this.rotationPitch, this.rotationYaw, this.rotationRoll);
    }

    public treePt qRotate(treePt r, float a){
        a/=2;
        float sa2 = (float)Math.sin(a);
        Quaternion q2 = new Quaternion(Math.cos(a), r.x*sa2, r.y*sa2, r.z*sa2);
        Quaternion q3 = q2.times(new Quaternion(0, this.x, this.y, this.z)).times(q2.conjugate());
        return new treePt(q3.x, q3.y, q3.z);
    }

    public void saveState(){
        savedPt = new treePt(this, true);
    }
}