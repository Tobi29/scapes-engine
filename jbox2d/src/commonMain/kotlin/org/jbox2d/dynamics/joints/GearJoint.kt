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
 * Created at 11:34:45 AM Jan 23, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.cross
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.SolverData
import org.jbox2d.pooling.IWorldPool
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert

//Gear Joint:
//C0 = (coordinate1 + ratio * coordinate2)_initial
//C = (coordinate1 + ratio * coordinate2) - C0 = 0
//J = [J1 ratio * J2]
//K = J * invM * JT
//= J1 * invM1 * J1T + ratio * ratio * J2 * invM2 * J2T
//
//Revolute:
//coordinate = rotation
//Cdot = angularVelocity
//J = [0 0 1]
//K = J * invM * JT = invI
//
//Prismatic:
//coordinate = dot(p - pg, ug)
//Cdot = dot(v + cross(w, r), ug)
//J = [ug cross(r, ug)]
//K = J * invM * JT = invMass + invI * cross(r, ug)^2

/**
 * A gear joint is used to connect two joints together. Either joint can be a revolute or prismatic
 * joint. You specify a gear ratio to bind the motions together: coordinate1 + ratio * coordinate2 =
 * constant The ratio can be negative or positive. If one joint is a revolute joint and the other
 * joint is a prismatic joint, then the ratio will have units of length or units of 1/length.
 *
 * @warning The revolute and prismatic joints must be attached to fixed bodies (which must be body1
 * on those joints).
 * @warning You have to manually destroy the gear joint if joint1 or joint2 is destroyed.
 * @author Daniel Murphy
 */
