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

import org.jbox2d.callbacks.ParticleQueryCallback
import org.jbox2d.callbacks.ParticleRaycastCallback
import org.jbox2d.callbacks.QueryCallback
import org.jbox2d.collision.RayCastOutput
import org.jbox2d.collision.RaycastInput
import org.jbox2d.collision.combine
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.*
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.TimeStep
import org.jbox2d.dynamics.World
import org.jbox2d.particle.VoronoiDiagram.VoronoiDiagramCallback
import org.tobi29.math.AABB2
import org.tobi29.math.max
import org.tobi29.math.min
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.floorToInt
import org.tobi29.utils.Pool
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class ParticleSystem(private var m_world: World) {

    internal var m_timestamp: Int = 0
    private var m_allParticleFlags: Int = 0
    private var m_allGroupFlags: Int = 0
    private var m_density: Double = 0.0
    private var m_inverseDensity: Double = 0.0
    var particleGravityScale: Double = 0.0
    internal var m_particleDiameter: Double = 0.0
    internal var m_inverseDiameter = 0.0
    internal var m_squaredDiameter: Double = 0.0

    var particleCount: Int = 0
        internal set
    internal var m_internalAllocatedCapacity: Int = 0
    private var m_maxCount: Int = 0
    internal var m_flagsBuffer: ParticleBufferInt
    internal var m_positionBuffer: ParticleBuffer<MutableVector2d>
    internal var m_velocityBuffer: ParticleBuffer<MutableVector2d>
    private var m_accumulationBuffer: DoubleArray? = null // temporary values
    private var m_accumulation2Buffer: Array<MutableVector2d>? =
        null // temporary vector values
    private var m_depthBuffer: DoubleArray? = null // distance from the surface

    var m_colorBuffer: ParticleBuffer<ParticleColor>
    var particleGroupBuffer: Array<ParticleGroup?>? = null
        internal set
    private var m_userDataBuffer: ParticleBuffer<Any?>

    internal var m_proxyBuffer = Pool { Proxy() }

    var m_contactCount: Int = 0
    private var m_contactCapacity: Int = 0
    var m_contactBuffer: Array<ParticleContact>? = null

    var m_bodyContactCount: Int = 0
    internal var m_bodyContactCapacity: Int = 0
    var m_bodyContactBuffer: Array<ParticleBodyContact>? = null

    private var m_pairCount: Int = 0
    private var m_pairCapacity: Int = 0
    private var m_pairBuffer: Array<Pair>? = null

    internal var m_triadCount: Int = 0
    internal var m_triadCapacity: Int = 0
    internal var m_triadBuffer: Array<Triad>? = null

    var particleGroupCount: Int = 0
        internal set
    private var m_groupList: ParticleGroup? = null

    private var m_pressureStrength: Double = 0.0
    var particleDamping: Double = 0.0
    private var m_elasticStrength: Double = 0.0
    private var m_springStrength: Double = 0.0
    private var m_viscousStrength: Double = 0.0
    private var m_surfaceTensionStrengthA: Double = 0.0
    private var m_surfaceTensionStrengthB: Double = 0.0
    private var m_powderStrength: Double = 0.0
    private var m_ejectionStrength: Double = 0.0
    private var m_colorMixingStrength: Double = 0.0

    private val temp = AABB2()
    private val dpcallback = DestroyParticlesInShapeCallback()

    private val temp2 = AABB2()
    private val tempVec = MutableVector2d()
    private val tempTransform = Transform()
    private val tempTransform2 = Transform()
    private val createParticleGroupCallback = CreateParticleGroupCallback()
    private val tempParticleDef = ParticleDef()

    private val ubccallback = UpdateBodyContactsCallback()

    private val sccallback = SolveCollisionCallback()

    private val tempVec2 = MutableVector2d()
    private val tempRot = Rot()
    private val tempXf = Transform()
    private val tempXf2 = Transform()

    private val newIndices = NewIndices()

    var particleDensity: Double
        get() = m_density
        set(density) {
            m_density = density
            m_inverseDensity = 1 / m_density
        }

    var particleRadius: Double
        get() = m_particleDiameter / 2
        set(radius) {
            m_particleDiameter = 2 * radius
            m_squaredDiameter = m_particleDiameter * m_particleDiameter
            m_inverseDiameter = 1 / m_particleDiameter
        }

    private val particleStride: Double
        get() = Settings.particleStride * m_particleDiameter

    internal val particleMass: Double
        get() {
            val stride = particleStride
            return m_density * stride * stride
        }

    internal val particleInvMass: Double
        get() = 1.777777 * m_inverseDensity * m_inverseDiameter * m_inverseDiameter

    val particleFlagsBuffer: IntArray?
        get() = m_flagsBuffer.data

    val particlePositionBuffer: Array<MutableVector2d>?
        get() = m_positionBuffer.data

    val particleVelocityBuffer: Array<MutableVector2d>?
        get() = m_velocityBuffer.data

    val particleColorBuffer: Array<ParticleColor>?
        get() {
            m_colorBuffer.data = requestParticleBuffer(
                m_colorBuffer.data
            ) { ParticleColor() }
            return m_colorBuffer.data
        }

    val particleUserDataBuffer: Array<Any?>?
        get() {
            m_userDataBuffer.data = requestParticleBuffer(
                m_userDataBuffer.data
            ) { null }
            return m_userDataBuffer.data
        }

    var particleMaxCount: Int
        get() = m_maxCount
        set(count) {
            assert { particleCount <= count }
            m_maxCount = count
        }

    init {
        m_timestamp = 0
        m_allParticleFlags = 0
        m_allGroupFlags = 0
        m_density = 1.0
        m_inverseDensity = 1.0
        particleGravityScale = 1.0
        m_particleDiameter = 1.0
        m_inverseDiameter = 1.0
        m_squaredDiameter = 1.0

        particleCount = 0
        m_internalAllocatedCapacity = 0
        m_maxCount = 0

        m_contactCount = 0
        m_contactCapacity = 0

        m_bodyContactCount = 0
        m_bodyContactCapacity = 0

        m_pairCount = 0
        m_pairCapacity = 0

        m_triadCount = 0
        m_triadCapacity = 0

        particleGroupCount = 0

        m_pressureStrength = 0.05
        particleDamping = 1.0
        m_elasticStrength = 0.25
        m_springStrength = 0.25
        m_viscousStrength = 0.25
        m_surfaceTensionStrengthA = 0.1
        m_surfaceTensionStrengthB = 0.2
        m_powderStrength = 0.5
        m_ejectionStrength = 0.5
        m_colorMixingStrength = 0.5

        m_flagsBuffer = ParticleBufferInt()
        m_positionBuffer = ParticleBuffer()
        m_velocityBuffer = ParticleBuffer()
        m_colorBuffer = ParticleBuffer()
        m_userDataBuffer = ParticleBuffer()
    }

    //  public void assertNotSamePosition() {
    //    for (int i = 0; i < m_count; i++) {
    //      Vec2 vi = m_positionBuffer.data[i];
    //      for (int j = i + 1; j < m_count; j++) {
    //        Vec2 vj = m_positionBuffer.data[j];
    //        assert { vi.x != vj.x || vi.y != vj.y };
    //      }
    //    }
    //  }

    fun createParticle(def: ParticleDef): Int {
        if (particleCount >= m_internalAllocatedCapacity) {
            var capacity =
                if (particleCount != 0) 2 * particleCount else Settings.minParticleBufferCapacity
            capacity = limitCapacity(capacity, m_maxCount)
            capacity = limitCapacity(
                capacity,
                m_flagsBuffer.userSuppliedCapacity
            )
            capacity = limitCapacity(
                capacity,
                m_positionBuffer.userSuppliedCapacity
            )
            capacity = limitCapacity(
                capacity,
                m_velocityBuffer.userSuppliedCapacity
            )
            capacity = limitCapacity(
                capacity,
                m_colorBuffer.userSuppliedCapacity
            )
            capacity = limitCapacity(
                capacity,
                m_userDataBuffer.userSuppliedCapacity
            )
            if (m_internalAllocatedCapacity < capacity) {
                m_flagsBuffer.data = reallocateBuffer(
                    m_flagsBuffer,
                    m_internalAllocatedCapacity, capacity, false
                )
                m_positionBuffer.data = reallocateBuffer(
                    m_positionBuffer,
                    m_internalAllocatedCapacity, capacity
                ) { MutableVector2d() }
                m_velocityBuffer.data = reallocateBuffer(
                    m_velocityBuffer,
                    m_internalAllocatedCapacity, capacity
                ) { MutableVector2d() }
                m_accumulationBuffer = BufferUtils.reallocateBuffer(
                    m_accumulationBuffer, 0, m_internalAllocatedCapacity,
                    capacity, false
                )
                m_accumulation2Buffer = BufferUtils.reallocateBufferDeferred(
                    m_accumulation2Buffer, m_internalAllocatedCapacity,
                    capacity
                ) { MutableVector2d() }
                m_depthBuffer = BufferUtils.reallocateBuffer(
                    m_depthBuffer, 0,
                    m_internalAllocatedCapacity, capacity,
                    true
                )
                m_colorBuffer.data = reallocateBufferDeferred(
                    m_colorBuffer,
                    m_internalAllocatedCapacity,
                    capacity
                ) { ParticleColor() }
                particleGroupBuffer = BufferUtils.reallocateBuffer(
                    particleGroupBuffer, m_internalAllocatedCapacity,
                    capacity
                ) { ParticleGroup() }
                m_userDataBuffer.data = reallocateBufferDeferred(
                    m_userDataBuffer, m_internalAllocatedCapacity,
                    capacity
                ) { null }
                m_internalAllocatedCapacity = capacity
            }
        }
        if (particleCount >= m_internalAllocatedCapacity) {
            return Settings.invalidParticleIndex
        }
        val index = particleCount++
        m_flagsBuffer.data!![index] = def.flags
        m_positionBuffer.data!![index].set(def.position)
        //    assertNotSamePosition();
        m_velocityBuffer.data!![index].set(def.velocity)
        particleGroupBuffer!![index] = null
        if (m_depthBuffer != null) {
            m_depthBuffer!![index] = 0.0
        }
        if (m_colorBuffer.data != null || def.color != null) {
            m_colorBuffer.data = requestParticleBuffer(
                m_colorBuffer.data
            ) { ParticleColor() }
            m_colorBuffer.data!![index].set(def.color!!)
        }
        if (m_userDataBuffer.data != null || def.userData != null) {
            m_userDataBuffer.data = requestParticleBuffer(
                m_userDataBuffer.data
            ) { null }
            m_userDataBuffer.data!![index] = def.userData
        }
        m_proxyBuffer.push().index = index
        return index
    }

    fun destroyParticle(
        index: Int,
        callDestructionListener: Boolean
    ) {
        var flags = ParticleType.b2_zombieParticle
        if (callDestructionListener) {
            flags = flags or ParticleType.b2_destructionListener
        }
        m_flagsBuffer.data!![index] = m_flagsBuffer.data!![index] or flags
    }

    fun destroyParticlesInShape(
        shape: Shape,
        xf: Transform,
        callDestructionListener: Boolean
    ): Int {
        dpcallback.init(this, shape, xf, callDestructionListener)
        shape.computeAABB(temp, xf, 0)
        m_world.queryAABB(dpcallback, temp)
        return dpcallback.destroyed
    }

    fun destroyParticlesInGroup(
        group: ParticleGroup,
        callDestructionListener: Boolean
    ) {
        for (i in group.bufferIndex until group.m_lastIndex) {
            destroyParticle(i, callDestructionListener)
        }
    }

    fun createParticleGroup(groupDef: ParticleGroupDef): ParticleGroup {
        val stride = particleStride
        val identity = tempTransform
        identity.setIdentity()
        val transform = tempTransform2
        transform.setIdentity()
        val firstIndex = particleCount
        if (groupDef.shape != null) {
            val particleDef = tempParticleDef
            particleDef.flags = groupDef.flags
            particleDef.color = groupDef.color
            particleDef.userData = groupDef.userData
            val shape = groupDef.shape
            transform.set(groupDef.position.now(), groupDef.angle)
            val aabb = temp
            val childCount = shape!!.childCount
            for (childIndex in 0 until childCount) {
                if (childIndex == 0) {
                    shape.computeAABB(aabb, identity, childIndex)
                } else {
                    val childAABB = temp2
                    shape.computeAABB(childAABB, identity, childIndex)
                    aabb.combine(childAABB)
                }
            }
            val upperBoundY = aabb.max.y
            val upperBoundX = aabb.max.x
            var y = (aabb.min.y / stride).floorToInt() * stride
            while (y < upperBoundY) {
                var x = (aabb.min.x / stride).floorToInt() * stride
                while (x < upperBoundX) {
                    val pp = Vector2d(x, y)
                    if (shape.testPoint(identity, pp)) {
                        val p = Transform.mul(transform, pp)
                        particleDef.position.x = p.x
                        particleDef.position.y = p.y
                        particleDef.velocity.set(
                            groupDef.angularVelocity cross
                                    (p - groupDef.position.now())
                        )
                        particleDef.velocity.add(groupDef.linearVelocity)
                        createParticle(particleDef)
                    }
                    x += stride
                }
                y += stride
            }
        }
        val lastIndex = particleCount

        val group = ParticleGroup()
        group.m_system = this
        group.bufferIndex = firstIndex
        group.m_lastIndex = lastIndex
        group.groupFlags = groupDef.groupFlags
        group.m_strength = groupDef.strength
        group.userData = groupDef.userData
        group.transform.set(transform)
        group.m_destroyAutomatically = groupDef.destroyAutomatically
        group.m_prev = null
        group.next = m_groupList
        if (m_groupList != null) {
            m_groupList!!.m_prev = group
        }
        m_groupList = group
        ++particleGroupCount
        for (i in firstIndex until lastIndex) {
            particleGroupBuffer!![i] = group
        }

        updateContacts(true)
        if (groupDef.flags and k_pairFlags != 0) {
            for (k in 0 until m_contactCount) {
                val contact = m_contactBuffer!![k]
                var a = contact.indexA
                var b = contact.indexB
                if (a > b) {
                    val temp = a
                    a = b
                    b = temp
                }
                if (firstIndex <= a && b < lastIndex) {
                    if (m_pairCount >= m_pairCapacity) {
                        val oldCapacity = m_pairCapacity
                        val newCapacity =
                            if (m_pairCount != 0) 2 * m_pairCount else Settings.minParticleBufferCapacity
                        m_pairBuffer = BufferUtils.reallocateBuffer(
                            m_pairBuffer, oldCapacity,
                            newCapacity
                        ) { Pair() }
                        m_pairCapacity = newCapacity
                    }
                    val pair = m_pairBuffer!![m_pairCount]
                    pair.indexA = a
                    pair.indexB = b
                    pair.flags = contact.flags
                    pair.strength = groupDef.strength
                    pair.distance = m_positionBuffer.data!![a] distance
                            m_positionBuffer.data!![b]
                    m_pairCount++
                }
            }
        }
        if (groupDef.flags and k_triadFlags != 0) {
            val diagram = VoronoiDiagram(lastIndex - firstIndex)
            for (i in firstIndex until lastIndex) {
                diagram.addGenerator(m_positionBuffer.data!![i], i)
            }
            diagram.generate(stride / 2)
            createParticleGroupCallback.system = this
            createParticleGroupCallback.def = groupDef
            createParticleGroupCallback.firstIndex = firstIndex
            diagram.getNodes(createParticleGroupCallback)
        }
        if (groupDef.groupFlags and ParticleGroupType.b2_solidParticleGroup != 0) {
            computeDepthForGroup(group)
        }

        return group
    }

    fun joinParticleGroups(
        groupA: ParticleGroup,
        groupB: ParticleGroup
    ) {
        assert { groupA !== groupB }
        RotateBuffer(groupB.bufferIndex, groupB.m_lastIndex, particleCount)
        assert { groupB.m_lastIndex == particleCount }
        RotateBuffer(groupA.bufferIndex, groupA.m_lastIndex, groupB.bufferIndex)
        assert { groupA.m_lastIndex == groupB.bufferIndex }

        var particleFlags = 0
        for (i in groupA.bufferIndex until groupB.m_lastIndex) {
            particleFlags = particleFlags or m_flagsBuffer.data!![i]
        }

        updateContacts(true)
        if (particleFlags and k_pairFlags != 0) {
            for (k in 0 until m_contactCount) {
                val contact = m_contactBuffer!![k]
                var a = contact.indexA
                var b = contact.indexB
                if (a > b) {
                    val temp = a
                    a = b
                    b = temp
                }
                if (groupA.bufferIndex <= a && a < groupA.m_lastIndex && groupB.bufferIndex <= b
                    && b < groupB.m_lastIndex) {
                    if (m_pairCount >= m_pairCapacity) {
                        val oldCapacity = m_pairCapacity
                        val newCapacity =
                            if (m_pairCount != 0) 2 * m_pairCount else Settings.minParticleBufferCapacity
                        m_pairBuffer = BufferUtils.reallocateBuffer(
                            m_pairBuffer, oldCapacity,
                            newCapacity
                        ) { Pair() }
                        m_pairCapacity = newCapacity
                    }
                    val pair = m_pairBuffer!![m_pairCount]
                    pair.indexA = a
                    pair.indexB = b
                    pair.flags = contact.flags
                    pair.strength = min(
                        groupA.m_strength,
                        groupB.m_strength
                    )
                    pair.distance = m_positionBuffer.data!![a] distance
                            m_positionBuffer.data!![b]
                    m_pairCount++
                }
            }
        }
        if (particleFlags and k_triadFlags != 0) {
            val diagram = VoronoiDiagram(
                groupB.m_lastIndex - groupA.bufferIndex
            )
            for (i in groupA.bufferIndex until groupB.m_lastIndex) {
                if (m_flagsBuffer.data!![i] and ParticleType.b2_zombieParticle == 0) {
                    diagram.addGenerator(m_positionBuffer.data!![i], i)
                }
            }
            diagram.generate(particleStride / 2)
            val callback = JoinParticleGroupsCallback()
            callback.system = this
            callback.groupA = groupA
            callback.groupB = groupB
            diagram.getNodes(callback)
        }

        for (i in groupB.bufferIndex until groupB.m_lastIndex) {
            particleGroupBuffer!![i] = groupA
        }
        val groupFlags = groupA.groupFlags or groupB.groupFlags
        groupA.groupFlags = groupFlags
        groupA.m_lastIndex = groupB.m_lastIndex
        groupB.bufferIndex = groupB.m_lastIndex
        destroyParticleGroup(groupB)

        if (groupFlags and ParticleGroupType.b2_solidParticleGroup != 0) {
            computeDepthForGroup(groupA)
        }
    }

    // Only called from solveZombie() or joinParticleGroups().
    private fun destroyParticleGroup(group: ParticleGroup?) {
        assert { particleGroupCount > 0 }
        assert { group != null }

        if (m_world.particleDestructionListener != null) {
            m_world.particleDestructionListener!!.sayGoodbye(group!!)
        }

        for (i in group!!.bufferIndex until group.m_lastIndex) {
            particleGroupBuffer!![i] = null
        }

        if (group.m_prev != null) {
            group.m_prev!!.next = group.next
        }
        if (group.next != null) {
            group.next!!.m_prev = group.m_prev
        }
        if (group === m_groupList) {
            m_groupList = group.next
        }

        --particleGroupCount
    }

    fun computeDepthForGroup(group: ParticleGroup) {
        for (i in group.bufferIndex until group.m_lastIndex) {
            m_accumulationBuffer!![i] = 0.0
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            if (a >= group.bufferIndex && a < group.m_lastIndex && b >= group.bufferIndex
                && b < group.m_lastIndex) {
                val w = contact.weight
                m_accumulationBuffer!![a] += w
                m_accumulationBuffer!![b] += w
            }
        }
        m_depthBuffer = requestParticleBuffer(m_depthBuffer)
        for (i in group.bufferIndex until group.m_lastIndex) {
            val w = m_accumulationBuffer!![i]
            m_depthBuffer!![i] = if (w < 0.8) 0.0 else Double.MAX_VALUE
        }
        val interationCount = group.particleCount
        for (t in 0 until interationCount) {
            var updated = false
            for (k in 0 until m_contactCount) {
                val contact = m_contactBuffer!![k]
                val a = contact.indexA
                val b = contact.indexB
                if (a >= group.bufferIndex && a < group.m_lastIndex && b >= group.bufferIndex
                    && b < group.m_lastIndex) {
                    val r = 1 - contact.weight
                    val ap0 = m_depthBuffer!![a]
                    val bp0 = m_depthBuffer!![b]
                    val ap1 = bp0 + r
                    val bp1 = ap0 + r
                    if (ap0 > ap1) {
                        m_depthBuffer!![a] = ap1
                        updated = true
                    }
                    if (bp0 > bp1) {
                        m_depthBuffer!![b] = bp1
                        updated = true
                    }
                }
            }
            if (!updated) {
                break
            }
        }
        for (i in group.bufferIndex until group.m_lastIndex) {
            val p = m_depthBuffer!![i]
            if (p < Double.MAX_VALUE) {
                m_depthBuffer!![i] *= m_particleDiameter
            } else {
                m_depthBuffer!![i] = 0.0
            }
        }
    }

    fun addContact(
        a: Int,
        b: Int
    ) {
        assert { a != b }
        val pa = m_positionBuffer.data!![a]
        val pb = m_positionBuffer.data!![b]
        val dx = pb.x - pa.x
        val dy = pb.y - pa.y
        val d2 = dx * dx + dy * dy
        //    assert { d2 != 0 };
        if (d2 < m_squaredDiameter) {
            if (m_contactCount >= m_contactCapacity) {
                val oldCapacity = m_contactCapacity
                val newCapacity =
                    if (m_contactCount != 0) 2 * m_contactCount else Settings.minParticleBufferCapacity
                m_contactBuffer = BufferUtils.reallocateBuffer(
                    m_contactBuffer,
                    oldCapacity, newCapacity
                ) { ParticleContact() }
                m_contactCapacity = newCapacity
            }
            val invD = if (d2 != 0.0) sqrt(1 / d2) else Double.MAX_VALUE
            val contact = m_contactBuffer!![m_contactCount]
            contact.indexA = a
            contact.indexB = b
            contact.flags = m_flagsBuffer.data!![a] or m_flagsBuffer.data!![b]
            contact.weight = 1 - d2 * invD * m_inverseDiameter
            contact.normal.x = invD * dx
            contact.normal.y = invD * dy
            m_contactCount++
        }
    }

    fun updateContacts(exceptZombie: Boolean) {
        for (p in 0 until m_proxyBuffer.size) {
            val proxy = m_proxyBuffer[p]
            val i = proxy.index
            val pos = m_positionBuffer.data!![i]
            proxy.tag = computeTag(
                m_inverseDiameter * pos.x,
                m_inverseDiameter * pos.y
            )
        }
        m_proxyBuffer.sort()
        m_contactCount = 0
        var c_index = 0
        for (i in 0 until m_proxyBuffer.size) {
            val a = m_proxyBuffer[i]
            val rightTag = computeRelativeTag(a.tag, 1, 0)
            for (j in i + 1 until m_proxyBuffer.size) {
                val b = m_proxyBuffer[j]
                if (rightTag < b.tag) {
                    break
                }
                addContact(a.index, b.index)
            }
            val bottomLeftTag = computeRelativeTag(a.tag, -1, 1)
            while (c_index < m_proxyBuffer.size) {
                val c = m_proxyBuffer[c_index]
                if (bottomLeftTag <= c.tag) {
                    break
                }
                c_index++
            }
            val bottomRightTag = computeRelativeTag(a.tag, 1, 1)

            for (b_index in c_index until m_proxyBuffer.size) {
                val b = m_proxyBuffer[b_index]
                if (bottomRightTag < b.tag) {
                    break
                }
                addContact(a.index, b.index)
            }
        }
        if (exceptZombie) {
            var j = m_contactCount
            var i = 0
            while (i < j) {
                if (m_contactBuffer!![i].flags and ParticleType.b2_zombieParticle != 0) {
                    --j
                    val temp = m_contactBuffer!![j]
                    m_contactBuffer!![j] = m_contactBuffer!![i]
                    m_contactBuffer!![i] = temp
                    --i
                }
                i++
            }
            m_contactCount = j
        }
    }

    fun updateBodyContacts() {
        val aabb = temp
        aabb.min.x = Double.MAX_VALUE
        aabb.min.y = Double.MAX_VALUE
        aabb.max.x = -Double.MAX_VALUE
        aabb.max.y = -Double.MAX_VALUE
        for (i in 0 until particleCount) {
            val p = m_positionBuffer.data!![i]
            aabb.min.set(min(p, aabb.min))
            aabb.max.set(max(p, aabb.max))
        }
        aabb.min.x -= m_particleDiameter
        aabb.min.y -= m_particleDiameter
        aabb.max.x += m_particleDiameter
        aabb.max.y += m_particleDiameter
        m_bodyContactCount = 0

        ubccallback.system = this
        m_world.queryAABB(ubccallback, aabb)
    }

    fun solveCollision(step: TimeStep) {
        val aabb = temp
        val lowerBound = aabb.min
        val upperBound = aabb.max
        lowerBound.x = Double.MAX_VALUE
        lowerBound.y = Double.MAX_VALUE
        upperBound.x = -Double.MAX_VALUE
        upperBound.y = -Double.MAX_VALUE
        for (i in 0 until particleCount) {
            val v = m_velocityBuffer.data!![i]
            val p1 = m_positionBuffer.data!![i]
            val p1x = p1.x
            val p1y = p1.y
            val p2x = p1x + step.dt * v.x
            val p2y = p1y + step.dt * v.y
            val bx = if (p1x < p2x) p1x else p2x
            val by = if (p1y < p2y) p1y else p2y
            lowerBound.x = if (lowerBound.x < bx) lowerBound.x else bx
            lowerBound.y = if (lowerBound.y < by) lowerBound.y else by
            val b1x = if (p1x > p2x) p1x else p2x
            val b1y = if (p1y > p2y) p1y else p2y
            upperBound.x = if (upperBound.x > b1x) upperBound.x else b1x
            upperBound.y = if (upperBound.y > b1y) upperBound.y else b1y
        }
        sccallback.step = step
        sccallback.system = this
        m_world.queryAABB(sccallback, aabb)
    }

    fun solve(step: TimeStep) {
        ++m_timestamp
        if (particleCount == 0) {
            return
        }
        m_allParticleFlags = 0
        for (i in 0 until particleCount) {
            m_allParticleFlags = m_allParticleFlags or m_flagsBuffer.data!![i]
        }
        if (m_allParticleFlags and ParticleType.b2_zombieParticle != 0) {
            solveZombie()
        }
        if (particleCount == 0) {
            return
        }
        m_allGroupFlags = 0
        var group = m_groupList
        while (group != null) {
            m_allGroupFlags = m_allGroupFlags or group.groupFlags
            group = group.next
        }
        val gravityx = step.dt * particleGravityScale * m_world.gravity.x
        val gravityy = step.dt * particleGravityScale * m_world.gravity.y
        val criticalVelocytySquared = getCriticalVelocitySquared(step)
        for (i in 0 until particleCount) {
            val v = m_velocityBuffer.data!![i]
            v.x += gravityx
            v.y += gravityy
            val v2 = v.x * v.x + v.y * v.y
            if (v2 > criticalVelocytySquared) {
                val a = if (v2 == 0.0) Double.MAX_VALUE else sqrt(
                    criticalVelocytySquared / v2
                )
                v.x *= a
                v.y *= a
            }
        }
        solveCollision(step)
        if (m_allGroupFlags and ParticleGroupType.b2_rigidParticleGroup != 0) {
            solveRigid(step)
        }
        if (m_allParticleFlags and ParticleType.b2_wallParticle != 0) {
            solveWall(step)
        }
        for (i in 0 until particleCount) {
            val pos = m_positionBuffer.data!![i]
            val vel = m_velocityBuffer.data!![i]
            pos.x += step.dt * vel.x
            pos.y += step.dt * vel.y
        }
        updateBodyContacts()
        updateContacts(false)
        if (m_allParticleFlags and ParticleType.b2_viscousParticle != 0) {
            solveViscous(step)
        }
        if (m_allParticleFlags and ParticleType.b2_powderParticle != 0) {
            solvePowder(step)
        }
        if (m_allParticleFlags and ParticleType.b2_tensileParticle != 0) {
            solveTensile(step)
        }
        if (m_allParticleFlags and ParticleType.b2_elasticParticle != 0) {
            solveElastic(step)
        }
        if (m_allParticleFlags and ParticleType.b2_springParticle != 0) {
            solveSpring(step)
        }
        if (m_allGroupFlags and ParticleGroupType.b2_solidParticleGroup != 0) {
            solveSolid(step)
        }
        if (m_allParticleFlags and ParticleType.b2_colorMixingParticle != 0) {
            solveColorMixing(step)
        }
        solvePressure(step)
        solveDamping(step)
    }

    private fun solvePressure(step: TimeStep) {
        // calculates the sum of contact-weights for each particle
        // that means dimensionless density
        for (i in 0 until particleCount) {
            m_accumulationBuffer!![i] = 0.0
        }
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            val a = contact.index
            val w = contact.weight
            m_accumulationBuffer!![a] += w
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            val w = contact.weight
            m_accumulationBuffer!![a] += w
            m_accumulationBuffer!![b] += w
        }
        // ignores powder particles
        if (m_allParticleFlags and k_noPressureFlags != 0) {
            for (i in 0 until particleCount) {
                if (m_flagsBuffer.data!![i] and k_noPressureFlags != 0) {
                    m_accumulationBuffer!![i] = 0.0
                }
            }
        }
        // calculates pressure as a linear function of density
        val pressurePerWeight = m_pressureStrength * getCriticalPressure(step)
        for (i in 0 until particleCount) {
            val w = m_accumulationBuffer!![i]
            val h = pressurePerWeight * max(
                0.0, min(
                    w,
                    Settings.maxParticleWeight
                ) - Settings.minParticleWeight
            )
            m_accumulationBuffer!![i] = h
        }
        // applies pressure between each particles in contact
        val velocityPerPressure = step.dt / (m_density * m_particleDiameter)
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            val a = contact.index
            val b = contact.body
            val w = contact.weight
            val m = contact.mass
            val n = contact.normal
            val p = m_positionBuffer.data!![a]
            val h = m_accumulationBuffer!![a] + pressurePerWeight * w
            val f = tempVec
            val coef = velocityPerPressure * w * m * h
            f.x = coef * n.x
            f.y = coef * n.y
            val velData = m_velocityBuffer.data!![a]
            val particleInvMass = particleInvMass
            velData.x -= particleInvMass * f.x
            velData.y -= particleInvMass * f.y
            b!!.applyLinearImpulse(f.now(), p.now(), true)
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            val w = contact.weight
            val n = contact.normal
            val h = m_accumulationBuffer!![a] + m_accumulationBuffer!![b]
            val fx = velocityPerPressure * w * h * n.x
            val fy = velocityPerPressure * w * h * n.y
            val velDataA = m_velocityBuffer.data!![a]
            val velDataB = m_velocityBuffer.data!![b]
            velDataA.x -= fx
            velDataA.y -= fy
            velDataB.x += fx
            velDataB.y += fy
        }
    }

    private fun solveDamping(step: TimeStep) {
        // reduces normal velocity of each contact
        val damping = particleDamping
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            val a = contact.index
            val b = contact.body
            val w = contact.weight
            val m = contact.mass
            val n = contact.normal
            val p = m_positionBuffer.data!![a]
            val tempX = p.x - b!!.m_sweep.c.x
            val tempY = p.y - b.m_sweep.c.y
            val velA = m_velocityBuffer.data!![a]
            // getLinearVelocityFromWorldPointToOut, with -= velA
            val vx =
                -b.m_angularVelocity * tempY + b.m_linearVelocity.x - velA.x
            val vy = b.m_angularVelocity * tempX + b.m_linearVelocity.y - velA.y
            // done
            val vn = vx * n.x + vy * n.y
            if (vn < 0) {
                val f = tempVec
                f.x = damping * w * m * vn * n.x
                f.y = damping * w * m * vn * n.y
                val invMass = particleInvMass
                velA.x += invMass * f.x
                velA.y += invMass * f.y
                f.x = -f.x
                f.y = -f.y
                b.applyLinearImpulse(f.now(), p.now(), true)
            }
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            val w = contact.weight
            val n = contact.normal
            val velA = m_velocityBuffer.data!![a]
            val velB = m_velocityBuffer.data!![b]
            val vx = velB.x - velA.x
            val vy = velB.y - velA.y
            val vn = vx * n.x + vy * n.y
            if (vn < 0) {
                val fx = damping * w * vn * n.x
                val fy = damping * w * vn * n.y
                velA.x += fx
                velA.y += fy
                velB.x -= fx
                velB.y -= fy
            }
        }
    }

    fun solveWall(step: TimeStep) {
        for (i in 0 until particleCount) {
            if (m_flagsBuffer.data!![i] and ParticleType.b2_wallParticle != 0) {
                val r = m_velocityBuffer.data!![i]
                r.x = 0.0
                r.y = 0.0
            }
        }
    }

    private fun solveRigid(step: TimeStep) {
        var group = m_groupList
        while (group != null) {
            if (group.groupFlags and ParticleGroupType.b2_rigidParticleGroup != 0) {
                group.updateStatistics()
                val temp = tempVec
                val cross = tempVec2
                val rotation = tempRot
                rotation.set(step.dt * group.m_angularVelocity)
                Rot.mulToOut(rotation, group.m_center, cross)
                temp.set(group.m_linearVelocity)
                temp.multiply(step.dt)
                temp.add(group.m_center)
                temp.subtract(cross)
                tempXf.p.set(temp)
                tempXf.q.set(rotation)
                group.transform.set(Transform.mul(tempXf, group.transform))
                val velocityTransform = tempXf2
                velocityTransform.p.x = step.inv_dt * tempXf.p.x
                velocityTransform.p.y = step.inv_dt * tempXf.p.y
                velocityTransform.q.sin = step.inv_dt * tempXf.q.sin
                velocityTransform.q.cos = step.inv_dt * (tempXf.q.cos - 1)
                for (i in group.bufferIndex until group.m_lastIndex) {
                    m_velocityBuffer.data!![i].set(
                        Transform.mul(
                            velocityTransform,
                            m_positionBuffer.data!![i].now()
                        )
                    )
                }
            }
            group = group.next
        }
    }

    private fun solveElastic(step: TimeStep) {
        val elasticStrength = step.inv_dt * m_elasticStrength
        for (k in 0 until m_triadCount) {
            val triad = m_triadBuffer!![k]
            if (triad.flags and ParticleType.b2_elasticParticle != 0) {
                val a = triad.indexA
                val b = triad.indexB
                val c = triad.indexC
                val oa = triad.pa
                val ob = triad.pb
                val oc = triad.pc
                val pa = m_positionBuffer.data!![a]
                val pb = m_positionBuffer.data!![b]
                val pc = m_positionBuffer.data!![c]
                val px = 1.0 / 3 * (pa.x + pb.x + pc.x)
                val py = 1.0 / 3 * (pa.y + pb.y + pc.y)
                var rs = (oa cross pa) + (ob cross pb) + (oc cross pc
                        )
                var rc = (oa dot pa) + (ob dot pb) + (oc dot pc)
                val r2 = rs * rs + rc * rc
                val invR = if (r2 == 0.0) Double.MAX_VALUE else sqrt(1.0 / r2)
                rs *= invR
                rc *= invR
                val strength = elasticStrength * triad.strength
                val roax = rc * oa.x - rs * oa.y
                val roay = rs * oa.x + rc * oa.y
                val robx = rc * ob.x - rs * ob.y
                val roby = rs * ob.x + rc * ob.y
                val rocx = rc * oc.x - rs * oc.y
                val rocy = rs * oc.x + rc * oc.y
                val va = m_velocityBuffer.data!![a]
                val vb = m_velocityBuffer.data!![b]
                val vc = m_velocityBuffer.data!![c]
                va.x += strength * (roax - (pa.x - px))
                va.y += strength * (roay - (pa.y - py))
                vb.x += strength * (robx - (pb.x - px))
                vb.y += strength * (roby - (pb.y - py))
                vc.x += strength * (rocx - (pc.x - px))
                vc.y += strength * (rocy - (pc.y - py))
            }
        }
    }

    private fun solveSpring(step: TimeStep) {
        val springStrength = step.inv_dt * m_springStrength
        for (k in 0 until m_pairCount) {
            val pair = m_pairBuffer!![k]
            if (pair.flags and ParticleType.b2_springParticle != 0) {
                val a = pair.indexA
                val b = pair.indexB
                val pa = m_positionBuffer.data!![a]
                val pb = m_positionBuffer.data!![b]
                val dx = pb.x - pa.x
                val dy = pb.y - pa.y
                val r0 = pair.distance
                var r1 = sqrt(dx * dx + dy * dy)
                if (r1 == 0.0) r1 = Double.MAX_VALUE
                val strength = springStrength * pair.strength
                val fx = strength * (r0 - r1) / r1 * dx
                val fy = strength * (r0 - r1) / r1 * dy
                val va = m_velocityBuffer.data!![a]
                val vb = m_velocityBuffer.data!![b]
                va.x -= fx
                va.y -= fy
                vb.x += fx
                vb.y += fy
            }
        }
    }

    private fun solveTensile(step: TimeStep) {
        m_accumulation2Buffer = requestParticleBuffer(
            m_accumulation2Buffer
        ) { MutableVector2d() }
        for (i in 0 until particleCount) {
            m_accumulationBuffer!![i] = 0.0
            m_accumulation2Buffer!![i].setXY(0.0, 0.0)
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            if (contact.flags and ParticleType.b2_tensileParticle != 0) {
                val a = contact.indexA
                val b = contact.indexB
                val w = contact.weight
                val n = contact.normal
                m_accumulationBuffer!![a] += w
                m_accumulationBuffer!![b] += w
                val a2A = m_accumulation2Buffer!![a]
                val a2B = m_accumulation2Buffer!![b]
                val inter = (1 - w) * w
                a2A.x -= inter * n.x
                a2A.y -= inter * n.y
                a2B.x += inter * n.x
                a2B.y += inter * n.y
            }
        }
        val strengthA = m_surfaceTensionStrengthA * getCriticalVelocity(step)
        val strengthB = m_surfaceTensionStrengthB * getCriticalVelocity(step)
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            if (contact.flags and ParticleType.b2_tensileParticle != 0) {
                val a = contact.indexA
                val b = contact.indexB
                val w = contact.weight
                val n = contact.normal
                val a2A = m_accumulation2Buffer!![a]
                val a2B = m_accumulation2Buffer!![b]
                val h = m_accumulationBuffer!![a] + m_accumulationBuffer!![b]
                val sx = a2B.x - a2A.x
                val sy = a2B.y - a2A.y
                val fn =
                    (strengthA * (h - 2) + strengthB * (sx * n.x + sy * n.y)) * w
                val fx = fn * n.x
                val fy = fn * n.y
                val va = m_velocityBuffer.data!![a]
                val vb = m_velocityBuffer.data!![b]
                va.x -= fx
                va.y -= fy
                vb.x += fx
                vb.y += fy
            }
        }
    }

    private fun solveViscous(step: TimeStep) {
        val viscousStrength = m_viscousStrength
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            val a = contact.index
            if (m_flagsBuffer.data!![a] and ParticleType.b2_viscousParticle != 0) {
                val b = contact.body
                val w = contact.weight
                val m = contact.mass
                val p = m_positionBuffer.data!![a]
                val va = m_velocityBuffer.data!![a]
                val tempX = p.x - b!!.m_sweep.c.x
                val tempY = p.y - b.m_sweep.c.y
                val vx =
                    -b.m_angularVelocity * tempY + b.m_linearVelocity.x - va.x
                val vy =
                    b.m_angularVelocity * tempX + b.m_linearVelocity.y - va.y
                val f = tempVec
                val pInvMass = particleInvMass
                f.x = viscousStrength * m * w * vx
                f.y = viscousStrength * m * w * vy
                va.x += pInvMass * f.x
                va.y += pInvMass * f.y
                f.x = -f.x
                f.y = -f.y
                b.applyLinearImpulse(f.now(), p.now(), true)
            }
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            if (contact.flags and ParticleType.b2_viscousParticle != 0) {
                val a = contact.indexA
                val b = contact.indexB
                val w = contact.weight
                val va = m_velocityBuffer.data!![a]
                val vb = m_velocityBuffer.data!![b]
                val vx = vb.x - va.x
                val vy = vb.y - va.y
                val fx = viscousStrength * w * vx
                val fy = viscousStrength * w * vy
                va.x += fx
                va.y += fy
                vb.x -= fx
                vb.y -= fy
            }
        }
    }

    private fun solvePowder(step: TimeStep) {
        val powderStrength = m_powderStrength * getCriticalVelocity(step)
        val minWeight = 1.0 - Settings.particleStride
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            val a = contact.index
            if (m_flagsBuffer.data!![a] and ParticleType.b2_powderParticle != 0) {
                val w = contact.weight
                if (w > minWeight) {
                    val b = contact.body
                    val m = contact.mass
                    val p = m_positionBuffer.data!![a]
                    val n = contact.normal
                    val f = tempVec
                    val va = m_velocityBuffer.data!![a]
                    val inter = powderStrength * m * (w - minWeight)
                    val pInvMass = particleInvMass
                    f.x = inter * n.x
                    f.y = inter * n.y
                    va.x -= pInvMass * f.x
                    va.y -= pInvMass * f.y
                    b!!.applyLinearImpulse(f.now(), p.now(), true)
                }
            }
        }
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            if (contact.flags and ParticleType.b2_powderParticle != 0) {
                val w = contact.weight
                if (w > minWeight) {
                    val a = contact.indexA
                    val b = contact.indexB
                    val n = contact.normal
                    val va = m_velocityBuffer.data!![a]
                    val vb = m_velocityBuffer.data!![b]
                    val inter = powderStrength * (w - minWeight)
                    val fx = inter * n.x
                    val fy = inter * n.y
                    va.x -= fx
                    va.y -= fy
                    vb.x += fx
                    vb.y += fy
                }
            }
        }
    }

    private fun solveSolid(step: TimeStep) {
        // applies extra repulsive force from solid particle groups
        m_depthBuffer = requestParticleBuffer(m_depthBuffer)
        val ejectionStrength = step.inv_dt * m_ejectionStrength
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            if (particleGroupBuffer!![a] !== particleGroupBuffer!![b]) {
                val w = contact.weight
                val n = contact.normal
                val h = m_depthBuffer!![a] + m_depthBuffer!![b]
                val va = m_velocityBuffer.data!![a]
                val vb = m_velocityBuffer.data!![b]
                val inter = ejectionStrength * h * w
                val fx = inter * n.x
                val fy = inter * n.y
                va.x -= fx
                va.y -= fy
                vb.x += fx
                vb.y += fy
            }
        }
    }

    private fun solveColorMixing(step: TimeStep) {
        // mixes color between contacting particles
        m_colorBuffer.data = requestParticleBuffer(
            m_colorBuffer.data
        ) { ParticleColor() }
        val colorMixing256 = (256 * m_colorMixingStrength).toInt()
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            if (m_flagsBuffer.data!![a] and m_flagsBuffer.data!![b] and ParticleType.b2_colorMixingParticle != 0) {
                val colorA = m_colorBuffer.data!![a]
                val colorB = m_colorBuffer.data!![b]
                val dr = colorMixing256 * (colorB.r - colorA.r) shr 8
                val dg = colorMixing256 * (colorB.g - colorA.g) shr 8
                val db = colorMixing256 * (colorB.b - colorA.b) shr 8
                val da = colorMixing256 * (colorB.a - colorA.a) shr 8
                colorA.r = (colorA.r + dr).toByte()
                colorA.g = (colorA.g + dg).toByte()
                colorA.b = (colorA.b + db).toByte()
                colorA.a = (colorA.a + da).toByte()
                colorB.r = (colorB.r - dr).toByte()
                colorB.g = (colorB.g - dg).toByte()
                colorB.b = (colorB.b - db).toByte()
                colorB.a = (colorB.a - da).toByte()
            }
        }
    }

    private fun solveZombie() {
        // removes particles with zombie flag
        var newCount = 0
        val newIndices = IntArray(particleCount)
        for (i in 0 until particleCount) {
            val flags = m_flagsBuffer.data!![i]
            if (flags and ParticleType.b2_zombieParticle != 0) {
                val destructionListener = m_world.particleDestructionListener
                if (flags and ParticleType.b2_destructionListener != 0 && destructionListener != null) {
                    destructionListener.sayGoodbye(i)
                }
                newIndices[i] = Settings.invalidParticleIndex
            } else {
                newIndices[i] = newCount
                if (i != newCount) {
                    m_flagsBuffer.data!![newCount] = m_flagsBuffer.data!![i]
                    m_positionBuffer.data!![newCount].set(
                        m_positionBuffer.data!![i]
                    )
                    m_velocityBuffer.data!![newCount].set(
                        m_velocityBuffer.data!![i]
                    )
                    particleGroupBuffer!![newCount] = particleGroupBuffer!![i]
                    if (m_depthBuffer != null) {
                        m_depthBuffer!![newCount] = m_depthBuffer!![i]
                    }
                    if (m_colorBuffer.data != null) {
                        m_colorBuffer.data!![newCount].set(
                            m_colorBuffer.data!![i]
                        )
                    }
                    if (m_userDataBuffer.data != null) {
                        m_userDataBuffer.data!![newCount] =
                                m_userDataBuffer.data!![i]
                    }
                }
                newCount++
            }
        }

        // update proxies
        for (k in 0 until m_proxyBuffer.size) {
            val proxy = m_proxyBuffer[k]
            proxy.index = newIndices[proxy.index]
        }

        // Proxy lastProxy = std.remove_if(
        // m_proxyBuffer!!, m_proxyBuffer!! + m_proxyCount,
        // Test.IsProxyInvalid);
        // m_proxyCount = (int) (lastProxy - m_proxyBuffer!!);
        run {
            var i = 0
            while (i < m_proxyBuffer.size) {
                if (Test.IsProxyInvalid(m_proxyBuffer[i])) {
                    val temp = m_proxyBuffer[m_proxyBuffer.size - 1]
                    m_proxyBuffer[m_proxyBuffer.size - 1] = m_proxyBuffer[i]
                    m_proxyBuffer[i] = temp
                    m_proxyBuffer.pop()
                } else i++
            }
        }

        // update contacts
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            contact.indexA = newIndices[contact.indexA]
            contact.indexB = newIndices[contact.indexB]
        }
        // ParticleContact lastContact = std.remove_if(
        // m_contactBuffer, m_contactBuffer + m_contactCount,
        // Test.IsContactInvalid);
        // m_contactCount = (int) (lastContact - m_contactBuffer);
        var j = m_contactCount
        run {
            var i = 0
            while (i < j) {
                if (Test.IsContactInvalid(m_contactBuffer!![i])) {
                    --j
                    val temp = m_contactBuffer!![j]
                    m_contactBuffer!![j] = m_contactBuffer!![i]
                    m_contactBuffer!![i] = temp
                    --i
                }
                i++
            }
        }
        m_contactCount = j

        // update particle-body contacts
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            contact.index = newIndices[contact.index]
        }
        // ParticleBodyContact lastBodyContact = std.remove_if(
        // m_bodyContactBuffer, m_bodyContactBuffer + m_bodyContactCount,
        // Test.IsBodyContactInvalid);
        // m_bodyContactCount = (int) (lastBodyContact - m_bodyContactBuffer);
        j = m_bodyContactCount
        run {
            var i = 0
            while (i < j) {
                if (Test.IsBodyContactInvalid(m_bodyContactBuffer!![i])) {
                    --j
                    val temp = m_bodyContactBuffer!![j]
                    m_bodyContactBuffer!![j] = m_bodyContactBuffer!![i]
                    m_bodyContactBuffer!![i] = temp
                    --i
                }
                i++
            }
        }
        m_bodyContactCount = j

        // update pairs
        for (k in 0 until m_pairCount) {
            val pair = m_pairBuffer!![k]
            pair.indexA = newIndices[pair.indexA]
            pair.indexB = newIndices[pair.indexB]
        }
        // Pair lastPair = std.remove_if(m_pairBuffer, m_pairBuffer + m_pairCount, Test.IsPairInvalid);
        // m_pairCount = (int) (lastPair - m_pairBuffer);
        j = m_pairCount
        run {
            var i = 0
            while (i < j) {
                if (Test.IsPairInvalid(m_pairBuffer!![i])) {
                    --j
                    val temp = m_pairBuffer!![j]
                    m_pairBuffer!![j] = m_pairBuffer!![i]
                    m_pairBuffer!![i] = temp
                    --i
                }
                i++
            }
        }
        m_pairCount = j

        // update triads
        for (k in 0 until m_triadCount) {
            val triad = m_triadBuffer!![k]
            triad.indexA = newIndices[triad.indexA]
            triad.indexB = newIndices[triad.indexB]
            triad.indexC = newIndices[triad.indexC]
        }
        // Triad lastTriad =
        // std.remove_if(m_triadBuffer, m_triadBuffer + m_triadCount, Test.isTriadInvalid);
        // m_triadCount = (int) (lastTriad - m_triadBuffer);
        j = m_triadCount
        run {
            var i = 0
            while (i < j) {
                if (Test.IsTriadInvalid(m_triadBuffer!![i])) {
                    --j
                    val temp = m_triadBuffer!![j]
                    m_triadBuffer!![j] = m_triadBuffer!![i]
                    m_triadBuffer!![i] = temp
                    --i
                }
                i++
            }
        }
        m_triadCount = j

        // update groups
        run {
            var group = m_groupList
            while (group != null) {
                var firstIndex = newCount
                var lastIndex = 0
                var modified = false
                for (i in group!!.bufferIndex until group!!.m_lastIndex) {
                    j = newIndices[i]
                    if (j >= 0) {
                        firstIndex = min(firstIndex, j)
                        lastIndex = max(lastIndex, j + 1)
                    } else {
                        modified = true
                    }
                }
                if (firstIndex < lastIndex) {
                    group!!.bufferIndex = firstIndex
                    group!!.m_lastIndex = lastIndex
                    if (modified) {
                        if (group!!.groupFlags and ParticleGroupType.b2_rigidParticleGroup != 0) {
                            group!!.m_toBeSplit = true
                        }
                    }
                } else {
                    group!!.bufferIndex = 0
                    group!!.m_lastIndex = 0
                    if (group!!.m_destroyAutomatically) {
                        group!!.m_toBeDestroyed = true
                    }
                }
                group = group!!.next
            }
        }

        // update particle count
        particleCount = newCount
        // m_world.m_stackAllocator.Free(newIndices);

        // destroy bodies with no particles
        var group = m_groupList
        while (group != null) {
            val next = group!!.next
            if (group!!.m_toBeDestroyed) {
                destroyParticleGroup(group)
            } else if (group!!.m_toBeSplit) {
                // TODO: split the group
            }
            group = next
        }
    }

    private class NewIndices {
        internal var start: Int = 0
        internal var mid: Int = 0
        internal var end: Int = 0

        internal fun getIndex(i: Int): Int {
            return if (i < start) {
                i
            } else if (i < mid) {
                i + end - mid
            } else if (i < end) {
                i + start - mid
            } else {
                i
            }
        }
    }


    internal fun RotateBuffer(
        start: Int,
        mid: Int,
        end: Int
    ) {
        // move the particles assigned to the given group toward the end of array
        if (start == mid || mid == end) {
            return
        }
        newIndices.start = start
        newIndices.mid = mid
        newIndices.end = end

        BufferUtils.rotate(m_flagsBuffer.data!!, start, mid, end)
        BufferUtils.rotate(m_positionBuffer.data!!, start, mid, end)
        BufferUtils.rotate(m_velocityBuffer.data!!, start, mid, end)
        BufferUtils.rotate(particleGroupBuffer!!, start, mid, end)
        if (m_depthBuffer != null) {
            BufferUtils.rotate(m_depthBuffer!!, start, mid, end)
        }
        if (m_colorBuffer.data != null) {
            BufferUtils.rotate(m_colorBuffer.data!!, start, mid, end)
        }
        if (m_userDataBuffer.data != null) {
            BufferUtils.rotate(m_userDataBuffer.data!!, start, mid, end)
        }

        // update proxies
        for (k in 0 until m_proxyBuffer.size) {
            val proxy = m_proxyBuffer[k]
            proxy.index = newIndices.getIndex(proxy.index)
        }

        // update contacts
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            contact.indexA = newIndices.getIndex(contact.indexA)
            contact.indexB = newIndices.getIndex(contact.indexB)
        }

        // update particle-body contacts
        for (k in 0 until m_bodyContactCount) {
            val contact = m_bodyContactBuffer!![k]
            contact.index = newIndices.getIndex(contact.index)
        }

        // update pairs
        for (k in 0 until m_pairCount) {
            val pair = m_pairBuffer!![k]
            pair.indexA = newIndices.getIndex(pair.indexA)
            pair.indexB = newIndices.getIndex(pair.indexB)
        }

        // update triads
        for (k in 0 until m_triadCount) {
            val triad = m_triadBuffer!![k]
            triad.indexA = newIndices.getIndex(triad.indexA)
            triad.indexB = newIndices.getIndex(triad.indexB)
            triad.indexC = newIndices.getIndex(triad.indexC)
        }

        // update groups
        var group = m_groupList
        while (group != null) {
            group.bufferIndex = newIndices.getIndex(group.bufferIndex)
            group.m_lastIndex = newIndices.getIndex(group.m_lastIndex - 1) + 1
            group = group.next
        }
    }

    internal fun getCriticalVelocity(step: TimeStep): Double {
        return m_particleDiameter * step.inv_dt
    }

    internal fun getCriticalVelocitySquared(step: TimeStep): Double {
        val velocity = getCriticalVelocity(step)
        return velocity * velocity
    }

    internal fun getCriticalPressure(step: TimeStep): Double {
        return m_density * getCriticalVelocitySquared(step)
    }

    internal fun setParticleBuffer(
        buffer: ParticleBufferInt,
        newData: IntArray?,
        newCapacity: Int
    ) {
        assert { newData != null && newCapacity != 0 || newData == null && newCapacity == 0 }
        if (buffer.userSuppliedCapacity != 0) {
            // m_world.m_blockAllocator.Free(buffer.data, sizeof(T) * m_internalAllocatedCapacity);
        }
        buffer.data = newData
        buffer.userSuppliedCapacity = newCapacity
    }

    internal fun <T> setParticleBuffer(
        buffer: ParticleBuffer<T>,
        newData: Array<T>?,
        newCapacity: Int
    ) {
        assert { newData != null && newCapacity != 0 || newData == null && newCapacity == 0 }
        if (buffer.userSuppliedCapacity != 0) {
            // m_world.m_blockAllocator.Free(buffer.data, sizeof(T) * m_internalAllocatedCapacity);
        }
        buffer.data = newData
        buffer.userSuppliedCapacity = newCapacity
    }

    fun setParticleFlagsBuffer(
        buffer: IntArray,
        capacity: Int
    ) {
        setParticleBuffer(m_flagsBuffer, buffer, capacity)
    }

    fun setParticlePositionBuffer(
        buffer: Array<MutableVector2d>,
        capacity: Int
    ) {
        setParticleBuffer(m_positionBuffer, buffer, capacity)
    }

    fun setParticleVelocityBuffer(
        buffer: Array<MutableVector2d>,
        capacity: Int
    ) {
        setParticleBuffer(m_velocityBuffer, buffer, capacity)
    }

    fun setParticleColorBuffer(
        buffer: Array<ParticleColor>,
        capacity: Int
    ) {
        setParticleBuffer(m_colorBuffer, buffer, capacity)
    }

    fun getParticleGroupList(): Array<ParticleGroup?>? {
        return particleGroupBuffer
    }

    fun setParticleUserDataBuffer(
        buffer: Array<Any?>,
        capacity: Int
    ) {
        setParticleBuffer(m_userDataBuffer, buffer, capacity)
    }

    fun queryAABB(
        callback: ParticleQueryCallback,
        aabb: AABB2
    ) {
        if (m_proxyBuffer.size == 0) {
            return
        }

        val lowerBoundX = aabb.min.x
        val lowerBoundY = aabb.min.y
        val upperBoundX = aabb.max.x
        val upperBoundY = aabb.max.y
        val firstProxy = lowerBound(
            m_proxyBuffer,
            computeTag(
                m_inverseDiameter * lowerBoundX,
                m_inverseDiameter * lowerBoundY
            )
        )
        val lastProxy = upperBound(
            m_proxyBuffer,
            computeTag(
                m_inverseDiameter * upperBoundX,
                m_inverseDiameter * upperBoundY
            )
        )
        for (proxy in firstProxy until lastProxy) {
            val i = m_proxyBuffer[proxy].index
            val p = m_positionBuffer.data!![i]
            if (lowerBoundX < p.x && p.x < upperBoundX && lowerBoundY < p.y && p.y < upperBoundY) {
                if (!callback.reportParticle(i)) {
                    break
                }
            }
        }
    }

    /**
     * @param callback
     * @param point1
     * @param point2
     */
    fun raycast(
        callback: ParticleRaycastCallback,
        point1: Vector2d,
        point2: Vector2d
    ) {
        if (m_proxyBuffer.size == 0) {
            return
        }
        val firstProxy = lowerBound(
            m_proxyBuffer,
            computeTag(
                m_inverseDiameter * min(
                    point1.x,
                    point2.x
                ) - 1,
                m_inverseDiameter * min(
                    point1.y,
                    point2.y
                ) - 1
            )
        )
        val lastProxy = upperBound(
            m_proxyBuffer,
            computeTag(
                m_inverseDiameter * max(
                    point1.x,
                    point2.x
                ) + 1,
                m_inverseDiameter * max(
                    point1.y,
                    point2.y
                ) + 1
            )
        )
        var fraction = 1.0
        // solving the following equation:
        // ((1-t)*point1+t*point2-position)^2=diameter^2
        // where t is a potential fraction
        val vx = point2.x - point1.x
        val vy = point2.y - point1.y
        var v2 = vx * vx + vy * vy
        if (v2 == 0.0) v2 = Double.MAX_VALUE
        for (proxy in firstProxy until lastProxy) {
            val i = m_proxyBuffer[proxy].index
            val posI = m_positionBuffer.data!![i]
            val px = point1.x - posI.x
            val py = point1.y - posI.y
            val pv = px * vx + py * vy
            val p2 = px * px + py * py
            val determinant = pv * pv - v2 * (p2 - m_squaredDiameter)
            if (determinant >= 0) {
                val sqrtDeterminant = sqrt(determinant)
                // find a solution between 0 and fraction
                var t = (-pv - sqrtDeterminant) / v2
                if (t > fraction) {
                    continue
                }
                if (t < 0) {
                    t = (-pv + sqrtDeterminant) / v2
                    if (t < 0 || t > fraction) {
                        continue
                    }
                }
                val f = callback.reportParticle(
                    i,
                    Vector2d(px + t * vx, py + t * vy).normalizedSafe(),
                    Vector2d(point1.x + t * vx, point1.y + t * vy), t
                )
                fraction = min(fraction, f)
                if (fraction <= 0) {
                    break
                }
            }
        }
    }

    fun computeParticleCollisionEnergy(): Double {
        var sum_v2 = 0.0
        for (k in 0 until m_contactCount) {
            val contact = m_contactBuffer!![k]
            val a = contact.indexA
            val b = contact.indexB
            val n = contact.normal
            val va = m_velocityBuffer.data!![a]
            val vb = m_velocityBuffer.data!![b]
            val vx = vb.x - va.x
            val vy = vb.y - va.y
            val vn = vx * n.x + vy * n.y
            if (vn < 0) {
                sum_v2 += vn * vn
            }
        }
        return 0.5 * particleMass * sum_v2
    }

    internal inline fun <reified T> requestParticleBuffer(
        buffer: Array<T>?,
        supplier: (Int) -> T
    ): Array<T> {
        var buffer = buffer
        if (buffer == null) {
            buffer = Array(m_internalAllocatedCapacity) { supplier(it) }
        }
        return buffer
    }

    internal fun requestParticleBuffer(buffer: DoubleArray?): DoubleArray {
        var buffer = buffer
        if (buffer == null) {
            buffer = DoubleArray(m_internalAllocatedCapacity)
        }
        return buffer
    }

    class ParticleBuffer<T> {
        var data: Array<T>? = null
        internal var userSuppliedCapacity: Int = 0
    }

    internal class ParticleBufferInt {
        var data: IntArray? = null
        var userSuppliedCapacity: Int = 0
    }

    /** Used for detecting particle contacts  */
    class Proxy : Comparable<Proxy> {
        internal var index: Int = 0
        internal var tag: Int = 0

        override fun compareTo(o: Proxy): Int {
            return tag.compareTo(o.tag)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (other !is Proxy) return false
            return tag == other.tag
        }
    }

    /** Connection between two particles  */
    class Pair {
        internal var indexA: Int = 0
        internal var indexB: Int = 0
        internal var flags: Int = 0
        internal var strength: Double = 0.0
        internal var distance: Double = 0.0
    }

    /** Connection between three particles  */
    class Triad {
        internal var indexA: Int = 0
        internal var indexB: Int = 0
        internal var indexC: Int = 0
        internal var flags: Int = 0
        internal var strength: Double = 0.0
        internal val pa = MutableVector2d()
        internal val pb = MutableVector2d()
        internal val pc = MutableVector2d()
        internal var ka: Double = 0.0
        internal var kb: Double = 0.0
        internal var kc: Double = 0.0
        internal var s: Double = 0.0
    }

    // Callback used with VoronoiDiagram.
    internal class CreateParticleGroupCallback : VoronoiDiagramCallback {

        var system: ParticleSystem? = null
        var def: ParticleGroupDef? = null // pointer
        var firstIndex: Int = 0
        override fun callback(
            a: Int,
            b: Int,
            c: Int
        ) {
            val pa = system!!.m_positionBuffer.data!![a]
            val pb = system!!.m_positionBuffer.data!![b]
            val pc = system!!.m_positionBuffer.data!![c]
            val dabx = pa.x - pb.x
            val daby = pa.y - pb.y
            val dbcx = pb.x - pc.x
            val dbcy = pb.y - pc.y
            val dcax = pc.x - pa.x
            val dcay = pc.y - pa.y
            val maxDistanceSquared =
                Settings.maxTriadDistanceSquared * system!!.m_squaredDiameter
            if (dabx * dabx + daby * daby < maxDistanceSquared
                && dbcx * dbcx + dbcy * dbcy < maxDistanceSquared
                && dcax * dcax + dcay * dcay < maxDistanceSquared) {
                if (system!!.m_triadCount >= system!!.m_triadCapacity) {
                    val oldCapacity = system!!.m_triadCapacity
                    val newCapacity = if (system!!.m_triadCount != 0)
                        2 * system!!.m_triadCount
                    else
                        Settings.minParticleBufferCapacity
                    system!!.m_triadBuffer = BufferUtils.reallocateBuffer(
                        system!!.m_triadBuffer, oldCapacity,
                        newCapacity
                    ) { Triad() }
                    system!!.m_triadCapacity = newCapacity
                }
                val triad = system!!.m_triadBuffer!![system!!.m_triadCount]
                triad.indexA = a
                triad.indexB = b
                triad.indexC = c
                triad.flags =
                        (system!!.m_flagsBuffer.data!![a] or system!!.m_flagsBuffer.data!![b]
                                or system!!.m_flagsBuffer.data!![c])
                triad.strength = def!!.strength
                val midPointx = 1.toDouble() / 3 * (pa.x + pb.x + pc.x)
                val midPointy = 1.toDouble() / 3 * (pa.y + pb.y + pc.y)
                triad.pa.x = pa.x - midPointx
                triad.pa.y = pa.y - midPointy
                triad.pb.x = pb.x - midPointx
                triad.pb.y = pb.y - midPointy
                triad.pc.x = pc.x - midPointx
                triad.pc.y = pc.y - midPointy
                triad.ka = -(dcax * dabx + dcay * daby)
                triad.kb = -(dabx * dbcx + daby * dbcy)
                triad.kc = -(dbcx * dcax + dbcy * dcay)
                triad.s = (pa cross pb) + (pb cross pc) + (pc cross pa
                        )
                system!!.m_triadCount++
            }
        }
    }

    // Callback used with VoronoiDiagram.
    internal class JoinParticleGroupsCallback : VoronoiDiagramCallback {

        var system: ParticleSystem? = null
        var groupA: ParticleGroup? = null
        var groupB: ParticleGroup? = null
        override fun callback(
            a: Int,
            b: Int,
            c: Int
        ) {
            // Create a triad if it will contain particles from both groups.
            val countA =
                ((if (a < groupB!!.bufferIndex) 1 else 0) + (if (b < groupB!!.bufferIndex) 1 else 0)
                        + if (c < groupB!!.bufferIndex) 1 else 0)
            if (countA in 1..2) {
                val af = system!!.m_flagsBuffer.data!![a]
                val bf = system!!.m_flagsBuffer.data!![b]
                val cf = system!!.m_flagsBuffer.data!![c]
                if (af and bf and cf and k_triadFlags != 0) {
                    val pa = system!!.m_positionBuffer.data!![a]
                    val pb = system!!.m_positionBuffer.data!![b]
                    val pc = system!!.m_positionBuffer.data!![c]
                    val dabx = pa.x - pb.x
                    val daby = pa.y - pb.y
                    val dbcx = pb.x - pc.x
                    val dbcy = pb.y - pc.y
                    val dcax = pc.x - pa.x
                    val dcay = pc.y - pa.y
                    val maxDistanceSquared =
                        Settings.maxTriadDistanceSquared * system!!.m_squaredDiameter
                    if (dabx * dabx + daby * daby < maxDistanceSquared
                        && dbcx * dbcx + dbcy * dbcy < maxDistanceSquared
                        && dcax * dcax + dcay * dcay < maxDistanceSquared) {
                        if (system!!.m_triadCount >= system!!.m_triadCapacity) {
                            val oldCapacity = system!!.m_triadCapacity
                            val newCapacity = if (system!!.m_triadCount != 0)
                                2 * system!!.m_triadCount
                            else
                                Settings.minParticleBufferCapacity
                            system!!.m_triadBuffer =
                                    BufferUtils.reallocateBuffer(
                                        system!!.m_triadBuffer, oldCapacity,
                                        newCapacity
                                    ) { Triad() }
                            system!!.m_triadCapacity = newCapacity
                        }
                        val triad =
                            system!!.m_triadBuffer!![system!!.m_triadCount]
                        triad.indexA = a
                        triad.indexB = b
                        triad.indexC = c
                        triad.flags = af or bf or cf
                        triad.strength = min(
                            groupA!!.m_strength,
                            groupB!!.m_strength
                        )
                        val midPointx = 1.toDouble() / 3 * (pa.x + pb.x + pc.x)
                        val midPointy = 1.toDouble() / 3 * (pa.y + pb.y + pc.y)
                        triad.pa.x = pa.x - midPointx
                        triad.pa.y = pa.y - midPointy
                        triad.pb.x = pb.x - midPointx
                        triad.pb.y = pb.y - midPointy
                        triad.pc.x = pc.x - midPointx
                        triad.pc.y = pc.y - midPointy
                        triad.ka = -(dcax * dabx + dcay * daby)
                        triad.kb = -(dabx * dbcx + daby * dbcy)
                        triad.kc = -(dbcx * dcax + dbcy * dcay)
                        triad.s = (pa cross pb) + (pb cross pc
                                ) + (pc cross pa)
                        system!!.m_triadCount++
                    }
                }
            }
        }
    }

    internal class DestroyParticlesInShapeCallback : ParticleQueryCallback {
        lateinit var system: ParticleSystem
        lateinit var shape: Shape
        lateinit var xf: Transform
        var callDestructionListener: Boolean = false
        var destroyed: Int = 0

        fun init(
            system: ParticleSystem,
            shape: Shape,
            xf: Transform,
            callDestructionListener: Boolean
        ) {
            this.system = system
            this.shape = shape
            this.xf = xf
            this.destroyed = 0
            this.callDestructionListener = callDestructionListener
        }

        override fun reportParticle(index: Int): Boolean {
            assert { index >= 0 && index < system.particleCount }
            if (shape.testPoint(
                    xf, system.m_positionBuffer.data!![index].now()
                )) {
                system.destroyParticle(index, callDestructionListener)
                destroyed++
            }
            return true
        }
    } // TODO Auto-generated constructor stub

    internal class UpdateBodyContactsCallback : QueryCallback {
        var system: ParticleSystem? = null

        private val tempVec = MutableVector2d()

        override fun reportFixture(fixture: Fixture): Boolean {
            if (fixture.isSensor) {
                return true
            }
            val shape = fixture.shape
            val b = fixture.body
            val bp = b!!.worldCenter
            val bm = b.mass
            val bI = b.inertia - bm * b.localCenter.lengthSqr()
            val invBm = if (bm > 0) 1 / bm else 0.0
            val invBI = if (bI > 0) 1 / bI else 0.0
            val childCount = shape!!.childCount
            for (childIndex in 0 until childCount) {
                val aabb = fixture.getAABB(childIndex)
                val aabblowerBoundx =
                    aabb.min.x - system!!.m_particleDiameter
                val aabblowerBoundy =
                    aabb.min.y - system!!.m_particleDiameter
                val aabbupperBoundx =
                    aabb.max.x + system!!.m_particleDiameter
                val aabbupperBoundy =
                    aabb.max.y + system!!.m_particleDiameter
                val firstProxy = lowerBound(
                    system!!.m_proxyBuffer,
                    computeTag(
                        system!!.m_inverseDiameter * aabblowerBoundx,
                        system!!.m_inverseDiameter * aabblowerBoundy
                    )
                )
                val lastProxy = upperBound(
                    system!!.m_proxyBuffer,
                    computeTag(
                        system!!.m_inverseDiameter * aabbupperBoundx,
                        system!!.m_inverseDiameter * aabbupperBoundy
                    )
                )

                for (proxy in firstProxy until lastProxy) {
                    val a = system!!.m_proxyBuffer[proxy].index
                    val ap = system!!.m_positionBuffer.data!![a]
                    if (ap.x in aabblowerBoundx..aabbupperBoundx
                        && ap.y in aabblowerBoundy..aabbupperBoundy) {
                        val d: Double
                        val n = tempVec
                        d = fixture.computeDistance(ap.now(), childIndex, n)
                        if (d < system!!.m_particleDiameter) {
                            val invAm =
                                if (system!!.m_flagsBuffer.data!![a] and ParticleType.b2_wallParticle != 0)
                                    0.0
                                else
                                    system!!.particleInvMass
                            val rpx = ap.x - bp.x
                            val rpy = ap.y - bp.y
                            val rpn = rpx * n.y - rpy * n.x
                            if (system!!.m_bodyContactCount >= system!!.m_bodyContactCapacity) {
                                val oldCapacity = system!!.m_bodyContactCapacity
                                val newCapacity =
                                    if (system!!.m_bodyContactCount != 0)
                                        2 * system!!.m_bodyContactCount
                                    else
                                        Settings.minParticleBufferCapacity
                                system!!.m_bodyContactBuffer =
                                        BufferUtils.reallocateBuffer(
                                            system!!.m_bodyContactBuffer,
                                            oldCapacity,
                                            newCapacity
                                        ) { ParticleBodyContact() }
                                system!!.m_bodyContactCapacity = newCapacity
                            }
                            val contact =
                                system!!.m_bodyContactBuffer!![system!!.m_bodyContactCount]
                            contact.index = a
                            contact.body = b
                            contact.weight = 1 - d * system!!.m_inverseDiameter
                            contact.normal.x = -n.x
                            contact.normal.y = -n.y
                            contact.mass = 1 /
                                    (invAm + invBm + invBI * rpn * rpn)
                            system!!.m_bodyContactCount++
                        }
                    }
                }
            }
            return true
        }
    }

    internal class SolveCollisionCallback : QueryCallback {
        var system: ParticleSystem? = null
        var step: TimeStep? = null

        private val input = RaycastInput()
        private val output = RayCastOutput()
        private val tempVec = MutableVector2d()
        private val tempVec2 = MutableVector2d()

        override fun reportFixture(fixture: Fixture): Boolean {
            if (fixture.isSensor) {
                return true
            }
            val shape = fixture.shape
            val body = fixture.body!!
            val childCount = shape!!.childCount
            for (childIndex in 0 until childCount) {
                val aabb = fixture.getAABB(childIndex)
                val aabblowerBoundx =
                    aabb.min.x - system!!.m_particleDiameter
                val aabblowerBoundy =
                    aabb.min.y - system!!.m_particleDiameter
                val aabbupperBoundx =
                    aabb.max.x + system!!.m_particleDiameter
                val aabbupperBoundy =
                    aabb.max.y + system!!.m_particleDiameter
                val firstProxy = lowerBound(
                    system!!.m_proxyBuffer,
                    computeTag(
                        system!!.m_inverseDiameter * aabblowerBoundx,
                        system!!.m_inverseDiameter * aabblowerBoundy
                    )
                )
                val lastProxy = upperBound(
                    system!!.m_proxyBuffer,
                    computeTag(
                        system!!.m_inverseDiameter * aabbupperBoundx,
                        system!!.m_inverseDiameter * aabbupperBoundy
                    )
                )

                for (proxy in firstProxy until lastProxy) {
                    val a = system!!.m_proxyBuffer[proxy].index
                    val ap = system!!.m_positionBuffer.data!![a]
                    if (ap.x in aabblowerBoundx..aabbupperBoundx
                        && ap.y in aabblowerBoundy..aabbupperBoundy) {
                        val av = system!!.m_velocityBuffer.data!![a]
                        val temp = tempVec
                        temp.set(Transform.mulTrans(body.m_xf0, ap.now()))
                        Transform.mulToOut(body.transform, temp, input.p1)
                        input.p2.x = ap.x + step!!.dt * av.x
                        input.p2.y = ap.y + step!!.dt * av.y
                        input.maxFraction = 1.0
                        if (fixture.raycast(output, input, childIndex)) {
                            val p = tempVec
                            p.x =
                                    ((1 - output.fraction) * input.p1.x + output.fraction * input.p2.x
                                            + Settings.linearSlop * output.normal.x)
                            p.y =
                                    ((1 - output.fraction) * input.p1.y + output.fraction * input.p2.y
                                            + Settings.linearSlop * output.normal.y)

                            val vx = step!!.inv_dt * (p.x - ap.x)
                            val vy = step!!.inv_dt * (p.y - ap.y)
                            av.x = vx
                            av.y = vy
                            val particleMass = system!!.particleMass
                            val ax = particleMass * (av.x - vx)
                            val ay = particleMass * (av.y - vy)
                            val b = output.normal
                            val fdn = ax * b.x + ay * b.y
                            val f = tempVec2
                            f.x = fdn * b.x
                            f.y = fdn * b.y
                            body.applyLinearImpulse(f.now(), p.now(), true)
                        }
                    }
                }
            }
            return true
        }
    }

    internal object Test {
        fun IsProxyInvalid(proxy: Proxy): Boolean {
            return proxy.index < 0
        }

        fun IsContactInvalid(contact: ParticleContact): Boolean {
            return contact.indexA < 0 || contact.indexB < 0
        }

        fun IsBodyContactInvalid(contact: ParticleBodyContact): Boolean {
            return contact.index < 0
        }

        fun IsPairInvalid(pair: Pair): Boolean {
            return pair.indexA < 0 || pair.indexB < 0
        }

        fun IsTriadInvalid(triad: Triad): Boolean {
            return triad.indexA < 0 || triad.indexB < 0 || triad.indexC < 0
        }
    }

    companion object {
        /** All particle types that require creating pairs  */
        private const val k_pairFlags = ParticleType.b2_springParticle
        /** All particle types that require creating triads  */
        private const val k_triadFlags = ParticleType.b2_elasticParticle
        /** All particle types that require computing depth  */
        private const val k_noPressureFlags = ParticleType.b2_powderParticle

        internal const val xTruncBits = 12
        internal const val yTruncBits = 12
        internal const val tagBits = 8 * 4 - 1  /* sizeof(int) */
        internal const val yOffset = 1 shl yTruncBits - 1
        internal const val yShift = tagBits - yTruncBits
        internal const val xShift = tagBits - yTruncBits - xTruncBits
        internal val xScale = 1 shl xShift
        internal val xOffset = xScale * (1 shl xTruncBits - 1)
        internal const val xMask = (1 shl xTruncBits) - 1
        internal const val yMask = (1 shl yTruncBits) - 1

        internal fun computeTag(
            x: Double,
            y: Double
        ): Int {
            return ((y + yOffset).toInt() shl yShift) + ((xScale * x).toInt() + xOffset)
        }

        internal fun computeRelativeTag(
            tag: Int,
            x: Int,
            y: Int
        ): Int {
            return tag + (y shl yShift) + (x shl xShift)
        }

        internal fun limitCapacity(
            capacity: Int,
            maxCount: Int
        ): Int {
            return if (maxCount != 0 && capacity > maxCount) maxCount else capacity
        }

        private fun lowerBound(
            ray: Pool<Proxy>,
            tag: Int
        ): Int {
            var length = ray.size
            var left = 0
            var step: Int
            var curr: Int
            while (length > 0) {
                step = length / 2
                curr = left + step
                if (ray[curr].tag < tag) {
                    left = curr + 1
                    length -= step + 1
                } else {
                    length = step
                }
            }
            return left
        }

        private fun upperBound(
            ray: Pool<Proxy>,
            tag: Int
        ): Int {
            var length = ray.size
            var left = 0
            var step: Int
            var curr: Int
            while (length > 0) {
                step = length / 2
                curr = left + step
                if (ray[curr].tag <= tag) {
                    left = curr + 1
                    length -= step + 1
                } else {
                    length = step
                }
            }
            return left
        }

        // reallocate a buffer
        internal inline fun <reified T> reallocateBuffer(
            buffer: ParticleBuffer<T>,
            oldCapacity: Int,
            newCapacity: Int,
            init: (Int) -> T
        ): Array<T>? {
            assert { newCapacity > oldCapacity }
            if (buffer.userSuppliedCapacity != 0 && newCapacity > buffer.userSuppliedCapacity)
                throw IllegalStateException("User supplied capacity too low")
            return BufferUtils.reallocateBuffer(
                buffer.data,
                oldCapacity, newCapacity, init
            )
        }

        // reallocate a buffer
        internal inline fun <reified T> reallocateBufferDeferred(
            buffer: ParticleBuffer<T>,
            oldCapacity: Int,
            newCapacity: Int,
            init: (Int) -> T
        ): Array<T>? {
            assert { newCapacity > oldCapacity }
            if (buffer.userSuppliedCapacity != 0 && newCapacity > buffer.userSuppliedCapacity)
                throw IllegalStateException("User supplied capacity too low")
            return BufferUtils.reallocateBufferDeferred(
                buffer.data,
                oldCapacity, newCapacity, init
            )
        }

        internal fun reallocateBuffer(
            buffer: ParticleBufferInt,
            oldCapacity: Int,
            newCapacity: Int,
            deferred: Boolean
        ): IntArray? {
            assert { newCapacity > oldCapacity }
            return BufferUtils.reallocateBuffer(
                buffer.data,
                buffer.userSuppliedCapacity, oldCapacity,
                newCapacity, deferred
            )
        }
    }
}
