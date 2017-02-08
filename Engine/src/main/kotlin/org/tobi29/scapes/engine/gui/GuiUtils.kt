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

package org.tobi29.scapes.engine.gui

import org.tobi29.scapes.engine.graphics.Mesh
import org.tobi29.scapes.engine.utils.math.cos
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.sin
import org.tobi29.scapes.engine.utils.math.toRad

object GuiUtils {

    fun rectangle(renderer: GuiRenderBatch,
                  minX: Float,
                  minY: Float,
                  maxX: Float,
                  maxY: Float,
                  r: Float,
                  g: Float,
                  b: Float,
                  a: Float) {
        rectangle(renderer, minX, minY, maxX, maxY, 0.0f, 0.0f, 1.0f, 1.0f, r,
                g, b, a)
    }

    fun rectangle(renderer: GuiRenderBatch,
                  minX: Float,
                  minY: Float,
                  maxX: Float,
                  maxY: Float,
                  minTX: Float,
                  minTY: Float,
                  maxTX: Float,
                  maxTY: Float,
                  r: Float,
                  g: Float,
                  b: Float,
                  a: Float) {
        val mesh = renderer.mesh()
        val tl = renderer.vector(minX.toDouble(), minY.toDouble())
        val tr = renderer.vector(maxX.toDouble(), minY.toDouble())
        val bl = renderer.vector(minX.toDouble(), maxY.toDouble())
        val br = renderer.vector(maxX.toDouble(), maxY.toDouble())
        mesh.addVertex(tl.floatX().toDouble(), tl.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                minTX.toDouble(),
                minTY.toDouble())
        mesh.addVertex(tr.floatX().toDouble(), tr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                maxTX.toDouble(),
                minTY.toDouble())
        mesh.addVertex(bl.floatX().toDouble(), bl.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                minTX.toDouble(),
                maxTY.toDouble())
        mesh.addVertex(bl.floatX().toDouble(), bl.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                minTX.toDouble(),
                maxTY.toDouble())
        mesh.addVertex(tr.floatX().toDouble(), tr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                maxTX.toDouble(),
                minTY.toDouble())
        mesh.addVertex(br.floatX().toDouble(), br.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(),
                maxTX.toDouble(),
                maxTY.toDouble())
    }

    fun shadow(renderer: GuiRenderBatch,
               minX: Float,
               minY: Float,
               maxX: Float,
               maxY: Float,
               r: Float,
               g: Float,
               b: Float,
               a: Float) {
        shadow(renderer, minX, minY, maxX, maxY, 8.0f, r, g, b, a)
    }

