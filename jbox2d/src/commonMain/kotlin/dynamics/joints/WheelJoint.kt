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
import kotlin.math.PI
import kotlin.math.abs

//Linear constraint (point-to-line)
//d = pB - pA = xB + rB - xA - rA
//C = dot(ay, d)
//Cdot = dot(d, cross(wA, ay)) + dot(ay, vB + cross(wB, rB) - vA - cross(wA, rA))
//   = -dot(ay, vA) - dot(cross(d + rA, ay), wA) + dot(ay, vB) + dot(cross(rB, ay), vB)
//J = [-ay, -cross(d + rA, ay), ay, cross(rB, ay)]

//Spring linear constraint
//C = dot(ax, d)
//Cdot = = -dot(ax, vA) - dot(cross(d + rA, ax), wA) + dot(ax, vB) + dot(cross(rB, ax), vB)
//J = [-ax -cross(d+rA, ax) ax cross(rB, ax)]

//Motor rotational constraint
//Cdot = wB - wA
//J = [0 0 -1 0 0 1]

/**
 * A wheel joint. This joint provides two degrees of freedom: translation along an axis fixed in
 * bodyA and rotation in the plane. You can use a joint limit to restrict the range of motion and a
 * joint motor to drive the rotation or to model rotational friction. This joint is designed for
 * vehicle suspensions.
 *
 * @author Daniel Murphy
 */
