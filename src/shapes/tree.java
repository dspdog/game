package shapes;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public final class tree implements java.io.Serializable {
    public treePt pts[];

    public float unitScale;

    public int pointsInUse;
    public boolean autoUpdateCenterEnabled;
    public boolean stateSaved;
    public boolean autoScale;
    public boolean evolutionDisqualified;

    public int iterations; //iterations x 100

    int pointNearest, pointSelected;
    treePt selectedPt;

    public treePt iterationDescriptorPt;

    boolean disqualified;
    boolean hasDrawList = false;

    public tree(){
        resetShape();
    }

    public void resetShape(){
        iterations = 550;

        hasDrawList = false;
        evolutionDisqualified=false;
        disqualified=false;

        pointNearest =0;
        pointSelected =0;

        autoUpdateCenterEnabled =false;
        stateSaved = false;
        pointsInUse = 1;
        unitScale = 115.47005383792515f; //distance from center to one of the points in preset #1
        autoScale = true;
        freshPoints();
    }

    public void selectNearestPt(){
        selectedPt = this.pts[pointNearest];
        pointSelected = pointNearest;
    }

    public tree(tree _oldShape){
        autoUpdateCenterEnabled =_oldShape.autoUpdateCenterEnabled;
        stateSaved = _oldShape.stateSaved;
        pointsInUse = _oldShape.pointsInUse;
        unitScale = _oldShape.unitScale; //distance from center to one of the points in preset #1
        autoScale = _oldShape.autoScale;
        iterations= _oldShape.iterations;

        freshPoints();
        for(int a=0; a< pointsInUse; a++){
            pts[a] = new treePt(_oldShape.pts[a], true);
        }
    }

    public void freshPoints(){
        pts = new treePt[1000];
        for(int a=0; a< 1000; a++){
            pts[a] = new treePt();
        }
        pointNearest=0;
        selectNearestPt();
    }

    public void saveToFile(String filename){
        try{
            FileOutputStream fileOut =
                    new FileOutputStream("./shapes/"+filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            System.out.println("ifsShape saved to "+filename);
        }catch(Exception i){
            i.printStackTrace();
        }
    }

    public tree getPerturbedShape(treePt intensityDescriptor, boolean staySymmetric, boolean affectIterations){

        tree pShape = new tree(this);

        //pShape.isoStepSize += new Random().nextGaussian();
        //pShape.isoStepSize = Math.max(2d, pShape.isoStepSize);
        if(affectIterations)
            pShape.iterations += new Random().nextGaussian() * 10;

        long seed = (long)(Math.random()*Long.MAX_VALUE);

        for(int i=0; i<pShape.pointsInUse; i++){
            pShape.pts[i] = new treePt(this.pts[i], true);
            if(staySymmetric){
                pShape.pts[i].perturb(intensityDescriptor, seed);
            }else{
                pShape.pts[i].perturb(intensityDescriptor, -1);
            }
        }

        return pShape;
    }

    public tree perturb(float intensity){
        perturb(false,false,intensity);
        return this;
    }

    public tree perturb(){
        perturb(1.0f);
        return this;
    }

    public tree perturb(boolean staySymmetric, boolean affectIterations, float intensity){
        treePt intensityDescriptor = new treePt(intensity,intensity,intensity,
                                                intensity,intensity,intensity);

        if(affectIterations)
            this.iterations += new Random().nextGaussian() * 10;

        long seed = (long)(Math.random()*Long.MAX_VALUE);

        for(int i=0; i<this.pointsInUse; i++){
            this.pts[i] = new treePt(this.pts[i], true);
            if(staySymmetric){
                this.pts[i].perturb(intensityDescriptor, seed);
            }else{
                this.pts[i].perturb(intensityDescriptor, -1);
            }
        }

        this.reIndex();
        return this;
    }

    public void saveState(){
        for(int a = 0; a < pointsInUse; a++){
            pts[a].saveState();
            stateSaved=true;
        }
    }

    public void addPoint(float x, float y, float z){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].z = z;
        pts[pointsInUse].scale = 0.5f;
        pts[pointsInUse].rotationPitch = 0;
        pts[pointsInUse].rotationYaw = 0;
        pts[pointsInUse].rotationRoll = 0;
        pts[pointsInUse].opacity = 1.0f;
        pointsInUse++;
        updateCenter();
    }

    public void addPoint(double x, double y, double z){
        addPoint((float)x, (float)y, (float)z);
    }

    public void addPointScaled(double x, double y, double z, double scale){
        addPointScaled((float) x, (float) y, (float) z, (float) scale);
    }

    public void addPointScaled(float x, float y, float z, float scale){
        pts[pointsInUse].x = x;
        pts[pointsInUse].y = y;
        pts[pointsInUse].z = z;
        pts[pointsInUse].scale = scale;
        pts[pointsInUse].rotationPitch = 0;
        pts[pointsInUse].rotationYaw = 0;
        pts[pointsInUse].rotationRoll = 0;
        pts[pointsInUse].opacity = 1.0f;
        pointsInUse++;
        updateCenter();
    }

    public void deletePoint(int selectedPoint){
        for(int a = selectedPoint; a < pointsInUse; a++){
            pts[a].x = pts[a + 1].x;
            pts[a].y = pts[a + 1].y;

            pts[a].scale = pts[a + 1].scale;
            pts[a].rotationPitch = pts[a + 1].rotationPitch;
            pts[a].rotationYaw = pts[a + 1].rotationYaw;
            pts[a].rotationRoll = pts[a + 1].rotationRoll;
        }

        pts[pointsInUse].x = 0.0f;
        pts[pointsInUse].y = 0.0f;

        pts[pointsInUse].scale = 0.5f;
        pts[pointsInUse].rotationPitch = 0;
        pts[pointsInUse].rotationYaw = 0;
        pts[pointsInUse].rotationRoll = 0;
        pointsInUse--;

        updateCenter();
    }

    public void clearPts(){
        for(int a = 0; a < pointsInUse; a++){
            deletePoint(pointsInUse-a);
        }
        pointsInUse=0;
    }

    void updateRadiusDegrees(){
        pts[0].degreesPitch = (float)Math.PI/2;
        pts[0].radius = unitScale*pts[0].scale;

        for(int a = 1; a < pointsInUse; a++){
            pts[a].radius = autoScale ? distance(pts[a].x - pts[0].x, pts[a].y - pts[0].y,  pts[a].z - pts[0].z) : pts[0].radius;
        }
    }

    void updateCenter(){
        float x = 0, y = 0;

        if(autoUpdateCenterEnabled){
            if(pointsInUse != 0){
                for(int a = 1; a < pointsInUse; a++){
                    x += pts[a].x;
                    y += pts[a].y;
                }
                pts[0].x  = x / (pointsInUse-1);
                pts[0].y  = y / (pointsInUse-1);
            }
        }

        updateRadiusDegrees();
    }

    void updateCenterOnce(){
        boolean oldState = autoUpdateCenterEnabled;

        autoUpdateCenterEnabled =true;
        updateCenter();
        autoUpdateCenterEnabled =oldState;
    }

    public void setToPreset(int preset){
        resetShape();
        switch(preset){
            case 0: // '\000'
                clearPts();
                pointsInUse=1;
                int centerx=512;
                int centery=512;
                int centerz=512;

                for(int i=0; i<4; i++){
                    this.addPoint(
                            Math.cos(Math.PI/4+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*200+centery,
                            centerz-256);
                }

                for(int i=0; i<4; i++){
                    this.addPoint(
                            Math.cos(Math.PI/4+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*200+centery,
                            centerz+256);
                }

                this.pts[0].z=centerz;
                break;

            case 9: // '\009'
                clearPts();
                pointsInUse=1;
                centerx=512;
                centery=512;
                centerz=512;

                for(int i=0; i<1; i++){
                    this.addPoint(
                            Math.cos(Math.PI/3+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/3+i*Math.PI/2)*200+centery,
                            centerz-256);
                }

                for(int i=0; i<3; i++){
                    this.addPoint(
                            Math.cos(Math.PI/3+i*Math.PI/2)*200+centerx,
                            Math.sin(Math.PI/3+i*Math.PI/2)*200+centery,
                            centerz+256);
                }

                this.pts[0].z=centerz;
                break;

            case 1: // '\001'

                clearPts();
                pointsInUse=1;
                centerx=512;
                centery=512;
                centerz=512;

                for(int i=0; i<4; i++){
                    this.addPointScaled(
                            Math.cos(Math.PI/4+i*Math.PI/2)*256+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*256+centery,
                            256, 1.0/Math.sqrt(3.0));
                    this.addPointScaled(
                            Math.cos(Math.PI/4+i*Math.PI/2)*256+centerx,
                            Math.sin(Math.PI/4+i*Math.PI/2)*256+centery,
                            256+512, 1.0/Math.sqrt(3.0));
                }
                this.pts[0].z=centerz;
                break;

        }

        updateCenterOnce();
    }






    final int NUM_LINES= 1024*1024; //MAX LINES
    public int lineIndex=0;
    long lastIndex = System.currentTimeMillis();

    int zMin, zMax, xMin, xMax, yMin, yMax;
    Random branchRandom = new Random();
    long indexCount;
    static ArrayList<Integer>[] zLists = new ArrayList[1024];

    final int[] lineDI= new int[NUM_LINES];
    final int[] lineXY1= new int[NUM_LINES];
    final int[] lineZS1= new int[NUM_LINES];
    final int[] lineXY2= new int[NUM_LINES];
    final int[] lineZS2= new int[NUM_LINES];

    public void reIndex(){
        Random rnd = new Random();
        rnd.setSeed(0);
        indexCount=0;
        clearZLists();
        xMin = 1024;
        yMin = 1024;
        zMin = 1024;
        xMax = 0;
        yMax = 0;
        zMax = 0;
        lineIndex=0;

        long randomScale = 5;
        int maxBranchDist = 10000;
        int pruneThresh=500;
        //System.out.println("CENTERPT " + this.pts[0].x + " " + this.pts[0].y  + " " + this.pts[0].z );
        indexFunction(0, (int)(this.iterations/100)+1, this.iterations%100,  1.0f, new treePt(0,0,0), new treePt(this.pts[0]), rnd, randomScale, (short)0, maxBranchDist, pruneThresh, 0);
    }

    public void clearZLists(){
        zLists = new ArrayList[1024];
        for(int i=0; i<1024; i++){
            zLists[i]= new ArrayList<>();
            //zLists[i].clear();
        }
    }
    long count=0;
    public void addLineToZList(int index, int z1, int z2, int max){
        int _z1 = Math.max(Math.min(z1, z2)-max, 1);
        int _z2 = Math.min(Math.max(z1, z2)+max, 1023);
        for(int i=_z1; i<_z2; i++){
            zLists[i].add(index);
            count++;
        }
    }


    private void indexFunction(int _index, int _iterations, int _subiters, float _cumulativeScale,
                               treePt _cumulativeRotation, treePt _dpt, Random _rnd, float rndScale, int dist, int distRemaining, double pruneThresh, long idNum){
        int randomSeed = 3;
        indexCount++;

        double WORLD_UNIT_SCALE = 256;

        treePt dpt = new treePt(_dpt);
        treePt cumulativeRotation = new treePt(_cumulativeRotation);
        treePt thePt = this.pts[_index];
        treePt centerPt = this.pts[0];

        cumulativeRotation = cumulativeRotation.add(
                new treePt(thePt.rotationPitch+(float)(_rnd.nextGaussian())*rndScale/1000f,
                        thePt.rotationYaw+(float)(_rnd.nextGaussian())*rndScale/1000f,
                        thePt.rotationRoll+(float)(_rnd.nextGaussian())*rndScale/1000f));

        if(_iterations==1){
            _cumulativeScale = _cumulativeScale * _subiters/100.0f;
        }

        treePt rpt = thePt.subtract(centerPt).scale(_cumulativeScale).getRotatedPt(cumulativeRotation);
        treePt odp = new treePt(dpt);
        dpt._add(rpt);

        lineDI[lineIndex]=(((short)(dist))<<16) + ((short)_iterations);

        lineXY1[lineIndex]=(((short)(dpt.x))<<16) + ((short)dpt.y);
        lineZS1[lineIndex]=(((short)(dpt.z))<<16) + ((short)(256f *_cumulativeScale*thePt.scale/centerPt.scale));
        lineXY2[lineIndex]=(((short)(odp.x))<<16) + ((short)odp.y);
        lineZS2[lineIndex]=(((short)(odp.z))<<16) + ((short)(256f * _cumulativeScale));

        int maxDist = 0;

        addLineToZList(lineIndex, (int)odp.z, (int)dpt.z, maxDist);

        xMin = (int)Math.max(0,Math.min(Math.min(xMin,odp.x-maxDist),Math.min(xMin,dpt.x-maxDist)));
        yMin = (int)Math.max(0,Math.min(Math.min(yMin,odp.y-maxDist),Math.min(yMin,dpt.y-maxDist)));
        zMin = (int)Math.max(0,Math.min(Math.min(zMin,odp.z-maxDist),Math.min(zMin,dpt.z-maxDist)));
        xMax = (int)Math.min(1023,Math.max(Math.max(xMax,odp.x+maxDist),Math.max(xMax,dpt.x+maxDist)));
        yMax = (int)Math.min(1023,Math.max(Math.max(yMax,odp.y+maxDist),Math.max(yMax,dpt.y+maxDist)));
        zMax = (int)Math.min(1023,Math.max(Math.max(zMax,odp.z+maxDist),Math.max(zMax,dpt.z+maxDist)));

        lineIndex++;
        lineIndex=Math.min(lineIndex, NUM_LINES-1);

        if(_iterations>1){

            double branchThresh=pruneThresh;

            _cumulativeScale *= thePt.scale/centerPt.scale;
            _cumulativeScale *= (float)(1.0f - _rnd.nextGaussian()*rndScale/3000f);

            branchRandom.setSeed(idNum+randomSeed);
            for(int i=1; i<this.pointsInUse; i++){
                if(pruneThresh<500)
                    distRemaining *= (1.0d-branchRandom.nextDouble()/(branchThresh/50d));

                int inc = (int)(_cumulativeScale*WORLD_UNIT_SCALE);
                if(distRemaining>inc){
                    indexFunction(i, _iterations-1, _subiters, _cumulativeScale, cumulativeRotation, dpt, _rnd, rndScale, dist, (int)(distRemaining-inc), pruneThresh, idNum*this.pointsInUse+i);
                }else{
                    _subiters = 100*distRemaining/inc;
                    indexFunction(i, 1, _subiters, _cumulativeScale, cumulativeRotation, dpt, _rnd, rndScale, dist, 0, pruneThresh, idNum*this.pointsInUse+i);
                }
            }
        }
    }

    public int getX1(int index){
        return lineXY1[index]>>16;
    }

    public int getY1(int index){
        return lineXY1[index]&65535;
    }

    public int getX2(int index){
        return lineXY2[index]>>16;
    }

    public int getY2(int index){
        return lineXY2[index] & 65535;
    }

    public int getZ1(int index){
        return lineZS1[index]>>16;
    }

    public float getS1(int index){
        return (lineZS1[index]&65535)/256f;
    }

    public int getZ2(int index){
        return lineZS2[index]>>16;
    }

    public float getS2(int index){
        return (lineZS2[index]&65535)/256f;
    }

    public static float distance(float x, float y, float z){
        return (float)Math.sqrt(x * x + y * y + z * z);
    }
}