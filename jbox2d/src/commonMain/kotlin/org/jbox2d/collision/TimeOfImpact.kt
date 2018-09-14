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

import org.jbox2d.collision.Distance.DistanceProxy
import org.jbox2d.collision.Distance.SimplexCache
import org.jbox2d.common.*
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.vector.*
import org.tobi29.stdex.ThreadLocal
import org.tobi29.stdex.assert
import kotlin.math.abs
import kotlin.math.max

/**
 * Class used for computing the time of impact. This class should not be constructed usually, just
 * retrieve from the [IWorldPool.getTimeOfImpact].
 *
 * @author daniel
 */
class TimeOfImpact(private val pool: IWorldPool) {


    // djm pooling
    private val cache = SimplexCache()
    private val distanceInput = DistanceInput()
    private val xfA = Transform()
    private val xfB = Transform()
    private val distanceOutput = DistanceOutput()
    private val fcn = SeparationFunction()
    private val indexes = IntArray(2)
    private val sweepA = Sweep()
    private val sweepB = Sweep()

    /**
     * Input parameters for TOI
     *
     * @author Daniel Murphy
     */
    class TOIInput {
        val proxyA = DistanceProxy()
        val proxyB = DistanceProxy()
        val sweepA = Sweep()
        val sweepB = Sweep()
        /**
         * defines sweep interval [0, tMax]
         */
        var tMax: Double = 0.0
    }

    enum class TOIOutputState {
        UNKNOWN, FAILED, OVERLAPPED, TOUCHING, SEPARATED
    }

    /**
     * Output parameters for TimeOfImpact
     *
     * @author daniel
     */
    class TOIOutput {
        var state: TOIOutputState? = null
        var t: Double = 0.0
    }

