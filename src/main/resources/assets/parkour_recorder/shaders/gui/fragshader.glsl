#version 110

in vec3 normal;
in vec4 color;

uniform vec4 masterColor;

void main()
{
	gl_FragColor = color * masterColor;
}
