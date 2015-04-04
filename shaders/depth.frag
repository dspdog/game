#version 110

uniform sampler2D texture1; //Remember back to my first tutorial (if you read it). Samplers are data types used to access textures. //To use textures from your main program, this must be uniform.

void main() {
    float d = gl_FragCoord.z;

    int powerLog2 = 12;
    for(int i=0; i<powerLog2; i++){d*=d;}

    float s = 1024.0;
    gl_FragColor = vec4(d,d,d,1.0);
}