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
package org.jbox2d.dynamics.contacts

import org.jbox2d.common.Settings
import org.tobi29.math.matrix.MutableMatrix2d
import org.tobi29.math.vector.MutableVector2d

class ContactVelocityConstraint {
    var points = Array(Settings.maxManifoldPoints) { VelocityConstraintPoint() }
    val normal = MutableVector2d()
    val normalMass = MutableMatrix2d()
    val K = MutableMatrix2d()
    var indexA: Int = 0
    var indexB: Int = 0
    var invMassA: Double = 0.0
    var invMassB: Double = 0.0
    var invIA: Double = 0.0
    var invIB: Double = 0.0
    var friction: Double = 0.0
    var restitution: Double = 0.0
    var tangentSpeed: Double = 0.0
    var pointCount: Int = 0
    var contactIndex: Int = 0

    class VelocityConstraintPoint {
        val rA = MutableVector2d()
        val rB = MutableVector2d()
        var normalImpulse: Double = 0.0
        var tangentImpulse: Double = 0.0
        var normalMass: Double = 0.0
        var tangentMass: Double = 0.0
        var velocityBias: Double = 0.0
    }
}
