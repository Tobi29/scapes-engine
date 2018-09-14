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

import kotlin.math.PI

/**
 * Global tuning constants based on MKS units and various integer maximums (vertices per shape,
 * pairs, etc.).
 */
object Settings {
    /** A "close to zero" float epsilon value for use  */
    const val EPSILON = 1.1920928955078125E-7

    // Collision

    /**
     * The maximum number of contact points between two convex shapes.
     */
    const val maxManifoldPoints = 2

    /**
     * The maximum number of vertices on a convex polygon.
     */
    const val maxPolygonVertices = 8

    /**
     * This is used to fatten AABBs in the dynamic tree. This allows proxies to move by a small amount
     * without triggering a tree adjustment. This is in meters.
     */
    const val aabbExtension = 0.1

    /**
     * This is used to fatten AABBs in the dynamic tree. This is used to predict the future position
     * based on the current displacement. This is a dimensionless multiplier.
     */
    const val aabbMultiplier = 2.0

    /**
     * A small length used as a collision and constraint tolerance. Usually it is chosen to be
     * numerically significant, but visually insignificant.
     */
    const val linearSlop = 0.005

    /**
     * A small angle used as a collision and constraint tolerance. Usually it is chosen to be
     * numerically significant, but visually insignificant.
     */
    const val angularSlop = 2.0 / 180.0 * PI

    /**
     * The radius of the polygon/edge shape skin. This should not be modified. Making this smaller
     * means polygons will have and insufficient for continuous collision. Making it larger may create
     * artifacts for vertex collision.
     */
    const val polygonRadius = 2.0 * linearSlop

    /** Maximum number of sub-steps per contact in continuous physics simulation.  */
    const val maxSubSteps = 8

    // Dynamics

    /**
     * Maximum number of contacts to be handled to solve a TOI island.
     */
    const val maxTOIContacts = 32

    /**
     * A velocity threshold for elastic collisions. Any collision with a relative linear velocity
     * below this threshold will be treated as inelastic.
     */
    const val velocityThreshold = 1.0

    /**
     * The maximum linear position correction used when solving constraints. This helps to prevent
     * overshoot.
     */
    const val maxLinearCorrection = 0.2

    /**
     * The maximum angular position correction used when solving constraints. This helps to prevent
     * overshoot.
     */
    const val maxAngularCorrection = 8.0 / 180.0 * PI

    /**
     * The maximum linear velocity of a body. This limit is very large and is used to prevent
     * numerical problems. You shouldn't need to adjust this.
     */
    const val maxTranslation = 2.0
    const val maxTranslationSquared = maxTranslation * maxTranslation

    /**
     * The maximum angular velocity of a body. This limit is very large and is used to prevent
     * numerical problems. You shouldn't need to adjust this.
     */
    const val maxRotation = 0.5 * PI
    const val maxRotationSquared = maxRotation * maxRotation

    /**
     * This scale factor controls how fast overlap is resolved. Ideally this would be 1 so that
     * overlap is removed in one time step. However using values close to 1 often lead to overshoot.
     */
    const val baumgarte = 0.2
    const val toiBaugarte = 0.75

    // Sleep

    /**
     * The time that a body must be still before it will go to sleep.
     */
    const val timeToSleep = 0.5

    /**
     * A body cannot sleep if its linear velocity is above this tolerance.
     */
    const val linearSleepTolerance = 0.01

    /**
     * A body cannot sleep if its angular velocity is above this tolerance.
     */
    const val angularSleepTolerance = 2.0 / 180.0 * PI

    // Particle

    /**
     * A symbolic constant that stands for particle allocation error.
     */
    const val invalidParticleIndex = -1

    /**
     * The standard distance between particles, divided by the particle radius.
     */
    const val particleStride = 0.75

    /**
     * The minimum particle weight that produces pressure.
     */
    const val minParticleWeight = 1.0

    /**
     * The upper limit for particle weight used in pressure calculation.
     */
    const val maxParticleWeight = 5.0

    /**
     * The maximum distance between particles in a triad, divided by the particle radius.
     */
    const val maxTriadDistance = 2
    const val maxTriadDistanceSquared = maxTriadDistance * maxTriadDistance

    /**
     * The initial size of particle data buffers.
     */
    const val minParticleBufferCapacity = 256
}
