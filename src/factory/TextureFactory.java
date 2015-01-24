package factory;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextureFactory {

    static public int ballTexture(){
        //load texture from png
        Texture myTexture;
        try {
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball.png")));
            System.out.println("TEXID" + myTexture.getTextureID());
            //myTexture.release();
            return myTexture.getTextureID();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
        //
    }


    static public Texture ballTextureT(){
        //load texture from png
        Texture myTexture;
        try {
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball6.png")));
            System.out.println("TEXID" + myTexture.getTextureID());
            //myTexture.release();
            return myTexture;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        //
    }

    static public int stringToTexture(String str, int width, int height){  //http://www.java-gaming.org/index.php?topic=25516.0
        //Generate a small test image by drawing to a BufferedImage
        //It's of course also possible to just load an image using ImageIO.load()
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        drawOutlinedText(str, g2d);

        return loadTexture(img);
    }

    /*
        //Texture Loader example:
        try {
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
     */

    static public void drawOutlinedText(String str, Graphics2D g2d){
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));

        String[] splitData = str.split("\n");
        int yOffset=0;
        int pixelPerLine = 14;

        int baseX = 4;
        int baseY = 18;

        g2d.setColor(Color.black); //drop shadow

        for(int x=0; x<2; x++){
            for(int y=0; y<2; y++){
                if(!(x==0 && y==0)){
                    yOffset=0;
                    for (String eachSplit : splitData) {
                        g2d.drawString(eachSplit, baseX+x, baseY+yOffset+y);
                        yOffset+=pixelPerLine;
                    }
                }
            }
        }

        g2d.setColor(Color.white); //text
        yOffset=0;
        for (String eachSplit : splitData) {
            g2d.drawString(eachSplit, baseX, baseY+yOffset);
            yOffset+=pixelPerLine;
        }
    }

    private static final int BYTES_PER_PIXEL = 4;
    public static int loadTexture(BufferedImage image){

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB

        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using
        // whatever OpenGL method you want, for example:

        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA4, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        //Return the texture ID so we can bind it later again
        return textureID;
    }

    public static ByteBuffer getPixelsBuffer(){ //http://wiki.lwjgl.org/index.php?title=Taking_Screen_Shots
        GL11.glReadBuffer(GL11.GL_FRONT);
        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = BYTES_PER_PIXEL; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
        return buffer;
    }

    public static void savePixelsBuffer(){
        ByteBuffer buffer = getPixelsBuffer();

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();

        File file = new File("pic1.PNG"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        long time1 = System.currentTimeMillis();

        for(int x = 1; x < width-1; x++) {
            for(int y = 1; y < height-1; y++) {
                color c = getColor(x,y,buffer,width);
                image.setRGB(x, height - (y + 1), c.rgb());
            }
        }

        long time2 = System.currentTimeMillis();
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }

        System.out.println("img saved " + file.getAbsolutePath() + " edges in " + (time2-time1) + ", saved in " + (System.currentTimeMillis()-time2));
    }

    public static color getColor(int x, int y, ByteBuffer buffer, int width){
        int i = (x + (width * y)) * BYTES_PER_PIXEL;
        return new color(
                buffer.get(i) & 0xFF,
                buffer.get(i + 1) & 0xFF,
                buffer.get(i + 2) & 0xFF);
    }

    static class color{
        public float r=0, g=0, b=0;
        public color(float _r, float _g, float _b){
            r=_r;
            b=_b;
            g=_g;
        }

        public int rgb(){
            return (0xFF << 24) | ((int)r << 16) | ((int)g << 8) | (int)b;
        }

        public color toGray(){
            float gray = (r+g+b)/3f;
            return new color(gray,gray,gray);
        }

        public void addDiff(float s, color c, color d){
            r+=(d.r-c.r)*s;
            g+=(d.g-c.g)*s;
            b+=(d.b-c.b)*s;
        }

        public void scale(float s){
            r*=s;
            g*=s;
            b*=s;
        }
    }
}
