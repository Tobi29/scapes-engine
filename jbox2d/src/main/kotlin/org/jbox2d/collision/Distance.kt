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

import org.jbox2d.collision.shapes.*
import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.Transform
import org.jbox2d.common.cross
import org.tobi29.math.vector.*
import org.tobi29.stdex.ThreadLocal
import org.tobi29.stdex.assert
import org.tobi29.stdex.copy
import kotlin.math.max

// updated to rev 100
/**
 * This is non-static for faster pooling. To get an instance, use the [SingletonPool], don't
 * construct a distance object.
 *
 * @author Daniel Murphy
 */
class Distance {

    private val simplex = Simplex()
    private val saveA = IntArray(3)
    private val saveB = IntArray(3)
    private val closestPoint = MutableVector2d()
    private val d = MutableVector2d()
    private val temp = MutableVector2d()
    private val normal = MutableVector2d()

    /**
     * GJK using Voronoi regions (Christer Ericson) and Barycentric coordinates.
     */
    private inner class SimplexVertex {
        val wA = MutableVector2d() // support point in shapeA
        val wB = MutableVector2d() // support point in shapeB
        val w = MutableVector2d() // wB - wA
        var a: Double = 0.0 // barycentric coordinate for closest point
        var indexA: Int = 0 // wA index
        var indexB: Int = 0 // wB index

        fun set(sv: SimplexVertex) {
            wA.set(sv.wA)
            wB.set(sv.wB)
            w.set(sv.w)
            a = sv.a
            indexA = sv.indexA
            indexB = sv.indexB
        }
    }

    /**
     * Used to warm start Distance. Set count to zero on first call.
     *
     * @author daniel
     */
    class SimplexCache {
        /** length or area  */
        var metric: Double = 0.0
        var count: Int = 0
        /** vertices on shape A  */
        val indexA = IntArray(3)
        /** vertices on shape B  */
        val indexB = IntArray(3)

        init {
            metric = 0.0
            count = 0
            indexA[0] = Int.MAX_VALUE
            indexA[1] = Int.MAX_VALUE
            indexA[2] = Int.MAX_VALUE
            indexB[0] = Int.MAX_VALUE
            indexB[1] = Int.MAX_VALUE
            indexB[2] = Int.MAX_VALUE
        }

        fun set(sc: SimplexCache) {
            copy(sc.indexA, indexA)
            copy(sc.indexB, indexB)
            metric = sc.metric
            count = sc.count
        }
    }

