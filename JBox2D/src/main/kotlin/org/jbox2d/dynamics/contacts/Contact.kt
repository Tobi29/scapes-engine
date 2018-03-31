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


import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.collision.WorldManifold
import org.jbox2d.common.Transform
import org.jbox2d.dynamics.Fixture
import org.jbox2d.pooling.IWorldPool
import kotlin.math.sqrt

/**
 * The class manages contact between two shapes. A contact exists for each overlapping AABB in the
 * broad-phase (except if filtered). Therefore a contact object may exist that has no contact
 * points.
 *
 * @author daniel
 */
abstract class Contact protected constructor(protected val pool: IWorldPool) {
    var m_flags: Int = 0

    // World pool and list pointers.
    var m_prev: Contact? = null
    /**
     * Get the next contact in the world's contact list.
     *
     * @return
     */
    var m_next: Contact? = null

    // Nodes for connecting bodies.
    var m_nodeA: ContactEdge? = null
    var m_nodeB: ContactEdge? = null

    /**
     * Get the first fixture in this contact.
     *
     * @return
     */
    var fixtureA: Fixture? = null
    /**
     * Get the second fixture in this contact.
     *
     * @return
     */
    var fixtureB: Fixture? = null

    var childIndexA: Int = 0
    var childIndexB: Int = 0

    /**
     * Get the contact manifold. Do not set the point count to zero. Instead call Disable.
     */
    val manifold: Manifold

    var m_toiCount: Double = 0.0
    var m_toi: Double = 0.0

    var friction: Double = 0.0
    var restitution: Double = 0.0

    var tangentSpeed: Double = 0.0

    /**
     * Is this contact touching
     *
     * @return
     */
    val isTouching: Boolean
        get() = m_flags and TOUCHING_FLAG == TOUCHING_FLAG

    /**
     * Has this contact been disabled?
     *
     * @return
     */
    /**
     * Enable/disable this contact. This can be used inside the pre-solve contact listener. The
     * contact is only disabled for the current time step (or sub-step in continuous collisions).
     *
     * @param flag
     */
    var isEnabled: Boolean
        get() = m_flags and ENABLED_FLAG == ENABLED_FLAG
        set(flag) {
            m_flags = if (flag) {
                m_flags or ENABLED_FLAG
            } else {
                m_flags and ENABLED_FLAG.inv()
            }
        }

    // djm pooling
    private val oldManifold = Manifold()

    init {
        fixtureA = null
        fixtureB = null
        m_nodeA = ContactEdge()
        m_nodeB = ContactEdge()
        manifold = Manifold()
    }

    /** initialization for pooling  */
    open fun init(
        fA: Fixture,
        indexA: Int,
        fB: Fixture,
        indexB: Int
    ) {
        m_flags = ENABLED_FLAG

        fixtureA = fA
        fixtureB = fB

        childIndexA = indexA
        childIndexB = indexB

        manifold.pointCount = 0

        m_prev = null
        m_next = null

        m_nodeA!!.contact = null
        m_nodeA!!.prev = null
        m_nodeA!!.next = null
        m_nodeA!!.other = null

        m_nodeB!!.contact = null
        m_nodeB!!.prev = null
        m_nodeB!!.next = null
        m_nodeB!!.other = null

        m_toiCount = 0.0
        friction = Contact.mixFriction(fA.m_friction, fB.m_friction)
        restitution = Contact.mixRestitution(fA.m_restitution, fB.m_restitution)

        tangentSpeed = 0.0
    }

    /**
     * Get the world manifold.
     */
    fun getWorldManifold(worldManifold: WorldManifold) {
        val bodyA = fixtureA!!.body
        val bodyB = fixtureB!!.body
        val shapeA = fixtureA!!.shape
        val shapeB = fixtureB!!.shape

        worldManifold.initialize(
            manifold, bodyA!!.transform, shapeA!!.m_radius,
            bodyB!!.transform, shapeB!!.m_radius
        )
    }

    fun resetFriction() {
        friction = Contact.mixFriction(
            fixtureA!!.m_friction,
            fixtureB!!.m_friction
        )
    }

