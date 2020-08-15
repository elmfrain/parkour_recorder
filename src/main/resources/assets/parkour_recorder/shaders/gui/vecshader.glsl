#version 110

uniform mat4 gl_ModelViewProjectionMatrix;

varying out vec3 normal;
varying out vec4 color;

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	color = gl_Color;
}