    private inner class Simplex {
        val m_v1 = SimplexVertex()
        val m_v2 = SimplexVertex()
        val m_v3 = SimplexVertex()
        val vertices = arrayOf(m_v1, m_v2, m_v3)
        var m_count: Int = 0

        private val e12 = MutableVector2d()

        // djm pooled
        private val case2 = MutableVector2d()
        private val case22 = MutableVector2d()

        // djm pooled, and from above
        private val case3 = MutableVector2d()
        private val case33 = MutableVector2d()

        // djm pooled, from above
        // return Vec2.cross(m_v2.w - m_v1.w, m_v3.w - m_v1.w);
        val metric: Double
            get() {
                when (m_count) {
                    0 -> {
                        assert { false }
                        return 0.0
                    }

                    1 -> return 0.0

                    2 -> return m_v1.w distance m_v2.w

                    3 -> {
                        case3.set(m_v2.w).subtract(m_v1.w)
                        case33.set(m_v3.w).subtract(m_v1.w)
                        return (case3 cross case33)
                    }

                    else -> {
                        assert { false }
                        return 0.0
                    }
                }
            }

        // djm pooled, and from above
        private val e13 = MutableVector2d()
        private val e23 = MutableVector2d()
        private val w1 = MutableVector2d()
        private val w2 = MutableVector2d()
        private val w3 = MutableVector2d()

        fun readCache(
            cache: SimplexCache,
            proxyA: DistanceProxy,
            transformA: Transform,
            proxyB: DistanceProxy,
            transformB: Transform
        ) {
            assert { cache.count <= 3 }

            // Copy data from cache.
            m_count = cache.count

            for (i in 0 until m_count) {
                val v = vertices[i]
                v.indexA = cache.indexA[i]
                v.indexB = cache.indexB[i]
                val wALocal = proxyA.getVertex(v.indexA)
                val wBLocal = proxyB.getVertex(v.indexB)
                Transform.mulToOut(transformA, wALocal, v.wA)
                Transform.mulToOut(transformB, wBLocal, v.wB)
                v.w.set(v.wB).subtract(v.wA)
                v.a = 0.0
            }

            // Compute the new simplex metric, if it is substantially different than
            // old metric then flush the simplex.
            if (m_count > 1) {
                val metric1 = cache.metric
                val metric2 = metric
                if (metric2 < 0.5 * metric1 || 2.0 * metric1 < metric2 || metric2 < Settings.EPSILON) {
                    // Reset the simplex.
                    m_count = 0
                }
            }

            // If the cache is empty or invalid ...
            if (m_count == 0) {
                val v = vertices[0]
                v.indexA = 0
                v.indexB = 0
                val wALocal = proxyA.getVertex(0)
                val wBLocal = proxyB.getVertex(0)
                Transform.mulToOut(transformA, wALocal, v.wA)
                Transform.mulToOut(transformB, wBLocal, v.wB)
                v.w.set(v.wB).subtract(v.wA)
                m_count = 1
            }
        }

        fun writeCache(cache: SimplexCache) {
            cache.metric = metric
            cache.count = m_count

            for (i in 0 until m_count) {
                cache.indexA[i] = vertices[i].indexA
                cache.indexB[i] = vertices[i].indexB
            }
        }

        fun getSearchDirection(out: MutableVector2d) {
            when (m_count) {
                1 -> {
                    out.set(m_v1.w).negate()
                    return
                }
                2 -> {
                    e12.set(m_v2.w).subtract(m_v1.w)
                    // use out for a temp variable real quick
                    out.set(m_v1.w).negate()
                    val sgn = e12 cross out

                    if (sgn > 0.0) {
                        // Origin is left of e12.
                        out.set(1.0 cross e12)
                        return
                    } else {
                        // Origin is right of e12.
                        out.set(e12 cross 1.0)
                        return
                    }
                }
                else -> {
                    assert { false }
                    out.setXY(0.0, 0.0)
                    return
                }
            }
        }

        /**
         * this returns pooled objects. don't keep or modify them
         *
         * @return
         */
        fun getClosestPoint(out: MutableVector2d) {
            when (m_count) {
                0 -> {
                    assert { false }
                    out.setXY(0.0, 0.0)
                    return
                }
                1 -> {
                    out.set(m_v1.w)
                    return
                }
                2 -> {
                    case22.set(m_v2.w)
                    case22.multiply(m_v2.a)
                    case2.set(m_v1.w)
                    case2.multiply(m_v1.a)
                    case2.add(case22)
                    out.set(case2)
                    return
                }
                3 -> {
                    out.setXY(0.0, 0.0)
                    return
                }
                else -> {
                    assert { false }
                    out.setXY(0.0, 0.0)
                    return
                }
            }
        }

        fun getWitnessPoints(
            pA: MutableVector2d,
            pB: MutableVector2d
        ) {
            when (m_count) {
                0 -> assert { false }

                1 -> {
                    pA.set(m_v1.wA)
                    pB.set(m_v1.wB)
                }

                2 -> {
                    case2.set(m_v1.wA).multiply(m_v1.a)
                    pA.set(m_v2.wA)
                    pA.multiply(m_v2.a)
                    pA.add(case2)
                    // m_v1.a * m_v1.wA + m_v2.a * m_v2.wA;
                    // *pB = m_v1.a * m_v1.wB + m_v2.a * m_v2.wB;
                    case2.set(m_v1.wB).multiply(m_v1.a)
                    pB.set(m_v2.wB)
                    pB.multiply(m_v2.a)
                    pB.add(case2)
                }

                3 -> {
                    pA.set(m_v1.wA).multiply(m_v1.a)
                    case3.set(m_v2.wA).multiply(m_v2.a)
                    case33.set(m_v3.wA).multiply(m_v3.a)
                    pA.add(case3)
                    pA.add(case33)
                    pB.set(pA)
                }

                else -> assert { false }
            } // *pA = m_v1.a * m_v1.wA + m_v2.a * m_v2.wA + m_v3.a * m_v3.wA;
            // *pB = *pA;
        }

        // djm pooled from above
        /**
         * Solve a line segment using barycentric coordinates.
         */
        fun solve2() {
            // Solve a line segment using barycentric coordinates.
            //
            // p = a1 * w1 + a2 * w2
            // a1 + a2 = 1
            //
            // The vector from the origin to the closest point on the line is
            // perpendicular to the line.
            // e12 = w2 - w1
            // dot(p, e) = 0
            // a1 * dot(w1, e) + a2 * dot(w2, e) = 0
            //
            // 2-by-2 linear system
            // [1 1 ][a1] = [1]
            // [w1.e12 w2.e12][a2] = [0]
            //
            // Define
            // d12_1 = dot(w2, e12)
            // d12_2 = -dot(w1, e12)
            // d12 = d12_1 + d12_2
            //
            // Solution
            // a1 = d12_1 / d12
            // a2 = d12_2 / d12
            val w1 = m_v1.w
            val w2 = m_v2.w
            e12.set(w2).subtract(w1)

            // w1 region
            val d12_2 = -(w1 dot e12)
            if (d12_2 <= 0.0) {
                // a2 <= 0, so we clamp it to 0
                m_v1.a = 1.0
                m_count = 1
                return
            }

            // w2 region
            val d12_1 = (w2 dot e12)
            if (d12_1 <= 0.0) {
                // a1 <= 0, so we clamp it to 0
                m_v2.a = 1.0
                m_count = 1
                m_v1.set(m_v2)
                return
            }

            // Must be in e12 region.
            val inv_d12 = 1.0 / (d12_1 + d12_2)
            m_v1.a = d12_1 * inv_d12
            m_v2.a = d12_2 * inv_d12
            m_count = 2
        }

        /**
         * Solve a line segment using barycentric coordinates.<br></br>
         * Possible regions:<br></br>
         * - points[2]<br></br>
         * - edge points[0]-points[2]<br></br>
         * - edge points[1]-points[2]<br></br>
         * - inside the triangle
         */
        fun solve3() {
            w1.set(m_v1.w)
            w2.set(m_v2.w)
            w3.set(m_v3.w)

            // Edge12
            // [1 1 ][a1] = [1]
            // [w1.e12 w2.e12][a2] = [0]
            // a3 = 0
            e12.set(w2).subtract(w1)
            val w1e12 = (w1 dot e12)
            val w2e12 = (w2 dot e12)
            val d12_2 = -w1e12

            // Edge13
            // [1 1 ][a1] = [1]
            // [w1.e13 w3.e13][a3] = [0]
            // a2 = 0
            e13.set(w3).subtract(w1)
            val w1e13 = (w1 dot e13)
            val w3e13 = (w3 dot e13)
            val d13_2 = -w1e13

            // Edge23
            // [1 1 ][a2] = [1]
            // [w2.e23 w3.e23][a3] = [0]
            // a1 = 0
            e23.set(w3).subtract(w2)
            val w2e23 = (w2 dot e23)
            val w3e23 = (w3 dot e23)
            val d23_2 = -w2e23

            // Triangle123
            val n123 = (e12 cross e13)

            val d123_1 = n123 * (w2 cross w3)
            val d123_2 = n123 * (w3 cross w1)
            val d123_3 = n123 * (w1 cross w2)

            // w1 region
            if (d12_2 <= 0.0 && d13_2 <= 0.0) {
                m_v1.a = 1.0
                m_count = 1
                return
            }

            // e12
            if (w2e12 > 0.0 && d12_2 > 0.0 && d123_3 <= 0.0) {
                val inv_d12 = 1.0 / (w2e12 + d12_2)
                m_v1.a = w2e12 * inv_d12
                m_v2.a = d12_2 * inv_d12
                m_count = 2
                return
            }

            // e13
            if (w3e13 > 0.0 && d13_2 > 0.0 && d123_2 <= 0.0) {
                val inv_d13 = 1.0 / (w3e13 + d13_2)
                m_v1.a = w3e13 * inv_d13
                m_v3.a = d13_2 * inv_d13
                m_count = 2
                m_v2.set(m_v3)
                return
            }

            // w2 region
            if (w2e12 <= 0.0 && d23_2 <= 0.0) {
                m_v2.a = 1.0
                m_count = 1
                m_v1.set(m_v2)
                return
            }

            // w3 region
            if (w3e13 <= 0.0 && w3e23 <= 0.0) {
                m_v3.a = 1.0
                m_count = 1
                m_v1.set(m_v3)
                return
            }

            // e23
            if (w3e23 > 0.0 && d23_2 > 0.0 && d123_1 <= 0.0) {
                val inv_d23 = 1.0 / (w3e23 + d23_2)
                m_v2.a = w3e23 * inv_d23
                m_v3.a = d23_2 * inv_d23
                m_count = 2
                m_v1.set(m_v3)
                return
            }

            // Must be in triangle123
            val inv_d123 = 1.0 / (d123_1 + d123_2 + d123_3)
            m_v1.a = d123_1 * inv_d123
            m_v2.a = d123_2 * inv_d123
            m_v3.a = d123_3 * inv_d123
            m_count = 3
        }
    }