    /**
     * Compute the upper bound on time before two shapes penetrate. Time is represented as a fraction
     * between [0,tMax]. This uses a swept separating axis and may miss some intermediate,
     * non-tunneling collision. If you change the time interval, you should call this function again.
     * Note: use Distance to compute the contact point and normal at the time of impact.
     *
     * @param output
     * @param input
     */
    fun timeOfImpact(
        output: TOIOutput,
        input: TOIInput
    ) {
        val stats = stats
        // CCD via the local separating axis method. This seeks progression
        // by computing the largest time at which separation is maintained.

        ++stats.toiCalls

        output.state = TOIOutputState.UNKNOWN
        output.t = input.tMax

        val proxyA = input.proxyA
        val proxyB = input.proxyB

        sweepA.set(input.sweepA)
        sweepB.set(input.sweepB)

        // Large rotations can make the root finder fail, so we normalize the
        // sweep angles.
        sweepA.normalize()
        sweepB.normalize()

        val tMax = input.tMax

        val totalRadius = proxyA.m_radius + proxyB.m_radius
        // djm: whats with all these constants?
        val target = max(
            Settings.linearSlop,
            totalRadius - 3.0 * Settings.linearSlop
        )
        val tolerance = 0.25 * Settings.linearSlop

        assert { target > tolerance }

        var t1 = 0.0
        var iter = 0

        cache.count = 0
        distanceInput.proxyA = input.proxyA
        distanceInput.proxyB = input.proxyB
        distanceInput.useRadii = false

        // The outer loop progressively attempts to compute new separating axes.
        // This loop terminates when an axis is repeated (no progress is made).
        while (true) {
            sweepA.getTransform(xfA, t1)
            sweepB.getTransform(xfB, t1)
            // System.out.printf("sweepA: %f, %f, sweepB: %f, %f\n",
            // sweepA.c.x, sweepA.c.y, sweepB.c.x, sweepB.c.y);
            // Get the distance between shapes. We can also use the results
            // to get a separating axis
            distanceInput.transformA = xfA
            distanceInput.transformB = xfB
            pool.distance.distance(distanceOutput, cache, distanceInput)

            // System.out.printf("Dist: %f at points %f, %f and %f, %f.  %d iterations\n",
            // distanceOutput.distance, distanceOutput.pointA.x, distanceOutput.pointA.y,
            // distanceOutput.pointB.x, distanceOutput.pointB.y,
            // distanceOutput.iterations);

            // If the shapes are overlapped, we give up on continuous collision.
            if (distanceOutput.distance <= 0.0) {
                // Failure!
                output.state = TOIOutputState.OVERLAPPED
                output.t = 0.0
                break
            }

            if (distanceOutput.distance < target + tolerance) {
                // Victory!
                output.state = TOIOutputState.TOUCHING
                output.t = t1
                break
            }

            // Initialize the separating axis.
            fcn.initialize(cache, proxyA, sweepA, proxyB, sweepB, t1)

            // Compute the TOI on the separating axis. We do this by successively
            // resolving the deepest point. This loop is bounded by the number of
            // vertices.
            var done = false
            var t2 = tMax
            var pushBackIter = 0
            while (true) {

                // Find the deepest point at t2. Store the witness point indices.
                var s2 = fcn.findMinSeparation(indexes, t2)
                // System.out.printf("s2: %f\n", s2);
                // Is the final configuration separated?
                if (s2 > target + tolerance) {
                    // Victory!
                    output.state = TOIOutputState.SEPARATED
                    output.t = tMax
                    done = true
                    break
                }

                // Has the separation reached tolerance?
                if (s2 > target - tolerance) {
                    // Advance the sweeps
                    t1 = t2
                    break
                }

                // Compute the initial separation of the witness points.
                var s1 = fcn.evaluate(indexes[0], indexes[1], t1)
                // Check for initial overlap. This might happen if the root finder
                // runs out of iterations.
                // System.out.printf("s1: %f, target: %f, tolerance: %f\n", s1, target,
                // tolerance);
                if (s1 < target - tolerance) {
                    output.state = TOIOutputState.FAILED
                    output.t = t1
                    done = true
                    break
                }

                // Check for touching
                if (s1 <= target + tolerance) {
                    // Victory! t1 should hold the TOI (could be 0.0).
                    output.state = TOIOutputState.TOUCHING
                    output.t = t1
                    done = true
                    break
                }

                // Compute 1D root of: f(x) - target = 0
                var rootIterCount = 0
                var a1 = t1
                var a2 = t2
                while (true) {
                    // Use a mix of the secant rule and bisection.
                    val t: Double = if (rootIterCount and 1 == 1) {
                        // Secant rule to improve convergence.
                        a1 + (target - s1) * (a2 - a1) / (s2 - s1)
                    } else {
                        // Bisection to guarantee progress.
                        0.5 * (a1 + a2)
                    }

                    ++rootIterCount
                    ++stats.toiRootIters

                    val s = fcn.evaluate(indexes[0], indexes[1], t)

                    if (abs(s - target) < tolerance) {
                        // t2 holds a tentative value for t1
                        t2 = t
                        break
                    }

                    // Ensure we continue to bracket the root.
                    if (s > target) {
                        a1 = t
                        s1 = s
                    } else {
                        a2 = t
                        s2 = s
                    }

                    if (rootIterCount == MAX_ROOT_ITERATIONS) {
                        break
                    }
                }

                stats.toiMaxRootIters =
                        max(stats.toiMaxRootIters, rootIterCount)

                ++pushBackIter

                if (pushBackIter == Settings.maxPolygonVertices || rootIterCount == MAX_ROOT_ITERATIONS) {
                    break
                }
            }

            ++iter
            ++stats.toiIters

            if (done) {
                // System.out.println("done");
                break
            }

            if (iter == MAX_ITERATIONS) {
                // System.out.println("failed, root finder stuck");
                // Root finder got stuck. Semi-victory.
                output.state = TOIOutputState.FAILED
                output.t = t1
                break
            }
        }

        // System.out.printf("final sweeps: %f, %f, %f; %f, %f, %f", input.s)
        stats.toiMaxIters = max(stats.toiMaxIters, iter)
    }

    companion object {
        const val MAX_ITERATIONS = 20
        const val MAX_ROOT_ITERATIONS = 50

        val stats by ThreadLocal { ToiStats() }
    }

    class ToiStats {
        var toiCalls = 0
        var toiIters = 0
        var toiMaxIters = 0
        var toiRootIters = 0
        var toiMaxRootIters = 0
    }
}