    fun shadow(renderer: GuiRenderBatch,
               minX: Float,
               minY: Float,
               maxX: Float,
               maxY: Float,
               radius: Float,
               r: Float,
               g: Float,
               b: Float,
               a: Float) {
        val mesh = renderer.mesh()
        val tli = renderer.vector(minX.toDouble(), minY.toDouble())
        val tri = renderer.vector(maxX.toDouble(), minY.toDouble())
        val bli = renderer.vector(minX.toDouble(), maxY.toDouble())
        val bri = renderer.vector(maxX.toDouble(), maxY.toDouble())
        val tlt = renderer.vector(minX.toDouble(), (minY - radius).toDouble())
        val trt = renderer.vector(maxX.toDouble(), (minY - radius).toDouble())
        val blb = renderer.vector(minX.toDouble(), (maxY + radius).toDouble())
        val brb = renderer.vector(maxX.toDouble(), (maxY + radius).toDouble())
        val tll = renderer.vector((minX - radius).toDouble(), minY.toDouble())
        val trr = renderer.vector((maxX + radius).toDouble(), minY.toDouble())
        val bll = renderer.vector((minX - radius).toDouble(), maxY.toDouble())
        val brr = renderer.vector((maxX + radius).toDouble(), maxY.toDouble())
        // Top
        mesh.addVertex(tlt.floatX().toDouble(), tlt.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(trt.floatX().toDouble(), trt.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(tli.floatX().toDouble(), tli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(tli.floatX().toDouble(), tli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(trt.floatX().toDouble(), trt.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(tri.floatX().toDouble(), tri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        // Bottom
        mesh.addVertex(bli.floatX().toDouble(), bli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(bri.floatX().toDouble(), bri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(blb.floatX().toDouble(), blb.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(blb.floatX().toDouble(), blb.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(bri.floatX().toDouble(), bri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(brb.floatX().toDouble(), brb.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        // Left
        mesh.addVertex(tll.floatX().toDouble(), tll.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(tli.floatX().toDouble(), tli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(bll.floatX().toDouble(), bll.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(bll.floatX().toDouble(), bll.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(tli.floatX().toDouble(), tli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(bli.floatX().toDouble(), bli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        // Right
        mesh.addVertex(tri.floatX().toDouble(), tri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(trr.floatX().toDouble(), trr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(bri.floatX().toDouble(), bri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(bri.floatX().toDouble(), bri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(trr.floatX().toDouble(), trr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(brr.floatX().toDouble(), brr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        // Top-left
        mesh.addVertex(tli.floatX().toDouble(), tli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(tll.floatX().toDouble(), tll.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(tlt.floatX().toDouble(), tlt.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        // Top-right
        mesh.addVertex(tri.floatX().toDouble(), tri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(trt.floatX().toDouble(), trt.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(trr.floatX().toDouble(), trr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        // Bottom-left
        mesh.addVertex(bli.floatX().toDouble(), bli.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(blb.floatX().toDouble(), blb.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(bll.floatX().toDouble(), bll.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        // Bottom-right
        mesh.addVertex(bri.floatX().toDouble(), bri.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble(), 0.0,
                0.0)
        mesh.addVertex(brr.floatX().toDouble(), brr.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
        mesh.addVertex(brb.floatX().toDouble(), brb.floatY().toDouble(), 0.0,
                r.toDouble(), g.toDouble(), b.toDouble(), 0.0, 0.0,
                0.0)
    }

    fun busy(mesh: Mesh,
             width: Double,
             height: Double,
             pixelSizeX: Double,
             pixelSizeY: Double,
             r: Double,
             g: Double,
             b: Double,
             a: Double) {
        val w2 = width * 0.5
        val h2 = height * 0.5
        val w3 = max(w2 - 3.0, 0.0)
        val h3 = max(h2 - 3.0, 0.0)
        val w1 = w2 + pixelSizeX
        val h1 = h2 + pixelSizeY
        val w4 = w3 - pixelSizeX
        val h4 = h3 - pixelSizeY
        val section = 5.0
        renderPart(mesh, 40.0, 140.0, section, w1, h1, w2, h2, w3, h3, w4, h4,
                r, g, b, a)
        renderPart(mesh, 220.0, 320.0, section, w1, h1, w2, h2, w3, h3, w4, h4,
                r, g, b, a)
    }

    private fun renderPart(mesh: Mesh,
                           start: Double,
                           end: Double,
                           section: Double,
                           w1: Double,
                           h1: Double,
                           w2: Double,
                           h2: Double,
                           w3: Double,
                           h3: Double,
                           w4: Double,
                           h4: Double,
                           r: Double,
                           g: Double,
                           b: Double,
                           a: Double) {
        var cos = cos(start.toRad())
        var sin = sin(start.toRad())
        mesh.color(r, g, b, 0.0)
        var dir = start + section
        while (dir <= end) {
            val ncos = cos(dir.toRad())
            val nsin = sin(dir.toRad())
            mesh.vertex(ncos * w1, nsin * h1, 0.0)
            mesh.vertex(cos * w1, sin * h1, 0.0)
            mesh.color(r, g, b, a)
            mesh.vertex(cos * w2, sin * h2, 0.0)
            mesh.vertex(ncos * w2, nsin * h2, 0.0)

            mesh.vertex(ncos * w2, nsin * h2, 0.0)
            mesh.vertex(cos * w2, sin * h2, 0.0)
            mesh.vertex(cos * w3, sin * h3, 0.0)
            mesh.vertex(ncos * w3, nsin * h3, 0.0)

            mesh.vertex(ncos * w3, nsin * h3, 0.0)
            mesh.vertex(cos * w3, sin * h3, 0.0)
            mesh.color(r, g, b, 0.0)
            mesh.vertex(cos * w4, sin * h4, 0.0)
            mesh.vertex(ncos * w4, nsin * h4, 0.0)
            cos = ncos
            sin = nsin
            dir += section
        }
    }
}