    fun resetRestitution() {
        restitution = Contact.mixRestitution(
            fixtureA!!.m_restitution,
            fixtureB!!.m_restitution
        )
    }

    abstract fun evaluate(
        manifold: Manifold,
        xfA: Transform,
        xfB: Transform
    )

    /**
     * Flag this contact for filtering. Filtering will occur the next time step.
     */
    fun flagForFiltering() {
        m_flags = m_flags or FILTER_FLAG
    }

    fun update(listener: ContactListener?) {

        oldManifold.set(manifold)

        // Re-enable this contact.
        m_flags = m_flags or ENABLED_FLAG

        var touching = false
        val wasTouching = m_flags and TOUCHING_FLAG == TOUCHING_FLAG

        val sensorA = fixtureA!!.isSensor
        val sensorB = fixtureB!!.isSensor
        val sensor = sensorA || sensorB

        val bodyA = fixtureA!!.body
        val bodyB = fixtureB!!.body
        val xfA = bodyA!!.transform
        val xfB = bodyB!!.transform
        // log.debug("TransformA: "+xfA);
        // log.debug("TransformB: "+xfB);

        if (sensor) {
            val shapeA = fixtureA!!.shape
            val shapeB = fixtureB!!.shape
            touching = pool.collision.testOverlap(
                shapeA!!, childIndexA,
                shapeB!!, childIndexB, xfA, xfB
            )

            // Sensors don't generate manifolds.
            manifold.pointCount = 0
        } else {
            evaluate(manifold, xfA, xfB)
            touching = manifold.pointCount > 0

            // Match old contact ids to new contact ids and copy the
            // stored impulses to warm start the solver.
            for (i in 0 until manifold.pointCount) {
                val mp2 = manifold.points[i]
                mp2.normalImpulse = 0.0
                mp2.tangentImpulse = 0.0
                val id2 = mp2.id

                for (j in 0 until oldManifold.pointCount) {
                    val mp1 = oldManifold.points[j]

                    if (mp1.id.isEqual(id2)) {
                        mp2.normalImpulse = mp1.normalImpulse
                        mp2.tangentImpulse = mp1.tangentImpulse
                        break
                    }
                }
            }

            if (touching != wasTouching) {
                bodyA.isAwake = true
                bodyB.isAwake = true
            }
        }

        m_flags = if (touching) {
            m_flags or TOUCHING_FLAG
        } else {
            m_flags and TOUCHING_FLAG.inv()
        }

        if (listener == null) {
            return
        }

        if (wasTouching == false && touching == true) {
            listener.beginContact(this)
        }

        if (wasTouching == true && touching == false) {
            listener.endContact(this)
        }

        if (sensor == false && touching) {
            listener.preSolve(this, oldManifold)
        }
    }

    companion object {
        // Flags stored in m_flags
        // Used when crawling contact graph when forming islands.
        const val ISLAND_FLAG = 0x0001
        // Set when the shapes are touching.
        const val TOUCHING_FLAG = 0x0002
        // This contact can be disabled (by user)
        const val ENABLED_FLAG = 0x0004
        // This contact needs filtering because a fixture filter was changed.
        const val FILTER_FLAG = 0x0008
        // This bullet contact had a TOI event
        const val BULLET_HIT_FLAG = 0x0010

        const val TOI_FLAG = 0x0020

        /**
         * Friction mixing law. The idea is to allow either fixture to drive the restitution to zero. For
         * example, anything slides on ice.
         * @param friction1
         * @param friction2
         * @return
         */
        fun mixFriction(
            friction1: Double,
            friction2: Double
        ): Double = sqrt(friction1 * friction2)

        /**
         * Restitution mixing law. The idea is allow for anything to bounce off an inelastic surface. For
         * example, a superball bounces on anything.
         * @param restitution1
         * @param restitution2
         * @return
         */
        fun mixRestitution(
            restitution1: Double,
            restitution2: Double
        ): Double =
            if (restitution1 > restitution2) restitution1 else restitution2
    }
}
