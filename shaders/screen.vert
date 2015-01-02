varying vec4 vertColor;

void main(){
    //gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
    gl_Position = ftransform();
    gl_FrontColor = gl_Color;
    //vertColor = vec4(0.6, 0.3, 0.4, 1.0);
}