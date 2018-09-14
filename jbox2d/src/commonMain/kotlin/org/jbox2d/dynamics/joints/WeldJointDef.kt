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
import org.tobi29.math.vector.Vector2d

/**
 * Created at 3:38:52 AM Jan 15, 2011
 */

/**
 * @author Daniel Murphy
 */
class WeldJointDef() : JointDef<WeldJoint>() {
    /**
     * The local anchor point relative to body1's origin.
     */
    val localAnchorA = MutableVector2d()

    /**
     * The local anchor point relative to body2's origin.
     */
    val localAnchorB = MutableVector2d()

    /**
     * The body2 angle minus body1 angle in the reference state (radians).
     */
    var referenceAngle: Double = 0.0

    /**
     * The mass-spring-damper frequency in Hertz. Rotation only. Disable softness with a value of 0.
     */
    var frequencyHz: Double = 0.0

    /**
     * The damping ratio. 0 = no damping, 1 = critical damping.
     */
    var dampingRatio: Double = 0.0

    init {
        referenceAngle = 0.0
    }

    constructor(
        bA: Body,
        bB: Body,
        anchor: Vector2d
    ) : this() {
        initialize(bA, bB, anchor)
    }

    /**
     * Initialize the bodies, anchors, and reference angle using a world anchor point.
     *
     * @param bA
     * @param bB
     * @param anchor
     */
    fun initialize(
        bA: Body,
        bB: Body,
        anchor: Vector2d
    ) {
        bodyA = bA
        bodyB = bB
        bodyA.getLocalPointToOut(MutableVector2d(anchor), localAnchorA)
        bodyB.getLocalPointToOut(MutableVector2d(anchor), localAnchorB)
        referenceAngle = bodyB.angle - bodyA.angle
    }

    override fun create(world: World): WeldJoint =
        WeldJoint(world.pool, this)
}
