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
import org.tobi29.math.matrix.MutableMatrix3d
import org.tobi29.math.matrix.xz
import org.tobi29.math.matrix.yz
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.clamp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//Linear constraint (point-to-line)
//d = p2 - p1 = x2 + r2 - x1 - r1
//C = dot(perp, d)
//Cdot = dot(d, cross(w1, perp)) + dot(perp, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//   = -dot(perp, v1) - dot(cross(d + r1, perp), w1) + dot(perp, v2) + dot(cross(r2, perp), v2)
//J = [-perp, -cross(d + r1, perp), perp, cross(r2,perp)]
//
//Angular constraint
//C = a2 - a1 + a_initial
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//
//K = J * invM * JT
//
//J = [-a -s1 a s2]
//  [0  -1  0  1]
//a = perp
//s1 = cross(d + r1, a) = cross(p2 - x1, a)
//s2 = cross(r2, a) = cross(p2 - x2, a)


//Motor/Limit linear constraint
//C = dot(ax1, d)
//Cdot = = -dot(ax1, v1) - dot(cross(d + r1, ax1), w1) + dot(ax1, v2) + dot(cross(r2, ax1), v2)
//J = [-ax1 -cross(d+r1,ax1) ax1 cross(r2,ax1)]

//Block Solver
//We develop a block solver that includes the joint limit. This makes the limit stiff (inelastic) even
//when the mass has poor distribution (leading to large torques about the joint anchor points).
//
//The Jacobian has 3 rows:
//J = [-uT -s1 uT s2] // linear
//  [0   -1   0  1] // angular
//  [-vT -a1 vT a2] // limit
//
//u = perp
//v = axis
//s1 = cross(d + r1, u), s2 = cross(r2, u)
//a1 = cross(d + r1, v), a2 = cross(r2, v)

//M * (v2 - v1) = JT * df
//J * v2 = bias
//
//v2 = v1 + invM * JT * df
//J * (v1 + invM * JT * df) = bias
//K * df = bias - J * v1 = -Cdot
//K = J * invM * JT
//Cdot = J * v1 - bias
//
//Now solve for f2.
//df = f2 - f1
//K * (f2 - f1) = -Cdot
//f2 = invK * (-Cdot) + f1
//
//Clamp accumulated limit impulse.
//lower: f2(3) = max(f2(3), 0)
//upper: f2(3) = min(f2(3), 0)
//
//Solve for correct f2(1:2)
//K(1:2, 1:2) * f2(1:2) = -Cdot(1:2) - K(1:2,3) * f2(3) + K(1:2,1:3) * f1
//                    = -Cdot(1:2) - K(1:2,3) * f2(3) + K(1:2,1:2) * f1(1:2) + K(1:2,3) * f1(3)
//K(1:2, 1:2) * f2(1:2) = -Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3)) + K(1:2,1:2) * f1(1:2)
//f2(1:2) = invK(1:2,1:2) * (-Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3))) + f1(1:2)
//
//Now compute impulse to be applied:
//df = f2 - f1

/**
 * A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in
 * bodyA. Relative rotation is prevented. You can use a joint limit to restrict the range of motion
 * and a joint motor to drive the motion or to model joint friction.
 *
 * @author Daniel
 */