    /**
     * A distance proxy is used by the GJK algorithm. It encapsulates any shape. TODO: see if we can
     * just do assignments with m_vertices, instead of copying stuff over
     *
     * @author daniel
     */
    class DistanceProxy {
        private val m_vertices: Array<MutableVector2d> = Array(
            Settings.maxPolygonVertices
        ) { MutableVector2d() }
        /**
         * Get the vertex count.
         *
         * @return
         */
        var vertexCount: Int = 0
        var m_radius: Double = 0.0

        init {
            vertexCount = 0
            m_radius = 0.0
        }

        /**
         * Initialize the proxy using the given shape. The shape must remain in scope while the proxy is
         * in use.
         */
        fun set(shape: Shape, index: Int) {
            when (shape.type) {
                ShapeType.CIRCLE -> {
                    val circle = shape as CircleShape
                    m_vertices[0].set(circle.m_p)
                    vertexCount = 1
                    m_radius = circle.m_radius
                }
                ShapeType.POLYGON -> {
                    val poly = shape as PolygonShape
                    vertexCount = poly.m_count
                    m_radius = poly.m_radius
                    for (i in 0 until vertexCount) {
                        m_vertices[i].set(poly.m_vertices[i])
                    }
                }
                ShapeType.CHAIN -> {
                    val chain = shape as ChainShape
                    assert { 0 <= index && index < chain.m_count }

                    val b0 = chain.m_vertices[index]
                    val b1 = if (index + 1 < chain.m_count) {
                        chain.m_vertices[index + 1]
                    } else {
                        chain.m_vertices[0]
                    }

                    m_vertices[0].set(b0)
                    m_vertices[1].set(b1)
                    vertexCount = 2
                    m_radius = chain.m_radius
                }
                ShapeType.EDGE -> {
                    val edge = shape as EdgeShape
                    m_vertices[0].set(edge.m_vertex1)
                    m_vertices[1].set(edge.m_vertex2)
                    vertexCount = 2
                    m_radius = edge.m_radius
                }
                else -> assert { false }
            }
        }

