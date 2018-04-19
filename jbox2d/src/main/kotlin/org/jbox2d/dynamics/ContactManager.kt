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
package org.jbox2d.dynamics

import org.jbox2d.callbacks.ContactFilter
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.callbacks.PairCallback
import org.jbox2d.collision.broadphase.BroadPhase
import org.jbox2d.dynamics.contacts.Contact

/**
 * Delegate of World.
 *
 * @author Daniel Murphy
 */
class ContactManager(private val pool: World,
                     var m_broadPhase: BroadPhase) : PairCallback {
    var m_contactList: Contact? = null
    var m_contactCount: Int = 0
    var m_contactFilter: ContactFilter? = null
    var m_contactListener: ContactListener? = null

    init {
        m_contactList = null
        m_contactCount = 0
        m_contactFilter = ContactFilter()
        m_contactListener = null
    }

    /**
     * Broad-phase callback.
     *
     * @param proxyUserDataA
     * @param proxyUserDataB
     */
    override fun addPair(proxyUserDataA: Any?,
                         proxyUserDataB: Any?) {
        val proxyA = proxyUserDataA as FixtureProxy
        val proxyB = proxyUserDataB as FixtureProxy

        var fixtureA = proxyA.fixture!!
        var fixtureB = proxyB.fixture!!

        var indexA = proxyA.childIndex
        var indexB = proxyB.childIndex

        var bodyA = fixtureA.body!!
        var bodyB = fixtureB.body!!

        // Are the fixtures on the same body?
        if (bodyA === bodyB) {
            return
        }

        // TODO_ERIN use a hash table to remove a potential bottleneck when both
        // bodies have a lot of contacts.
        // Does a contact already exist?
        var edge = bodyB.contactList
        while (edge != null) {
            if (edge.other === bodyA) {
                val fA = edge.contact!!.fixtureA
                val fB = edge.contact!!.fixtureB
                val iA = edge.contact!!.childIndexA
                val iB = edge.contact!!.childIndexB

                if (fA === fixtureA && iA == indexA && fB === fixtureB && iB == indexB) {
                    // A contact already exists.
                    return
                }

                if (fA === fixtureB && iA == indexB && fB === fixtureA && iB == indexA) {
                    // A contact already exists.
                    return
                }
            }

            edge = edge.next
        }

        // Does a joint override collision? is at least one body dynamic?
        if (bodyB.shouldCollide(bodyA) == false) {
            return
        }

        // Check user filtering.
        if (m_contactFilter != null && m_contactFilter?.shouldCollide(fixtureA,
                fixtureB) == false) {
            return
        }

        // Call the factory.
        val c = pool.popContact(fixtureA, indexA, fixtureB, indexB) ?: return

        // Contact creation may swap fixtures.
        fixtureA = c.fixtureA!!
        fixtureB = c.fixtureB!!
        indexA = c.childIndexA
        indexB = c.childIndexB
        bodyA = fixtureA.body!!
        bodyB = fixtureB.body!!

        // Insert into the world.
        c.m_prev = null
        c.m_next = m_contactList
        if (m_contactList != null) {
            m_contactList!!.m_prev = c
        }
        m_contactList = c

        // Connect to island graph.

        // Connect to body A
        c.m_nodeA!!.contact = c
        c.m_nodeA!!.other = bodyB

        c.m_nodeA!!.prev = null
        c.m_nodeA!!.next = bodyA.contactList
        if (bodyA.contactList != null) {
            bodyA.contactList!!.prev = c.m_nodeA
        }
        bodyA.contactList = c.m_nodeA

        // Connect to body B
        c.m_nodeB!!.contact = c
        c.m_nodeB!!.other = bodyA

        c.m_nodeB!!.prev = null
        c.m_nodeB!!.next = bodyB.contactList
        if (bodyB.contactList != null) {
            bodyB.contactList!!.prev = c.m_nodeB
        }
        bodyB.contactList = c.m_nodeB

        // wake up the bodies
        if (!fixtureA.isSensor && !fixtureB.isSensor) {
            bodyA.isAwake = true
            bodyB.isAwake = true
        }

        ++m_contactCount
    }

    fun findNewContacts() {
        m_broadPhase.updatePairs(this)
    }

    fun destroy(c: Contact) {
        val fixtureA = c.fixtureA!!
        val fixtureB = c.fixtureB!!
        val bodyA = fixtureA.body!!
        val bodyB = fixtureB.body!!

        if (m_contactListener != null && c.isTouching) {
            m_contactListener!!.endContact(c)
        }

        // Remove from the world.
        if (c.m_prev != null) {
            c.m_prev!!.m_next = c.m_next
        }

        if (c.m_next != null) {
            c.m_next!!.m_prev = c.m_prev
        }

        if (c === m_contactList) {
            m_contactList = c.m_next
        }

        // Remove from body 1
        if (c.m_nodeA!!.prev != null) {
            c.m_nodeA!!.prev!!.next = c.m_nodeA!!.next
        }

        if (c.m_nodeA!!.next != null) {
            c.m_nodeA!!.next!!.prev = c.m_nodeA!!.prev
        }

        if (c.m_nodeA === bodyA.contactList) {
            bodyA.contactList = c.m_nodeA!!.next
        }

        // Remove from body 2
        if (c.m_nodeB!!.prev != null) {
            c.m_nodeB!!.prev!!.next = c.m_nodeB!!.next
        }

        if (c.m_nodeB!!.next != null) {
            c.m_nodeB!!.next!!.prev = c.m_nodeB!!.prev
        }

        if (c.m_nodeB === bodyB.contactList) {
            bodyB.contactList = c.m_nodeB!!.next
        }

        // Call the factory.
        pool.pushContact(c)
        --m_contactCount
    }

    /**
     * This is the top level collision call for the time step. Here all the narrow phase collision is
     * processed for the world contact list.
     */
    fun collide() {
        // Update awake contacts.
        var c = m_contactList
        while (c != null) {
            val fixtureA = c.fixtureA!!
            val fixtureB = c.fixtureB!!
            val indexA = c.childIndexA
            val indexB = c.childIndexB
            val bodyA = fixtureA.body!!
            val bodyB = fixtureB.body!!

            // is this contact flagged for filtering?
            if (c.m_flags and Contact.FILTER_FLAG == Contact.FILTER_FLAG) {
                // Should these bodies collide?
                if (bodyB.shouldCollide(bodyA) == false) {
                    val cNuke = c
                    c = cNuke.m_next
                    destroy(cNuke)
                    continue
                }

                // Check user filtering.
                if (m_contactFilter != null && m_contactFilter?.shouldCollide(
                        fixtureA, fixtureB) == false) {
                    val cNuke = c
                    c = cNuke.m_next
                    destroy(cNuke)
                    continue
                }

                // Clear the filtering flag.
                c.m_flags = c.m_flags and Contact.FILTER_FLAG.inv()
            }

            val activeA = bodyA.isAwake && bodyA.m_type != BodyType.STATIC
            val activeB = bodyB.isAwake && bodyB.m_type != BodyType.STATIC

            // At least one body must be awake and it must be dynamic or kinematic.
            if (activeA == false && activeB == false) {
                c = c.m_next
                continue
            }

            val proxyIdA = fixtureA.m_proxies!![indexA].proxyId
            val proxyIdB = fixtureB.m_proxies!![indexB].proxyId
            val overlap = m_broadPhase.testOverlap(proxyIdA, proxyIdB)

            // Here we destroy contacts that cease to overlap in the broad-phase.
            if (overlap == false) {
                val cNuke = c
                c = cNuke.m_next
                destroy(cNuke)
                continue
            }

            // The contact persists.
            c.update(m_contactListener)
            c = c.m_next
        }
    }
}
