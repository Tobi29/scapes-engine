/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.graphics

import org.tobi29.scapes.engine.resource.Resource

val SHADER_GUI = Resource("""
uniform 1 Matrix4 uniform_ModelViewProjectionMatrix;
uniform 3 Texture2 uniform_Texture;

shader vertex(0 Vector4 attribute_Position,
              1 Vector4 attribute_Color,
              2 Vector2 attribute_Texture) {
    varying_Texture = attribute_Texture;
    varying_Color = attribute_Color;
    out_Position = uniform_ModelViewProjectionMatrix * attribute_Position;
}

shader fragment(Vector4 varying_Color,
                Vector2 varying_Texture) {
    Vector4 color = texture(uniform_Texture, varying_Texture);
    out_Color.a = color.a * varying_Color.a;
    if (out_Color.a <= 0.01) {
        discard();
    }
    out_Color.rgb = color.rgb * varying_Color.rgb;
}

outputs(0 Vector4 out_Color);
""")

val SHADER_TEXTURED = Resource("""
uniform 1 Matrix4 uniform_ModelViewProjectionMatrix;
uniform 3 Texture2 uniform_Texture;

shader vertex(0 Vector4 attribute_Position,
              1 Vector4 attribute_Color,
              2 Vector2 attribute_Texture) {
    varying_Texture = attribute_Texture;
    varying_Color = attribute_Color;
    out_Position = uniform_ModelViewProjectionMatrix * attribute_Position;
}

shader fragment(Vector4 varying_Color,
                Vector2 varying_Texture) {
    Vector4 color = texture(uniform_Texture, varying_Texture);
    out_Color.a = color.a * varying_Color.a;
    if (out_Color.a <= 0.01) {
        discard();
    }
    out_Color.rgb = color.rgb * varying_Color.rgb;
}

outputs(0 Vector4 out_Color);
""")

val SHADER_TEXTURED_FOG = Resource("""
uniform 0 Matrix4 uniform_ModelViewMatrix;
uniform 1 Matrix4 uniform_ModelViewProjectionMatrix;
uniform 3 Texture2 uniform_Texture;
uniform 4 Vector3 uniform_FogColor;
uniform 5 Float uniform_FogEnd;

shader vertex(0 Vector4 attribute_Position,
              1 Vector4 attribute_Color,
              2 Vector2 attribute_Texture) {
    varying_Texture = attribute_Texture;
    varying_Color = attribute_Color;
    varying_Depth = length((uniform_ModelViewMatrix * attribute_Position).xyz);
    out_Position = uniform_ModelViewProjectionMatrix * attribute_Position;
}

shader fragment(Vector4 varying_Color,
                Vector2 varying_Texture,
                Float varying_Depth) {
    Vector4 color = texture(uniform_Texture, varying_Texture);
    Float fog = min(varying_Depth / uniform_FogEnd, 1.0);
    out_Color.a = color.a * varying_Color.a;
    if (out_Color.a <= 0.01) {
        discard();
    }
    out_Color.rgb = mix(color.rgb * varying_Color.rgb, uniform_FogColor, fog);
}

outputs(0 Vector4 out_Color);
""")