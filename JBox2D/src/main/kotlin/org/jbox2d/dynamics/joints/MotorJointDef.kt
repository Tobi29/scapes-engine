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

import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.tobi29.math.vector.MutableVector2d

/**
 * Motor joint definition.
 *
 * @author dmurph
 */
class MotorJointDef() : JointDef<MotorJoint>() {
    /**
     * Position of bodyB minus the position of bodyA, in bodyA's frame, in meters.
     */
    val linearOffset = MutableVector2d()

    /**
     * The bodyB angle minus bodyA angle in radians.
     */
    var angularOffset: Double = 0.0

    /**
     * The maximum motor force in N.
     */
    var maxForce: Double = 0.0

    /**
     * The maximum motor torque in N-m.
     */
    var maxTorque: Double = 0.0

    /**
     * Position correction factor in the range [0,1].
     */
    var correctionFactor: Double = 0.0

    init {
        angularOffset = 0.0
        maxForce = 1.0
        maxTorque = 1.0
        correctionFactor = 0.3
    }

    constructor(
        bA: Body, bB: Body
    ) : this() {
        initialize(bA, bB)
    }

    fun initialize(
        bA: Body,
        bB: Body
    ) {
        bodyA = bA
        bodyB = bB
        val xB = bodyB.position
        linearOffset.set(bodyA.getLocalPoint(xB))

        val angleA = bodyA.angle
        val angleB = bodyB.angle
        angularOffset = angleB - angleA
    }

    override fun create(world: World): MotorJoint =
        MotorJoint(world.pool, this)
}
