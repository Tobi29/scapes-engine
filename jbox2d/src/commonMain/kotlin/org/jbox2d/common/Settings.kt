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
package org.jbox2d.common

import org.tobi29.stdex.Constant
import kotlin.math.PI

/**
 * Global tuning constants based on MKS units and various integer maximums (vertices per shape,
 * pairs, etc.).
 */
object Settings {
    /** A "close to zero" float epsilon value for use  */
    @Constant
    inline val EPSILON
        get() = 1.1920928955078125E-7

    // Collision

    /**
     * The maximum number of contact points between two convex shapes.
     */
    @Constant
    inline val maxManifoldPoints
        get() = 2

    /**
     * The maximum number of vertices on a convex polygon.
     */
    @Constant
    inline val maxPolygonVertices
        get() = 8

    /**
     * This is used to fatten AABBs in the dynamic tree. This allows proxies to move by a small amount
     * without triggering a tree adjustment. This is in meters.
     */
    @Constant
    inline val aabbExtension
        get() = 0.1

    /**
     * This is used to fatten AABBs in the dynamic tree. This is used to predict the future position
     * based on the current displacement. This is a dimensionless multiplier.
     */
    @Constant
    inline val aabbMultiplier
        get() = 2.0

    /**
     * A small length used as a collision and constraint tolerance. Usually it is chosen to be
     * numerically significant, but visually insignificant.
     */
    @Constant
    inline val linearSlop
        get() = 0.005

    /**
     * A small angle used as a collision and constraint tolerance. Usually it is chosen to be
     * numerically significant, but visually insignificant.
     */
    @Constant
    inline val angularSlop
        get() = 2.0 / 180.0 * PI

    /**
     * The radius of the polygon/edge shape skin. This should not be modified. Making this smaller
     * means polygons will have and insufficient for continuous collision. Making it larger may create
     * artifacts for vertex collision.
     */
    @Constant
    inline val polygonRadius
        get() = 2.0 * linearSlop

    /** Maximum number of sub-steps per contact in continuous physics simulation.  */
    @Constant
    inline val maxSubSteps
        get() = 8

    // Dynamics

    /**
     * Maximum number of contacts to be handled to solve a TOI island.
     */
    @Constant
    inline val maxTOIContacts
        get() = 32

    /**
     * A velocity threshold for elastic collisions. Any collision with a relative linear velocity
     * below this threshold will be treated as inelastic.
     */
    @Constant
    inline val velocityThreshold
        get() = 1.0

    /**
     * The maximum linear position correction used when solving constraints. This helps to prevent
     * overshoot.
     */
    @Constant
    inline val maxLinearCorrection
        get() = 0.2

    /**
     * The maximum angular position correction used when solving constraints. This helps to prevent
     * overshoot.
     */
    @Constant
    inline val maxAngularCorrection
        get() = 8.0 / 180.0 * PI

    /**
     * The maximum linear velocity of a body. This limit is very large and is used to prevent
     * numerical problems. You shouldn't need to adjust this.
     */
    @Constant
    inline val maxTranslation
        get() = 2.0
    @Constant
    inline val maxTranslationSquared
        get() = maxTranslation * maxTranslation

    /**
     * The maximum angular velocity of a body. This limit is very large and is used to prevent
     * numerical problems. You shouldn't need to adjust this.
     */
    @Constant
    inline val maxRotation
        get() = 0.5 * PI
    @Constant
    inline val maxRotationSquared
        get() = maxRotation * maxRotation

    /**
     * This scale factor controls how fast overlap is resolved. Ideally this would be 1 so that
     * overlap is removed in one time step. However using values close to 1 often lead to overshoot.
     */
    @Constant
    inline val baumgarte
        get() = 0.2
    @Constant
    inline val toiBaugarte
        get() = 0.75

    // Sleep

    /**
     * The time that a body must be still before it will go to sleep.
     */
    @Constant
    inline val timeToSleep
        get() = 0.5

    /**
     * A body cannot sleep if its linear velocity is above this tolerance.
     */
    @Constant
    inline val linearSleepTolerance
        get() = 0.01

    /**
     * A body cannot sleep if its angular velocity is above this tolerance.
     */
    @Constant
    inline val angularSleepTolerance
        get() = 2.0 / 180.0 * PI

    // Particle

    /**
     * A symbolic constant that stands for particle allocation error.
     */
    @Constant
    inline val invalidParticleIndex
        get() = -1

    /**
     * The standard distance between particles, divided by the particle radius.
     */
    @Constant
    inline val particleStride
        get() = 0.75

    /**
     * The minimum particle weight that produces pressure.
     */
    @Constant
    inline val minParticleWeight
        get() = 1.0

    /**
     * The upper limit for particle weight used in pressure calculation.
     */
    @Constant
    inline val maxParticleWeight
        get() = 5.0

    /**
     * The maximum distance between particles in a triad, divided by the particle radius.
     */
    @Constant
    inline val maxTriadDistance
        get() = 2
    @Constant
    inline val maxTriadDistanceSquared
        get() = maxTriadDistance * maxTriadDistance

    /**
     * The initial size of particle data buffers.
     */
    @Constant
    inline val minParticleBufferCapacity
        get() = 256
}
