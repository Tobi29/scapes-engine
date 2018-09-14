/*
 * Copyright 2012-2018 Tobi29
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
import org.tobi29.stdex.math.toRad
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object GuiUtils {

    fun rectangle(renderer: GuiRenderBatch,
                  minX: Double,
                  minY: Double,
                  maxX: Double,
                  maxY: Double,
                  r: Double,
                  g: Double,
                  b: Double,
                  a: Double) {
        rectangle(renderer, minX, minY, maxX, maxY, 0.0, 0.0, 1.0, 1.0, r, g, b, a)
    }

    fun rectangle(renderer: GuiRenderBatch,
                  minX: Double,
                  minY: Double,
                  maxX: Double,
                  maxY: Double,
                  minTX: Double,
                  minTY: Double,
                  maxTX: Double,
                  maxTY: Double,
                  r: Double,
                  g: Double,
                  b: Double,
                  a: Double) {
        val mesh = renderer.mesh()
        val tl = renderer.vector(minX, minY)
        val tr = renderer.vector(maxX, minY)
        val bl = renderer.vector(minX, maxY)
        val br = renderer.vector(maxX, maxY)
        mesh.addVertex(tl.x, tl.y, 0.0, r, g, b, a, minTX, minTY)
        mesh.addVertex(tr.x, tr.y, 0.0, r, g, b, a, maxTX, minTY)
        mesh.addVertex(bl.x, bl.y, 0.0, r, g, b, a, minTX, maxTY)
        mesh.addVertex(bl.x, bl.y, 0.0, r, g, b, a, minTX, maxTY)
        mesh.addVertex(tr.x, tr.y, 0.0, r, g, b, a, maxTX, minTY)
        mesh.addVertex(br.x, br.y, 0.0, r, g, b, a, maxTX, maxTY)
    }

    fun shadow(renderer: GuiRenderBatch,
               minX: Double,
               minY: Double,
               maxX: Double,
               maxY: Double,
               r: Double,
               g: Double,
               b: Double,
               a: Double) {
        shadow(renderer, minX, minY, maxX, maxY, 8.0, r, g, b, a)
    }

    fun shadow(renderer: GuiRenderBatch,
               minX: Double,
               minY: Double,
               maxX: Double,
               maxY: Double,
               radius: Double,
               r: Double,
               g: Double,
               b: Double,
               a: Double) {
        val mesh = renderer.mesh()
        val tli = renderer.vector(minX, minY)
        val tri = renderer.vector(maxX, minY)
        val bli = renderer.vector(minX, maxY)
        val bri = renderer.vector(maxX, maxY)
        val tlt = renderer.vector(minX, (minY - radius))
        val trt = renderer.vector(maxX, (minY - radius))
        val blb = renderer.vector(minX, (maxY + radius))
        val brb = renderer.vector(maxX, (maxY + radius))
        val tll = renderer.vector((minX - radius), minY)
        val trr = renderer.vector((maxX + radius), minY)
        val bll = renderer.vector((minX - radius), maxY)
        val brr = renderer.vector((maxX + radius), maxY)

        // Top
        mesh.addVertex(tlt.x, tlt.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(trt.x, trt.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(tli.x, tli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(tli.x, tli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(trt.x, trt.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(tri.x, tri.y, 0.0, r, g, b, a, 0.0, 0.0)

        // Bottom
        mesh.addVertex(bli.x, bli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(bri.x, bri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(blb.x, blb.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(blb.x, blb.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(bri.x, bri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(brb.x, brb.y, 0.0, r, g, b, 0.0, 0.0, 0.0)

        // Left
        mesh.addVertex(tll.x, tll.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(tli.x, tli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(bll.x, bll.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(bll.x, bll.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(tli.x, tli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(bli.x, bli.y, 0.0, r, g, b, a, 0.0, 0.0)

        // Right
        mesh.addVertex(tri.x, tri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(trr.x, trr.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(bri.x, bri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(bri.x, bri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(trr.x, trr.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(brr.x, brr.y, 0.0, r, g, b, 0.0, 0.0, 0.0)

        // Top-left
        mesh.addVertex(tli.x, tli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(tll.x, tll.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(tlt.x, tlt.y, 0.0, r, g, b, 0.0, 0.0, 0.0)

        // Top-right
        mesh.addVertex(tri.x, tri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(trt.x, trt.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(trr.x, trr.y, 0.0, r, g, b, 0.0, 0.0, 0.0)

        // Bottom-left
        mesh.addVertex(bli.x, bli.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(blb.x, blb.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(bll.x, bll.y, 0.0, r, g, b, 0.0, 0.0, 0.0)

        // Bottom-right
        mesh.addVertex(bri.x, bri.y, 0.0, r, g, b, a, 0.0, 0.0)
        mesh.addVertex(brr.x, brr.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
        mesh.addVertex(brb.x, brb.y, 0.0, r, g, b, 0.0, 0.0, 0.0)
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
