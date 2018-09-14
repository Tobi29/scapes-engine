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
package org.jbox2d.dynamics.joints

import org.jbox2d.common.Settings
import org.jbox2d.common.cross
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.SolverData
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.contacts.Position
import org.tobi29.math.vector.*
import kotlin.math.sqrt

class ConstantVolumeJoint(
    private val world: World,
    def: ConstantVolumeJointDef
) : Joint(
    world.pool,
    def
) {

    val bodies: Array<Body>
    private val targetLengths: DoubleArray
    private var targetVolume: Double = 0.0

    private val normals: Array<MutableVector2d>
    private var m_impulse = 0.0

    var joints: Array<DistanceJoint>? = null
        private set

    private val bodyArea: Double
        get() {
            var area = 0.0
            for (i in bodies.indices) {
                val next = if (i == bodies.size - 1) 0 else i + 1
                area += bodies[i].worldCenter.x * bodies[next].worldCenter.y - bodies[next].worldCenter.x * bodies[i].worldCenter.y
            }
            area *= .5
            return area
        }

    fun inflate(factor: Double) {
        targetVolume *= factor
    }

    init {
        if (def.bodies.size <= 2) {
            throw IllegalArgumentException(
                "You cannot create a constant volume joint with less than three bodies."
            )
        }
        bodies = def.bodies.toTypedArray()

        targetLengths = DoubleArray(bodies.size)
        for (i in targetLengths.indices) {
            val next = if (i == targetLengths.size - 1) 0 else i + 1
            val dist = bodies[i].worldCenter distance bodies[next].worldCenter
            targetLengths[i] = dist
        }
        targetVolume = bodyArea

        if (def.joints != null && def.joints!!.size != def.bodies.size) {
            throw IllegalArgumentException(
                "Incorrect joint definition.  Joints have to correspond to the bodies"
            )
        }
        if (def.joints == null) {
            val djd = DistanceJointDef()
            joints = Array(bodies.size) {
                val next = if (it == targetLengths.size - 1) 0 else it + 1
                djd.frequencyHz = def.frequencyHz // 20.0;
                djd.dampingRatio = def.dampingRatio // 50.0;
                djd.collideConnected = def.collideConnected
                djd.initialize(
                    bodies[it], bodies[next],
                    bodies[it].worldCenter,
                    bodies[next].worldCenter
                )
                world.createJoint(djd)
            }
        } else {
            joints = def.joints!!.toTypedArray()
        }

        normals = Array(bodies.size) { MutableVector2d() }
    }

    override fun destructor() {
        for (i in joints!!.indices) {
            world.destroyJoint(joints!![i])
        }
    }

    private fun getSolverArea(positions: Array<Position>): Double {
        var area = 0.0
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) 0 else i + 1
            area += positions[bodies[i].m_islandIndex].c.x * positions[bodies[next].m_islandIndex].c.y - positions[bodies[next].m_islandIndex].c.x * positions[bodies[i].m_islandIndex].c.y
        }
        area *= .5
        return area
    }

    private fun constrainEdges(positions: Array<Position>): Boolean {
        var perimeter = 0.0
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) 0 else i + 1
            val dx =
                positions[bodies[next].m_islandIndex].c.x - positions[bodies[i].m_islandIndex].c.x
            val dy =
                positions[bodies[next].m_islandIndex].c.y - positions[bodies[i].m_islandIndex].c.y
            var dist = sqrt(dx * dx + dy * dy)
            if (dist < Settings.EPSILON) {
                dist = 1.0
            }
            normals[i].x = dy / dist
            normals[i].y = -dx / dist
            perimeter += dist
        }

        val delta = pool.popMutableVector2d()

        val deltaArea = targetVolume - getSolverArea(positions)
        val toExtrude = 0.5 * deltaArea / perimeter // *relaxationFactor
        // float sumdeltax = 0.0;
        var done = true
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) 0 else i + 1
            delta.setXY(
                toExtrude * (normals[i].x + normals[next].x),
                toExtrude * (normals[i].y + normals[next].y)
            )
            // sumdeltax += dx;
            val normSqrd = delta.lengthSqr()
            if (normSqrd > Settings.maxLinearCorrection * Settings.maxLinearCorrection) {
                delta.multiply(Settings.maxLinearCorrection / sqrt(normSqrd))
            }
            if (normSqrd > Settings.linearSlop * Settings.linearSlop) {
                done = false
            }
            positions[bodies[next].m_islandIndex].c.x += delta.x
            positions[bodies[next].m_islandIndex].c.y += delta.y
            // bodies[next].m_linearVelocity.x += delta.x * step.inv_dt;
            // bodies[next].m_linearVelocity.y += delta.y * step.inv_dt;
        }

        pool.pushMutableVector2d(1)
        // System.out.println(sumdeltax);
        return done
    }

    override fun initVelocityConstraints(step: SolverData) {
        val velocities = step.velocities
        val positions = step.positions
        val d = pool.getMutableVector2dArray(bodies.size)

        for (i in bodies.indices) {
            val prev = if (i == 0) bodies.size - 1 else i - 1
            val next = if (i == bodies.size - 1) 0 else i + 1
            d[i].set(positions[bodies[next].m_islandIndex].c)
            d[i].subtract(positions[bodies[prev].m_islandIndex].c)
        }

        if (step.step.warmStarting) {
            m_impulse *= step.step.dtRatio
            // float lambda = -2.0 * crossMassSum / dotMassSum;
            // System.out.println(crossMassSum + " " +dotMassSum);
            // lambda = MathUtils.clamp(lambda, -Settings.maxLinearCorrection,
            // Settings.maxLinearCorrection);
            // m_impulse = lambda;
            for (i in bodies.indices) {
                velocities[bodies[i].m_islandIndex].v.x += bodies[i].m_invMass * d[i].y * .5 * m_impulse
                velocities[bodies[i].m_islandIndex].v.y += bodies[i].m_invMass * -d[i].x * .5 * m_impulse
            }
        } else {
            m_impulse = 0.0
        }
    }

    override fun solvePositionConstraints(step: SolverData): Boolean {
        return constrainEdges(step.positions)
    }

    override fun solveVelocityConstraints(step: SolverData) {
        var crossMassSum = 0.0
        var dotMassSum = 0.0

        val velocities = step.velocities
        val positions = step.positions
        val d = pool.getMutableVector2dArray(bodies.size)

        for (i in bodies.indices) {
            val prev = if (i == 0) bodies.size - 1 else i - 1
            val next = if (i == bodies.size - 1) 0 else i + 1
            d[i].set(positions[bodies[next].m_islandIndex].c)
            d[i].subtract(positions[bodies[prev].m_islandIndex].c)
            dotMassSum += d[i].lengthSqr() / bodies[i].mass
            crossMassSum += (velocities[bodies[i].m_islandIndex].v cross d[i]
                    )
        }
        val lambda = -2.0 * crossMassSum / dotMassSum
        // System.out.println(crossMassSum + " " +dotMassSum);
        // lambda = MathUtils.clamp(lambda, -Settings.maxLinearCorrection,
        // Settings.maxLinearCorrection);
        m_impulse += lambda
        // System.out.println(m_impulse);
        for (i in bodies.indices) {
            velocities[bodies[i].m_islandIndex].v.x += bodies[i].m_invMass * d[i].y * .5 * lambda
            velocities[bodies[i].m_islandIndex].v.y += bodies[i].m_invMass * -d[i].x * .5 * lambda
        }
    }

    /** No-op  */
    override fun getAnchorA(argOut: MutableVector2d) {}

    /** No-op  */
    override fun getAnchorB(argOut: MutableVector2d) {}

    /** No-op  */
    override fun getReactionForce(
        inv_dt: Double,
        argOut: MutableVector2d
    ) {
    }

    /** No-op  */
    override fun getReactionTorque(inv_dt: Double): Double {
        return 0.0
    }
}
