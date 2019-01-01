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

import org.jbox2d.collision.shapes.MassData
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Rot
import org.jbox2d.common.Sweep
import org.jbox2d.common.Transform
import org.jbox2d.common.isValid
import org.jbox2d.dynamics.contacts.ContactEdge
import org.jbox2d.dynamics.joints.JointEdge
import org.tobi29.math.cosTable
import org.tobi29.math.sinTable
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert

/**
 * A rigid body. These are created via World.createBody.
 *
 * @author Daniel Murphy
 */
class Body(
    bd: BodyDef,
    /**
     * Get the parent world of this body.
     */
    var world: World
) {

    var m_type: BodyType

    var m_flags: Int = 0

    var m_islandIndex: Int = 0

    /**
     * The body origin transform.
     */
    /**
     * Get the body transform for the body's origin.
     *
     * @return the world transform of the body's origin.
     */
    val transform = Transform()
    /**
     * The previous transform for particle simulation
     */
    val m_xf0 = Transform()

    /**
     * The swept motion for CCD
     */
    val m_sweep = Sweep()

    val m_linearVelocity = MutableVector2d()
    var m_angularVelocity = 0.0

    val m_force = MutableVector2d()
    var m_torque = 0.0
    var m_prev: Body? = null
    /** Get the next body in the world's body list.  */
    var next: Body? = null

    /** Get the list of all fixtures attached to this body.  */
    var fixtureList: Fixture? = null
    var m_fixtureCount: Int = 0

    /** Get the list of all joints attached to this body.  */
    var jointList: JointEdge? = null
    /**
     * Get the list of all contacts attached to this body.
     *
     * @warning this list changes during the time step and you may miss some collisions if you don't
     * use ContactListener.
     */
    var contactList: ContactEdge? = null

    /**
     * Get the total mass of the body.
     *
     * @return the mass, usually in kilograms (kg).
     */
    var mass: Double = 0.0
    var m_invMass: Double = 0.0

    // Rotational inertia about the center of mass.
    private var m_I: Double = 0.0
    var m_invI: Double = 0.0

    /** Get the linear damping of the body.  */
    /** Set the linear damping of the body.  */
    var linearDamping: Double = 0.0
    /** Get the angular damping of the body.  */
    /** Set the angular damping of the body.  */
    var angularDamping: Double = 0.0
    /**
     * Get the gravity scale of the body.
     *
     * @return
     */
    /**
     * Set the gravity scale of the body.
     *
     * @param gravityScale
     */
    var gravityScale: Double = 0.0

    var m_sleepTime: Double = 0.0

    /** Get the user data pointer that was provided in the body definition.  */
    /**
     * Set the user data. Use this to store your application specific data.
     */
    var userData: Any? = null

    private val fixDef = FixtureDef()

    /**
     * Get the world body origin position. Do not modify.
     *
     * @return the world position of the body's origin.
     */
    val position: Vector2d get() = transform.p.now()

    /**
     * Get the angle in radians.
     *
     * @return the current world rotation angle in radians.
     */
    val angle: Double get() = m_sweep.a

    /**
     * Get the world position of the center of mass.
     */
    val worldCenter: Vector2d get() = m_sweep.c.now()

    /**
     * Get the local position of the center of mass.
     */
    val localCenter: Vector2d get() = m_sweep.localCenter.now()

    /**
     * Get the linear velocity of the center of mass. Do not modify, instead use
     * [.setLinearVelocity].
     *
     * @return the linear velocity of the center of mass.
     */
    /**
     * Set the linear velocity of the center of mass.
     *
     * @param v the new linear velocity of the center of mass.
     */
    var linearVelocity: MutableVector2d
        get() = m_linearVelocity
        set(v) {
            if (m_type == BodyType.STATIC) {
                return
            }

            if ((v dot v) > 0.0) {
                isAwake = true
            }

            m_linearVelocity.set(v)
        }

    /**
     * Get the angular velocity.
     *
     * @return the angular velocity in radians/second.
     */
    /**
     * Set the angular velocity.
     *
     * @param omega the new angular velocity in radians/second.
     */
    var angularVelocity: Double
        get() = m_angularVelocity
        set(w) {
            if (m_type == BodyType.STATIC) {
                return
            }

            if (w * w > 0.0) {
                isAwake = true
            }

            m_angularVelocity = w
        }

    /**
     * Get the central rotational inertia of the body.
     *
     * @return the rotational inertia, usually in kg-m^2.
     */
    val inertia: Double
        get() = m_I + mass * (m_sweep.localCenter.x * m_sweep.localCenter.x + m_sweep.localCenter.y * m_sweep.localCenter.y)

    private val pmd = MassData()

    /**
     * Set the type of this body. This may alter the mass and velocity.
     *
     * @param type
     */
    // Delete the attached contacts.
    // Touch the proxies so that new contacts will be created (when appropriate)
    var type: BodyType
        get() = m_type
        set(type) {
            assert { world.isLocked == false }
            if (world.isLocked == true) {
                return
            }

            if (m_type == type) {
                return
            }

            m_type = type

            resetMassData()

            if (m_type == BodyType.STATIC) {
                m_linearVelocity.setXY(0.0, 0.0)
                m_angularVelocity = 0.0
                m_sweep.a0 = m_sweep.a
                m_sweep.c0.set(m_sweep.c)
                synchronizeFixtures()
            }

            isAwake = true

            m_force.setXY(0.0, 0.0)
            m_torque = 0.0
            var ce = contactList
            while (ce != null) {
                val ce0 = ce
                ce = ce.next
                world.contactManager.destroy(ce0.contact!!)
            }
            contactList = null
            val broadPhase = world.contactManager.m_broadPhase
            var f = fixtureList
            while (f != null) {
                val proxyCount = f.m_proxyCount
                for (i in 0 until proxyCount) {
                    broadPhase.touchProxy(f.m_proxies!![i].proxyId)
                }
                f = f.next
            }
        }

    /** Is this body treated like a bullet for continuous collision detection?  */
    /** Should this body be treated like a bullet for continuous collision detection?  */
    var isBullet: Boolean
        get() = m_flags and e_bulletFlag == e_bulletFlag
        set(flag) {
            m_flags = if (flag) {
                m_flags or e_bulletFlag
            } else {
                m_flags and e_bulletFlag.inv()
            }
        }

    /**
     * Is this body allowed to sleep
     *
     * @return
     */
    /**
     * You can disable sleeping on this body. If you disable sleeping, the body will be woken.
     *
     * @param flag
     */
    var isSleepingAllowed: Boolean
        get() = m_flags and e_autoSleepFlag == e_autoSleepFlag
        set(flag) = if (flag) {
            m_flags = m_flags or e_autoSleepFlag
        } else {
            m_flags = m_flags and e_autoSleepFlag.inv()
            isAwake = true
        }

    /**
     * Get the sleeping state of this body.
     *
     * @return true if the body is awake.
     */
    /**
     * Set the sleep state of the body. A sleeping body has very low CPU cost.
     *
     * @param flag set to true to put body to sleep, false to wake it.
     * @param flag
     */
    var isAwake: Boolean
        get() = m_flags and e_awakeFlag == e_awakeFlag
        set(flag) {
            if (flag) {
                if (m_flags and e_awakeFlag == 0) {
                    m_flags = m_flags or e_awakeFlag
                    m_sleepTime = 0.0
                }
            } else {
                m_flags = m_flags and e_awakeFlag.inv()
                m_sleepTime = 0.0
                m_linearVelocity.setXY(0.0, 0.0)
                m_angularVelocity = 0.0
                m_force.setXY(0.0, 0.0)
                m_torque = 0.0
            }
        }

    /**
     * Get the active state of the body.
     *
     * @return
     */
    /**
     * Set the active state of the body. An inactive body is not simulated and cannot be collided with
     * or woken up. If you pass a flag of true, all fixtures will be added to the broad-phase. If you
     * pass a flag of false, all fixtures will be removed from the broad-phase and all contacts will
     * be destroyed. Fixtures and joints are otherwise unaffected. You may continue to create/destroy
     * fixtures and joints on inactive bodies. Fixtures on an inactive body are implicitly inactive
     * and will not participate in collisions, ray-casts, or queries. Joints connected to an inactive
     * body are implicitly inactive. An inactive body is still owned by a World object and remains in
     * the body list.
     *
     * @param flag
     */
    // Create all proxies.
    // Contacts are created the next time step.
    // Destroy all proxies.
    // Destroy the attached contacts.
    var isActive: Boolean
        get() = m_flags and e_activeFlag == e_activeFlag
        set(flag) {
            assert { world.isLocked == false }

            if (flag == isActive) {
                return
            }

            if (flag) {
                m_flags = m_flags or e_activeFlag
                val broadPhase = world.contactManager.m_broadPhase
                var f = fixtureList
                while (f != null) {
                    f.createProxies(broadPhase, transform)
                    f = f.next
                }
            } else {
                m_flags = m_flags and e_activeFlag.inv()
                val broadPhase = world.contactManager.m_broadPhase
                var f = fixtureList
                while (f != null) {
                    f.destroyProxies(broadPhase)
                    f = f.next
                }
                var ce = contactList
                while (ce != null) {
                    val ce0 = ce
                    ce = ce.next
                    world.contactManager.destroy(ce0.contact!!)
                }
                contactList = null
            }
        }

    /**
     * Does this body have fixed rotation?
     *
     * @return
     */
    /**
     * Set this body to have fixed rotation. This causes the mass to be reset.
     *
     * @param flag
     */
    var isFixedRotation: Boolean
        get() = m_flags and e_fixedRotationFlag == e_fixedRotationFlag
        set(flag) {
            m_flags = if (flag) {
                m_flags or e_fixedRotationFlag
            } else {
                m_flags and e_fixedRotationFlag.inv()
            }

            resetMassData()
        }

    // djm pooling
    private val pxf = Transform()


    init {
        assert { bd.position.isValid }
        assert { bd.linearVelocity.isValid }
        assert { bd.gravityScale >= 0.0 }
        assert { bd.angularDamping >= 0.0 }
        assert { bd.linearDamping >= 0.0 }

        m_flags = 0

        if (bd.bullet) {
            m_flags = m_flags or e_bulletFlag
        }
        if (bd.fixedRotation) {
            m_flags = m_flags or e_fixedRotationFlag
        }
        if (bd.allowSleep) {
            m_flags = m_flags or e_autoSleepFlag
        }
        if (bd.awake) {
            m_flags = m_flags or e_awakeFlag
        }
        if (bd.isActive) {
            m_flags = m_flags or e_activeFlag
        }

        transform.p.set(bd.position)
        transform.q.set(bd.angle)

        m_sweep.localCenter.setXY(0.0, 0.0)
        m_sweep.c0.set(transform.p)
        m_sweep.c.set(transform.p)
        m_sweep.a0 = bd.angle
        m_sweep.a = bd.angle
        m_sweep.alpha0 = 0.0

        jointList = null
        contactList = null
        m_prev = null
        next = null

        m_linearVelocity.set(bd.linearVelocity)
        m_angularVelocity = bd.angularVelocity

        linearDamping = bd.linearDamping
        angularDamping = bd.angularDamping
        gravityScale = bd.gravityScale

        m_force.setXY(0.0, 0.0)
        m_torque = 0.0

        m_sleepTime = 0.0

        m_type = bd.type

        if (m_type == BodyType.DYNAMIC) {
            mass = 1.0
            m_invMass = 1.0
        } else {
            mass = 0.0
            m_invMass = 0.0
        }

        m_I = 0.0
        m_invI = 0.0

        userData = bd.userData

        fixtureList = null
        m_fixtureCount = 0
    }

    /**
     * Creates a fixture and attach it to this body. Use this function if you need to set some fixture
     * parameters, like friction. Otherwise you can create the fixture directly from a shape. If the
     * density is non-zero, this function automatically updates the mass of the body. Contacts are not
     * created until the next time step.
     *
     * @param def the fixture definition.
     * @warning This function is locked during callbacks.
     */
    fun createFixture(def: FixtureDef): Fixture? {
        assert { world.isLocked == false }

        if (world.isLocked == true) {
            return null
        }

        val fixture = Fixture()
        fixture.create(this, def)

        if (m_flags and e_activeFlag == e_activeFlag) {
            val broadPhase = world.contactManager.m_broadPhase
            fixture.createProxies(broadPhase, transform)
        }

        fixture.next = fixtureList
        fixtureList = fixture
        ++m_fixtureCount

        fixture.body = this

        // Adjust mass properties if needed.
        if (fixture.m_density > 0.0) {
            resetMassData()
        }

        // Let the world know we have a new fixture. This will cause new contacts
        // to be created at the beginning of the next time step.
        world.m_flags = world.m_flags or World.NEW_FIXTURE

        return fixture
    }

    /**
     * Creates a fixture from a shape and attach it to this body. This is a convenience function. Use
     * FixtureDef if you need to set parameters like friction, restitution, user data, or filtering.
     * If the density is non-zero, this function automatically updates the mass of the body.
     *
     * @param shape the shape to be cloned.
     * @param density the shape density (set to zero for static bodies).
     * @warning This function is locked during callbacks.
     */
    fun createFixture(
        shape: Shape,
        density: Double
    ): Fixture? {
        fixDef.shape = shape
        fixDef.density = density

        return createFixture(fixDef)
    }

    /**
     * Destroy a fixture. This removes the fixture from the broad-phase and destroys all contacts
     * associated with this fixture. This will automatically adjust the mass of the body if the body
     * is dynamic and the fixture has positive density. All fixtures attached to a body are implicitly
     * destroyed when the body is destroyed.
     *
     * @param fixture the fixture to be removed.
     * @warning This function is locked during callbacks.
     */
    fun destroyFixture(fixture: Fixture?) {
        var fixture = fixture
        assert { world.isLocked == false }
        if (world.isLocked == true) {
            return
        }

        assert { fixture!!.body === this }

        // Remove the fixture from this body's singly linked list.
        assert { m_fixtureCount > 0 }
        var node = fixtureList
        var last: Fixture? = null // java change
        var found = false
        while (node != null) {
            if (node === fixture) {
                node = fixture.next
                found = true
                break
            }
            last = node
            node = node.next
        }

        // You tried to remove a shape that is not attached to this body.
        assert { found }

        // java change, remove it from the list
        if (last == null) {
            fixtureList = fixture!!.next
        } else {
            last.next = fixture!!.next
        }

        // Destroy any contacts associated with the fixture.
        var edge = contactList
        while (edge != null) {
            val c = edge.contact!!
            edge = edge.next

            val fixtureA = c.fixtureA
            val fixtureB = c.fixtureB

            if (fixture === fixtureA || fixture === fixtureB) {
                // This destroys the contact and removes it from
                // this body's contact list.
                world.contactManager.destroy(c)
            }
        }

        if (m_flags and e_activeFlag == e_activeFlag) {
            val broadPhase = world.contactManager.m_broadPhase
            fixture.destroyProxies(broadPhase)
        }

        fixture.destroy()
        fixture.body = null
        fixture.next = null
        fixture = null

        --m_fixtureCount

        // Reset the mass data.
        resetMassData()
    }

    /**
     * Set the position of the body's origin and rotation. This breaks any contacts and wakes the
     * other bodies. Manipulating a body's transform may cause non-physical behavior. Note: contacts
     * are updated on the next call to World.step().
     *
     * @param position the world position of the body's local origin.
     * @param angle the world rotation in radians.
     */
    fun setTransform(
        position: Vector2d,
        angle: Double
    ) {
        assert { world.isLocked == false }
        if (world.isLocked == true) {
            return
        }

        transform.q.set(angle)
        transform.p.set(position)

        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOut(transform, m_sweep.localCenter, m_sweep.c)
        m_sweep.a = angle

        m_sweep.c0.set(m_sweep.c)
        m_sweep.a0 = m_sweep.a

        val broadPhase = world.contactManager.m_broadPhase
        var f = fixtureList
        while (f != null) {
            f.synchronize(broadPhase, transform, transform)
            f = f.next
        }
    }

    /**
     * Apply a force at a world point. If the force is not applied at the center of mass, it will
     * generate a torque and affect the angular velocity. This wakes up the body.
     *
     * @param force the world force vector, usually in Newtons (N).
     * @param point the world position of the point of application.
     */
    fun applyForce(
        force: Vector2d,
        point: Vector2d
    ) {
        if (m_type != BodyType.DYNAMIC) {
            return
        }

        if (isAwake == false) {
            isAwake = true
        }

        // m_force.addLocal(force);
        // Vec2 temp = tltemp.get();
        // temp.set(point).subLocal(m_sweep.c);
        // m_torque += Vec2.cross(temp, force);

        m_force.x += force.x
        m_force.y += force.y

        m_torque += (point.x - m_sweep.c.x) * force.y - (point.y - m_sweep.c.y) * force.x
    }

    /**
     * Apply a force to the center of mass. This wakes up the body.
     *
     * @param force the world force vector, usually in Newtons (N).
     */
    fun applyForceToCenter(force: Vector2d) {
        if (m_type != BodyType.DYNAMIC) {
            return
        }

        if (isAwake == false) {
            isAwake = true
        }

        m_force.x += force.x
        m_force.y += force.y
    }

    /**
     * Apply a torque. This affects the angular velocity without affecting the linear velocity of the
     * center of mass. This wakes up the body.
     *
     * @param torque about the z-axis (out of the screen), usually in N-m.
     */
    fun applyTorque(torque: Double) {
        if (m_type != BodyType.DYNAMIC) {
            return
        }

        if (isAwake == false) {
            isAwake = true
        }

        m_torque += torque
    }

    /**
     * Apply an impulse at a point. This immediately modifies the velocity. It also modifies the
     * angular velocity if the point of application is not at the center of mass. This wakes up the
     * body if 'wake' is set to true. If the body is sleeping and 'wake' is false, then there is no
     * effect.
     *
     * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
     * @param point the world position of the point of application.
     * @param wake also wake up the body
     */
    fun applyLinearImpulse(
        impulse: Vector2d,
        point: Vector2d,
        wake: Boolean
    ) {
        if (m_type != BodyType.DYNAMIC) {
            return
        }

        if (!isAwake) {
            if (wake) {
                isAwake = true
            } else {
                return
            }
        }

        m_linearVelocity.x += impulse.x * m_invMass
        m_linearVelocity.y += impulse.y * m_invMass

        m_angularVelocity += m_invI * ((point.x - m_sweep.c.x) * impulse.y - (point.y - m_sweep.c.y) * impulse.x)
    }

    /**
     * Apply an angular impulse.
     *
     * @param impulse the angular impulse in units of kg*m*m/s
     */
    fun applyAngularImpulse(impulse: Double) {
        if (m_type != BodyType.DYNAMIC) {
            return
        }

        if (isAwake == false) {
            isAwake = true
        }
        m_angularVelocity += m_invI * impulse
    }

    /**
     * Get the mass data of the body. The rotational inertia is relative to the center of mass.
     *
     * @return a struct containing the mass, inertia and center of the body.
     */
    fun getMassData(data: MassData) {
        // data.mass = m_mass;
        // data.I = m_I + m_mass * Vec2.dot(m_sweep.localCenter, m_sweep.localCenter);
        // data.center.set(m_sweep.localCenter);

        data.mass = mass
        data.i = m_I + mass *
                (m_sweep.localCenter.x * m_sweep.localCenter.x + m_sweep.localCenter.y * m_sweep.localCenter.y)
        data._center.x = m_sweep.localCenter.x
        data._center.y = m_sweep.localCenter.y
    }

    /**
     * Set the mass properties to override the mass properties of the fixtures. Note that this changes
     * the center of mass position. Note that creating or destroying fixtures can also alter the mass.
     * This function has no effect if the body isn't dynamic.
     *
     * @param massData the mass properties.
     */
    fun setMassData(massData: MassData) {
        // TODO_ERIN adjust linear velocity and torque to account for movement of center.
        assert { world.isLocked == false }
        if (world.isLocked == true) {
            return
        }

        if (m_type != BodyType.DYNAMIC) {
            return
        }

        m_invMass = 0.0
        m_I = 0.0
        m_invI = 0.0

        mass = massData.mass
        if (mass <= 0.0) {
            mass = 1.0
        }

        m_invMass = 1.0 / mass

        if (massData.i > 0.0 && m_flags and e_fixedRotationFlag == 0) {
            m_I = massData.i - mass * (massData._center dot massData._center)
            assert { m_I > 0.0 }
            m_invI = 1.0 / m_I
        }

        val oldCenter = world.pool.popMutableVector2d()
        // Move center of mass.
        oldCenter.set(m_sweep.c)
        m_sweep.localCenter.set(massData._center)
        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOut(transform, m_sweep.localCenter, m_sweep.c0)
        m_sweep.c.set(m_sweep.c0)

        // Update center of mass velocity.
        // m_linearVelocity += Cross(m_angularVelocity, m_sweep.c - oldCenter);
        val temp = world.pool.popMutableVector2d()
        temp.set(m_sweep.c).subtract(oldCenter)
        temp.setXY(-m_angularVelocity * temp.y, m_angularVelocity * temp.x)
        m_linearVelocity.add(temp)

        world.pool.pushMutableVector2d(2)
    }

    /**
     * This resets the mass properties to the sum of the mass properties of the fixtures. This
     * normally does not need to be called unless you called setMassData to override the mass and you
     * later want to reset the mass.
     */
    fun resetMassData() {
        // Compute mass data from shapes. Each shape has its own density.
        mass = 0.0
        m_invMass = 0.0
        m_I = 0.0
        m_invI = 0.0
        m_sweep.localCenter.setXY(0.0, 0.0)

        // Static and kinematic bodies have zero mass.
        if (m_type == BodyType.STATIC || m_type == BodyType.KINEMATIC) {
            // m_sweep.c0 = m_sweep.c = m_xf.position;
            m_sweep.c0.set(transform.p)
            m_sweep.c.set(transform.p)
            m_sweep.a0 = m_sweep.a
            return
        }

        assert { m_type == BodyType.DYNAMIC }

        // Accumulate mass over all fixtures.
        val localCenter = world.pool.popMutableVector2d()
        localCenter.setXY(0.0, 0.0)
        val temp = world.pool.popMutableVector2d()
        val massData = pmd
        var f = fixtureList
        while (f != null) {
            if (f.m_density == 0.0) {
                f = f.next
                continue
            }
            f.getMassData(massData)
            mass += massData.mass
            // center += massData.mass * massData.center;
            temp.set(massData._center).multiply(massData.mass)
            localCenter.add(temp)
            m_I += massData.i
            f = f.next
        }

        // Compute center of mass.
        if (mass > 0.0) {
            m_invMass = 1.0 / mass
            localCenter.multiply(m_invMass)
        } else {
            // Force all dynamic bodies to have a positive mass.
            mass = 1.0
            m_invMass = 1.0
        }

        if (m_I > 0.0 && m_flags and e_fixedRotationFlag == 0) {
            // Center the inertia about the center of mass.
            m_I -= mass * (localCenter dot localCenter)
            assert { m_I > 0.0 }
            m_invI = 1.0 / m_I
        } else {
            m_I = 0.0
            m_invI = 0.0
        }

        val oldCenter = world.pool.popMutableVector2d()
        // Move center of mass.
        oldCenter.set(m_sweep.c)
        m_sweep.localCenter.set(localCenter)
        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOut(transform, m_sweep.localCenter, m_sweep.c0)
        m_sweep.c.set(m_sweep.c0)

        // Update center of mass velocity.
        // m_linearVelocity += Cross(m_angularVelocity, m_sweep.c - oldCenter);
        temp.set(m_sweep.c)
        temp.subtract(oldCenter)

        oldCenter.setXY(-m_angularVelocity * temp.y, m_angularVelocity * temp.x)
        m_linearVelocity.add(oldCenter)

        world.pool.pushMutableVector2d(3)
    }

    /**
     * Get the world coordinates of a point given the local coordinates.
     *
     * @param localPoint a point on the body measured relative the the body's origin.
     * @return the same point expressed in world coordinates.
     */
    fun getWorldPoint(localPoint: Vector2d): Vector2d {
        val v = MutableVector2d()
        getWorldPointToOut(MutableVector2d(localPoint), v)
        return v.now()
    }

    fun getWorldPointToOut(
        localPoint: MutableVector2d,
        out: MutableVector2d
    ) {
        Transform.mulToOut(transform, localPoint, out)
    }

    /**
     * Get the world coordinates of a vector given the local coordinates.
     *
     * @param localVector a vector fixed in the body.
     * @return the same vector expressed in world coordinates.
     */
    fun getWorldVector(localVector: Vector2d): Vector2d {
        val out = MutableVector2d()
        getWorldVectorToOut(MutableVector2d(localVector), out)
        return out.now()
    }

    fun getWorldVectorToOut(
        localVector: MutableVector2d,
        out: MutableVector2d
    ) {
        Rot.mulToOut(transform.q, localVector, out)
    }

    /**
     * Gets a local point relative to the body's origin given a world point.
     *
     * @param a point in world coordinates.
     * @return the corresponding local point relative to the body's origin.
     */
    fun getLocalPoint(worldPoint: Vector2d): Vector2d {
        val out = MutableVector2d()
        getLocalPointToOut(MutableVector2d(worldPoint), out)
        return out.now()
    }

    fun getLocalPointToOut(
        worldPoint: MutableVector2d,
        out: MutableVector2d
    ) {
        Transform.mulTransToOut(transform, worldPoint, out)
    }

    /**
     * Gets a local vector given a world vector.
     *
     * @param a vector in world coordinates.
     * @return the corresponding local vector.
     */
    fun getLocalVector(worldVector: Vector2d): Vector2d {
        val out = MutableVector2d()
        getLocalVectorToOut(MutableVector2d(worldVector), out)
        return out.now()
    }

    fun getLocalVectorToOut(
        worldVector: MutableVector2d,
        out: MutableVector2d
    ) {
        Rot.mulTrans(transform.q, worldVector, out)
    }

    /**
     * Get the world linear velocity of a world point attached to this body.
     *
     * @param a point in world coordinates.
     * @return the world velocity of a point.
     */
    fun getLinearVelocityFromWorldPoint(worldPoint: Vector2d): Vector2d {
        val out = MutableVector2d()
        getLinearVelocityFromWorldPointToOut(MutableVector2d(worldPoint), out)
        return out.now()
    }

    fun getLinearVelocityFromWorldPointToOut(
        worldPoint: MutableVector2d,
        out: MutableVector2d
    ) {
        val tempX = worldPoint.x - m_sweep.c.x
        val tempY = worldPoint.y - m_sweep.c.y
        out.x = -m_angularVelocity * tempY + m_linearVelocity.x
        out.y = m_angularVelocity * tempX + m_linearVelocity.y
    }

    /**
     * Get the world velocity of a local point.
     *
     * @param a point in local coordinates.
     * @return the world velocity of a point.
     */
    fun getLinearVelocityFromLocalPoint(localPoint: Vector2d): Vector2d {
        val out = MutableVector2d()
        getLinearVelocityFromLocalPointToOut(MutableVector2d(localPoint), out)
        return out.now()
    }

    fun getLinearVelocityFromLocalPointToOut(
        localPoint: MutableVector2d,
        out: MutableVector2d
    ) {
        getWorldPointToOut(localPoint, out)
        getLinearVelocityFromWorldPointToOut(out, out)
    }

    fun synchronizeFixtures() {
        val xf1 = pxf
        // xf1.position = m_sweep.c0 - Mul(xf1.R, m_sweep.localCenter);

        // xf1.q.set(m_sweep.a0);
        // Rot.mulToOutUnsafe(xf1.q, m_sweep.localCenter, xf1.p);
        // xf1.p.mulLocal(-1).addLocal(m_sweep.c0);
        // inlined:
        xf1.q.sin = sinTable(m_sweep.a0)
        xf1.q.cos = cosTable(m_sweep.a0)
        xf1.p.x = m_sweep.c0.x - xf1.q.cos * m_sweep.localCenter.x + xf1.q.sin *
                m_sweep.localCenter.y
        xf1.p.y = m_sweep.c0.y - xf1.q.sin * m_sweep.localCenter.x - xf1.q.cos *
                m_sweep.localCenter.y
        // end inline

        var f = fixtureList
        while (f != null) {
            f.synchronize(world.contactManager.m_broadPhase, xf1, transform)
            f = f.next
        }
    }

    fun synchronizeTransform() {
        // m_xf.q.set(m_sweep.a);
        //
        // // m_xf.position = m_sweep.c - Mul(m_xf.R, m_sweep.localCenter);
        // Rot.mulToOutUnsafe(m_xf.q, m_sweep.localCenter, m_xf.p);
        // m_xf.p.mulLocal(-1).addLocal(m_sweep.c);
        //
        transform.q.sin = sinTable(m_sweep.a)
        transform.q.cos = cosTable(m_sweep.a)
        val q = transform.q
        val v = m_sweep.localCenter
        transform.p.x = m_sweep.c.x - q.cos * v.x + q.sin * v.y
        transform.p.y = m_sweep.c.y - q.sin * v.x - q.cos * v.y
    }

    /**
     * This is used to prevent connected bodies from colliding. It may lie, depending on the
     * collideConnected flag.
     *
     * @param other
     * @return
     */
    fun shouldCollide(other: Body): Boolean {
        // At least one body should be dynamic.
        if (m_type != BodyType.DYNAMIC && other.m_type != BodyType.DYNAMIC) {
            return false
        }

        // Does a joint prevent collision?
        var jn = jointList
        while (jn != null) {
            if (jn.other === other) {
                if (jn.joint!!.collideConnected == false) {
                    return false
                }
            }
            jn = jn.next
        }

        return true
    }

    fun advance(t: Double) {
        // Advance to the new safe time. This doesn't sync the broad-phase.
        m_sweep.advance(t)
        m_sweep.c.set(m_sweep.c0)
        m_sweep.a = m_sweep.a0
        transform.q.set(m_sweep.a)
        // m_xf.position = m_sweep.c - Mul(m_xf.R, m_sweep.localCenter);
        Rot.mulToOut(transform.q, m_sweep.localCenter, transform.p)
        transform.p.multiply(-1.0)
        transform.p.add(m_sweep.c)
    }

    companion object {
        const val e_islandFlag = 0x0001
        const val e_awakeFlag = 0x0002
        const val e_autoSleepFlag = 0x0004
        const val e_bulletFlag = 0x0008
        const val e_fixedRotationFlag = 0x0010
        const val e_activeFlag = 0x0020
        const val e_toiFlag = 0x0040
    }
}
