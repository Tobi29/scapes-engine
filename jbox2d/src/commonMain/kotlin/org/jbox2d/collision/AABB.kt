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
package org.jbox2d.collision

import org.jbox2d.common.Settings
import org.jbox2d.common.isValid
import org.jbox2d.pooling.DefaultWorldPool
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.AABB2
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.subtract
import kotlin.math.abs
import kotlin.math.min

/** Verify that the bounds are sorted  */
val AABB2.isValid: Boolean
    get() {
        val dx = max.x - min.x
        if (dx < 0.0) {
            return false
        }
        val dy = max.y - min.y
        return if (dy < 0) {
            false
        } else min.isValid && max.isValid
    }

val AABB2.perimeter: Double
    get() = 2.0 * (max.x - min.x + max.y - min.y)

val AABB2.vertices: Array<Vector2d>
    get() = min.now().let { lower ->
        max.now().let { upper ->
            arrayOf(
                lower,
                Vector2d(upper.x, lower.y),
                upper,
                Vector2d(lower.x, upper.y)
            )
        }
    }

/**
 * Combine two AABBs into this one.
 *
 * @param aabb1
 * @param aab
 */
fun AABB2.combine(
    aabb1: AABB2,
    aab: AABB2
) {
    min.x =
            if (aabb1.min.x < aab.min.x) aabb1.min.x else aab.min.x
    min.y =
            if (aabb1.min.y < aab.min.y) aabb1.min.y else aab.min.y
    max.x =
            if (aabb1.max.x > aab.max.x) aabb1.max.x else aab.max.x
    max.y =
            if (aabb1.max.y > aab.max.y) aabb1.max.y else aab.max.y
}

/**
 * Combines another aabb with this one
 *
 * @param aabb
 */
fun AABB2.combine(aabb: AABB2) {
    min.x =
            if (min.x < aabb.min.x) min.x else aabb.min.x
    min.y =
            if (min.y < aabb.min.y) min.y else aabb.min.y
    max.x =
            if (max.x > aabb.max.x) max.x else aabb.max.x
    max.y =
            if (max.y > aabb.max.y) max.y else aabb.max.y
}

/**
 * From Real-time Collision Detection, p179.
 *
 * @param output
 * @param input
 */
fun AABB2.raycast(
    output: RayCastOutput,
    input: RaycastInput,
    argPool: IWorldPool = DefaultWorldPool(4)
): Boolean {
    var tmin = -Double.MAX_VALUE
    var tmax = Double.MAX_VALUE

    val p = argPool.popMutableVector2d()
    val d = argPool.popMutableVector2d()
    val absD = argPool.popMutableVector2d()
    val normal = argPool.popMutableVector2d()

    p.set(input.p1)
    d.set(input.p2)
    d.subtract(input.p1)
    absD.setXY(abs(d.x), abs(d.y))

    // x then y
    if (absD.x < Settings.EPSILON) {
        // Parallel.
        if (p.x < min.x || max.x < p.x) {
            argPool.pushMutableVector2d(4)
            return false
        }
    } else {
        val invD = 1.0 / d.x
        var t1 = (min.x - p.x) * invD
        var t2 = (max.x - p.x) * invD

        // Sign of the normal vector.
        var s = -1.0

        if (t1 > t2) {
            val temp = t1
            t1 = t2
            t2 = temp
            s = 1.0
        }

        // Push the min up
        if (t1 > tmin) {
            normal.setXY(0.0, 0.0)
            normal.x = s
            tmin = t1
        }

        // Pull the max down
        tmax = min(tmax, t2)

        if (tmin > tmax) {
            argPool.pushMutableVector2d(4)
            return false
        }
    }

    if (absD.y < Settings.EPSILON) {
        // Parallel.
        if (p.y < min.y || max.y < p.y) {
            argPool.pushMutableVector2d(4)
            return false
        }
    } else {
        val invD = 1.0 / d.y
        var t1 = (min.y - p.y) * invD
        var t2 = (max.y - p.y) * invD

        // Sign of the normal vector.
        var s = -1.0

        if (t1 > t2) {
            val temp = t1
            t1 = t2
            t2 = temp
            s = 1.0
        }

        // Push the min up
        if (t1 > tmin) {
            normal.setXY(0.0, 0.0)
            normal.y = s
            tmin = t1
        }

        // Pull the max down
        tmax = min(tmax, t2)

        if (tmin > tmax) {
            argPool.pushMutableVector2d(4)
            return false
        }
    }

    // Does the ray start inside the box?
    // Does the ray intersect beyond the max fraction?
    if (tmin < 0.0 || input.maxFraction < tmin) {
        argPool.pushMutableVector2d(4)
        return false
    }

    // Intersection.
    output.fraction = tmin
    output.normal.x = normal.x
    output.normal.y = normal.y
    argPool.pushMutableVector2d(4)
    return true
}

