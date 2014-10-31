import org.lwjgl.opengl.GL11;

public class GeometryFactory {
    static void gridCube(float res){
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
}
