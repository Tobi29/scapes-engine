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
/**
 * Created at 12:12:02 PM Jan 23, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.cross
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import kotlin.math.abs

/**
 * The pulley joint is connected to two bodies and two fixed ground points. The pulley supports a
 * ratio such that: length1 + ratio * length2 <= constant Yes, the force transmitted is scaled by
 * the ratio. Warning: the pulley joint can get a bit squirrelly by itself. They often work better
 * when combined with prismatic joints. You should also cover the the anchor points with static
 * shapes to prevent one side from going to zero length.
 *
 * @author Daniel Murphy
 */
class PulleyJoint(
    argWorldPool: IWorldPool,
    def: PulleyJointDef
) : Joint(argWorldPool, def) {

    val groundAnchorA = MutableVector2d()
    val groundAnchorB = MutableVector2d()
    val lengthA: Double
    val lengthB: Double

    // Solver shared
    val localAnchorA = MutableVector2d()
    val localAnchorB = MutableVector2d()
    private val m_constant: Double
    val ratio: Double
    private var m_impulse: Double = 0.0

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_uA = MutableVector2d()
    private val m_uB = MutableVector2d()
    private val m_rA = MutableVector2d()
    private val m_rB = MutableVector2d()
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0
    private var m_mass: Double = 0.0

    val currentLengthA: Double
        get() {
            val p = pool.popMutableVector2d()
            bodyA.getWorldPointToOut(localAnchorA, p)
            p.subtract(groundAnchorA)
            val length = p.length()
            pool.pushMutableVector2d(1)
            return length
        }

    val currentLengthB: Double
        get() {
            val p = pool.popMutableVector2d()
            bodyB.getWorldPointToOut(localAnchorB, p)
            p.subtract(groundAnchorB)
            val length = p.length()
            pool.pushMutableVector2d(1)
            return length
        }

    val length1: Double
        get() {
            val p = pool.popMutableVector2d()
            bodyA.getWorldPointToOut(localAnchorA, p)
            p.subtract(groundAnchorA)

            val len = p.length()
            pool.pushMutableVector2d(1)
            return len
        }

    val length2: Double
        get() {
            val p = pool.popMutableVector2d()
            bodyB.getWorldPointToOut(localAnchorB, p)
            p.subtract(groundAnchorB)

            val len = p.length()
            pool.pushMutableVector2d(1)
            return len
        }

    init {
        groundAnchorA.set(def.groundAnchorA)
        groundAnchorB.set(def.groundAnchorB)
        localAnchorA.set(def.localAnchorA)
        localAnchorB.set(def.localAnchorB)

        assert { def.ratio != 0.0 }
        ratio = def.ratio

        lengthA = def.lengthA
        lengthB = def.lengthB

        m_constant = def.lengthA + ratio * def.lengthB
        m_impulse = 0.0
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
        argOut.set(m_uB)
        argOut.multiply(m_impulse)
        argOut.multiply(inv_dt)
    }

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

        m_uA.set(cA)
        m_uA.add(m_rA)
        m_uA.subtract(groundAnchorA)
        m_uB.set(cB)
        m_uB.add(m_rB)
        m_uB.subtract(groundAnchorB)

        val lengthA = m_uA.length()
        val lengthB = m_uB.length()

        if (lengthA > 10.0 * Settings.linearSlop) {
            m_uA.multiply(1.0 / lengthA)
        } else {
            m_uA.setXY(0.0, 0.0)
        }

        if (lengthB > 10.0 * Settings.linearSlop) {
            m_uB.multiply(1.0 / lengthB)
        } else {
            m_uB.setXY(0.0, 0.0)
        }

        // Compute effective mass.
        val ruA = (m_rA cross m_uA)
        val ruB = (m_rB cross m_uB)

        val mA = m_invMassA + m_invIA * ruA * ruA
        val mB = m_invMassB + m_invIB * ruB * ruB

        m_mass = mA + ratio * ratio * mB

        if (m_mass > 0.0) {
            m_mass = 1.0 / m_mass
        }

        if (data.step.warmStarting) {

            // Scale impulses to support variable time steps.
            m_impulse *= data.step.dtRatio

            // Warm starting.
            val PA = pool.popMutableVector2d()
            val PB = pool.popMutableVector2d()

            PA.set(m_uA).multiply(-m_impulse)
            PB.set(m_uB).multiply(-ratio * m_impulse)

            vA.x += m_invMassA * PA.x
            vA.y += m_invMassA * PA.y
            wA += m_invIA * (m_rA cross PA)
            vB.x += m_invMassB * PB.x
            vB.y += m_invMassB * PB.y
            wB += m_invIB * (m_rB cross PB)

            pool.pushMutableVector2d(2)
        } else {
            m_impulse = 0.0
        }
        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(1)
        pool.pushRot(2)
    }

    override fun solveVelocityConstraints(data: SolverData) {
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        val vpA = pool.popMutableVector2d()
        val vpB = pool.popMutableVector2d()
        val PA = pool.popMutableVector2d()
        val PB = pool.popMutableVector2d()

        vpA.set(wA cross m_rA)
        vpA.add(vA)
        vpB.set(wB cross m_rB)
        vpB.add(vB)

        val Cdot = -(m_uA dot vpA) - ratio * (m_uB dot vpB)
        val impulse = -m_mass * Cdot
        m_impulse += impulse

        PA.set(m_uA).multiply(-impulse)
        PB.set(m_uB).multiply(-ratio * impulse)
        vA.x += m_invMassA * PA.x
        vA.y += m_invMassA * PA.y
        wA += m_invIA * (m_rA cross PA)
        vB.x += m_invMassB * PB.x
        vB.y += m_invMassB * PB.y
        wB += m_invIB * (m_rB cross PB)

        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(4)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        val qA = pool.popRot()
        val qB = pool.popRot()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()
        val uA = pool.popMutableVector2d()
        val uB = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()
        val PA = pool.popMutableVector2d()
        val PB = pool.popMutableVector2d()

        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a

        qA.set(aA)
        qB.set(aB)

        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)

        uA.set(cA)
        uA.add(rA)
        uA.subtract(groundAnchorA)
        uB.set(cB)
        uB.add(rB)
        uB.subtract(groundAnchorB)

        val lengthA = uA.length()
        val lengthB = uB.length()

        if (lengthA > 10.0 * Settings.linearSlop) {
            uA.multiply(1.0 / lengthA)
        } else {
            uA.setXY(0.0, 0.0)
        }

        if (lengthB > 10.0 * Settings.linearSlop) {
            uB.multiply(1.0 / lengthB)
        } else {
            uB.setXY(0.0, 0.0)
        }

        // Compute effective mass.
        val ruA = (rA cross uA)
        val ruB = (rB cross uB)

        val mA = m_invMassA + m_invIA * ruA * ruA
        val mB = m_invMassB + m_invIB * ruB * ruB

        var mass = mA + ratio * ratio * mB

        if (mass > 0.0) {
            mass = 1.0 / mass
        }

        val C = m_constant - lengthA - ratio * lengthB
        val linearError = abs(C)

        val impulse = -mass * C

        PA.set(uA)
        PA.multiply(-impulse)
        PB.set(uB)
        PB.multiply(-ratio * impulse)

        cA.x += m_invMassA * PA.x
        cA.y += m_invMassA * PA.y
        aA += m_invIA * (rA cross PA)
        cB.x += m_invMassB * PB.x
        cB.y += m_invMassB * PB.y
        aB += m_invIB * (rB cross PB)

        //    data.positions[m_indexA].c.set(cA);
        data.positions[m_indexA].a = aA
        //    data.positions[m_indexB].c.set(cB);
        data.positions[m_indexB].a = aB

        pool.pushRot(2)
        pool.pushMutableVector2d(7)

        return linearError < Settings.linearSlop
    }
}
