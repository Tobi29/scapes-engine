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
 * Created at 7:27:31 AM Jan 21, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d

/**
 * Wheel joint definition. This requires defining a line of motion using an axis and an anchor
 * point. The definition uses local anchor points and a local axis so that the initial configuration
 * can violate the constraint slightly. The joint translation is zero when the local anchor points
 * coincide in world space. Using local anchors and a local axis helps when saving and loading a
 * game.
 *
 * @author Daniel Murphy
 */
class WheelJointDef() : JointDef<WheelJoint>() {

    /**
     * The local anchor point relative to body1's origin.
     */
    val localAnchorA = MutableVector2d()

    /**
     * The local anchor point relative to body2's origin.
     */
    val localAnchorB = MutableVector2d()

    /**
     * The local translation axis in body1.
     */
    val localAxisA = MutableVector2d()

    /**
     * Enable/disable the joint motor.
     */
    var enableMotor: Boolean = false

    /**
     * The maximum motor torque, usually in N-m.
     */
    var maxMotorTorque: Double = 0.0

    /**
     * The desired motor speed in radians per second.
     */
    var motorSpeed: Double = 0.0

    /**
     * Suspension frequency, zero indicates no suspension
     */
    var frequencyHz: Double = 0.0

    /**
     * Suspension damping ratio, one indicates critical damping
     */
    var dampingRatio: Double = 0.0

    init {
        localAxisA.setXY(1.0, 0.0)
        enableMotor = false
        maxMotorTorque = 0.0
        motorSpeed = 0.0
    }

    constructor(
        b1: Body,
        b2: Body,
        anchor: Vector2d,
        axis: Vector2d
    ) : this() {
        initialize(b1, b2, anchor, axis)
    }

    fun initialize(
        b1: Body,
        b2: Body,
        anchor: Vector2d,
        axis: Vector2d
    ) {
        bodyA = b1
        bodyB = b2
        localAnchorA.set(b1.getLocalPoint(anchor))
        localAnchorB.set(b2.getLocalPoint(anchor))
        localAxisA.set(bodyA.getLocalVector(axis))
    }

    override fun create(world: World): WheelJoint =
        WheelJoint(world.pool, this)
}
