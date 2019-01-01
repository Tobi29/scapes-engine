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
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.lengthSqr
import org.tobi29.math.vector.normalizeSafe
import org.tobi29.stdex.math.sqr
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * A circle shape.
 */
class CircleShape : Shape(ShapeType.CIRCLE) {
    private val _center = MutableVector2d()
    var center by _center

    override val childCount: Int
        get() = 1

    init {
        radius = 0.0
    }

    override fun clone(): Shape {
        val shape = CircleShape()
        shape.center = center
        shape.radius = radius
        return shape
    }

    override fun testPoint(
        transform: Transform,
        p: Vector2d
    ): Boolean {
        // Rot.mulToOutUnsafe(transform.q, m_p, center);
        // center.addLocal(transform.p);
        //
        // final Vec2 d = center.subLocal(p).negateLocal();
        // return Vec2.dot(d, d) <= m_radius * m_radius;
        val q = transform.q
        val tp = transform.p
        val centerx = -(q.cos * _center.x - q.sin * _center.y + tp.x - p.x)
        val centery = -(q.sin * _center.x + q.cos * _center.y + tp.y - p.y)

        return centerx * centerx + centery * centery <= radius * radius
    }

    override fun computeDistanceToOut(
        transform: Transform,
        point: Vector2d,
        childIndex: Int,
        normalOut: MutableVector2d
    ): Double {
        val xfq = transform.q
        val centerx = xfq.cos * _center.x - xfq.sin * _center.y + transform.p.x
        val centery = xfq.sin * _center.x + xfq.cos * _center.y + transform.p.y
        val dx = point.x - centerx
        val dy = point.y - centery
        val d1 = sqrt(dx * dx + dy * dy)
        normalOut.x = dx * 1 / d1
        normalOut.y = dy * 1 / d1
        return d1 - radius
    }

    // Collision Detection in Interactive 3D Environments by Gino van den Bergen
    // From Section 3.1.2
    // x = s + a * r
    // norm(x) = radius
    override fun raycast(
        output: RayCastOutput,
        input: RaycastInput,
        transform: Transform,
        childIndex: Int
    ): Boolean {

        val inputp1 = input.p1
        val inputp2 = input.p2
        val tq = transform.q
        val tp = transform.p

        // Rot.mulToOutUnsafe(transform.q, m_p, position);
        // position.addLocal(transform.p);
        val positionx = tq.cos * _center.x - tq.sin * _center.y + tp.x
        val positiony = tq.sin * _center.x + tq.cos * _center.y + tp.y

        val sx = inputp1.x - positionx
        val sy = inputp1.y - positiony
        // final float b = Vec2.dot(s, s) - m_radius * m_radius;
        val b = sx * sx + sy * sy - radius * radius

        // Solve quadratic equation.
        val rx = inputp2.x - inputp1.x
        val ry = inputp2.y - inputp1.y
        // final float c = Vec2.dot(s, r);
        // final float rr = Vec2.dot(r, r);
        val c = sx * rx + sy * ry
        val rr = rx * rx + ry * ry
        val sigma = c * c - rr * b

        // Check for negative discriminant and short segment.
        if (sigma < 0.0 || rr < Settings.EPSILON) {
            return false
        }

        // Find the point of intersection of the line with the circle.
        var a = -(c + sqrt(sigma))

        // Is the intersection point on the segment?
        if (0.0 <= a && a <= input.maxFraction * rr) {
            a /= rr
            output.fraction = a
            output.normal.x = rx * a + sx
            output.normal.y = ry * a + sy
            output.normal.normalizeSafe()
            return true
        }

        return false
    }

    override fun computeAABB(
        aabb: AABB2,
        transform: Transform,
        childIndex: Int
    ) {
        val tq = transform.q
        val tp = transform.p
        val px = tq.cos * _center.x - tq.sin * _center.y + tp.x
        val py = tq.sin * _center.x + tq.cos * _center.y + tp.y

        aabb.min.x = px - radius
        aabb.min.y = py - radius
        aabb.max.x = px + radius
        aabb.max.y = py + radius
    }

    override fun computeMass(
        massData: MassData,
        density: Double
    ) {
        massData.mass = density * PI * radius * radius
        massData.center = center

        // inertia about the local origin
        // massData.I = massData.mass * (0.5 * m_radius * m_radius + Vec2.dot(m_p, m_p));
        massData.i = massData.mass * (0.5 * sqr(radius) + _center.lengthSqr())
    }
}
