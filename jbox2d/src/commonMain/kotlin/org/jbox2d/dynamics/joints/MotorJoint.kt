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
import org.jbox2d.common.invertToOut
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.matrix.*
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.clamp

//Point-to-point constraint
//Cdot = v2 - v1
//   = v2 + cross(w2, r2) - v1 - cross(w1, r1)
//J = [-I -r1_skew I r2_skew ]
//Identity used:
//w k % (rx i + ry j) = w * (-ry i + rx j)

//Angle constraint
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//K = invI1 + invI2

/**
 * A motor joint is used to control the relative motion between two bodies. A typical usage is to
 * control the movement of a dynamic body with respect to the ground.
 *
 * @author dmurph
 */
class MotorJoint(
    pool: IWorldPool,
    def: MotorJointDef
) : Joint(pool, def) {

    // Solver shared
    /**
     * Get the target linear offset, in frame A, in meters. Do not modify.
     */
    /**
     * Set the target linear offset, in frame A, in meters.
     */
    var linearOffset = MutableVector2d()
        set(linearOffset) {
            if (linearOffset.x != this.linearOffset.x || linearOffset.y != this.linearOffset.y) {
                bodyA.isAwake = true
                bodyB.isAwake = true
                this.linearOffset.set(linearOffset)
            }
        }
    private var m_angularOffset: Double = 0.0
    private val m_linearImpulse = MutableVector2d()
    private var m_angularImpulse: Double = 0.0
    private var m_maxForce: Double = 0.0
    private var m_maxTorque: Double = 0.0
    var correctionFactor: Double = 0.0

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_rA = MutableVector2d()
    private val m_rB = MutableVector2d()
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private val m_linearError = MutableVector2d()
    private var m_angularError: Double = 0.0
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0
    private val m_linearMass = MutableMatrix2d()
    private var m_angularMass: Double = 0.0

    /**
     * Set the target angular offset, in radians.
     *
     * @param angularOffset
     */
    var angularOffset: Double
        get() = m_angularOffset
        set(angularOffset) {
            if (angularOffset != m_angularOffset) {
                bodyA.isAwake = true
                bodyB.isAwake = true
                m_angularOffset = angularOffset
            }
        }

    /**
     * Get the maximum friction force in N.
     */
    /**
     * Set the maximum friction force in N.
     *
     * @param force
     */
    var maxForce: Double
        get() = m_maxForce
        set(force) {
            assert { force >= 0.0 }
            m_maxForce = force
        }

    /**
     * Get the maximum friction torque in N*m.
     */
    /**
     * Set the maximum friction torque in N*m.
     */
    var maxTorque: Double
        get() = m_maxTorque
        set(torque) {
            assert { torque >= 0.0 }
            m_maxTorque = torque
        }

    init {
        linearOffset.set(def.linearOffset)
        m_angularOffset = def.angularOffset

        m_angularImpulse = 0.0

        m_maxForce = def.maxForce
        m_maxTorque = def.maxTorque
        correctionFactor = def.correctionFactor
    }

    override fun getAnchorA(out: MutableVector2d) {
        out.set(bodyA.position)
    }

    override fun getAnchorB(out: MutableVector2d) {
        out.set(bodyB.position)
    }

    override fun getReactionForce(
        inv_dt: Double,
        out: MutableVector2d
    ) {
        out.set(m_linearImpulse).multiply(inv_dt)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return m_angularImpulse * inv_dt
    }

    /**
     * Get the target linear offset, in frame A, in meters.
     */
    fun getLinearOffset(out: MutableVector2d) {
        out.set(linearOffset)
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
        val K = pool.popMatrix2d()

        qA.set(aA)
        qB.set(aB)

        // Compute the effective mass matrix.
        // m_rA = b2Mul(qA, -m_localCenterA);
        // m_rB = b2Mul(qB, -m_localCenterB);
        m_rA.x = qA.cos * -m_localCenterA.x - qA.sin * -m_localCenterA.y
        m_rA.y = qA.sin * -m_localCenterA.x + qA.cos * -m_localCenterA.y
        m_rB.x = qB.cos * -m_localCenterB.x - qB.sin * -m_localCenterB.y
        m_rB.y = qB.sin * -m_localCenterB.x + qB.cos * -m_localCenterB.y

        // J = [-I -r1_skew I r2_skew]
        // [ 0 -1 0 1]
        // r_skew = [-ry; rx]

        // Matlab
        // K = [ mA+r1y^2*iA+mB+r2y^2*iB, -r1y*iA*r1x-r2y*iB*r2x, -r1y*iA-r2y*iB]
        // [ -r1y*iA*r1x-r2y*iB*r2x, mA+r1x^2*iA+mB+r2x^2*iB, r1x*iA+r2x*iB]
        // [ -r1y*iA-r2y*iB, r1x*iA+r2x*iB, iA+iB]
        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        K.xx = mA + mB + iA * m_rA.y * m_rA.y + iB * m_rB.y * m_rB.y
        K.xy = -iA * m_rA.x * m_rA.y - iB * m_rB.x * m_rB.y
        K.yx = K.xy
        K.yy = mA + mB + iA * m_rA.x * m_rA.x + iB * m_rB.x * m_rB.x

        K.invertToOut(m_linearMass)

        m_angularMass = iA + iB
        if (m_angularMass > 0.0) {
            m_angularMass = 1.0 / m_angularMass
        }

        // m_linearError = cB + m_rB - cA - m_rA - b2Mul(qA, m_linearOffset);
        Rot.mulToOut(qA, linearOffset, temp)
        m_linearError.x = cB.x + m_rB.x - cA.x - m_rA.x - temp.x
        m_linearError.y = cB.y + m_rB.y - cA.y - m_rA.y - temp.y
        m_angularError = aB - aA - m_angularOffset

        if (data.step.warmStarting) {
            // Scale impulses to support a variable time step.
            m_linearImpulse.x *= data.step.dtRatio
            m_linearImpulse.y *= data.step.dtRatio
            m_angularImpulse *= data.step.dtRatio

            val P = m_linearImpulse
            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * (m_rA.x * P.y - m_rA.y * P.x + m_angularImpulse)
            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * (m_rB.x * P.y - m_rB.y * P.x + m_angularImpulse)
        } else {
            m_linearImpulse.setXY(0.0, 0.0)
            m_angularImpulse = 0.0
        }

        pool.pushMutableVector2d(1)
        pool.pushMatrix2d(1)
        pool.pushRot(2)

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

        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        val h = data.step.dt
        val inv_h = data.step.inv_dt

        val temp = pool.popMutableVector2d()

        // Solve angular friction
        run {
            val Cdot = wB - wA + inv_h * correctionFactor * m_angularError
            var impulse = -m_angularMass * Cdot

            val oldImpulse = m_angularImpulse
            val maxImpulse = h * m_maxTorque
            m_angularImpulse = clamp(
                m_angularImpulse + impulse,
                -maxImpulse, maxImpulse
            )
            impulse = m_angularImpulse - oldImpulse

            wA -= iA * impulse
            wB += iB * impulse
        }

        val Cdot = pool.popMutableVector2d()

        // Solve linear friction
        run {
            // Cdot = vB + b2Cross(wB, m_rB) - vA - b2Cross(wA, m_rA) + inv_h * m_correctionFactor *
            // m_linearError;
            Cdot.x = vB.x + -wB * m_rB.y - vA.x - -wA * m_rA.y + inv_h *
                    correctionFactor * m_linearError.x
            Cdot.y = vB.y + wB * m_rB.x - vA.y - wA * m_rA.x + inv_h *
                    correctionFactor * m_linearError.y

            m_linearMass.multiply(Cdot, temp)
            temp.negate()
            val oldImpulse = pool.popMutableVector2d()
            oldImpulse.set(m_linearImpulse)
            m_linearImpulse.add(temp)

            val maxImpulse = h * m_maxForce

            if (m_linearImpulse.lengthSqr() > maxImpulse * maxImpulse) {
                m_linearImpulse.normalize()
                m_linearImpulse.multiply(maxImpulse)
            }

            temp.x = m_linearImpulse.x - oldImpulse.x
            temp.y = m_linearImpulse.y - oldImpulse.y

            vA.x -= mA * temp.x
            vA.y -= mA * temp.y
            wA -= iA * (m_rA.x * temp.y - m_rA.y * temp.x)

            vB.x += mB * temp.x
            vB.y += mB * temp.y
            wB += iB * (m_rB.x * temp.y - m_rB.y * temp.x)
        }

        pool.pushMutableVector2d(3)

        // data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        return true
    }
}
