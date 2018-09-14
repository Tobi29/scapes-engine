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
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 *
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package org.jbox2d.dynamics.joints

import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.cross
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.vector.*
import org.tobi29.stdex.math.clamp
import kotlin.math.PI
import kotlin.math.abs

//C = norm(p2 - p1) - L
//u = (p2 - p1) / norm(p2 - p1)
//Cdot = dot(u, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//J = [-u -cross(r1, u) u cross(r2, u)]
//K = J * invM * JT
//= invMass1 + invI1 * cross(r1, u)^2 + invMass2 + invI2 * cross(r2, u)^2

/**
 * A distance joint constrains two points on two bodies to remain at a fixed distance from each
 * other. You can view this as a massless, rigid rod.
 */
class DistanceJoint(
    argWorld: IWorldPool,
    def: DistanceJointDef
) : Joint(argWorld, def) {

    var frequency: Double = 0.0
    var dampingRatio: Double = 0.0
    private var m_bias: Double = 0.0

    // Solver shared
    val localAnchorA = MutableVector2d(def.localAnchorA)
    val localAnchorB = MutableVector2d(def.localAnchorB)
    private var m_gamma: Double = 0.0
    private var m_impulse: Double = 0.0
    var length: Double = 0.0

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_u = MutableVector2d()
    private val m_rA = MutableVector2d()
    private val m_rB = MutableVector2d()
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0
    private var m_mass: Double = 0.0

    init {
        length = def.length
        m_impulse = 0.0
        frequency = def.frequencyHz
        dampingRatio = def.dampingRatio
        m_gamma = 0.0
        m_bias = 0.0
    }

    override fun getAnchorA(argOut: MutableVector2d) {
        bodyA.getWorldPointToOut(localAnchorA, argOut)
    }

    override fun getAnchorB(argOut: MutableVector2d) {
        bodyB.getWorldPointToOut(localAnchorB, argOut)
    }

    /**
     * Get the reaction force given the inverse time step. Unit is N.
     */
    override fun getReactionForce(
        inv_dt: Double,
        argOut: MutableVector2d
    ) {
        argOut.x = m_impulse * m_u.x * inv_dt
        argOut.y = m_impulse * m_u.y * inv_dt
    }

    /**
     * Get the reaction torque given the inverse time step. Unit is N*m. This is always zero for a
     * distance joint.
     */
    override fun getReactionTorque(inv_dt: Double): Double {
        return 0.0
    }

    override fun initVelocityConstraints(data: SolverData) {

        m_indexA = bodyA.m_islandIndex
        m_indexB = bodyB.m_islandIndex
        m_localCenterA.set(bodyA.m_sweep.localCenter)
        m_localCenterB.set(bodyB.m_sweep.localCenter)
        m_invMassA = bodyA.m_invMass
        m_invMassB = bodyB.m_invMass
        m_invIA = bodyA.m_invI
        m_invIB = bodyB.m_invI

        val cA = data.positions[m_indexA].c
        val aA = data.positions[m_indexA].a
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w

        val cB = data.positions[m_indexB].c
        val aB = data.positions[m_indexB].a
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        val qA = pool.popRot()
        val qB = pool.popRot()

        qA.set(aA)
        qB.set(aB)

        // use m_u as temporary variable
        m_u.set(localAnchorA)
        m_u.subtract(m_localCenterA)
        Rot.mulToOut(qA, m_u, m_rA)
        m_u.set(localAnchorB)
        m_u.subtract(m_localCenterB)
        Rot.mulToOut(qB, m_u, m_rB)
        m_u.set(cB)
        m_u.add(m_rB)
        m_u.subtract(cA)
        m_u.subtract(m_rA)

        pool.pushRot(2)

        // Handle singularity.
        val length = m_u.length()
        if (length > Settings.linearSlop) {
            m_u.x *= 1.0 / length
            m_u.y *= 1.0 / length
        } else {
            m_u.setXY(0.0, 0.0)
        }


        val crAu = (m_rA cross m_u)
        val crBu = (m_rB cross m_u)
        var invMass =
            m_invMassA + m_invIA * crAu * crAu + m_invMassB + m_invIB * crBu * crBu

        // Compute the effective mass matrix.
        m_mass = if (invMass != 0.0) 1.0 / invMass else 0.0

        if (frequency > 0.0) {
            val C = length - this.length

            // Frequency
            val omega = 2.0 * PI * frequency

            // Damping coefficient
            val d = 2.0 * m_mass * dampingRatio * omega

            // Spring stiffness
            val k = m_mass * omega * omega

            // magic formulas
            val h = data.step.dt
            m_gamma = h * (d + h * k)
            m_gamma = if (m_gamma != 0.0) 1.0 / m_gamma else 0.0
            m_bias = C * h * k * m_gamma

            invMass += m_gamma
            m_mass = if (invMass != 0.0) 1.0 / invMass else 0.0
        } else {
            m_gamma = 0.0
            m_bias = 0.0
        }
        if (data.step.warmStarting) {

            // Scale the impulse to support a variable time step.
            m_impulse *= data.step.dtRatio

            val P = pool.popMutableVector2d()
            P.set(m_u).multiply(m_impulse)

            vA.x -= m_invMassA * P.x
            vA.y -= m_invMassA * P.y
            wA -= m_invIA * (m_rA cross P)

            vB.x += m_invMassB * P.x
            vB.y += m_invMassB * P.y
            wB += m_invIB * (m_rB cross P)

            pool.pushMutableVector2d(1)
        } else {
            m_impulse = 0.0
        }
        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB
    }

    override fun solveVelocityConstraints(data: SolverData) {
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        val vpA = pool.popMutableVector2d()
        val vpB = pool.popMutableVector2d()

        // Cdot = dot(u, v + cross(w, r))
        vpA.set(wA cross m_rA)
        vpA.add(vA)
        vpB.set(wB cross m_rB)
        vpB.add(vB)
        vpB.subtract(vpA)
        val Cdot = (m_u dot vpB)

        val impulse = -m_mass * (Cdot + m_bias + m_gamma * m_impulse)
        m_impulse += impulse


        val Px = impulse * m_u.x
        val Py = impulse * m_u.y

        vA.x -= m_invMassA * Px
        vA.y -= m_invMassA * Py
        wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px)
        vB.x += m_invMassB * Px
        vB.y += m_invMassB * Py
        wB += m_invIB * (m_rB.x * Py - m_rB.y * Px)

        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(2)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        if (frequency > 0.0) {
            return true
        }
        val qA = pool.popRot()
        val qB = pool.popRot()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()
        val u = pool.popMutableVector2d()

        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a

        qA.set(aA)
        qB.set(aB)

        u.set(localAnchorA)
        u.subtract(m_localCenterA)
        Rot.mulToOut(qA, u, rA)
        u.set(localAnchorB)
        u.subtract(m_localCenterB)
        Rot.mulToOut(qB, u, rB)
        u.set(cB)
        u.add(rB)
        u.subtract(cA)
        u.subtract(rA)


        val length = u.length()
        u.normalizeSafe()
        var C = length - this.length
        C = clamp(
            C, -Settings.maxLinearCorrection,
            Settings.maxLinearCorrection
        )

        val impulse = -m_mass * C
        val Px = impulse * u.x
        val Py = impulse * u.y

        cA.x -= m_invMassA * Px
        cA.y -= m_invMassA * Py
        aA -= m_invIA * (rA.x * Py - rA.y * Px)
        cB.x += m_invMassB * Px
        cB.y += m_invMassB * Py
        aB += m_invIB * (rB.x * Py - rB.y * Px)

        //    data.positions[m_indexA].c.set(cA);
        data.positions[m_indexA].a = aA
        //    data.positions[m_indexB].c.set(cB);
        data.positions[m_indexB].a = aB

        pool.pushMutableVector2d(3)
        pool.pushRot(2)

        return abs(C) < Settings.linearSlop
    }
}
