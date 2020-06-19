#version 110

in vec4 texCoord;
in vec4 theColor;

uniform sampler2D tex1;
uniform bool texturesEnabled;
uniform float displayWidth;
uniform float displayHeight;

void main()
{
	bool test = tex1 != 0;
	if(texturesEnabled)
	{
		float amount = gl_FragCoord.x / displayWidth;
		vec4 sample = texture2D(tex1, texCoord.xy);
		gl_FragColor = vec4((theColor * sample).rgb, amount);
	}
	else
	{
		gl_FragColor = theColor;
	}
}