class PrismaticJoint(
    argWorld: IWorldPool,
    def: PrismaticJointDef
) : Joint(argWorld, def) {

    // Solver shared
    val localAnchorA: MutableVector2d = MutableVector2d(def.localAnchorA)
    val localAnchorB: MutableVector2d = MutableVector2d(def.localAnchorB)
    val localAxisA: MutableVector2d = MutableVector2d(def.localAxisA)
    private val m_localYAxisA: MutableVector2d
    var referenceAngle: Double = 0.0
    private val m_impulse = MutableVector3d()
    private var m_motorImpulse: Double = 0.0
    /**
     * Get the lower joint limit, usually in meters.
     *
     * @return
     */
    var lowerLimit: Double = 0.0
        private set
    /**
     * Get the upper joint limit, usually in meters.
     *
     * @return
     */
    var upperLimit: Double = 0.0
        private set
    private var m_maxMotorForce: Double = 0.0
    private var m_motorSpeed: Double = 0.0
    /**
     * Is the joint limit enabled?
     *
     * @return
     */
    var isLimitEnabled: Boolean = false
        private set
    /**
     * Is the joint motor enabled?
     *
     * @return
     */
    var isMotorEnabled: Boolean = false
        private set
    private var m_limitState: LimitState? = null

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private val m_localCenterA = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassA: Double = 0.0
    private var m_invMassB: Double = 0.0
    private var m_invIA: Double = 0.0
    private var m_invIB: Double = 0.0
    private val m_axis: MutableVector2d
    private val m_perp: MutableVector2d
    private var m_s1: Double = 0.0
    private var m_s2: Double = 0.0
    private var m_a1: Double = 0.0
    private var m_a2: Double = 0.0
    private val m_K: MutableMatrix3d
    private var m_motorMass: Double =
        0.0 // effective mass for motor/limit translational constraint.

    /**
     * Get the current joint translation, usually in meters.
     */
    val jointSpeed: Double
        get() {
            val bA = bodyA
            val bB = bodyB

            val temp = pool.popMutableVector2d()
            val rA = pool.popMutableVector2d()
            val rB = pool.popMutableVector2d()
            val p1 = pool.popMutableVector2d()
            val p2 = pool.popMutableVector2d()
            val d = pool.popMutableVector2d()
            val axis = pool.popMutableVector2d()
            val temp2 = pool.popMutableVector2d()
            val temp3 = pool.popMutableVector2d()

            temp.set(localAnchorA).subtract(bA.m_sweep.localCenter)
            Rot.mulToOut(bA.transform.q, temp, rA)

            temp.set(localAnchorB).subtract(bB.m_sweep.localCenter)
            Rot.mulToOut(bB.transform.q, temp, rB)

            p1.set(bA.m_sweep.c).add(rA)
            p2.set(bB.m_sweep.c).add(rB)

            d.set(p2).subtract(p1)
            Rot.mulToOut(bA.transform.q, localAxisA, axis)

            val vA = bA.m_linearVelocity
            val vB = bB.m_linearVelocity
            val wA = bA.m_angularVelocity
            val wB = bB.m_angularVelocity


            temp.set(wA cross axis)
            temp2.set(wB cross rB)
            temp3.set(wA cross rA)

            temp2.add(vB)
            temp2.subtract(vA)
            temp2.subtract(temp3)
            val speed = (d dot temp) + (axis dot temp2)

            pool.pushMutableVector2d(9)

            return speed
        }

    val jointTranslation: Double
        get() {
            val pA = pool.popMutableVector2d()
            val pB = pool.popMutableVector2d()
            val axis = pool.popMutableVector2d()
            bodyA.getWorldPointToOut(localAnchorA, pA)
            bodyB.getWorldPointToOut(localAnchorB, pB)
            bodyA.getWorldVectorToOut(localAxisA, axis)
            pB.subtract(pA)
            val translation = (pB dot axis)
            pool.pushMutableVector2d(3)
            return translation
        }

    /**
     * Get the motor speed, usually in meters per second.
     *
     * @return
     */
    /**
     * Set the motor speed, usually in meters per second.
     *
     * @param speed
     */
    var motorSpeed: Double
        get() = m_motorSpeed
        set(speed) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            m_motorSpeed = speed
        }

    /**
     * Set the maximum motor force, usually in N.
     *
     * @param force
     */
    var maxMotorForce: Double
        get() = m_maxMotorForce
        set(force) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            m_maxMotorForce = force
        }

    init {
        localAxisA.normalize()
        m_localYAxisA = MutableVector2d()
        m_localYAxisA.set(1.0 cross localAxisA)
        referenceAngle = def.referenceAngle

        m_motorMass = 0.0
        m_motorImpulse = 0.0

        lowerLimit = def.lowerTranslation
        upperLimit = def.upperTranslation
        m_maxMotorForce = def.maxMotorForce
        m_motorSpeed = def.motorSpeed
        isLimitEnabled = def.enableLimit
        isMotorEnabled = def.enableMotor
        m_limitState = LimitState.INACTIVE

        m_K = MutableMatrix3d()
        m_axis = MutableVector2d()
        m_perp = MutableVector2d()
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
        temp.set(m_axis).multiply(m_motorImpulse + m_impulse.z)
        argOut.set(m_perp)
        argOut.multiply(m_impulse.x)
        argOut.add(temp)
        argOut.multiply(inv_dt)
        pool.pushMutableVector2d(1)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return inv_dt * m_impulse.y
    }

    /**
     * Enable/disable the joint limit.
     *
     * @param flag
     */
    fun enableLimit(flag: Boolean) {
        if (flag != isLimitEnabled) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            isLimitEnabled = flag
            m_impulse.z = 0.0
        }
    }

    /**
     * Set the joint limits, usually in meters.
     *
     * @param lower
     * @param upper
     */
    fun setLimits(
        lower: Double,
        upper: Double
    ) {
        assert { lower <= upper }
        if (lower != lowerLimit || upper != upperLimit) {
            bodyA.isAwake = true
            bodyB.isAwake = true
            lowerLimit = lower
            upperLimit = upper
            m_impulse.z = 0.0
        }
    }

    /**
     * Enable/disable the joint motor.
     *
     * @param flag
     */
    fun enableMotor(flag: Boolean) {
        bodyA.isAwake = true
        bodyB.isAwake = true
        isMotorEnabled = flag
    }

    /**
     * Get the current motor force, usually in N.
     *
     * @param inv_dt
     * @return
     */
    fun getMotorForce(inv_dt: Double): Double {
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
        val d = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()

        qA.set(aA)
        qB.set(aB)

        // Compute the effective masses.
        d.set(localAnchorA)
        d.subtract(m_localCenterA)
        Rot.mulToOut(qA, d, rA)
        d.set(localAnchorB)
        d.subtract(m_localCenterB)
        Rot.mulToOut(qB, d, rB)
        d.set(cB)
        d.subtract(cA)
        d.add(rB)
        d.subtract(rA)

        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        // Compute motor Jacobian and effective mass.
        run {
            Rot.mulToOut(qA, localAxisA, m_axis)
            temp.set(d).add(rA)
            m_a1 = (temp cross m_axis)
            m_a2 = (rB cross m_axis)

            m_motorMass = (mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2)
            if (m_motorMass > 0.0) {
                m_motorMass = 1.0 / m_motorMass
            }
        }

        // Prismatic constraint.
        run {
            Rot.mulToOut(qA, m_localYAxisA, m_perp)

            temp.set(d).add(rA)
            m_s1 = (temp cross m_perp)
            m_s2 = (rB cross m_perp)

            val k11 = mA + mB + iA * m_s1 * m_s1 + iB * m_s2 * m_s2
            val k12 = iA * m_s1 + iB * m_s2
            val k13 = iA * m_s1 * m_a1 + iB * m_s2 * m_a2
            var k22 = iA + iB
            if (k22 == 0.0) {
                // For bodies with fixed rotation.
                k22 = 1.0
            }
            val k23 = iA * m_a1 + iB * m_a2
            val k33 = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2

            m_K.set(
                k11, k12, k13,
                k12, k22, k23,
                k13, k23, k33
            )
        }

        // Compute motor and limit terms.
        if (isLimitEnabled) {

            val jointTranslation = (m_axis dot d)
            if (abs(upperLimit - lowerLimit) < 2.0 * Settings.linearSlop) {
                m_limitState = LimitState.EQUAL
            } else if (jointTranslation <= lowerLimit) {
                if (m_limitState != LimitState.AT_LOWER) {
                    m_limitState = LimitState.AT_LOWER
                    m_impulse.z = 0.0
                }
            } else if (jointTranslation >= upperLimit) {
                if (m_limitState != LimitState.AT_UPPER) {
                    m_limitState = LimitState.AT_UPPER
                    m_impulse.z = 0.0
                }
            } else {
                m_limitState = LimitState.INACTIVE
                m_impulse.z = 0.0
            }
        } else {
            m_limitState = LimitState.INACTIVE
            m_impulse.z = 0.0
        }

        if (isMotorEnabled == false) {
            m_motorImpulse = 0.0
        }

        if (data.step.warmStarting) {
            // Account for variable time step.
            m_impulse.multiply(data.step.dtRatio)
            m_motorImpulse *= data.step.dtRatio

            val P = pool.popMutableVector2d()
            temp.set(m_axis)
            temp.multiply(m_motorImpulse + m_impulse.z)
            P.set(m_perp)
            P.multiply(m_impulse.x)
            P.add(temp)

            val LA =
                m_impulse.x * m_s1 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a1
            val LB =
                m_impulse.x * m_s2 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a2

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB

            pool.pushMutableVector2d(1)
        } else {
            m_impulse.setXYZ(0.0, 0.0, 0.0)
            m_motorImpulse = 0.0
        }

        // data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushRot(2)
        pool.pushMutableVector2d(4)
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

        val temp = pool.popMutableVector2d()

        // Solve linear motor constraint.
        if (isMotorEnabled && m_limitState != LimitState.EQUAL) {
            temp.set(vB).subtract(vA)
            val Cdot = (m_axis dot temp) + m_a2 * wB - m_a1 * wA
            var impulse = m_motorMass * (m_motorSpeed - Cdot)
            val oldImpulse = m_motorImpulse
            val maxImpulse = data.step.dt * m_maxMotorForce
            m_motorImpulse = clamp(
                m_motorImpulse + impulse,
                -maxImpulse, maxImpulse
            )
            impulse = m_motorImpulse - oldImpulse

            val P = pool.popMutableVector2d()
            P.set(m_axis).multiply(impulse)
            val LA = impulse * m_a1
            val LB = impulse * m_a2

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB

            pool.pushMutableVector2d(1)
        }

        val Cdot1 = pool.popMutableVector2d()
        temp.set(vB).subtract(vA)
        Cdot1.x = (m_perp dot temp) + m_s2 * wB - m_s1 * wA
        Cdot1.y = wB - wA
        // System.out.println(Cdot1);

        if (isLimitEnabled && m_limitState != LimitState.INACTIVE) {
            // Solve prismatic and limit constraint in block form.
            val Cdot2: Double = (m_axis dot temp) + m_a2 * wB - m_a1 * wA
            temp.set(vB).subtract(vA)

            val Cdot = pool.popMutableVector3d()
            Cdot.setXYZ(Cdot1.x, Cdot1.y, Cdot2)
            Cdot.negate()

            val f1 = pool.popMutableVector3d()
            val df = pool.popMutableVector3d()

            f1.setXYZ(
                m_impulse.x, m_impulse.y,
                m_impulse.z
            )
            m_K.solve33ToOut(Cdot, df)
            // Cdot.negateLocal(); not used anymore
            m_impulse.add(df)

            if (m_limitState == LimitState.AT_LOWER) {
                m_impulse.z = max(m_impulse.z, 0.0)
            } else if (m_limitState == LimitState.AT_UPPER) {
                m_impulse.z = min(m_impulse.z, 0.0)
            }

            // f2(1:2) = invK(1:2,1:2) * (-Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3))) +
            // f1(1:2)
            val b = pool.popMutableVector2d()
            val f2r = pool.popMutableVector2d()

            temp.setXY(m_K.xz, m_K.yz).multiply(
                m_impulse.z - f1.z
            )
            b.set(Cdot1)
            b.negate()
            b.subtract(temp)

            m_K.solve22ToOut(b, f2r)
            f2r.addX(f1.x)
            f2r.addY(f1.y)
            m_impulse.x = f2r.x
            m_impulse.y = f2r.y

            df.setXYZ(
                m_impulse.x, m_impulse.y,
                m_impulse.z
            ).subtract(f1)

            val P = pool.popMutableVector2d()
            temp.set(m_axis)
            temp.multiply(df.z)
            P.set(m_perp)
            P.multiply(df.x)
            P.add(temp)

            val LA = df.x * m_s1 + df.y + df.z * m_a1
            val LB = df.x * m_s2 + df.y + df.z * m_a2

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB

            pool.pushMutableVector2d(3)
            pool.pushMutableVector3d(3)
        } else {
            // Limit is inactive, just solve the prismatic constraint in block form.
            val df = pool.popMutableVector2d()
            Cdot1.negate()
            m_K.solve22ToOut(Cdot1, df)
            Cdot1.negate()

            m_impulse.x += df.x
            m_impulse.y += df.y

            val P = pool.popMutableVector2d()
            P.set(m_perp).multiply(df.x)
            val LA = df.x * m_s1 + df.y
            val LB = df.x * m_s2 + df.y

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * LA

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * LB

            pool.pushMutableVector2d(2)
        }

        // data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(2)
    }


    override fun solvePositionConstraints(data: SolverData): Boolean {

        val qA = pool.popRot()
        val qB = pool.popRot()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()
        val d = pool.popMutableVector2d()
        val axis = pool.popMutableVector2d()
        val perp = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()
        val C1 = pool.popMutableVector2d()

        val impulse = pool.popMutableVector3d()

        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a

        qA.set(aA)
        qB.set(aB)

        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        // Compute fresh Jacobians
        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)
        d.set(cB)
        d.add(rB)
        d.subtract(cA)
        d.subtract(rA)

        Rot.mulToOut(qA, localAxisA, axis)
        temp.set(d)
        temp.add(rA)
        val a1 = temp cross axis
        val a2 = rB cross axis
        Rot.mulToOut(qA, m_localYAxisA, perp)

        temp.set(d)
        temp.add(rA)
        val s1 = temp cross perp
        val s2 = rB cross perp

        C1.x = perp dot d
        C1.y = aB - aA - referenceAngle

        var linearError = abs(C1.x)
        val angularError = abs(C1.y)

        var active = false
        var C2 = 0.0
        if (isLimitEnabled) {
            val translation = (axis dot d)
            if (abs(upperLimit - lowerLimit) < 2.0 * Settings.linearSlop) {
                // Prevent large angular corrections
                C2 = clamp(
                    translation, -Settings.maxLinearCorrection,
                    Settings.maxLinearCorrection
                )
                linearError = max(
                    linearError,
                    abs(translation)
                )
                active = true
            } else if (translation <= lowerLimit) {
                // Prevent large linear corrections and allow some slop.
                C2 = clamp(
                    translation - lowerLimit + Settings.linearSlop,
                    -Settings.maxLinearCorrection, 0.0
                )
                linearError = max(
                    linearError,
                    lowerLimit - translation
                )
                active = true
            } else if (translation >= upperLimit) {
                // Prevent large linear corrections and allow some slop.
                C2 = clamp(
                    translation - upperLimit - Settings.linearSlop, 0.0,
                    Settings.maxLinearCorrection
                )
                linearError = max(
                    linearError,
                    translation - upperLimit
                )
                active = true
            }
        }

        if (active) {
            val k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2
            val k12 = iA * s1 + iB * s2
            val k13 = iA * s1 * a1 + iB * s2 * a2
            var k22 = iA + iB
            if (k22 == 0.0) {
                // For fixed rotation
                k22 = 1.0
            }
            val k23 = iA * a1 + iB * a2
            val k33 = mA + mB + iA * a1 * a1 + iB * a2 * a2

            val K = pool.popMatrix3d()
            K.set(
                k11, k12, k13,
                k12, k22, k23,
                k13, k23, k33
            )

            val C = pool.popMutableVector3d()
            C.x = C1.x
            C.y = C1.y
            C.z = C2
            C.negate()

            K.solve33ToOut(C, impulse)
            pool.pushMutableVector3d(1)
            pool.pushMatrix3d(1)
        } else {
            val k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2
            val k12 = iA * s1 + iB * s2
            var k22 = iA + iB
            if (k22 == 0.0) {
                k22 = 1.0
            }

            val K = pool.popMatrix2d()
            K.set(
                k11, k12,
                k12, k22
            )

            // temp is impulse1
            C1.negate()
            K.solveToOut(C1, temp)
            C1.negate()

            impulse.x = temp.x
            impulse.y = temp.y
            impulse.z = 0.0

            pool.pushMatrix2d(1)
        }

        val Px = impulse.x * perp.x + impulse.z * axis.x
        val Py = impulse.x * perp.y + impulse.z * axis.y
        val LA = impulse.x * s1 + impulse.y + impulse.z * a1
        val LB = impulse.x * s2 + impulse.y + impulse.z * a2

        cA.x -= mA * Px
        cA.y -= mA * Py
        aA -= iA * LA
        cB.x += mB * Px
        cB.y += mB * Py
        aB += iB * LB

        // data.positions[m_indexA].c.set(cA);
        data.positions[m_indexA].a = aA
        // data.positions[m_indexB].c.set(cB);
        data.positions[m_indexB].a = aB

        pool.pushMutableVector2d(7)
        pool.pushMutableVector3d(1)
        pool.pushRot(2)

        return linearError <= Settings.linearSlop && angularError <= Settings.angularSlop
    }
}