        /**
         * Get the supporting vertex index in the given direction.
         *
         * @param d
         * @return
         */
        fun getSupport(d: Vector2d): Int {
            var bestIndex = 0
            var bestValue = m_vertices[0].now() dot d
            for (i in 1 until vertexCount) {
                val value = m_vertices[i].now() dot d
                if (value > bestValue) {
                    bestIndex = i
                    bestValue = value
                }
            }

            return bestIndex
        }

        /**
         * Get the supporting vertex in the given direction.
         *
         * @param d
         * @return
         */
        fun getSupportVertex(d: Vector2d): Vector2d {
            var bestIndex = 0
            var bestValue = m_vertices[0].now() dot d
            for (i in 1 until vertexCount) {
                val value = m_vertices[i].now() dot d
                if (value > bestValue) {
                    bestIndex = i
                    bestValue = value
                }
            }

            return m_vertices[bestIndex].now()
        }

        /**
         * Get a vertex by index. Used by Distance.
         *
         * @param index
         * @return
         */
        fun getVertex(index: Int): MutableVector2d {
            assert { index in 0 until vertexCount }
            return m_vertices[index]
        }
    }

    /**
     * Compute the closest points between two shapes. Supports any combination of: CircleShape and
     * PolygonShape. The simplex cache is input/output. On the first call set SimplexCache.count to
     * zero.
     *
     * @param output
     * @param cache
     * @param input
     */
    fun distance(
        output: DistanceOutput,
        cache: SimplexCache,
        input: DistanceInput
    ) {
        val stats = stats
        stats.GJK_CALLS++

        val proxyA = input.proxyA
        val proxyB = input.proxyB

        val transformA = input.transformA
        val transformB = input.transformB

        // Initialize the simplex.
        simplex.readCache(cache, proxyA, transformA, proxyB, transformB)

        // Get simplex vertices as an array.
        val vertices = simplex.vertices

        // These store the vertices of the last simplex so that we
        // can check for duplicates and prevent cycling.
        // (pooled above)
        var saveCount = 0

        simplex.getClosestPoint(closestPoint)
        var distanceSqr1 = closestPoint.lengthSqr()
        var distanceSqr2 = distanceSqr1

        // Main iteration loop
        var iter = 0
        while (iter < MAX_ITERS) {

            // Copy simplex so we can identify duplicates.
            saveCount = simplex.m_count
            for (i in 0 until saveCount) {
                saveA[i] = vertices[i].indexA
                saveB[i] = vertices[i].indexB
            }

            when (simplex.m_count) {
                1 -> {
                }
                2 -> simplex.solve2()
                3 -> simplex.solve3()
                else -> assert { false }
            }

            // If we have 3 points, then the origin is in the corresponding triangle.
            if (simplex.m_count == 3) {
                break
            }

            // Compute closest point.
            simplex.getClosestPoint(closestPoint)
            distanceSqr2 = closestPoint.lengthSqr()

            // ensure progress
            if (distanceSqr2 >= distanceSqr1) {
                // break;
            }
            distanceSqr1 = distanceSqr2

            // get search direction;
            simplex.getSearchDirection(d)

            // Ensure the search direction is numerically fit.
            if (d.lengthSqr() < Settings.EPSILON * Settings.EPSILON) {
                // The origin is probably contained by a line segment
                // or triangle. Thus the shapes are overlapped.

                // We can't return zero here even though there may be overlap.
                // In case the simplex is a point, segment, or triangle it is difficult
                // to determine if the origin is contained in the CSO or very close to it.
                break
            }
            /*
       * SimplexVertex* vertex = vertices + simplex.m_count; vertex.indexA =
       * proxyA.GetSupport(MulT(transformA.R, -d)); vertex.wA = Mul(transformA,
       * proxyA.GetVertex(vertex.indexA)); Vec2 wBLocal; vertex.indexB =
       * proxyB.GetSupport(MulT(transformB.R, d)); vertex.wB = Mul(transformB,
       * proxyB.GetVertex(vertex.indexB)); vertex.w = vertex.wB - vertex.wA;
       */

            // Compute a tentative new simplex vertex using support points.
            val vertex = vertices[simplex.m_count]

            d.negate()
            Rot.mulTrans(transformA.q, d, temp)
            vertex.indexA = proxyA.getSupport(temp.now())
            Transform.mulToOut(
                transformA,
                proxyA.getVertex(vertex.indexA), vertex.wA
            )
            // Vec2 wBLocal;
            d.negate()
            Rot.mulTrans(transformB.q, d, temp)
            vertex.indexB = proxyB.getSupport(temp.now())
            Transform.mulToOut(
                transformB,
                proxyB.getVertex(vertex.indexB), vertex.wB
            )
            vertex.w.set(vertex.wB).subtract(vertex.wA)

            // Iteration count is equated to the number of support point calls.
            ++iter
            ++stats.GJK_ITERS

            // Check for duplicate support points. This is the main termination criteria.
            var duplicate = false
            for (i in 0 until saveCount) {
                if (vertex.indexA == saveA[i] && vertex.indexB == saveB[i]) {
                    duplicate = true
                    break
                }
            }

            // If we found a duplicate support point we must exit to avoid cycling.
            if (duplicate) {
                break
            }

            // New vertex is ok and needed.
            ++simplex.m_count
        }

        stats.GJK_MAX_ITERS = max(stats.GJK_MAX_ITERS, iter)

        // Prepare output.
        simplex.getWitnessPoints(output.pointA, output.pointB)
        output.distance = output.pointA distance output.pointB
        output.iterations = iter

        // Cache the simplex.
        simplex.writeCache(cache)

        // Apply radii if requested.
        if (input.useRadii) {
            val rA = proxyA.m_radius
            val rB = proxyB.m_radius

            if (output.distance > rA + rB && output.distance > Settings.EPSILON) {
                // Shapes are still no overlapped.
                // Move the witness points to the outer surface.
                output.distance -= rA + rB
                normal.set(output.pointB)
                normal.subtract(output.pointA)
                normal.normalize()
                temp.set(normal).multiply(rA)
                output.pointA.add(temp)
                temp.set(normal).multiply(rB)
                output.pointB.subtract(temp)
            } else {
                // Shapes are overlapped when radii are considered.
                // Move the witness points to the middle.
                // Vec2 p = 0.5 * (output.pointA + output.pointB);
                output.pointA.add(output.pointB)
                output.pointA.multiply(.5)
                output.pointB.set(output.pointA)
                output.distance = 0.0
            }
        }
    }

    companion object {
        const val MAX_ITERS = 20

        val stats by ThreadLocal { DistanceStats() }
    }

    class DistanceStats {
        var GJK_CALLS = 0
        var GJK_ITERS = 0
        var GJK_MAX_ITERS = 20
    }
}
