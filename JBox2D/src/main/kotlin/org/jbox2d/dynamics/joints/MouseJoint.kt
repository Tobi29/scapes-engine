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
import kotlin.math.PI

/**
 * A mouse joint is used to make a point on a body track a specified world point. This a soft
 * constraint with a maximum force. This allows the constraint to stretch and without applying huge
 * forces. NOTE: this joint is not documented in the manual because it was developed to be used in
 * the testbed. If you want to learn how to use the mouse joint, look at the testbed.
 *
 * @author Daniel
 */
class MouseJoint(
    argWorld: IWorldPool,
    def: MouseJointDef
) : Joint(argWorld, def) {

    private val m_localAnchorB = MutableVector2d()
    private val _target = MutableVector2d()
    var target: Vector2d
        get() = _target.now()
        set(target) {
            if (bodyB.isAwake == false) {
                bodyB.isAwake = true
            }
            _target.set(target)
        }
    // / set/get the frequency in Hertz.
    var frequency: Double = 0.0
    // / set/get the damping ratio (dimensionless).
    var dampingRatio: Double = 0.0
    private var m_beta: Double = 0.0

    // Solver shared
    private val m_impulse = MutableVector2d()
    // / set/get the maximum force in Newtons.
    var maxForce: Double = 0.0
    private var m_gamma: Double = 0.0

    // Solver temp
    private var m_indexB: Int = 0
    private val m_rB = MutableVector2d()
    private val m_localCenterB = MutableVector2d()
    private var m_invMassB: Double = 0.0
    private var m_invIB: Double = 0.0
    private val m_mass = Matrix2d()
    private val m_C = MutableVector2d()

    init {
        assert { def.target.isValid }
        assert { def.maxForce >= 0 }
        assert { def.frequencyHz >= 0 }
        assert { def.dampingRatio >= 0 }

        _target.set(def.target)
        Transform.mulTransToOut(bodyB.transform, _target, m_localAnchorB)

        maxForce = def.maxForce
        m_impulse.setXY(0.0, 0.0)

        frequency = def.frequencyHz
        dampingRatio = def.dampingRatio

        m_beta = 0.0
        m_gamma = 0.0
    }

    override fun getAnchorA(argOut: MutableVector2d) {
        argOut.set(_target)
    }

    override fun getAnchorB(argOut: MutableVector2d) {
        bodyB.getWorldPointToOut(m_localAnchorB, argOut)
    }

    override fun getReactionForce(
        invDt: Double,
        argOut: MutableVector2d
    ) {
        argOut.set(m_impulse).multiply(invDt)
    }

    override fun getReactionTorque(invDt: Double): Double {
        return invDt * 0.0
    }

    override fun initVelocityConstraints(data: SolverData) {
        m_indexB = bodyB.m_islandIndex
        m_localCenterB.set(bodyB.m_sweep.localCenter)
        m_invMassB = bodyB.m_invMass
        m_invIB = bodyB.m_invI

        val cB = data.positions[m_indexB].c
        val aB = data.positions[m_indexB].a
        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        val qB = pool.popRot()

        qB.set(aB)

        val mass = bodyB.mass

        // Frequency
        val omega = 2.0 * PI * frequency

        // Damping coefficient
        val d = 2.0 * mass * dampingRatio * omega

        // Spring stiffness
        val k = mass * (omega * omega)

        // magic formulas
        // gamma has units of inverse mass.
        // beta has units of inverse time.
        val h = data.step.dt
        assert { d + h * k > Settings.EPSILON }
        m_gamma = h * (d + h * k)
        if (m_gamma != 0.0) {
            m_gamma = 1.0 / m_gamma
        }
        m_beta = h * k * m_gamma

        val temp = pool.popMutableVector2d()

        // Compute the effective mass matrix.
        temp.set(m_localAnchorB)
        temp.subtract(m_localCenterB)
        Rot.mulToOut(qB, temp, m_rB)

        // K = [(1/m1 + 1/m2) * eye(2) - skew(r1) * invI1 * skew(r1) - skew(r2) * invI2 * skew(r2)]
        // = [1/m1+1/m2 0 ] + invI1 * [r1.y*r1.y -r1.x*r1.y] + invI2 * [r1.y*r1.y -r1.x*r1.y]
        // [ 0 1/m1+1/m2] [-r1.x*r1.y r1.x*r1.x] [-r1.x*r1.y r1.x*r1.x]
        val K = pool.popMatrix2d()
        K.xx = m_invMassB + m_invIB * m_rB.y * m_rB.y + m_gamma
        K.xy = -m_invIB * m_rB.x * m_rB.y
        K.yx = K.xy
        K.yy = m_invMassB + m_invIB * m_rB.x * m_rB.x + m_gamma

        K.invertToOut(m_mass)

        m_C.set(cB)
        m_C.add(m_rB)
        m_C.subtract(_target)
        m_C.multiply(m_beta)

        // Cheat with some damping
        wB *= 0.98

        if (data.step.warmStarting) {
            m_impulse.multiply(data.step.dtRatio)
            vB.x += m_invMassB * m_impulse.x
            vB.y += m_invMassB * m_impulse.y
            wB += m_invIB * (m_rB cross m_impulse)
        } else {
            m_impulse.setXY(0.0, 0.0)
        }

        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(1)
        pool.pushMatrix2d(1)
        pool.pushRot(1)
    }

    override fun solvePositionConstraints(data: SolverData): Boolean {
        return true
    }

    override fun solveVelocityConstraints(data: SolverData) {

        val vB = data.velocities[m_indexB].v
        var wB = data.velocities[m_indexB].w

        // Cdot = v + cross(w, r)
        val Cdot = pool.popMutableVector2d()
        Cdot.set(wB cross m_rB)
        Cdot.add(vB)

        val impulse = pool.popMutableVector2d()
        val temp = pool.popMutableVector2d()

        temp.set(m_impulse)
        temp.multiply(m_gamma)
        temp.add(m_C)
        temp.add(Cdot)
        temp.negate()
        m_mass.multiply(temp, impulse)

        temp.set(m_impulse)
        m_impulse.add(impulse)
        val maxImpulse = data.step.dt * maxForce
        if (m_impulse.lengthSqr() > maxImpulse * maxImpulse) {
            m_impulse.multiply(maxImpulse / m_impulse.length())
        }
        impulse.set(m_impulse).subtract(temp)

        vB.x += m_invMassB * impulse.x
        vB.y += m_invMassB * impulse.y
        wB += m_invIB * (m_rB cross impulse)

        //    data.velocities[m_indexB].v.set(vB);
        data.velocities[m_indexB].w = wB

        pool.pushMutableVector2d(3)
    }

}
