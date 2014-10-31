import org.lwjgl.opengl.GL11;

/**
 * Created by user on 10/28/14.
 */
public class GeometryFactory {
    static void grid(){
        GL11.glBegin(GL11.GL_LINES);
        for(int x=0; x<100; x++){
            for(int y=0; y<100; y++){
                GL11.glColor3f(x/100f, y/100f, 0.5f);
                GL11.glVertex3f(x*1f,0,y*1f);
                GL11.glVertex3f((x+1)*1f,0,y*1f);
                GL11.glVertex3f(x*1f,0,y*1f);
                GL11.glVertex3f(x*1f,0,(y+1)*1f);
            }
        }

        GL11.glVertex3f(0,0,0);GL11.glVertex3f(0,100,0);

        GL11.glVertex3f(100,0,0);GL11.glVertex3f(100,100,0);
        GL11.glVertex3f(0,0,100);GL11.glVertex3f(0,100,100);
        GL11.glVertex3f(100,0,100);GL11.glVertex3f(100,100,100);

        GL11.glEnd();
    }
}
