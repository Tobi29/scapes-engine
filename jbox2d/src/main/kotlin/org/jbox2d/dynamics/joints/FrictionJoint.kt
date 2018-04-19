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
 * Created at 7:27:32 AM Jan 20, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.common.Rot
import org.jbox2d.common.cross
import org.jbox2d.common.invertToOut
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.matrix.*
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.clamp

/**
 * @author Daniel Murphy
 */
class FrictionJoint(
    argWorldPool: IWorldPool,
    def: FrictionJointDef
) : Joint(argWorldPool, def) {

    val localAnchorA = MutableVector2d(def.localAnchorA)
    val localAnchorB = MutableVector2d(def.localAnchorB)

    // Solver shared
    private val m_linearImpulse: MutableVector2d = MutableVector2d()
    private var m_angularImpulse: Double = 0.0
    private var m_maxForce: Double = 0.0
    private var m_maxTorque: Double = 0.0

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
    private val m_linearMass = Matrix2d()
    private var m_angularMass: Double = 0.0

    var maxForce: Double
        get() = m_maxForce
        set(force) {
            assert { force >= 0.0 }
            m_maxForce = force
        }

    var maxTorque: Double
        get() = m_maxTorque
        set(torque) {
            assert { torque >= 0.0 }
            m_maxTorque = torque
        }

    init {
        m_angularImpulse = 0.0

        m_maxForce = def.maxForce
        m_maxTorque = def.maxTorque
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
        argOut.set(m_linearImpulse).multiply(inv_dt)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return inv_dt * m_angularImpulse
    }

    /**
     * @see org.jbox2d.dynamics.joints.Joint.initVelocityConstraints
     */
    override fun initVelocityConstraints(data: SolverData) {
        m_indexA = bodyA.m_islandIndex
        m_indexB = bodyB.m_islandIndex
        m_localCenterA.set(bodyA.m_sweep.localCenter)
        m_localCenterB.set(bodyB.m_sweep.localCenter)
        m_invMassA = bodyA.m_invMass
        m_invMassB = bodyB.m_invMass
        m_invIA = bodyA.m_invI
        m_invIB = bodyB.m_invI

        val aA = data.positions[m_indexA].a
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w

        val aB = data.positions[m_indexB].a
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w


        val temp = pool.popMutableVector2d()
        val qA = pool.popRot()
        val qB = pool.popRot()

        qA.set(aA)
        qB.set(aB)

        // Compute the effective mass matrix.
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

        val K = pool.popMatrix2d()
        K.xx = mA + mB + iA * m_rA.y * m_rA.y + iB * m_rB.y * m_rB.y
        K.xy = -iA * m_rA.x * m_rA.y - iB * m_rB.x * m_rB.y
        K.yx = K.xy
        K.yy = mA + mB + iA * m_rA.x * m_rA.x + iB * m_rB.x * m_rB.x

        K.invertToOut(m_linearMass)

        m_angularMass = iA + iB
        if (m_angularMass > 0.0) {
            m_angularMass = 1.0 / m_angularMass
        }

        if (data.step.warmStarting) {
            // Scale impulses to support a variable time step.
            m_linearImpulse.multiply(data.step.dtRatio)
            m_angularImpulse *= data.step.dtRatio

            val P = pool.popMutableVector2d()
            P.set(m_linearImpulse)

            temp.set(P).multiply(mA)
            vA.subtract(temp)
            wA -= iA * ((m_rA cross P) + m_angularImpulse)

            temp.set(P).multiply(mB)
            vB.add(temp)
            wB += iB * ((m_rB cross P) + m_angularImpulse)

            pool.pushMutableVector2d(1)
        } else {
            m_linearImpulse.setXY(0.0, 0.0)
            m_angularImpulse = 0.0
        }
        //    data.velocities[m_indexA].v.set(vA);
        if (data.velocities[m_indexA].w != wA) {
            assert { data.velocities[m_indexA].w != wA }
        }
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushRot(2)
        pool.pushMutableVector2d(1)
        pool.pushMatrix2d(1)
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

        // Solve angular friction
        run {
            val Cdot = wB - wA
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

        // Solve linear friction
        run {
            val Cdot = pool.popMutableVector2d()
            val temp = pool.popMutableVector2d()

            temp.set(wA cross m_rA)
            Cdot.set(wB cross m_rB)
            Cdot.add(vB)
            Cdot.subtract(vA)
            Cdot.subtract(temp)

            val impulse = pool.popMutableVector2d()
            m_linearMass.multiply(Cdot, impulse)
            impulse.negate()


            val oldImpulse = pool.popMutableVector2d()
            oldImpulse.set(m_linearImpulse)
            m_linearImpulse.add(impulse)

            val maxImpulse = h * m_maxForce

            if (m_linearImpulse.lengthSqr() > maxImpulse * maxImpulse) {
                m_linearImpulse.normalize()
                m_linearImpulse.multiply(maxImpulse)
            }

            impulse.set(m_linearImpulse).subtract(oldImpulse)

            temp.set(impulse)
            temp.multiply(mA)
            vA.subtract(temp)
            wA -= iA * (m_rA.x * impulse.y - m_rA.y * impulse.x)

            temp.set(impulse)
            temp.multiply(mB)
            vB.add(temp)
            wB += iB * (m_rB.x * impulse.y - m_rB.y * impulse.x)

        }

        //    data.velocities[m_indexA].v.set(vA);
        if (data.velocities[m_indexA].w != wA) {
            assert { data.velocities[m_indexA].w != wA }
        }
        data.velocities[m_indexA].w = wA

        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(4)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        return true
    }
}
