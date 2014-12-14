package shapes.cloud;

/**
 * Created by user on 12/13/2014.
 */
public class kParticle {
    final int NEIGHBOR_MAX = 20;
    final int[] neighbors = new int[NEIGHBOR_MAX];
    int numNeighbors;

    public float vx, vy, vz; //velocity
    public float px, py, pz; //position
    public float mass, density, pressure;

    public kParticle(){
        vx=0; vy=0; vz=0;
        px=0; py=0; pz=0;

        mass = 1.0f;
        density = 1.0f;
        density = 1.0f;

        clearNeighbors();
    }


    //neighbors logic

    public void clearNeighbors(){
        for(int i=0; i<NEIGHBOR_MAX; i++){
            neighbors[i]=-1;
        }
        numNeighbors=0;
    }

    public void addNeighbor(int i){
        if(!alreadyNeighbors(i) && numNeighbors<NEIGHBOR_MAX){
            neighbors[numNeighbors]=i;
            numNeighbors++;
        }
    }

    public boolean alreadyNeighbors(int j){
        for(int i=0; i<numNeighbors; i++){
            if(neighbors[i]==j)return true;
        }
        return false;
    }
}
