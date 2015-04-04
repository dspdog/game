package utils;

import org.lwjgl.opengl.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ShaderHelper { //http://wiki.lwjgl.org/index.php?title=GLSL_Shaders_with_LWJGL
    /*
       * if the shaders are setup ok we can use shaders, otherwise we just
       * use default settings
       */
    private static boolean useShader=false;

    /*
    * program shader, to which is attached a vertex and fragment shaders.
    * They are set to 0 as a check because GL will assign unique int
    * values to each
    */
    private static int program=0;

    public static void bindShaders(){
        bindTexture0Shader();
    }

    public static void bindDepthShader(){
        bindShaders("screen.vert", "depth.frag");
    }

    public static void bindTexture0Shader(){
        bindShaders("screen.vert", "plain_texture0.frag");
    }

    public static void bindShaders(String vertShader, String fragShader){
        ShaderHelper.setupShaders(vertShader, fragShader);
        if(ShaderHelper.useShader){
            ARBShaderObjects.glUseProgramObjectARB(ShaderHelper.program);
        }
        ShaderHelper.setTextureUnit0(ShaderHelper.program);
    }

    public static void releaseShaders(){
        if(ShaderHelper.useShader)
            ARBShaderObjects.glUseProgramObjectARB(0);
    }


    private static void setTextureUnit0(int programId) {
        //Please note your program must be linked before calling this and I would advise the program be in use also.
        int loc = GL20.glGetUniformLocation(programId, "texture1");
        //First of all, we retrieve the location of the sampler in memory.
        GL20.glUniform1i(loc, 0);
        //Then we pass the 0 value to the sampler meaning it is to use texture unit 0.
    }

    private static void setupShaders(String vertShaderName, String fragShaderName){
        int vertShader = 0, fragShader = 0;

        try {
            vertShader = createShader("shaders/" + vertShaderName,ARBVertexShader.GL_VERTEX_SHADER_ARB);
            fragShader = createShader("shaders/" + fragShaderName,ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
        }
        catch(Exception exc) {
            exc.printStackTrace();
            return;
        }
        finally {
            if(vertShader == 0 || fragShader == 0)
                return;
        }

        program = ARBShaderObjects.glCreateProgramObjectARB();

        if(program == 0)
            return;

        /*
        * if the vertex and fragment shaders setup sucessfully,
        * attach them to the shader program, link the sahder program
        * (into the GL context I suppose), and validate
        */
        ARBShaderObjects.glAttachObjectARB(program, vertShader);
        ARBShaderObjects.glAttachObjectARB(program, fragShader);

        ARBShaderObjects.glLinkProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println(getLogInfo(program));
            return;
        }

        ARBShaderObjects.glValidateProgramARB(program);
        if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println(getLogInfo(program));
            return;
        }

        useShader = true;
    }

    /* With the exception of syntax, setting up vertex and fragment shaders
    * is the same.
    * @param the name and path to the vertex shader
    */
    static private int createShader(String filename, int shaderType) throws Exception {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if(shader == 0)
                return 0;

            ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

            return shader;
        }
        catch(Exception exc) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exc;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    static private String readFileAsString(String filename) throws Exception {
        StringBuilder source = new StringBuilder();

        FileInputStream in = new FileInputStream(filename);

        Exception exception = null;

        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));

            Exception innerExc= null;
            try {
                String line;
                while((line = reader.readLine()) != null)
                    source.append(line).append('\n');
            }
            catch(Exception exc) {
                exception = exc;
            }
            finally {
                try {
                    reader.close();
                }
                catch(Exception exc) {
                    if(innerExc == null)
                        innerExc = exc;
                    else
                        exc.printStackTrace();
                }
            }

            if(innerExc != null)
                throw innerExc;
        }
        catch(Exception exc) {
            exception = exc;
        }
        finally {
            try {
                in.close();
            }
            catch(Exception exc) {
                if(exception == null)
                    exception = exc;
                else
                    exc.printStackTrace();
            }

            if(exception != null)
                throw exception;
        }

        return source.toString();
    }
}

/*
//http://stackoverflow.com/questions/15777757/drawing-normals-in-lwjgl-messes-with-lighting
//Feel free to use this for whatever you want, no licenses applied or anything.

//p1, p2, p3 - Vertices of triangle
public Vector3f getNormal(Vector3f p1, Vector3f p2, Vector3f p3) {

    //Create normal vector we are going to output.
    Vector3f output = new Vector3f();

    //Calculate vectors used for creating normal (these are the edges of the triangle).
    Vector3f calU = new Vector3f(p2.x-p1.x, p2.y-p1.y, p2.z-p1.z);
    Vector3f calV = new Vector3f(p3.x-p1.x, p3.y-p1.y, p3.z-p1.z);

    //The output vector is equal to the cross products of the two edges of the triangle
    output.x = calU.y*calV.z - calU.z*calV.y;
    output.y = calU.z*calV.x - calU.x*calV.z;
    output.z = calU.x*calV.y - calU.y*calV.x;

    //Return the resulting vector.
    return output.normalise();
}
 */