internal enum class Type {
    POINTS, FACE_A, FACE_B
}


internal class SeparationFunction {

    private lateinit var m_proxyA: DistanceProxy
    private lateinit var m_proxyB: DistanceProxy
    private lateinit var m_type: Type
    private val m_localPoint = MutableVector2d()
    private val m_axis = MutableVector2d()
    private lateinit var m_sweepA: Sweep
    private lateinit var m_sweepB: Sweep

    // djm pooling
    private val localPointA = MutableVector2d()
    private val localPointB = MutableVector2d()
    private val pointA = MutableVector2d()
    private val pointB = MutableVector2d()
    private val localPointA1 = MutableVector2d()
    private val localPointA2 = MutableVector2d()
    private val normal = MutableVector2d()
    private val localPointB1 = MutableVector2d()
    private val localPointB2 = MutableVector2d()
    private val temp = MutableVector2d()
    private val xfa = Transform()
    private val xfb = Transform()

    private val axisA = MutableVector2d()
    private val axisB = MutableVector2d()

    // TODO_ERIN might not need to return the separation

    fun initialize(
        cache: SimplexCache,
        proxyA: DistanceProxy,
        sweepA: Sweep,
        proxyB: DistanceProxy,
        sweepB: Sweep,
        t1: Double
    ): Double {
        m_proxyA = proxyA
        m_proxyB = proxyB
        val count = cache.count
        assert { count in 1..2 }

        m_sweepA = sweepA
        m_sweepB = sweepB

        m_sweepA.getTransform(xfa, t1)
        m_sweepB.getTransform(xfb, t1)

        // log.debug("initializing separation.\n" +
        // "cache: "+cache.count+"-"+cache.metric+"-"+cache.indexA+"-"+cache.indexB+"\n"
        // "distance: "+proxyA.

        if (count == 1) {
            m_type = Type.POINTS
            /*
       * Vec2 localPointA = m_proxyA.GetVertex(cache.indexA[0]); Vec2 localPointB =
       * m_proxyB.GetVertex(cache.indexB[0]); Vec2 pointA = Mul(transformA, localPointA); Vec2
       * pointB = Mul(transformB, localPointB); m_axis = pointB - pointA; m_axis.Normalize();
       */
            localPointA.set(m_proxyA.getVertex(cache.indexA[0]))
            localPointB.set(m_proxyB.getVertex(cache.indexB[0]))
            Transform.mulToOut(xfa, localPointA, pointA)
            Transform.mulToOut(xfb, localPointB, pointB)
            m_axis.set(pointB).subtract(pointA)
            return m_axis.length().also {
                if (it > Settings.EPSILON) m_axis.multiply(1.0 / it)
            }
        } else if (cache.indexA[0] == cache.indexA[1]) {
            // Two points on B and one on A.
            m_type = Type.FACE_B

            localPointB1.set(m_proxyB.getVertex(cache.indexB[0]))
            localPointB2.set(m_proxyB.getVertex(cache.indexB[1]))

            temp.set(localPointB2).subtract(localPointB1)
            m_axis.set(temp.now() cross 1.0).normalizeSafe()

            Rot.mulToOut(xfb.q, m_axis, normal)

            m_localPoint.set(localPointB1)
            m_localPoint.add(localPointB2)
            m_localPoint.multiply(.5)
            Transform.mulToOut(xfb, m_localPoint, pointB)

            localPointA.set(proxyA.getVertex(cache.indexA[0]))
            Transform.mulToOut(xfa, localPointA, pointA)

            temp.set(pointA).subtract(pointB)
            var s = (temp dot normal)
            if (s < 0.0) {
                m_axis.negate()
                s = -s
            }
            return s
        } else {
            // Two points on A and one or two points on B.
            m_type = Type.FACE_A

            localPointA1.set(m_proxyA.getVertex(cache.indexA[0]))
            localPointA2.set(m_proxyA.getVertex(cache.indexA[1]))

            temp.set(localPointA2).subtract(localPointA1)
            m_axis.set(temp cross 1.0).normalizeSafe()

            Rot.mulToOut(xfa.q, m_axis, normal)

            m_localPoint.set(localPointA1)
            m_localPoint.add(localPointA2)
            m_localPoint.multiply(.5)
            Transform.mulToOut(xfa, m_localPoint, pointA)

            localPointB.set(m_proxyB.getVertex(cache.indexB[0]))
            Transform.mulToOut(xfb, localPointB, pointB)

            temp.set(pointB).subtract(pointA)
            var s = (temp dot normal)
            if (s < 0.0) {
                m_axis.negate()
                s = -s
            }
            return s
        }
    }

