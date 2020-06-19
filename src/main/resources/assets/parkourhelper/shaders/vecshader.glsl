#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTex;
layout (location = 1) in vec3 aNorm;
layout (location = 3) in vec3 aColor;


out vec3 color;
out vec3 norm;

uniform mat4 projection;
uniform mat4 modelView;
uniform mat4 worldSpace;
uniform mat4 normalSpace;

void main()
{
	vec4 vert = vec4(aPos, 1.0);
    gl_Position = projection * modelView * vert;
    color = aColor;
    norm = (normalSpace * vec4(aNorm, 1.0)).xyz;
}
