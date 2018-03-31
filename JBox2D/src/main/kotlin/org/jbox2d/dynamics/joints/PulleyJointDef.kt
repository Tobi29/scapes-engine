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
 * Created at 12:11:41 PM Jan 23, 2011
 */
package org.jbox2d.dynamics.joints

import org.jbox2d.common.Settings
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.length
import org.tobi29.math.vector.minus
import org.tobi29.stdex.assert

/**
 * Pulley joint definition. This requires two ground anchors, two dynamic body anchor points, and a
 * pulley ratio.
 *
 * @author Daniel Murphy
 */
class PulleyJointDef : JointDef<PulleyJoint>() {

    /**
     * The first ground anchor in world coordinates. This point never moves.
     */
    val groundAnchorA = MutableVector2d(-1.0, 1.0)

    /**
     * The second ground anchor in world coordinates. This point never moves.
     */
    val groundAnchorB = MutableVector2d(1.0, 1.0)

    /**
     * The local anchor point relative to bodyA's origin.
     */
    val localAnchorA = MutableVector2d(-1.0, 0.0)

    /**
     * The local anchor point relative to bodyB's origin.
     */
    val localAnchorB = MutableVector2d(1.0, 0.0)

    /**
     * The a reference length for the segment attached to bodyA.
     */
    var lengthA: Double = 0.0

    /**
     * The a reference length for the segment attached to bodyB.
     */
    var lengthB: Double = 0.0

    /**
     * The pulley ratio, used to simulate a block-and-tackle.
     */
    var ratio: Double = 0.0

    init {
        lengthA = 0.0
        lengthB = 0.0
        ratio = 1.0
        collideConnected = true
    }

    override fun create(world: World): PulleyJoint =
        PulleyJoint(world.pool, this)

    /**
     * Initialize the bodies, anchors, lengths, max lengths, and ratio using the world anchors.
     */
    fun initialize(
        b1: Body,
        b2: Body,
        ga1: Vector2d,
        ga2: Vector2d,
        anchor1: Vector2d,
        anchor2: Vector2d,
        r: Double
    ) {
        bodyA = b1
        bodyB = b2
        groundAnchorA.set(ga1)
        groundAnchorB.set(ga2)
        localAnchorA.set(bodyA.getLocalPoint(anchor1))
        localAnchorB.set(bodyB.getLocalPoint(anchor2))
        val d1 = anchor1 - ga1
        lengthA = d1.length()
        val d2 = anchor2 - ga2
        lengthB = d2.length()
        ratio = r
        assert { ratio > Settings.EPSILON }
    }

    /**
     * Initialize the bodies, anchors, lengths, max lengths, and ratio using the world anchors.
     */
    fun initialize(
        b1: Body,
        b2: Body,
        ga1: MutableVector2d,
        ga2: MutableVector2d,
        anchor1: MutableVector2d,
        anchor2: MutableVector2d,
        r: Double
    ) {
        bodyA = b1
        bodyB = b2
        groundAnchorA.set(ga1)
        groundAnchorB.set(ga2)
        localAnchorA.set(bodyA.getLocalPoint(anchor1.now()))
        localAnchorB.set(bodyB.getLocalPoint(anchor2.now()))
        val d1 = anchor1.now() - ga1.now()
        lengthA = d1.length()
        val d2 = anchor2.now() - ga2.now()
        lengthB = d2.length()
        ratio = r
        assert { ratio > Settings.EPSILON }
    }
}