class GearJoint(
    argWorldPool: IWorldPool,
    def: GearJointDef
) : Joint(argWorldPool, def) {

    val joint1: Joint = def.joint1!!
    val joint2: Joint = def.joint2!!

    // Body A is connected to body C
    // Body B is connected to body D
    private val m_bodyC: Body?
    private val m_bodyD: Body?

    // Solver shared
    private val m_localAnchorA = MutableVector2d()
    private val m_localAnchorB = MutableVector2d()
    private val m_localAnchorC = MutableVector2d()
    private val m_localAnchorD = MutableVector2d()

    private val m_localAxisC = MutableVector2d()
    private val m_localAxisD = MutableVector2d()

    private var m_referenceAngleA: Double = 0.0
    private var m_referenceAngleB: Double = 0.0

    private val m_constant: Double
    var ratio: Double = 0.0

    private var m_impulse: Double = 0.0

    // Solver temp
    private var m_indexA: Int = 0
    private var m_indexB: Int = 0
    private var m_indexC: Int = 0
    private var m_indexD: Int = 0
    private val m_lcA = MutableVector2d()
    private val m_lcB = MutableVector2d()
    private val m_lcC = MutableVector2d()
    private val m_lcD = MutableVector2d()
    private var m_mA: Double = 0.0
    private var m_mB: Double = 0.0
    private var m_mC: Double = 0.0
    private var m_mD: Double = 0.0
    private var m_iA: Double = 0.0
    private var m_iB: Double = 0.0
    private var m_iC: Double = 0.0
    private var m_iD: Double = 0.0
    private val m_JvAC = MutableVector2d()
    private val m_JvBD = MutableVector2d()
    private var m_JwA: Double = 0.0
    private var m_JwB: Double = 0.0
    private var m_JwC: Double = 0.0
    private var m_JwD: Double = 0.0
    private var m_mass: Double = 0.0

    init {
        assert { joint1 is RevoluteJoint || joint1 is PrismaticJoint }
        assert { joint1 is RevoluteJoint || joint1 is PrismaticJoint }

        val coordinateA: Double
        val coordinateB: Double

        // TODO_ERIN there might be some problem with the joint edges in Joint.

        m_bodyC = joint1.bodyA
        bodyA = joint1.bodyB

        // Get geometry of joint1
        val xfA = bodyA.transform
        val aA = bodyA.m_sweep.a
        val xfC = m_bodyC.transform
        val aC = m_bodyC.m_sweep.a

        if (joint1 is RevoluteJoint) {
            val revolute = def.joint1 as RevoluteJoint
            m_localAnchorC.set(revolute.localAnchorA)
            m_localAnchorA.set(revolute.localAnchorB)
            m_referenceAngleA = revolute.referenceAngle
            m_localAxisC.setXY(0.0, 0.0)

            coordinateA = aA - aC - m_referenceAngleA
        } else {
            val pA = pool.popMutableVector2d()
            val temp = pool.popMutableVector2d()
            val prismatic = def.joint1 as PrismaticJoint
            m_localAnchorC.set(prismatic.localAnchorA)
            m_localAnchorA.set(prismatic.localAnchorB)
            m_referenceAngleA = prismatic.referenceAngle
            m_localAxisC.set(prismatic.localAxisA)

            val pC = m_localAnchorC
            Rot.mulToOut(xfA.q, m_localAnchorA, temp)
            temp.add(xfA.p)
            temp.subtract(xfC.p)
            Rot.mulTrans(xfC.q, temp, pA)
            pA.subtract(pC)
            coordinateA = (pA dot m_localAxisC)
            pool.pushMutableVector2d(2)
        }

        m_bodyD = joint2.bodyA
        bodyB = joint2.bodyB

        // Get geometry of joint2
        val xfB = bodyB.transform
        val aB = bodyB.m_sweep.a
        val xfD = m_bodyD.transform
        val aD = m_bodyD.m_sweep.a

        if (joint2 is RevoluteJoint) {
            val revolute = def.joint2 as RevoluteJoint
            m_localAnchorD.set(revolute.localAnchorA)
            m_localAnchorB.set(revolute.localAnchorB)
            m_referenceAngleB = revolute.referenceAngle
            m_localAxisD.setXY(0.0, 0.0)

            coordinateB = aB - aD - m_referenceAngleB
        } else {
            val pB = pool.popMutableVector2d()
            val temp = pool.popMutableVector2d()
            val prismatic = def.joint2 as PrismaticJoint
            m_localAnchorD.set(prismatic.localAnchorA)
            m_localAnchorB.set(prismatic.localAnchorB)
            m_referenceAngleB = prismatic.referenceAngle
            m_localAxisD.set(prismatic.localAxisA)

            val pD = m_localAnchorD
            Rot.mulToOut(xfB.q, m_localAnchorB, temp)
            temp.add(xfB.p)
            temp.subtract(xfD.p)
            Rot.mulTrans(xfD.q, temp, pB)
            pB.subtract(pD)
            coordinateB = (pB dot m_localAxisD)
            pool.pushMutableVector2d(2)
        }

        ratio = def.ratio

        m_constant = coordinateA + ratio * coordinateB

        m_impulse = 0.0
    }

    override fun getAnchorA(argOut: MutableVector2d) {
        bodyA.getWorldPointToOut(m_localAnchorA, argOut)
    }

    override fun getAnchorB(argOut: MutableVector2d) {
        bodyB.getWorldPointToOut(m_localAnchorB, argOut)
    }

    override fun getReactionForce(
        inv_dt: Double,
        argOut: MutableVector2d
    ) {
        argOut.set(m_JvAC)
        argOut.multiply(m_impulse)
        argOut.multiply(inv_dt)
    }

    override fun getReactionTorque(inv_dt: Double): Double {
        val L = m_impulse * m_JwA
        return inv_dt * L
    }

    override fun initVelocityConstraints(data: SolverData) {
        m_indexA = bodyA.m_islandIndex
        m_indexB = bodyB.m_islandIndex
        m_indexC = m_bodyC!!.m_islandIndex
        m_indexD = m_bodyD!!.m_islandIndex
        m_lcA.set(bodyA.m_sweep.localCenter)
        m_lcB.set(bodyB.m_sweep.localCenter)
        m_lcC.set(m_bodyC.m_sweep.localCenter)
        m_lcD.set(m_bodyD.m_sweep.localCenter)
        m_mA = bodyA.m_invMass
        m_mB = bodyB.m_invMass
        m_mC = m_bodyC.m_invMass
        m_mD = m_bodyD.m_invMass
        m_iA = bodyA.m_invI
        m_iB = bodyB.m_invI
        m_iC = m_bodyC.m_invI
        m_iD = m_bodyD.m_invI

        // Vec2 cA = data.positions[m_indexA].c;
        val aA = data.positions[m_indexA].a
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w

        // Vec2 cB = data.positions[m_indexB].c;
        val aB = data.positions[m_indexB].a
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        // Vec2 cC = data.positions[m_indexC].c;
        val aC = data.positions[m_indexC].a
        val vC = data.velocities[m_indexC].v
        var wC = data.velocities[m_indexC].w

        // Vec2 cD = data.positions[m_indexD].c;
        val aD = data.positions[m_indexD].a
        val vD = data.velocities[m_indexD].v
        var wD = data.velocities[m_indexD].w

        val qA = pool.popRot()
        val qB = pool.popRot()
        val qC = pool.popRot()
        val qD = pool.popRot()
        qA.set(aA)
        qB.set(aB)
        qC.set(aC)
        qD.set(aD)

        m_mass = 0.0

        val temp = pool.popMutableVector2d()

        if (joint1 is RevoluteJoint) {
            m_JvAC.setXY(0.0, 0.0)
            m_JwA = 1.0
            m_JwC = 1.0
            m_mass += m_iA + m_iC
        } else {
            val rC = pool.popMutableVector2d()
            val rA = pool.popMutableVector2d()
            Rot.mulToOut(qC, m_localAxisC, m_JvAC)
            temp.set(m_localAnchorC)
            temp.subtract(m_lcC)
            Rot.mulToOut(qC, temp, rC)
            temp.set(m_localAnchorA)
            temp.subtract(m_lcA)
            Rot.mulToOut(qA, temp, rA)
            m_JwC = (rC cross m_JvAC)
            m_JwA = (rA cross m_JvAC)
            m_mass += m_mC + m_mA + m_iC * m_JwC * m_JwC + m_iA * m_JwA * m_JwA
            pool.pushMutableVector2d(2)
        }

        if (joint2 is RevoluteJoint) {
            m_JvBD.setXY(0.0, 0.0)
            m_JwB = ratio
            m_JwD = ratio
            m_mass += ratio * ratio * (m_iB + m_iD)
        } else {
            val u = pool.popMutableVector2d()
            val rD = pool.popMutableVector2d()
            val rB = pool.popMutableVector2d()
            Rot.mulToOut(qD, m_localAxisD, u)
            temp.set(m_localAnchorD)
            temp.subtract(m_lcD)
            Rot.mulToOut(qD, temp, rD)
            temp.set(m_localAnchorB)
            temp.subtract(m_lcB)
            Rot.mulToOut(qB, temp, rB)
            m_JvBD.set(u).multiply(ratio)
            m_JwD = ratio * (rD cross u)
            m_JwB = ratio * (rB cross u)
            m_mass += ratio * ratio * (m_mD + m_mB) + m_iD * m_JwD * m_JwD + m_iB * m_JwB * m_JwB
            pool.pushMutableVector2d(3)
        }

        // Compute effective mass.
        m_mass = if (m_mass > 0.0) 1.0 / m_mass else 0.0

        if (data.step.warmStarting) {
            vA.x += m_mA * m_impulse * m_JvAC.x
            vA.y += m_mA * m_impulse * m_JvAC.y
            wA += m_iA * m_impulse * m_JwA

            vB.x += m_mB * m_impulse * m_JvBD.x
            vB.y += m_mB * m_impulse * m_JvBD.y
            wB += m_iB * m_impulse * m_JwB

            vC.x -= m_mC * m_impulse * m_JvAC.x
            vC.y -= m_mC * m_impulse * m_JvAC.y
            wC -= m_iC * m_impulse * m_JwC

            vD.x -= m_mD * m_impulse * m_JvBD.x
            vD.y -= m_mD * m_impulse * m_JvBD.y
            wD -= m_iD * m_impulse * m_JwD
        } else {
            m_impulse = 0.0
        }
        pool.pushMutableVector2d(1)
        pool.pushRot(4)

        // data.velocities[m_indexA].v = vA;
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v = vB;
        data.velocities[m_indexB].w = wB
        // data.velocities[m_indexC].v = vC;
        data.velocities[m_indexC].w = wC
        // data.velocities[m_indexD].v = vD;
        data.velocities[m_indexD].w = wD
    }

    override fun solveVelocityConstraints(data: SolverData) {
        val vA = data.velocities[m_indexA].v
        var wA = data.velocities[m_indexA].w
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w
        val vC = data.velocities[m_indexC].v
        var wC = data.velocities[m_indexC].w
        val vD = data.velocities[m_indexD].v
        var wD = data.velocities[m_indexD].w

        val temp1 = pool.popMutableVector2d()
        val temp2 = pool.popMutableVector2d()
        temp1.set(vA)
        temp1.subtract(vC)
        temp2.set(vB)
        temp2.subtract(vD)
        var Cdot = (m_JvAC dot temp1) + (m_JvBD dot temp2)
        Cdot += m_JwA * wA - m_JwC * wC + (m_JwB * wB - m_JwD * wD)
        pool.pushMutableVector2d(2)

        val impulse = -m_mass * Cdot
        m_impulse += impulse

        vA.x += m_mA * impulse * m_JvAC.x
        vA.y += m_mA * impulse * m_JvAC.y
        wA += m_iA * impulse * m_JwA

        vB.x += m_mB * impulse * m_JvBD.x
        vB.y += m_mB * impulse * m_JvBD.y
        wB += m_iB * impulse * m_JwB

        vC.x -= m_mC * impulse * m_JvAC.x
        vC.y -= m_mC * impulse * m_JvAC.y
        wC -= m_iC * impulse * m_JwC

        vD.x -= m_mD * impulse * m_JvBD.x
        vD.y -= m_mD * impulse * m_JvBD.y
        wD -= m_iD * impulse * m_JwD


        // data.velocities[m_indexA].v = vA;
        data.velocities[m_indexA].w = wA
        // data.velocities[m_indexB].v = vB;
        data.velocities[m_indexB].w = wB
        // data.velocities[m_indexC].v = vC;
        data.velocities[m_indexC].w = wC
        // data.velocities[m_indexD].v = vD;
        data.velocities[m_indexD].w = wD
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        val cA = data.positions[m_indexA].c
        var aA = data.positions[m_indexA].a
        val cB = data.positions[m_indexB].c
        var aB = data.positions[m_indexB].a
        val cC = data.positions[m_indexC].c
        var aC = data.positions[m_indexC].a
        val cD = data.positions[m_indexD].c
        var aD = data.positions[m_indexD].a

        val qA = pool.popRot()
        val qB = pool.popRot()
        val qC = pool.popRot()
        val qD = pool.popRot()
        qA.set(aA)
        qB.set(aB)
        qC.set(aC)
        qD.set(aD)

        val linearError = 0.0

        val coordinateA: Double
        val coordinateB: Double

        val temp = pool.popMutableVector2d()
        val JvAC = pool.popMutableVector2d()
        val JvBD = pool.popMutableVector2d()
        val JwA: Double
        val JwB: Double
        val JwC: Double
        val JwD: Double
        var mass = 0.0

        if (joint1 is RevoluteJoint) {
            JvAC.setXY(0.0, 0.0)
            JwA = 1.0
            JwC = 1.0
            mass += m_iA + m_iC

            coordinateA = aA - aC - m_referenceAngleA
        } else {
            val rC = pool.popMutableVector2d()
            val rA = pool.popMutableVector2d()
            val pC = pool.popMutableVector2d()
            val pA = pool.popMutableVector2d()
            Rot.mulToOut(qC, m_localAxisC, JvAC)
            temp.set(m_localAnchorC)
            temp.subtract(m_lcC)
            Rot.mulToOut(qC, temp, rC)
            temp.set(m_localAnchorA)
            temp.subtract(m_lcA)
            Rot.mulToOut(qA, temp, rA)
            JwC = rC cross JvAC
            JwA = rA cross JvAC
            mass += m_mC + m_mA + m_iC * JwC * JwC + m_iA * JwA * JwA

            pC.set(m_localAnchorC)
            pC.subtract(m_lcC)
            temp.set(rA)
            temp.add(cA)
            temp.subtract(cC)
            Rot.mulTrans(qC, temp, pA)
            pA.subtract(pC)
            coordinateA = pA dot m_localAxisC
            pool.pushMutableVector2d(4)
        }

        if (joint2 is RevoluteJoint) {
            JvBD.setXY(0.0, 0.0)
            JwB = ratio
            JwD = ratio
            mass += ratio * ratio * (m_iB + m_iD)

            coordinateB = aB - aD - m_referenceAngleB
        } else {
            val u = pool.popMutableVector2d()
            val rD = pool.popMutableVector2d()
            val rB = pool.popMutableVector2d()
            val pD = pool.popMutableVector2d()
            val pB = pool.popMutableVector2d()
            Rot.mulToOut(qD, m_localAxisD, u)
            temp.set(m_localAnchorD)
            temp.subtract(m_lcD)
            Rot.mulToOut(qD, temp, rD)
            temp.set(m_localAnchorB)
            temp.subtract(m_lcB)
            Rot.mulToOut(qB, temp, rB)
            JvBD.set(u).multiply(ratio)
            JwD = rD cross u
            JwB = rB cross u
            mass += ratio * ratio * (m_mD + m_mB) + m_iD * JwD * JwD + m_iB * JwB * JwB

            pD.set(m_localAnchorD).subtract(m_lcD)
            temp.set(rB)
            temp.add(cB)
            temp.subtract(cD)
            Rot.mulTrans(qD, temp, pB)
            pB.subtract(pD)
            coordinateB = pB dot m_localAxisD
            pool.pushMutableVector2d(5)
        }

        val C = coordinateA + ratio * coordinateB - m_constant

        var impulse = 0.0
        if (mass > 0.0) {
            impulse = -C / mass
        }
        pool.pushMutableVector2d(3)
        pool.pushRot(4)

        cA.x += m_mA * impulse * JvAC.x
        cA.y += m_mA * impulse * JvAC.y
        aA += m_iA * impulse * JwA

        cB.x += m_mB * impulse * JvBD.x
        cB.y += m_mB * impulse * JvBD.y
        aB += m_iB * impulse * JwB

        cC.x -= m_mC * impulse * JvAC.x
        cC.y -= m_mC * impulse * JvAC.y
        aC -= m_iC * impulse * JwC

        cD.x -= m_mD * impulse * JvBD.x
        cD.y -= m_mD * impulse * JvBD.y
        aD -= m_iD * impulse * JwD

        // data.positions[m_indexA].c = cA;
        data.positions[m_indexA].a = aA
        // data.positions[m_indexB].c = cB;
        data.positions[m_indexB].a = aB
        // data.positions[m_indexC].c = cC;
        data.positions[m_indexC].a = aC
        // data.positions[m_indexD].c = cD;
        data.positions[m_indexD].a = aD

        // TODO_ERIN not implemented
        return linearError < Settings.linearSlop
    }
}
