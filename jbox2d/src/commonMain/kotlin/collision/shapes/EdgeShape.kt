/*
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jbox2d.collision.shapes

import org.jbox2d.collision.RayCastOutput
import org.jbox2d.collision.RaycastInput
import org.jbox2d.common.Settings
import org.jbox2d.common.Transform
import org.tobi29.math.AABB2
import org.tobi29.math.vector.*
import kotlin.math.sqrt

/**
 * A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The
 * connectivity information is used to ensure correct contact normals.
 *
 * @author Daniel
 */
class EdgeShape : Shape(ShapeType.EDGE) {
    val _vertex1 = MutableVector2d()
    /**
     * edge vertex 1
     */
    var vertex1 by _vertex1

    val _vertex2 = MutableVector2d()
    /**
     * edge vertex 2
     */
    var vertex2 by _vertex2

    /**
     * optional adjacent vertex 1. Used for smooth collision
     */
    var vertex0: Vector2d? = null

    /**
     * optional adjacent vertex 2. Used for smooth collision
     */
    var vertex3: Vector2d? = null

    override val childCount: Int
        get() = 1

    // for pooling
    private val normal = MutableVector2d()

    init {
        radius = Settings.polygonRadius
    }

    fun set(
        v1: Vector2d,
        v2: Vector2d
    ) {
        _vertex1.set(v1)
        _vertex2.set(v2)
        vertex0 = null
        vertex3 = null
    }

    fun set(
        v1: MutableVector2d,
        v2: MutableVector2d
    ) {
        _vertex1.set(v1)
        _vertex2.set(v2)
        vertex0 = null
        vertex3 = null
    }

    override fun testPoint(
        xf: Transform,
        p: Vector2d
    ): Boolean {
        return false
    }

    override fun computeDistanceToOut(
        transform: Transform,
        point: Vector2d,
        childIndex: Int,
        normalOut: MutableVector2d
    ): Double {
        val xfqc = transform.q.cos
        val xfqs = transform.q.sin
        val xfpx = transform.p.x
        val xfpy = transform.p.y
        val v1x = xfqc * _vertex1.x - xfqs * _vertex1.y + xfpx
        val v1y = xfqs * _vertex1.x + xfqc * _vertex1.y + xfpy
        val v2x = xfqc * _vertex2.x - xfqs * _vertex2.y + xfpx
        val v2y = xfqs * _vertex2.x + xfqc * _vertex2.y + xfpy

        var dx = point.x - v1x
        var dy = point.y - v1y
        val sx = v2x - v1x
        val sy = v2y - v1y
        val ds = dx * sx + dy * sy
        if (ds > 0) {
            val s2 = sx * sx + sy * sy
            if (ds > s2) {
                dx = point.x - v2x
                dy = point.y - v2y
            } else {
                dx -= ds / s2 * sx
                dy -= ds / s2 * sy
            }
        }

        val d1 = sqrt(dx * dx + dy * dy)
        if (d1 > 0) {
            normalOut.x = 1 / d1 * dx
            normalOut.y = 1 / d1 * dy
        } else {
            normalOut.x = 0.0
            normalOut.y = 0.0
        }
        return d1
    }

