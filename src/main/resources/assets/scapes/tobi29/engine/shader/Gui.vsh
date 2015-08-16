#version 150

in vec4 attribute_Position;
in vec4 attribute_Color;
in vec2 attribute_Texture;

uniform mat4 uniform_ModelViewProjectionMatrix;

out vec4 varying_Color;
out vec2 varying_Texture;

void main(void)  {
    varying_Texture = attribute_Texture;
    varying_Color = attribute_Color;
    gl_Position = uniform_ModelViewProjectionMatrix * attribute_Position;
}
