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
 * Created at 3:38:38 AM Jan 15, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.common.*
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.matrix.*
import org.tobi29.math.vector.*
import kotlin.math.PI
import kotlin.math.abs

//Point-to-point constraint
//C = p2 - p1
//Cdot = v2 - v1
//   = v2 + cross(w2, r2) - v1 - cross(w1, r1)
//J = [-I -r1_skew I r2_skew ]
//Identity used:
//w k % (rx i + ry j) = w * (-ry i + rx j)

//Angle constraint
//C = angle2 - angle1 - referenceAngle
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//K = invI1 + invI2

/**
 * A weld joint essentially glues two bodies together. A weld joint may distort somewhat because the
 * island constraint solver is approximate.
 *
 * @author Daniel Murphy
 */
class WeldJoint(
    argWorld: IWorldPool,
    def: WeldJointDef
) : Joint(argWorld, def) {

    var frequency: Double = 0.0
    var dampingRatio: Double = 0.0
    private var m_bias: Double = 0.0

    // Solver shared
    val localAnchorA: MutableVector2d = MutableVector2d(def.localAnchorA)
    val localAnchorB: MutableVector2d = MutableVector2d(def.localAnchorB)
    val referenceAngle: Double = def.referenceAngle
    private var m_gamma: Double = 0.0
    private val m_impulse = MutableVector3d()


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
    private val m_mass = MutableMatrix3d()

    init {
        frequency = def.frequencyHz
        dampingRatio = def.dampingRatio
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
        argOut.setXY(m_impulse.x, m_impulse.y)
        argOut.multiply(inv_dt)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        return inv_dt * m_impulse.z
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

        val K = pool.popMatrix3d()

        K.xx = mA + mB + m_rA.y * m_rA.y * iA + m_rB.y * m_rB.y * iB
        K.yx = -m_rA.y * m_rA.x * iA - m_rB.y * m_rB.x * iB
        K.zx = -m_rA.y * iA - m_rB.y * iB
        K.xy = K.yx
        K.yy = mA + mB + m_rA.x * m_rA.x * iA + m_rB.x * m_rB.x * iB
        K.zy = m_rA.x * iA + m_rB.x * iB
        K.xz = K.zx
        K.yz = K.zy
        K.zz = iA + iB

        if (frequency > 0.0) {
            K.getInverse22(m_mass)

            var invM = iA + iB
            val m = if (invM > 0.0) 1.0 / invM else 0.0

            val C = aB - aA - referenceAngle

            // Frequency
            val omega = 2.0 * PI * frequency

            // Damping coefficient
            val d = 2.0 * m * dampingRatio * omega

            // Spring stiffness
            val k = m * omega * omega

            // magic formulas
            val h = data.step.dt
            m_gamma = h * (d + h * k)
            m_gamma = if (m_gamma != 0.0) 1.0 / m_gamma else 0.0
            m_bias = (C * h * k * m_gamma)

            invM += m_gamma
            m_mass.zz = if (invM != 0.0) 1.0 / invM else 0.0
        } else {
            K.getSymInverse33(m_mass)
            m_gamma = 0.0
            m_bias = 0.0
        }

        if (data.step.warmStarting) {
            val P = pool.popMutableVector2d()
            // Scale impulses to support a variable time step.
            m_impulse.multiply(data.step.dtRatio)

            P.setXY(m_impulse.x, m_impulse.y)

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * ((m_rA cross P) + m_impulse.z)

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * ((m_rB cross P) + m_impulse.z)
            pool.pushMutableVector2d(1)
        } else {
            m_impulse.setXYZ(0.0, 0.0, 0.0)
        }

        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(1)
        pool.pushRot(2)
        pool.pushMatrix3d(1)
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

        val Cdot1 = pool.popMutableVector2d()
        val P = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()
        if (frequency > 0.0) {
            val Cdot2 = wB - wA

            val impulse2 = -m_mass.zz * (Cdot2 + m_bias + m_gamma * m_impulse.z)
            m_impulse.z += impulse2

            wA -= iA * impulse2
            wB += iB * impulse2

            Cdot1.set(wB cross m_rB)
            temp.set(wA cross m_rA)
            Cdot1.add(vB)
            Cdot1.subtract(vA)
            Cdot1.subtract(temp)

            matrix2dMultiply(
                m_mass.xx, m_mass.xy, m_mass.yx, m_mass.yy,
                Cdot1.x, Cdot1.y
            ) { x, y ->
                P.setXY(x, y)
            }
            P.negate()

            m_impulse.x += P.x
            m_impulse.y += P.y

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * (m_rA cross P)

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * (m_rB cross P)
        } else {
            temp.set(wA cross m_rA)
            Cdot1.set(wB cross m_rB)
            Cdot1.add(vB)
            Cdot1.subtract(vA)
            Cdot1.subtract(temp)
            val Cdot2 = wB - wA

            val Cdot = pool.popMutableVector3d()
            Cdot.setXYZ(Cdot1.x, Cdot1.y, Cdot2)

            val impulse = pool.popMutableVector3d()
            m_mass.multiply(Cdot, impulse)
            impulse.negate()
            m_impulse.add(impulse)

            P.setXY(impulse.x, impulse.y)

            vA.x -= mA * P.x
            vA.y -= mA * P.y
            wA -= iA * ((m_rA cross P) + impulse.z)

            vB.x += mB * P.x
            vB.y += mB * P.y
            wB += iB * ((m_rB cross P) + impulse.z)

            pool.pushMutableVector3d(2)
        }

        //    data.velocities[m_indexA].v.set(vA);
        data.velocities[m_indexA].w = wA
        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(3)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a
        val qA = pool.popRot()
        val qB = pool.popRot()
        val temp = pool.popMutableVector2d()
        val rA = pool.popMutableVector2d()
        val rB = pool.popMutableVector2d()

        qA.set(aA)
        qB.set(aB)

        val mA = m_invMassA
        val mB = m_invMassB
        val iA = m_invIA
        val iB = m_invIB

        temp.set(localAnchorA)
        temp.subtract(m_localCenterA)
        Rot.mulToOut(qA, temp, rA)
        temp.set(localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, rB)
        val positionError: Double
        val angularError: Double

        val K = pool.popMatrix3d()
        val C1 = pool.popMutableVector2d()
        val P = pool.popMutableVector2d()

        K.xx = mA + mB + rA.y * rA.y * iA + rB.y * rB.y * iB
        K.yx = -rA.y * rA.x * iA - rB.y * rB.x * iB
        K.zx = -rA.y * iA - rB.y * iB
        K.xy = K.yx
        K.yy = mA + mB + rA.x * rA.x * iA + rB.x * rB.x * iB
        K.zy = rA.x * iA + rB.x * iB
        K.xz = K.zx
        K.yz = K.zy
        K.zz = iA + iB
        if (frequency > 0.0) {
            C1.set(cB)
            C1.add(rB)
            C1.subtract(cA)
            C1.subtract(rA)

            positionError = C1.length()
            angularError = 0.0

            K.solve22ToOut(C1, P)
            P.negate()

            cA.x -= mA * P.x
            cA.y -= mA * P.y
            aA -= iA * (rA cross P)

            cB.x += mB * P.x
            cB.y += mB * P.y
            aB += iB * (rB cross P)
        } else {
            C1.set(cB)
            C1.add(rB)
            C1.subtract(cA)
            C1.subtract(rA)
            val C2 = aB - aA - referenceAngle

            positionError = C1.length()
            angularError = abs(C2)

            val C = pool.popMutableVector3d()
            val impulse = pool.popMutableVector3d()
            C.setXYZ(C1.x, C1.y, C2)

            K.solve33ToOut(C, impulse)
            impulse.negate()
            P.setXY(impulse.x, impulse.y)

            cA.x -= mA * P.x
            cA.y -= mA * P.y
            aA -= iA * ((rA cross P) + impulse.z)

            cB.x += mB * P.x
            cB.y += mB * P.y
            aB += iB * ((rB cross P) + impulse.z)
            pool.pushMutableVector3d(2)
        }

        //    data.positions[m_indexA].c.set(cA);
        data.positions[m_indexA].a = aA
        //    data.positions[m_indexB].c.set(cB);
        data.positions[m_indexB].a = aB

        pool.pushMutableVector2d(5)
        pool.pushRot(2)
        pool.pushMatrix3d(1)

        return positionError <= Settings.linearSlop && angularError <= Settings.angularSlop
    }
}
