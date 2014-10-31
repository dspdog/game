import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import shapes.tree;

public class GeometryFactory {
    static void gridCube(){
        float res = 100;
        GL11.glBegin(GL11.GL_LINES);
        for(float x=0; x<res; x++){
            for(float y=0; y<res; y++){
                for(float z=0; z<res; z++){
                    GL11.glColor3f(x/res, y/res, z/res);
                    GL11.glVertex3f(x*1f,y*1f,z*1f);
                    GL11.glVertex3f((x+1)*1f,y*1f,z*1f);
                    GL11.glVertex3f(x*1f,y*1f,z*1f);
                    GL11.glVertex3f(x*1f,(y+1)*1f,z*1f);
                }
            }
        }
        GL11.glEnd();
    }

    static void gridFlat(){
        float res = 100;
        GL11.glBegin(GL11.GL_LINES);
        for(float x=0; x<res; x++){
            for(float z=0; z<res; z++){
                float y=0;
                GL11.glColor3f(x/res, y/res, z/res);
                GL11.glVertex3f(x*1f,y*1f,z*1f);
                GL11.glVertex3f((x+1)*1f,y*1f,z*1f);
                GL11.glVertex3f(x*1f,y*1f,z*1f);
                GL11.glVertex3f(x*1f,(y+1)*1f,z*1f);
            }
        }
        GL11.glEnd();
    }

    static void plane(){
        int size = 250;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor3f(0, 0, 0);
        GL11.glVertex3f(0,0,0);
        GL11.glColor3f(1, 0, 0);
        GL11.glVertex3f(size,0,0);
        GL11.glColor3f(1, 0, 1);
        GL11.glVertex3f(size,0,size);
        GL11.glColor3f(0, 0, 1);
        GL11.glVertex3f(0,0,size);
        GL11.glEnd();
    }

    static void addObj(Object o){
        if(o instanceof tree){
            tree((shapes.tree)o);
        }
    }

    static void tree(shapes.tree theTree){
        float scale = 0.12f;

        //GL11.glLineWidth(4);
        GL11.glBegin(GL11.GL_LINES);
        for(int i=0; i<theTree.lineIndex; i++){
            GL11.glColor3f(theTree.getX1(i)*scale/100f, theTree.getY1(i)*scale/100f, theTree.getZ1(i)*scale/100f);
            GL11.glVertex3f(theTree.getX1(i)*scale,theTree.getY1(i)*scale,theTree.getZ1(i)*scale);
            GL11.glVertex3f(theTree.getX2(i)*scale,theTree.getY2(i)*scale,theTree.getZ2(i)*scale);
        }
        GL11.glEnd();
    }
}