    // float FindMinSeparation(int* indexA, int* indexB, float t) const
    fun findMinSeparation(
        indexes: IntArray,
        t: Double
    ): Double {

        m_sweepA.getTransform(xfa, t)
        m_sweepB.getTransform(xfb, t)

        when (m_type) {
            Type.POINTS -> {
                Rot.mulTrans(xfa.q, m_axis, axisA)
                m_axis.negate()
                Rot.mulTrans(xfb.q, m_axis, axisB)
                m_axis.negate()

                indexes[0] = m_proxyA.getSupport(axisA.now())
                indexes[1] = m_proxyB.getSupport(axisB.now())

                localPointA.set(m_proxyA.getVertex(indexes[0]))
                localPointB.set(m_proxyB.getVertex(indexes[1]))

                Transform.mulToOut(xfa, localPointA, pointA)
                Transform.mulToOut(xfb, localPointB, pointB)

                pointB.subtract(pointA)
                return pointB dot m_axis
            }
            Type.FACE_A -> {
                Rot.mulToOut(xfa.q, m_axis, normal)
                Transform.mulToOut(xfa, m_localPoint, pointA)

                normal.negate()
                Rot.mulTrans(xfb.q, normal, axisB)
                normal.negate()

                indexes[0] = -1
                indexes[1] = m_proxyB.getSupport(axisB.now())

                localPointB.set(m_proxyB.getVertex(indexes[1]))
                Transform.mulToOut(xfb, localPointB, pointB)
                pointB.subtract(pointA)
                return pointB dot normal
            }
            Type.FACE_B -> {
                Rot.mulToOut(xfb.q, m_axis, normal)
                Transform.mulToOut(xfb, m_localPoint, pointB)

                normal.negate()
                Rot.mulTrans(xfa.q, normal, axisA)
                normal.negate()

                indexes[1] = -1
                indexes[0] = m_proxyA.getSupport(axisA.now())

                localPointA.set(m_proxyA.getVertex(indexes[0]))
                Transform.mulToOut(xfa, localPointA, pointA)
                pointA.subtract(pointB)
                return pointA dot normal
            }
            else -> {
                assert { false }
                indexes[0] = -1
                indexes[1] = -1
                return 0.0
            }
        }
    }

    fun evaluate(
        indexA: Int,
        indexB: Int,
        t: Double
    ): Double {
        m_sweepA.getTransform(xfa, t)
        m_sweepB.getTransform(xfb, t)

        when (m_type) {
            Type.POINTS -> {
                localPointA.set(m_proxyA.getVertex(indexA))
                localPointB.set(m_proxyB.getVertex(indexB))

                Transform.mulToOut(xfa, localPointA, pointA)
                Transform.mulToOut(xfb, localPointB, pointB)

                pointB.subtract(pointA)
                return pointB dot m_axis
            }
            Type.FACE_A -> {
                Rot.mulToOut(xfa.q, m_axis, normal)
                Transform.mulToOut(xfa, m_localPoint, pointA)

                localPointB.set(m_proxyB.getVertex(indexB))
                Transform.mulToOut(xfb, localPointB, pointB)

                pointB.subtract(pointA)
                return pointB dot normal
            }
            Type.FACE_B -> {
                Rot.mulToOut(xfb.q, m_axis, normal)
                Transform.mulToOut(xfb, m_localPoint, pointB)

                localPointA.set(m_proxyA.getVertex(indexA))
                Transform.mulToOut(xfa, localPointA, pointA)

                pointA.subtract(pointB)
                return pointA dot normal
            }
            else -> {
                assert { false }
                return 0.0
            }
        }
    }
}
