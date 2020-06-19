#version 110

uniform mat4 gl_ModelViewProjectionMatrix;

varying out vec4 texCoord;
varying out vec4 theColor;

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	texCoord = gl_MultiTexCoord0;
	theColor = gl_Color;
}