class WheelJoint(
    argPool: IWorldPool,
    def: WheelJointDef
) : Joint(argPool, def) {

    var springFrequencyHz: Double = 0.0
    var springDampingRatio: Double = 0.0

    // Solver shared
    val localAnchorA = MutableVector2d()
    val localAnchorB = MutableVector2d()
    /** For serialization  */
    val localAxisA = MutableVector2d()
    private val m_localYAxisA = MutableVector2d()

    private var m_impulse: Double = 0.0
    private var m_motorImpulse: Double = 0.0
    private var m_springImpulse: Double = 0.0

    private var m_maxMotorTorque: Double = 0.0
    private var m_motorSpeed: Double = 0.0
    var isMotorEnabled: Boolean = false
        private set

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0

    private val m_ax = MutableVector2d()
    private val m_ay = MutableVector2d()
    private var m_sAx: Double = 0.0
    private var m_sBx: Double = 0.0
    private var m_sAy: Double = 0.0
    private var m_sBy: Double = 0.0

    private var m_mass: Double = 0.0
    private var m_motorMass: Double = 0.0
    private var m_springMass: Double = 0.0

    private var m_bias: Double = 0.0
    private var m_gamma: Double = 0.0

    val jointTranslation: Double
        get() {
            val b1 = bodyA
            val b2 = bodyB

            val p1 = pool.popMutableVector2d()
            val p2 = pool.popMutableVector2d()
            val axis = pool.popMutableVector2d()
            b1.getWorldPointToOut(localAnchorA, p1)
            b2.getWorldPointToOut(localAnchorA, p2)
            p2.subtract(p1)
            b1.getWorldVectorToOut(localAxisA, axis)

            val translation = (p2 dot axis)
            pool.pushMutableVector2d(3)
            return translation
        }

    val jointSpeed: Double
        get() = bodyA.m_angularVelocity - bodyB.m_angularVelocity

    var motorSpeed: Double
        get() = m_motorSpeed
        set(speed) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            m_motorSpeed = speed
        }

    var maxMotorTorque: Double
        get() = m_maxMotorTorque
        set(torque) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            m_maxMotorTorque = torque
        }

    // pooling
    private val rA = MutableVector2d()
    private val rB = MutableVector2d()
    private val d = MutableVector2d()

    init {
        localAnchorA.set(def.localAnchorA)
        localAnchorB.set(def.localAnchorB)
        localAxisA.set(def.localAxisA)
        m_localYAxisA.set(1.0 cross localAxisA)


        m_motorMass = 0.0
        m_motorImpulse = 0.0

        m_maxMotorTorque = def.maxMotorTorque
        m_motorSpeed = def.motorSpeed
        isMotorEnabled = def.enableMotor

        springFrequencyHz = def.frequencyHz
        springDampingRatio = def.dampingRatio
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
        val temp = pool.popMutableVector2d()
        temp.set(m_ay)
        temp.multiply(m_impulse)
        argOut.set(m_ax)
        argOut.multiply(m_springImpulse)
        argOut.add(temp)
        argOut.multiply(inv_dt)
        pool.pushMutableVector2d(1)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return inv_dt * m_motorImpulse
    }

    fun enableMotor(flag: Boolean) {
        bodyA.isAwake = true
        bodyB.isAwake = true
        isMotorEnabled = flag
    }

    fun getMotorTorque(inv_dt: Double): Double {
        return m_motorImpulse * inv_dt
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

        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

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
        temp.set(localAnchorA).subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB).subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)
        d.set(cB)
        d.add(rB)
        d.subtract(cA)
        d.subtract(rA)

        // Point to line constraint
        run {
            Rot.mulToOut(qA, m_localYAxisA, m_ay)
            temp.set(d).add(rA)
            m_sAy = (temp cross m_ay)
            m_sBy = (rB cross m_ay)

            m_mass = mA + mB + iA * m_sAy * m_sAy + iB * m_sBy * m_sBy

            if (m_mass > 0.0) {
                m_mass = 1.0 / m_mass
            }
        }

        // Spring constraint
        m_springMass = 0.0
        m_bias = 0.0
        m_gamma = 0.0
        if (springFrequencyHz > 0.0) {
            Rot.mulToOut(qA, localAxisA, m_ax)
            temp.set(d).add(rA)
            m_sAx = (temp cross m_ax)
            m_sBx = (rB cross m_ax)

            val invMass = mA + mB + iA * m_sAx * m_sAx + iB * m_sBx * m_sBx

            if (invMass > 0.0) {
                m_springMass = 1.0 / invMass

                val C = (d dot m_ax)

                // Frequency
                val omega = 2.0 * PI * springFrequencyHz

                // Damping coefficient
                val d = 2.0 * m_springMass * springDampingRatio * omega

                // Spring stiffness
                val k = m_springMass * omega * omega

                // magic formulas
                val h = data.step.dt
                m_gamma = h * (d + h * k)
                if (m_gamma > 0.0) {
                    m_gamma = 1.0 / m_gamma
                }

                m_bias = C * h * k * m_gamma

                m_springMass = invMass + m_gamma
                if (m_springMass > 0.0) {
                    m_springMass = 1.0 / m_springMass
                }
            }
        } else {
            m_springImpulse = 0.0
        }

        // Rotational motor
        if (isMotorEnabled) {
            m_motorMass = iA + iB
            if (m_motorMass > 0.0) {
                m_motorMass = 1.0 / m_motorMass
            }
        } else {
            m_motorMass = 0.0
            m_motorImpulse = 0.0
        }

        if (data.step.warmStarting) {
            val P = pool.popMutableVector2d()
            // Account for variable time step.
            m_impulse *= data.step.dtRatio
            m_springImpulse *= data.step.dtRatio
            m_motorImpulse *= data.step.dtRatio

            P.x = m_impulse * m_ay.x + m_springImpulse * m_ax.x
            P.y = m_impulse * m_ay.y + m_springImpulse * m_ax.y
            val LA =
                m_impulse * m_sAy + m_springImpulse * m_sAx + m_motorImpulse
            val LB =
                m_impulse * m_sBy + m_springImpulse * m_sBx + m_motorImpulse

            vA.x -= m_invMassA * P.x
            vA.y -= m_invMassA * P.y
            wA -= m_invIA * LA

            vB.x += m_invMassB * P.x
            vB.y += m_invMassB * P.y
            wB += m_invIB * LB
            pool.pushMutableVector2d(1)
        } else {
            m_impulse = 0.0
            m_springImpulse = 0.0
            m_motorImpulse = 0.0
        }
        pool.pushRot(2)
        pool.pushMutableVector2d(1)

        // data.velocities[m_indexA].v = vA;
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v = vB;
        data.velocities[m_indexB].w = wB
    }

    override fun solveVelocityConstraints(data: SolverData) {
        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        val temp = pool.popMutableVector2d()
        val P = pool.popMutableVector2d()

        // Solve spring constraint
        run {
            temp.set(vB).subtract(vA)
            val Cdot = (m_ax dot temp) + m_sBx * wB - m_sAx * wA
            val impulse =
                -m_springMass * (Cdot + m_bias + m_gamma * m_springImpulse)
            m_springImpulse += impulse

            P.x = impulse * m_ax.x
            P.y = impulse * m_ax.y
            val LA = impulse * m_sAx
            val LB = impulse * m_sBx

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB
        }

        // Solve rotational motor constraint
        run {
            val Cdot = wB - wA - m_motorSpeed
            var impulse = -m_motorMass * Cdot

            val oldImpulse = m_motorImpulse
            val maxImpulse = data.step.dt * m_maxMotorTorque
            m_motorImpulse = clamp(
                m_motorImpulse + impulse,
                -maxImpulse, maxImpulse
            )
            impulse = m_motorImpulse - oldImpulse

            wA -= iA * impulse
            wB += iB * impulse
        }

        // Solve point to line constraint
        run {
            temp.set(vB).subtract(vA)
            val Cdot = (m_ay dot temp) + m_sBy * wB - m_sAy * wA
            val impulse = -m_mass * Cdot
            m_impulse += impulse

            P.x = impulse * m_ay.x
            P.y = impulse * m_ay.y
            val LA = impulse * m_sAy
            val LB = impulse * m_sBy

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB
        }
        pool.pushMutableVector2d(2)

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
        val temp = pool.popMutableVector2d()

        qA.set(aA)
        qB.set(aB)

        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)
        d.set(cB)
        d.subtract(cA)
        d.add(rB)
        d.subtract(rA)

        val ay = pool.popMutableVector2d()
        Rot.mulToOut(qA, m_localYAxisA, ay)

        temp.set(d).add(rA)
        val sAy = temp cross ay
        val sBy = rB cross ay

        val C = d dot ay

        val k =
            m_invMassA + m_invMassB + m_invIA * m_sAy * m_sAy + m_invIB * m_sBy * m_sBy

        val impulse: Double
        impulse = if (k != 0.0) {
            -C / k
        } else {
            0.0
        }

        val P = pool.popMutableVector2d()
        P.x = impulse * ay.x
        P.y = impulse * ay.y
        val LA = impulse * sAy
        val LB = impulse * sBy

        cA.x -= m_invMassA * P.x
        cA.y -= m_invMassA * P.y
        aA -= m_invIA * LA
        cB.x += m_invMassB * P.x
        cB.y += m_invMassB * P.y
        aB += m_invIB * LB

        pool.pushMutableVector2d(3)
        pool.pushRot(2)
        // data.positions[m_indexA].c = cA;
        data.positions[m_indexA].a = aA
        // data.positions[m_indexB].c = cB;
        data.positions[m_indexB].a = aB

        return abs(C) <= Settings.linearSlop
    }
}
