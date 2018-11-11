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

import org.jbox2d.common.*
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.matrix.*
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.clamp
import kotlin.math.abs

//Point-to-point constraint
//C = p2 - p1
//Cdot = v2 - v1
//   = v2 + cross(w2, r2) - v1 - cross(w1, r1)
//J = [-I -r1_skew I r2_skew ]
//Identity used:
//w k % (rx i + ry j) = w * (-ry i + rx j)

//Motor constraint
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//K = invI1 + invI2

/**
 * A revolute joint constrains two bodies to share a common point while they are free to rotate
 * about the point. The relative rotation about the shared point is the joint angle. You can limit
 * the relative rotation with a joint limit that specifies a lower and upper angle. You can use a
 * motor to drive the relative rotation about the shared point. A maximum motor torque is provided
 * so that infinite forces are not generated.
 *
 * @author Daniel Murphy
 */
class RevoluteJoint(
    argWorld: IWorldPool,
    def: RevoluteJointDef
) : Joint(argWorld, def) {

    // Solver shared
    val localAnchorA = MutableVector2d()
    val localAnchorB = MutableVector2d()
    private val m_impulse = MutableVector3d()
    private var m_motorImpulse: Double = 0.0

    var isMotorEnabled: Boolean = false
        private set
    private var m_maxMotorTorque: Double = 0.0
    private var m_motorSpeed: Double = 0.0

    var isLimitEnabled: Boolean = false
        private set
    var referenceAngle: Double = 0.0
    var lowerLimit: Double = 0.0
        private set
    var upperLimit: Double = 0.0
        private set

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_rA = MutableVector2d()
    private val m_rB = MutableVector2d()
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0
    private val m_mass =
        MutableMatrix3d() // effective mass for point-to-point constraint.
    private var m_motorMass: Double =
        0.0 // effective mass for motor/limit angular constraint.
    private var m_limitState: LimitState? = null

    val jointAngle: Double
        get() {
            val b1 = bodyA
            val b2 = bodyB
            return b2.m_sweep.a - b1.m_sweep.a - referenceAngle
        }

    val jointSpeed: Double
        get() {
            val b1 = bodyA
            val b2 = bodyB
            return b2.m_angularVelocity - b1.m_angularVelocity
        }

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

    init {
        localAnchorA.set(def.localAnchorA)
        localAnchorB.set(def.localAnchorB)
        referenceAngle = def.referenceAngle

        m_motorImpulse = 0.0

        lowerLimit = def.lowerAngle
        upperLimit = def.upperAngle
        m_maxMotorTorque = def.maxMotorTorque
        m_motorSpeed = def.motorSpeed
        isLimitEnabled = def.enableLimit
        isMotorEnabled = def.enableMotor
        m_limitState = LimitState.INACTIVE
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

        // Vec2 cA = data.positions[m_indexA].c;
        val aA = data.positions[m_indexA].a
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w

        // Vec2 cB = data.positions[m_indexB].c;
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

        val fixedRotation = iA + iB == 0.0

        m_mass.xx = mA + mB + m_rA.y * m_rA.y * iA + m_rB.y * m_rB.y * iB
        m_mass.yx = -m_rA.y * m_rA.x * iA - m_rB.y * m_rB.x * iB
        m_mass.zx = -m_rA.y * iA - m_rB.y * iB
        m_mass.xy = m_mass.yx
        m_mass.yy = mA + mB + m_rA.x * m_rA.x * iA + m_rB.x * m_rB.x * iB
        m_mass.zy = m_rA.x * iA + m_rB.x * iB
        m_mass.xz = m_mass.zx
        m_mass.yz = m_mass.zy
        m_mass.zz = iA + iB

        m_motorMass = (iA + iB)
        if (m_motorMass > 0.0) {
            m_motorMass = 1.0 / m_motorMass
        }

        if (isMotorEnabled == false || fixedRotation) {
            m_motorImpulse = 0.0
        }

        if (isLimitEnabled && fixedRotation == false) {
            val jointAngle = aB - aA - referenceAngle
            if (abs(upperLimit - lowerLimit) < 2.0 * Settings.angularSlop) {
                m_limitState = LimitState.EQUAL
            } else if (jointAngle <= lowerLimit) {
                if (m_limitState != LimitState.AT_LOWER) {
                    m_impulse.z = 0.0
                }
                m_limitState = LimitState.AT_LOWER
            } else if (jointAngle >= upperLimit) {
                if (m_limitState != LimitState.AT_UPPER) {
                    m_impulse.z = 0.0
                }
                m_limitState = LimitState.AT_UPPER
            } else {
                m_limitState = LimitState.INACTIVE
                m_impulse.z = 0.0
            }
        } else {
            m_limitState = LimitState.INACTIVE
        }

        if (data.step.warmStarting) {
            val P = pool.popMutableVector2d()
            // Scale impulses to support a variable time step.
            m_impulse.x *= data.step.dtRatio
            m_impulse.y *= data.step.dtRatio
            m_motorImpulse *= data.step.dtRatio

            P.x = m_impulse.x
            P.y = m_impulse.y

            vA.x -= (mA * P.x)
            vA.y -= (mA * P.y)
            wA -= iA * ((m_rA cross P) + m_motorImpulse + m_impulse.z)

            vB.x += (mB * P.x)
            vB.y += (mB * P.y)
            wB += iB * ((m_rB cross P) + m_motorImpulse + m_impulse.z)
            pool.pushMutableVector2d(1)
        } else {
            m_impulse.setXYZ(0.0, 0.0, 0.0)
            m_motorImpulse = 0.0
        }
        // data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(1)
        pool.pushRot(2)
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

        val fixedRotation = iA + iB == 0.0

        // Solve motor constraint.
        if (isMotorEnabled && m_limitState != LimitState.EQUAL && fixedRotation == false) {
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
        val temp = pool.popMutableVector2d()

        // Solve limit constraint.
        if (isLimitEnabled && m_limitState != LimitState.INACTIVE && fixedRotation == false) {

            val Cdot1 = pool.popMutableVector2d()
            val Cdot = pool.popMutableVector3d()

            // Solve point-to-point constraint
            temp.set(wA cross m_rA)
            Cdot1.set(wB cross m_rB)
            Cdot1.add(vB)
            Cdot1.subtract(vA)
            Cdot1.subtract(temp)
            val Cdot2 = wB - wA
            Cdot.setXYZ(Cdot1.x, Cdot1.y, Cdot2)

            val impulse = pool.popMutableVector3d()
            m_mass.solve33ToOut(Cdot, impulse)
            impulse.negate()

            if (m_limitState == LimitState.EQUAL) {
                m_impulse.add(impulse)
            } else if (m_limitState == LimitState.AT_LOWER) {
                val newImpulse = m_impulse.z + impulse.z
                if (newImpulse < 0.0) {
                    val rhs = pool.popMutableVector2d()
                    rhs.setXY(m_mass.zx, m_mass.zy)
                    rhs.multiply(m_impulse.z)
                    rhs.subtract(Cdot1)
                    m_mass.solve22ToOut(rhs, temp)
                    impulse.x = temp.x
                    impulse.y = temp.y
                    impulse.z = -m_impulse.z
                    m_impulse.x += temp.x
                    m_impulse.y += temp.y
                    m_impulse.z = 0.0
                    pool.pushMutableVector2d(1)
                } else {
                    m_impulse.add(impulse)
                }
            } else if (m_limitState == LimitState.AT_UPPER) {
                val newImpulse = m_impulse.z + impulse.z
                if (newImpulse > 0.0) {
                    val rhs = pool.popMutableVector2d()
                    rhs.setXY(m_mass.zx, m_mass.zy)
                    rhs.multiply(m_impulse.z)
                    rhs.subtract(Cdot1)
                    m_mass.solve22ToOut(rhs, temp)
                    impulse.x = temp.x
                    impulse.y = temp.y
                    impulse.z = -m_impulse.z
                    m_impulse.x += temp.x
                    m_impulse.y += temp.y
                    m_impulse.z = 0.0
                    pool.pushMutableVector2d(1)
                } else {
                    m_impulse.add(impulse)
                }
            }
            val P = pool.popMutableVector2d()

            P.setXY(impulse.x, impulse.y)

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * ((m_rA cross P) + impulse.z)

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * ((m_rB cross P) + impulse.z)

            pool.pushMutableVector2d(2)
            pool.pushMutableVector3d(2)
        } else {

            // Solve point-to-point constraint
            val Cdot = pool.popMutableVector2d()
            val impulse = pool.popMutableVector2d()

            temp.set(wA cross m_rA)
            Cdot.set(wB cross m_rB)
            Cdot.add(vB)
            Cdot.subtract(vA)
            Cdot.subtract(temp)
            Cdot.negate()
            m_mass.solve22ToOut(Cdot, impulse) // just leave negated

            m_impulse.x += impulse.x
            m_impulse.y += impulse.y

            vA.x -= mA * impulse.x
            vA.y -= mA * impulse.y
            wA -= iA * (m_rA cross impulse)

            vB.x += mB * impulse.x
            vB.y += mB * impulse.y
            wB += iB * (m_rB cross impulse)

            pool.pushMutableVector2d(2)
        }

        // data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(1)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        val qA = pool.popRot()
        val qB = pool.popRot()
        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a

        qA.set(aA)
        qB.set(aB)

        var angularError = 0.0
        var positionError = 0.0

        val fixedRotation = m_invIA + m_invIB == 0.0

        // Solve angular limit constraint.
        if (isLimitEnabled && m_limitState != LimitState.INACTIVE && fixedRotation == false) {
            val angle = aB - aA - referenceAngle
            var limitImpulse = 0.0

            if (m_limitState == LimitState.EQUAL) {
                // Prevent large angular corrections
                val C = clamp(
                    angle - lowerLimit,
                    -Settings.maxAngularCorrection,
                    Settings.maxAngularCorrection
                )
                limitImpulse = -m_motorMass * C
                angularError = abs(C)
            } else if (m_limitState == LimitState.AT_LOWER) {
                var C = angle - lowerLimit
                angularError = -C

                // Prevent large angular corrections and allow some slop.
                C = clamp(
                    C + Settings.angularSlop,
                    -Settings.maxAngularCorrection, 0.0
                )
                limitImpulse = -m_motorMass * C
            } else if (m_limitState == LimitState.AT_UPPER) {
                var C = angle - upperLimit
                angularError = C

                // Prevent large angular corrections and allow some slop.
                C = clamp(
                    C - Settings.angularSlop, 0.0,
                    Settings.maxAngularCorrection
                )
                limitImpulse = -m_motorMass * C
            }

            aA -= m_invIA * limitImpulse
            aB += m_invIB * limitImpulse
        }
        // Solve point-to-point constraint.
        run {
            qA.set(aA)
            qB.set(aB)

            val rA = pool.popMutableVector2d()
            val rB = pool.popMutableVector2d()
            val C = pool.popMutableVector2d()
            val impulse = pool.popMutableVector2d()

            C.set(localAnchorA)
            C.subtract(m_localCenterA)
            Rot.mulToOut(qA, C, rA)
            C.set(localAnchorB)
            C.subtract(m_localCenterB)
            Rot.mulToOut(qB, C, rB)
            C.set(cB)
            C.add(rB)
            C.subtract(cA)
            C.subtract(rA)
            positionError = C.length()

            val mA = m_invMassA
            val mB = m_invMassB
            val iA = m_invIA
            val iB = m_invIB

            val K = pool.popMatrix2d()
            K.xx = mA + mB + iA * rA.y * rA.y + iB * rB.y * rB.y
            K.xy = -iA * rA.x * rA.y - iB * rB.x * rB.y
            K.yx = K.xy
            K.yy = mA + mB + iA * rA.x * rA.x + iB * rB.x * rB.x
            K.solveToOut(C, impulse)
            impulse.negate()

            cA.x -= mA * impulse.x
            cA.y -= mA * impulse.y
            aA -= iA * (rA cross impulse)

            cB.x += mB * impulse.x
            cB.y += mB * impulse.y
            aB += iB * (rB cross impulse)

            pool.pushMutableVector2d(4)
            pool.pushMatrix2d(1)
        }
        // data.positions[m_indexA].c.set(cA);
        data.positions[m_indexA].a = aA
        // data.positions[m_indexB].c.set(cB);
        data.positions[m_indexB].a = aB

        pool.pushRot(2)

        return positionError <= Settings.linearSlop && angularError <= Settings.angularSlop
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
        argOut.setXY(m_impulse.x, m_impulse.y).multiply(
            inv_dt
        )
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return inv_dt * m_impulse.z
    }

    fun enableMotor(flag: Boolean) {
        bodyA.isAwake = true
        bodyB.isAwake = true
        isMotorEnabled = flag
    }

    fun getMotorTorque(inv_dt: Double): Double {
        return m_motorImpulse * inv_dt
    }

    fun enableLimit(flag: Boolean) {
        if (flag != isLimitEnabled) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            isLimitEnabled = flag
            m_impulse.z = 0.0
        }
    }

    fun setLimits(
        lower: Double,
        upper: Double
    ) {
        assert { lower <= upper }
        if (lower != lowerLimit || upper != upperLimit) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            m_impulse.z = 0.0
            lowerLimit = lower
            upperLimit = upper
        }
    }
}
