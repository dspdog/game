package factory;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
            myTexture = TextureLoader.getTexture("PNG", new FileInputStream(new File("./res/myball2.png")));
            System.out.println("TEXID" + myTexture.getTextureID());
            //myTexture.release();
            return myTexture;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        //
    }

    static public int proceduralTexture(){  //http://www.java-gaming.org/index.php?topic=25516.0
        //Generate a small test image by drawing to a BufferedImage
        //It's of course also possible to just load an image using ImageIO.load()
        BufferedImage test = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = test.createGraphics();

        g2d.setColor(new Color(0.5f, 1.0f, 1.0f, 0.5f));
        g2d.fillRect(0, 0, 128, 128); //A transparent white background

        g2d.setColor(Color.red);
        g2d.drawRect(0, 0, 127, 127); //A red frame around the image
        g2d.fillRect(10, 10, 10, 10); //A red box

        g2d.setColor(Color.blue);
        g2d.drawString("Test image", 10, 64); //Some blue text

        return loadTexture(test);
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
}
