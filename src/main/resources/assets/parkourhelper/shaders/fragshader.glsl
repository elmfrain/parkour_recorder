#version 330 core

in vec3 color;
in vec3 norm;

uniform vec4 masterColor;
uniform vec4 maskColor;
uniform bool enableWhiteScreen;

void main()
{
	vec4 preColor = vec4(color * (norm.y / 2.0 + 1.0), 1.0) * masterColor;
	if(enableWhiteScreen)
	{
		gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0 - color.r * preColor.a);
	}
	else
	{
		gl_FragColor = preColor;
	}
}