    // p = p1 + t * d
    // v = v1 + s * e
    // p1 + t * d = v1 + s * e
    // s * e - t * d = p1 - v1
    override fun raycast(
        output: RayCastOutput,
        input: RaycastInput,
        xf: Transform,
        childIndex: Int
    ): Boolean {

        var tempx: Double
        var tempy: Double
        val v1 = _vertex1
        val v2 = _vertex2
        val xfq = xf.q
        val xfp = xf.p

        // Put the ray into the edge's frame of reference.
        // b2Vec2 p1 = b2MulT(xf.q, input.p1 - xf.p);
        // b2Vec2 p2 = b2MulT(xf.q, input.p2 - xf.p);
        tempx = input.p1.x - xfp.x
        tempy = input.p1.y - xfp.y
        val p1x = xfq.cos * tempx + xfq.sin * tempy
        val p1y = -xfq.sin * tempx + xfq.cos * tempy

        tempx = input.p2.x - xfp.x
        tempy = input.p2.y - xfp.y
        val p2x = xfq.cos * tempx + xfq.sin * tempy
        val p2y = -xfq.sin * tempx + xfq.cos * tempy

        val dx = p2x - p1x
        val dy = p2y - p1y

        // final Vec2 normal = pool2.set(v2).subLocal(v1);
        // normal.set(normal.y, -normal.x);
        normal.x = v2.y - v1.y
        normal.y = v1.x - v2.x
        normal.normalizeSafe()
        val normalx = normal.x
        val normaly = normal.y

        // q = p1 + t * d
        // dot(normal, q - v1) = 0
        // dot(normal, p1 - v1) + t * dot(normal, d) = 0
        tempx = v1.x - p1x
        tempy = v1.y - p1y
        val numerator = normalx * tempx + normaly * tempy
        val denominator = normalx * dx + normaly * dy

        if (denominator == 0.0) {
            return false
        }

        val t = numerator / denominator
        if (t < 0.0 || 1.0 < t) {
            return false
        }

        // Vec2 q = p1 + t * d;
        val qx = p1x + t * dx
        val qy = p1y + t * dy

        // q = v1 + s * r
        // s = dot(q - v1, r) / dot(r, r)
        // Vec2 r = v2 - v1;
        val rx = v2.x - v1.x
        val ry = v2.y - v1.y
        val rr = rx * rx + ry * ry
        if (rr == 0.0) {
            return false
        }
        tempx = qx - v1.x
        tempy = qy - v1.y
        // float s = Vec2.dot(pool5, r) / rr;
        val s = (tempx * rx + tempy * ry) / rr
        if (s < 0.0 || 1.0 < s) {
            return false
        }

        output.fraction = t
        if (numerator > 0.0) {
            // output.normal = -b2Mul(xf.q, normal);
            output.normal.x = -xfq.cos * normal.x + xfq.sin * normal.y
            output.normal.y = -xfq.sin * normal.x - xfq.cos * normal.y
        } else {
            // output->normal = b2Mul(xf.q, normal);
            output.normal.x = xfq.cos * normal.x - xfq.sin * normal.y
            output.normal.y = xfq.sin * normal.x + xfq.cos * normal.y
        }
        return true
    }

    override fun computeAABB(
        aabb: AABB2,
        transform: Transform,
        childIndex: Int
    ) {
        val lowerBound = aabb.min
        val upperBound = aabb.max
        val xfq = transform.q

        val v1x = xfq.cos * _vertex1.x - xfq.sin * _vertex1.y + transform.p.x
        val v1y = xfq.sin * _vertex1.x + xfq.cos * _vertex1.y + transform.p.y
        val v2x = xfq.cos * _vertex2.x - xfq.sin * _vertex2.y + transform.p.x
        val v2y = xfq.sin * _vertex2.x + xfq.cos * _vertex2.y + transform.p.y

        lowerBound.x = if (v1x < v2x) v1x else v2x
        lowerBound.y = if (v1y < v2y) v1y else v2y
        upperBound.x = if (v1x > v2x) v1x else v2x
        upperBound.y = if (v1y > v2y) v1y else v2y

        lowerBound.x -= radius
        lowerBound.y -= radius
        upperBound.x += radius
        upperBound.y += radius
    }

    override fun computeMass(
        massData: MassData,
        density: Double
    ) {
        massData.mass = 0.0
        massData._center.set(_vertex1)
        massData._center.add(_vertex2)
        massData._center.multiply(0.5)
        massData.i = 0.0
    }

    override fun clone(): Shape {
        val edge = EdgeShape()
        edge.radius = this.radius
        edge.vertex0 = this.vertex0
        edge._vertex1.set(this._vertex1)
        edge._vertex2.set(this._vertex2)
        edge.vertex3 = this.vertex3
        return edge
    }
}
