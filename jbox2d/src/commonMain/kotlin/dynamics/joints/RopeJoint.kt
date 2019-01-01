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

import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.cross
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.vector.*
import org.tobi29.stdex.math.clamp
import kotlin.math.min

/**
 * A rope joint enforces a maximum distance between two points on two bodies. It has no other
 * effect. Warning: if you attempt to change the maximum length during the simulation you will get
 * some non-physical behavior. A model that would allow you to dynamically modify the length would
 * have some sponginess, so I chose not to implement it that way. See DistanceJoint if you want to
 * dynamically control length.
 *
 * @author Daniel Murphy
 */
class RopeJoint(
    worldPool: IWorldPool,
    def: RopeJointDef
) : Joint(worldPool, def) {
    // Solver shared
    val localAnchorA = MutableVector2d()
    val localAnchorB = MutableVector2d()
    var maxLength: Double = 0.0
    private var m_length: Double = 0.0
    private var m_impulse: Double = 0.0

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
    var limitState: LimitState? = null
        private set

    init {
        localAnchorA.set(def.localAnchorA)
        localAnchorB.set(def.localAnchorB)

        maxLength = def.maxLength

        m_mass = 0.0
        m_impulse = 0.0
        limitState = LimitState.INACTIVE
        m_length = 0.0
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
        val temp = pool.popMutableVector2d()

        qA.set(aA)
        qB.set(aB)

        // Compute the effective masses.
        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, m_rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, m_rB)

        m_u.set(cB)
        m_u.add(m_rB)
        m_u.subtract(cA)
        m_u.subtract(m_rA)

        m_length = m_u.length()

        val C = m_length - maxLength
        limitState = if (C > 0.0) {
            LimitState.AT_UPPER
        } else {
            LimitState.INACTIVE
        }

        if (m_length > Settings.linearSlop) {
            m_u.multiply(1.0 / m_length)
        } else {
            m_u.setXY(0.0, 0.0)
            m_mass = 0.0
            m_impulse = 0.0
            pool.pushRot(2)
            pool.pushMutableVector2d(1)
            return
        }

        // Compute effective mass.
        val crA = (m_rA cross m_u)
        val crB = (m_rB cross m_u)
        val invMass =
            m_invMassA + m_invIA * crA * crA + m_invMassB + m_invIB * crB * crB

        m_mass = if (invMass != 0.0) 1.0 / invMass else 0.0

        if (data.step.warmStarting) {
            // Scale the impulse to support a variable time step.
            m_impulse *= data.step.dtRatio

            val Px = m_impulse * m_u.x
            val Py = m_impulse * m_u.y
            vA.x -= m_invMassA * Px
            vA.y -= m_invMassA * Py
            wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px)

            vB.x += m_invMassB * Px
            vB.y += m_invMassB * Py
            wB += m_invIB * (m_rB.x * Py - m_rB.y * Px)
        } else {
            m_impulse = 0.0
        }

        pool.pushRot(2)
        pool.pushMutableVector2d(1)

        // data.velocities[m_indexA].v = vA;
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v = vB;
        data.velocities[m_indexB].w = wB
    }

    override fun solveVelocityConstraints(data: SolverData) {
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        // Cdot = dot(u, v + cross(w, r))
        val vpA = pool.popMutableVector2d()
        val vpB = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()

        vpA.set(wA cross m_rA)
        vpA.add(vA)
        vpB.set(wB cross m_rB)
        vpB.add(vB)

        val C = m_length - maxLength
        temp.set(vpB)
        temp.subtract(vpA)
        var Cdot = (m_u dot temp)

        // Predictive constraint.
        if (C < 0.0) {
            Cdot += data.step.inv_dt * C
        }

        var impulse = -m_mass * Cdot
        val oldImpulse = m_impulse
        m_impulse = min(0.0, m_impulse + impulse)
        impulse = m_impulse - oldImpulse

        val Px = impulse * m_u.x
        val Py = impulse * m_u.y
        vA.x -= m_invMassA * Px
        vA.y -= m_invMassA * Py
        wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px)
        vB.x += m_invMassB * Px
        vB.y += m_invMassB * Py
        wB += m_invIB * (m_rB.x * Py - m_rB.y * Px)

        pool.pushMutableVector2d(3)

        // data.velocities[m_indexA].v = vA;
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v = vB;
        data.velocities[m_indexB].w = wB
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a

        val qA = pool.popRot()
        val qB = pool.popRot()
        val u = pool.popMutableVector2d()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()

        qA.set(aA)
        qB.set(aB)

        // Compute the effective masses.
        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)
        u.set(cB)
        u.add(rB)
        u.subtract(cA)
        u.subtract(rA)

        val length = u.length()
        if (length > Settings.EPSILON) u.multiply(1.0 / length)
        var C = length - maxLength

        C = clamp(C, 0.0, Settings.maxLinearCorrection)

        val impulse = -m_mass * C
        val Px = impulse * u.x
        val Py = impulse * u.y

        cA.x -= m_invMassA * Px
        cA.y -= m_invMassA * Py
        aA -= m_invIA * (rA.x * Py - rA.y * Px)
        cB.x += m_invMassB * Px
        cB.y += m_invMassB * Py
        aB += m_invIB * (rB.x * Py - rB.y * Px)

        pool.pushRot(2)
        pool.pushMutableVector2d(4)

        // data.positions[m_indexA].c = cA;
        data.positions[m_indexA].a = aA
        // data.positions[m_indexB].c = cB;
        data.positions[m_indexB].a = aB

        return length - maxLength < Settings.linearSlop
    }

    override fun getAnchorA(argOut: MutableVector2d) {
        bodyA.getWorldPointToOut(localAnchorA, argOut)
    }

    override fun getAnchorB(argOut: MutableVector2d) {
        bodyB.getWorldPointToOut(localAnchorB, argOut)
    }

    override fun getReactionForce(
        inv_dt: Double,
        argOut: MutableVector2d
    ) {
        argOut.set(m_u)
        argOut.multiply(inv_dt)
        argOut.multiply(m_impulse)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return 0.0
    }

}
