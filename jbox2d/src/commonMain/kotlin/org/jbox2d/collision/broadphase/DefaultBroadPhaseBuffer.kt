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
package org.jbox2d.collision.broadphase

import org.jbox2d.callbacks.PairCallback
import org.jbox2d.callbacks.TreeCallback
import org.jbox2d.callbacks.TreeRaycastCallback
import org.jbox2d.collision.RaycastInput
import org.tobi29.math.AABB2
import org.tobi29.math.vector.Vector2d
import org.tobi29.stdex.copy
import org.tobi29.utils.Pool

interface BroadPhaseStrategyBuffer {
    val tree: BroadPhaseStrategy
}

/**
 * The broad-phase is used for computing pairs and performing volume queries and ray casts. This
 * broad-phase does not persist pairs. Instead, this reports potentially new pairs. It is up to the
 * client to consume the new pairs and to track subsequent overlap.
 * @author Daniel Murphy
 */
class DefaultBroadPhaseBuffer(
    override val tree: BroadPhaseStrategy
) : TreeCallback, BroadPhase, BroadPhaseStrategyBuffer {
    override var proxyCount = 0
        private set

    private var moveBuffer = IntArray(16)
    private val moveCapacity get() = moveBuffer.size
    private var moveCount = 0
    private val pairBuffer = Pool { Pair() }
    private var queryProxyId = BroadPhase.NULL_PROXY

    override val treeHeight: Int get() = tree.height
    override val treeBalance: Int get() = tree.maxBalance
    override val treeQuality: Double get() = tree.areaRatio

    override fun createProxy(
        aabb: AABB2,
        userData: Any?
    ): Int {
        val proxyId = tree.createProxy(aabb, userData)
        proxyCount++
        bufferMove(proxyId)
        return proxyId
    }

    override fun destroyProxy(proxyId: Int) {
        unbufferMove(proxyId)
        proxyCount--
        tree.destroyProxy(proxyId)
    }

    override fun moveProxy(
        proxyId: Int,
        aabb: AABB2,
        displacement: Vector2d
    ) {
        val buffer = tree.moveProxy(proxyId, aabb, displacement)
        if (buffer) {
            bufferMove(proxyId)
        }
    }

    override fun touchProxy(proxyId: Int) {
        bufferMove(proxyId)
    }

    override fun getUserData(proxyId: Int): Any? {
        return tree.getUserData(proxyId)
    }

    override fun getFatAABB(proxyId: Int): AABB2 {
        return tree.getFatAABB(proxyId)
    }

    override fun testOverlap(
        proxyIdA: Int,
        proxyIdB: Int
    ): Boolean {
        // return AABB.testOverlap(proxyA.aabb, proxyB.aabb);
        // return m_tree.overlap(proxyIdA, proxyIdB);
        val a = tree.getFatAABB(proxyIdA)
        val b = tree.getFatAABB(proxyIdB)
        if (b.min.x - a.max.x > 0.0 || b.min.y - a.max.y > 0.0) {
            return false
        }

        return !(a.min.x - b.max.x > 0.0 || a.min.y - b.max.y > 0.0)
    }

    override fun updatePairs(callback: PairCallback) {
        // Reset pair buffer
        pairBuffer.reset()

        // Perform tree queries for all moving proxies.
        for (i in 0 until moveCount) {
            queryProxyId = moveBuffer[i]
            if (queryProxyId == BroadPhase.NULL_PROXY) {
                continue
            }

            // We have to query the tree with the fat AABB so that
            // we don't fail to create a pair that may touch later.
            val fatAABB = tree.getFatAABB(queryProxyId)

            // Query tree, create pairs and add them pair buffer.
            // log.debug("quering aabb: "+m_queryProxy.aabb);
            tree.query(this, fatAABB)
        }
        // log.debug("Number of pairs found: "+m_pairCount);

        // Reset move buffer
        moveCount = 0

        // Sort the pair buffer to expose duplicates.
        pairBuffer.sort()

        // Send the pairs back to the client.
        var i = 0
        while (i < pairBuffer.size) {
            val primaryPair = pairBuffer[i]
            val userDataA = tree.getUserData(primaryPair.proxyIdA)
            val userDataB = tree.getUserData(primaryPair.proxyIdB)

            // log.debug("returning pair: "+userDataA+", "+userDataB);
            callback.addPair(userDataA, userDataB)
            i++

            // Skip any duplicate pairs.
            while (i < pairBuffer.size) {
                val pair = pairBuffer[i]
                if (pair.proxyIdA != primaryPair.proxyIdA || pair.proxyIdB != primaryPair.proxyIdB) {
                    break
                }
                i++
            }
        }
    }

    override fun query(
        callback: TreeCallback,
        aabb: AABB2
    ) {
        tree.query(callback, aabb)
    }

    override fun raycast(
        callback: TreeRaycastCallback,
        input: RaycastInput
    ) {
        tree.raycast(callback, input)
    }

    private fun bufferMove(proxyId: Int) {
        if (moveCount == moveCapacity) {
            val old = moveBuffer
            moveBuffer = IntArray(moveCapacity * 2)
            copy(old, moveBuffer)
        }

        moveBuffer[moveCount] = proxyId
        moveCount++
    }

    private fun unbufferMove(proxyId: Int) {
        for (i in 0 until moveCount) {
            if (moveBuffer[i] == proxyId) {
                moveBuffer[i] = BroadPhase.NULL_PROXY
            }
        }
    }

    /**
     * This is called from DynamicTree::query when we are gathering pairs.
     */
    override fun treeCallback(proxyId: Int): Boolean {
        // A proxy cannot form a pair with itself.
        if (proxyId == queryProxyId) {
            return true
        }

        val pair = pairBuffer.push()
        if (proxyId < queryProxyId) {
            pair.proxyIdA = proxyId
            pair.proxyIdB = queryProxyId
        } else {
            pair.proxyIdA = queryProxyId
            pair.proxyIdB = proxyId
        }

        return true
    }
}
