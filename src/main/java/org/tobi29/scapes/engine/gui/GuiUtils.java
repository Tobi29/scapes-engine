/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.Mesh;

public class GuiUtils {
    public static void renderShadow(Mesh mesh, float x1, float y1, float x2,
            float y2, float a) {
        renderShadow(mesh, x1, y1, x2, y2, 0, 0, 0, a);
    }

    public static void renderShadow(Mesh mesh, float x1, float y1, float x2,
            float y2, float r, float g, float b, float a) {
        mesh.color(r, g, b, 0);
        mesh.vertex(x1, y1 - 8, 0.0f);
        mesh.vertex(x1 - 8, y1, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x1, y1, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x1 - 8, y2, 0.0f);
        mesh.vertex(x1, y2 + 8, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x1, y2, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2 + 8, y1, 0.0f);
        mesh.vertex(x2, y1 - 8, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x2, y1, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2, y2 + 8, 0.0f);
        mesh.vertex(x2 + 8, y2, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x2, y2, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2, y1 - 8, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x1, y1, 0.0f);
        mesh.vertex(x2, y1, 0.0f);
        mesh.vertex(x1, y1, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2, y1 - 8, 0.0f);
        mesh.vertex(x1, y1 - 8, 0.0f);
        mesh.vertex(x1, y2 + 8, 0.0f);
        mesh.vertex(x2, y2 + 8, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x1, y2, 0.0f);
        mesh.vertex(x2, y2, 0.0f);
        mesh.vertex(x1, y2, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2, y2 + 8, 0.0f);
        mesh.vertex(x1 - 8, y1, 0.0f);
        mesh.vertex(x1 - 8, y2, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x1, y1, 0.0f);
        mesh.vertex(x1, y2, 0.0f);
        mesh.vertex(x1, y1, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x1 - 8, y2, 0.0f);
        mesh.vertex(x2 + 8, y2, 0.0f);
        mesh.color(r, g, b, a);
        mesh.vertex(x2, y1, 0.0f);
        mesh.vertex(x2, y2, 0.0f);
        mesh.vertex(x2, y1, 0.0f);
        mesh.color(r, g, b, 0);
        mesh.vertex(x2 + 8, y2, 0.0f);
        mesh.vertex(x2 + 8, y1, 0.0f);
    }
}
