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

import org.tobi29.scapes.engine.graphics.Mesh;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

public final class GuiUtils {
    private GuiUtils() {
    }

    public static void rectangle(GuiRenderBatch renderer, float minX,
            float minY, float maxX, float maxY, float r, float g, float b,
            float a) {
        rectangle(renderer, minX, minY, maxX, maxY, 0.0f, 0.0f, 1.0f, 1.0f, r,
                g, b, a);
    }

    public static void rectangle(GuiRenderBatch renderer, float minX,
            float minY, float maxX, float maxY, float minTX, float minTY,
            float maxTX, float maxTY, float r, float g, float b, float a) {
        Mesh mesh = renderer.mesh();
        Vector3 tl = renderer.vector(minX, minY);
        Vector3 tr = renderer.vector(maxX, minY);
        Vector3 bl = renderer.vector(minX, maxY);
        Vector3 br = renderer.vector(maxX, maxY);
        mesh.addVertex(tl.floatX(), tl.floatY(), 0.0f, r, g, b, a, minTX,
                minTY);
        mesh.addVertex(tr.floatX(), tr.floatY(), 0.0f, r, g, b, a, maxTX,
                minTY);
        mesh.addVertex(bl.floatX(), bl.floatY(), 0.0f, r, g, b, a, minTX,
                maxTY);
        mesh.addVertex(bl.floatX(), bl.floatY(), 0.0f, r, g, b, a, minTX,
                maxTY);
        mesh.addVertex(tr.floatX(), tr.floatY(), 0.0f, r, g, b, a, maxTX,
                minTY);
        mesh.addVertex(br.floatX(), br.floatY(), 0.0f, r, g, b, a, maxTX,
                maxTY);
    }

    public static void shadow(GuiRenderBatch renderer, float minX, float minY,
            float maxX, float maxY, float r, float g, float b, float a) {
        shadow(renderer, minX, minY, maxX, maxY, 8.0f, r, g, b, a);
    }

    public static void shadow(GuiRenderBatch renderer, float minX, float minY,
            float maxX, float maxY, float radius, float r, float g, float b,
            float a) {
        Mesh mesh = renderer.mesh();
        Vector3 tli = renderer.vector(minX, minY);
        Vector3 tri = renderer.vector(maxX, minY);
        Vector3 bli = renderer.vector(minX, maxY);
        Vector3 bri = renderer.vector(maxX, maxY);
        Vector3 tlt = renderer.vector(minX, minY - radius);
        Vector3 trt = renderer.vector(maxX, minY - radius);
        Vector3 blb = renderer.vector(minX, maxY + radius);
        Vector3 brb = renderer.vector(maxX, maxY + radius);
        Vector3 tll = renderer.vector(minX - radius, minY);
        Vector3 trr = renderer.vector(maxX + radius, minY);
        Vector3 bll = renderer.vector(minX - radius, maxY);
        Vector3 brr = renderer.vector(maxX + radius, maxY);
        // Top
        mesh.addVertex(tlt.floatX(), tlt.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(trt.floatX(), trt.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(tli.floatX(), tli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(tli.floatX(), tli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(trt.floatX(), trt.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(tri.floatX(), tri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        // Bottom
        mesh.addVertex(bli.floatX(), bli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(bri.floatX(), bri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(blb.floatX(), blb.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(blb.floatX(), blb.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(bri.floatX(), bri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(brb.floatX(), brb.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        // Left
        mesh.addVertex(tll.floatX(), tll.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(tli.floatX(), tli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(bll.floatX(), bll.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(bll.floatX(), bll.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(tli.floatX(), tli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(bli.floatX(), bli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        // Right
        mesh.addVertex(tri.floatX(), tri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(trr.floatX(), trr.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(bri.floatX(), bri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(bri.floatX(), bri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(trr.floatX(), trr.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(brr.floatX(), brr.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        // Top-left
        mesh.addVertex(tli.floatX(), tli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(tll.floatX(), tll.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(tlt.floatX(), tlt.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        // Top-right
        mesh.addVertex(tri.floatX(), tri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(trt.floatX(), trt.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(trr.floatX(), trr.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        // Bottom-left
        mesh.addVertex(bli.floatX(), bli.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(blb.floatX(), blb.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(bll.floatX(), bll.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        // Bottom-right
        mesh.addVertex(bri.floatX(), bri.floatY(), 0.0f, r, g, b, a, 0.0f,
                0.0f);
        mesh.addVertex(brr.floatX(), brr.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
        mesh.addVertex(brb.floatX(), brb.floatY(), 0.0f, r, g, b, 0.0f, 0.0f,
                0.0f);
    }
}
