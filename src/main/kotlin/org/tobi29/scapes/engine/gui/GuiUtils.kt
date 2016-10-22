/*
 * Copyright 2012-2016 Tobi29
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
}
