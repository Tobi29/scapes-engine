#version 150

uniform sampler2D uniform_Texture;

in vec4 varying_Color;
in vec2 varying_Texture;

out vec4 out_Color;

void main(void)  {
    vec4 color = texture(uniform_Texture, varying_Texture);
    out_Color.a = color.a * varying_Color.a;
    if (out_Color.a <= 0.01) {
        discard;
    }
    out_Color.rgb = color.rgb * varying_Color.rgb;
}