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

package org.jbox2d.particle

import org.jbox2d.common.Transform
import org.tobi29.math.vector.MutableVector2d

class ParticleGroup {

    internal var m_system: ParticleSystem? = null
    var bufferIndex: Int = 0
        internal set
    internal var m_lastIndex: Int = 0
    var groupFlags: Int = 0
    internal var m_strength: Double = 0.0
    internal var m_prev: ParticleGroup? = null
    var next: ParticleGroup? = null
        internal set

    private var m_timestamp: Int = 0
    internal var m_mass: Double = 0.0
    private var m_inertia: Double = 0.0
    internal val m_center = MutableVector2d()
    internal val m_linearVelocity = MutableVector2d()
    internal var m_angularVelocity: Double = 0.0
    val transform = Transform()

    internal var m_destroyAutomatically: Boolean = false
    internal var m_toBeDestroyed: Boolean = false
    internal var m_toBeSplit: Boolean = false

    var userData: Any? = null

    val particleCount: Int
        get() = m_lastIndex - bufferIndex

    val mass: Double
        get() {
            updateStatistics()
            return m_mass
        }

    val inertia: Double
        get() {
            updateStatistics()
            return m_inertia
        }

    val center: MutableVector2d
        get() {
            updateStatistics()
            return m_center
        }

    val linearVelocity: MutableVector2d
        get() {
            updateStatistics()
            return m_linearVelocity
        }

    val angularVelocity: Double
        get() {
            updateStatistics()
            return m_angularVelocity
        }

    val position: MutableVector2d
        get() = transform.p

    val angle: Double
        get() = transform.q.angle

    init {
        // m_system = null;
        bufferIndex = 0
        m_lastIndex = 0
        groupFlags = 0
        m_strength = 1.0

        m_timestamp = -1
        m_mass = 0.0
        m_inertia = 0.0
        m_angularVelocity = 0.0
        transform.setIdentity()

        m_destroyAutomatically = true
        m_toBeDestroyed = false
        m_toBeSplit = false
    }


    fun updateStatistics() {
        if (m_timestamp != m_system!!.m_timestamp) {
            val m = m_system!!.particleMass
            m_mass = 0.0
            m_center.setXY(0.0, 0.0)
            m_linearVelocity.setXY(0.0, 0.0)
            for (i in bufferIndex until m_lastIndex) {
                m_mass += m
                val pos = m_system!!.m_positionBuffer.data!![i]
                m_center.x += m * pos.x
                m_center.y += m * pos.y
                val vel = m_system!!.m_velocityBuffer.data!![i]
                m_linearVelocity.x += m * vel.x
                m_linearVelocity.y += m * vel.y
            }
            if (m_mass > 0) {
                m_center.x *= 1 / m_mass
                m_center.y *= 1 / m_mass
                m_linearVelocity.x *= 1 / m_mass
                m_linearVelocity.y *= 1 / m_mass
            }
            m_inertia = 0.0
            m_angularVelocity = 0.0
            for (i in bufferIndex until m_lastIndex) {
                val pos = m_system!!.m_positionBuffer.data!![i]
                val vel = m_system!!.m_velocityBuffer.data!![i]
                val px = pos.x - m_center.x
                val py = pos.y - m_center.y
                val vx = vel.x - m_linearVelocity.x
                val vy = vel.y - m_linearVelocity.y
                m_inertia += m * (px * px + py * py)
                m_angularVelocity += m * (px * vy - py * vx)
                //println("$m * ($px * $vy - $py * $vx) = ${m * (px * vy - py * vx)}")
            }
            if (m_inertia > 0) {
                m_angularVelocity *= 1 / m_inertia
            }
            m_timestamp = m_system!!.m_timestamp
        }
    }
}
