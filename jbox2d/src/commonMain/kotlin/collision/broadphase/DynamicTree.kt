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

import org.jbox2d.callbacks.TreeCallback
import org.jbox2d.callbacks.TreeRaycastCallback
import org.jbox2d.collision.RaycastInput
import org.jbox2d.collision.combine
import org.jbox2d.collision.isValid
import org.jbox2d.collision.perimeter
import org.jbox2d.common.Settings
import org.tobi29.math.AABB2
import org.tobi29.math.overlaps
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.normalizeSafe
import org.tobi29.stdex.assert
import org.tobi29.stdex.copy
import kotlin.math.abs
import kotlin.math.max

/**
 * A dynamic tree arranges data in a binary tree to accelerate queries such as volume queries and
 * ray casts. Leafs are proxies with an AABB. In the tree we expand the proxy AABB by _fatAABBFactor
 * so that the proxy AABB is bigger than the client object. This allows the client object to move by
 * small amounts without triggering a tree update.
 * @author daniel
 */
class DynamicTree : BroadPhaseStrategy {
    var root: DynamicTreeNode? = null
        private set
    private var nodes = emptyArray<DynamicTreeNode>()
    private var nodeCount = 0
    private var nodeCapacity = 0

    private var freeList = 0

    private var nodeStack = arrayOfNulls<DynamicTreeNode>(20)
    private var nodeStackIndex = 0

    private val r = MutableVector2d()
    private val aabb = AABB2()
    private val subInput = RaycastInput()

    override val height: Int get() = root?.height ?: 0

    override val maxBalance: Int
        get() {
            var maxBalance = 0
            for (i in 0 until nodeCapacity) {
                val node = nodes[i]
                if (node.height <= 1) {
                    continue
                }

                assert { node.child1 != null }

                val child1 = node.child1!!
                val child2 = node.child2!!
                val balance = abs(child2.height - child1.height)
                maxBalance = max(maxBalance, balance)
            }

            return maxBalance
        }

    override // Free node in pool
    val areaRatio: Double
        get() {
            val root = root ?: return 0.0

            val rootArea = root.aabb.perimeter

            var totalArea = 0.0
            for (i in 0 until nodeCapacity) {
                val node = nodes[i]
                if (node.height < 0) {
                    continue
                }

                totalArea += node.aabb.perimeter
            }

            return totalArea / rootArea
        }

    private val combinedAABB = AABB2()

    init {
        root = null
        nodeCount = 0
        nodeCapacity = 16
        nodes = Array(16) { DynamicTreeNode(it) }

        // Build a linked list for the free list.
        for (i in nodeCapacity - 1 downTo 0) {
            nodes[i].parent =
                    if (i == nodeCapacity - 1) null else nodes[i + 1]
            nodes[i].height = -1
        }
        freeList = 0
    }

    override fun createProxy(
        aabb: AABB2,
        userData: Any?
    ): Int {
        assert { aabb.isValid }
        val node = allocateNode()
        val proxyId = node.id
        // Fatten the aabb
        val nodeAABB = node.aabb
        nodeAABB.min.x = aabb.min.x - Settings.aabbExtension
        nodeAABB.min.y = aabb.min.y - Settings.aabbExtension
        nodeAABB.max.x = aabb.max.x + Settings.aabbExtension
        nodeAABB.max.y = aabb.max.y + Settings.aabbExtension
        node.userData = userData

        insertLeaf(proxyId)

        return proxyId
    }

    override fun destroyProxy(proxyId: Int) {
        assert { proxyId in 0 until nodeCapacity }
        val node = nodes[proxyId]
        assert { node.child1 == null }

        removeLeaf(node)
        freeNode(node)
    }

    override fun moveProxy(
        proxyId: Int,
        aabb: AABB2,
        displacement: Vector2d
    ): Boolean {
        assert { aabb.isValid }
        assert { proxyId in 0 until nodeCapacity }
        val node = nodes[proxyId]
        assert { node.child1 == null }

        val nodeAABB = node.aabb
        // if (nodeAABB.contains(aabb)) {
        if (nodeAABB.min.x <= aabb.min.x && nodeAABB.min.y <= aabb.min.y
            && aabb.max.x <= nodeAABB.max.x && aabb.max.y <= nodeAABB.max.y) {
            return false
        }

        removeLeaf(node)

        // Extend AABB
        val lowerBound = nodeAABB.min
        val upperBound = nodeAABB.max
        lowerBound.x = aabb.min.x - Settings.aabbExtension
        lowerBound.y = aabb.min.y - Settings.aabbExtension
        upperBound.x = aabb.max.x + Settings.aabbExtension
        upperBound.y = aabb.max.y + Settings.aabbExtension

        // Predict AABB displacement.
        val dx = displacement.x * Settings.aabbMultiplier
        val dy = displacement.y * Settings.aabbMultiplier
        if (dx < 0.0) {
            lowerBound.x += dx
        } else {
            upperBound.x += dx
        }

        if (dy < 0.0) {
            lowerBound.y += dy
        } else {
            upperBound.y += dy
        }

        insertLeaf(proxyId)
        return true
    }

    override fun getUserData(proxyId: Int): Any? {
        assert { proxyId in 0 until nodeCapacity }
        return nodes[proxyId].userData
    }

    override fun getFatAABB(proxyId: Int): AABB2 {
        assert { proxyId in 0 until nodeCapacity }
        return nodes[proxyId].aabb
    }

    override fun query(
        callback: TreeCallback,
        aabb: AABB2
    ) {
        assert { aabb.isValid }
        nodeStackIndex = 0
        nodeStack[nodeStackIndex++] = root

        while (nodeStackIndex > 0) {
            val node = nodeStack[--nodeStackIndex] ?: continue

            if (node.aabb overlaps aabb) {
                if (node.child1 == null) {
                    val proceed = callback.treeCallback(node.id)
                    if (!proceed) {
                        return
                    }
                } else {
                    if (nodeStack.size - nodeStackIndex - 2 <= 0) {
                        val newBuffer = arrayOfNulls<DynamicTreeNode>(
                            nodeStack.size * 2
                        )
                        copy(nodeStack, newBuffer)
                        nodeStack = newBuffer
                    }
                    nodeStack[nodeStackIndex++] = node.child1
                    nodeStack[nodeStackIndex++] = node.child2
                }
            }
        }
    }

    override fun raycast(
        callback: TreeRaycastCallback,
        input: RaycastInput
    ) {
        val p1 = input.p1
        val p2 = input.p2
        val p1x = p1.x
        val p2x = p2.x
        val p1y = p1.y
        val p2y = p2.y
        val vx: Double
        val vy: Double
        val rx: Double = r.x
        val ry: Double = r.y
        val absVx: Double
        val absVy: Double
        var cx: Double
        var cy: Double
        var hx: Double
        var hy: Double
        var tempx: Double
        var tempy: Double
        r.x = p2x - p1x
        r.y = p2y - p1y
        assert { r.x * r.x + r.y * r.y > 0.0 }
        r.normalizeSafe()

        // v is perpendicular to the segment.
        vx = -1.0 * ry
        vy = 1.0 * rx
        absVx = abs(vx)
        absVy = abs(vy)

        // Separating axis for segment (Gino, p80).
        // |dot(v, p1 - c)| > dot(|v|, h)

        var maxFraction = input.maxFraction

        // Build a bounding box for the segment.
        val segAABB = aabb
        // Vec2 t = p1 + maxFraction * (p2 - p1);
        // before inline
        // temp.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
        // Vec2.minToOut(p1, temp, segAABB.lowerBound);
        // Vec2.maxToOut(p1, temp, segAABB.upperBound);
        tempx = (p2x - p1x) * maxFraction + p1x
        tempy = (p2y - p1y) * maxFraction + p1y
        segAABB.min.x = if (p1x < tempx) p1x else tempx
        segAABB.min.y = if (p1y < tempy) p1y else tempy
        segAABB.max.x = if (p1x > tempx) p1x else tempx
        segAABB.max.y = if (p1y > tempy) p1y else tempy
        // end inline

        nodeStackIndex = 0
        nodeStack[nodeStackIndex++] = root
        while (nodeStackIndex > 0) {
            val node = nodeStack[--nodeStackIndex] ?: continue

            val nodeAABB = node.aabb
            if (!(nodeAABB overlaps segAABB)) {
                continue
            }

            // Separating axis for segment (Gino, p80).
            // |dot(v, p1 - c)| > dot(|v|, h)
            // node.aabb.getCenterToOut(c);
            // node.aabb.getExtentsToOut(h);
            cx = (nodeAABB.min.x + nodeAABB.max.x) * .5
            cy = (nodeAABB.min.y + nodeAABB.max.y) * .5
            hx = (nodeAABB.max.x - nodeAABB.min.x) * .5
            hy = (nodeAABB.max.y - nodeAABB.min.y) * .5
            tempx = p1x - cx
            tempy = p1y - cy
            val separation = abs(
                vx * tempx + vy * tempy
            ) - (absVx * hx + absVy * hy)
            if (separation > 0.0) {
                continue
            }

            if (node.child1 == null) {
                subInput.p1.x = p1x
                subInput.p1.y = p1y
                subInput.p2.x = p2x
                subInput.p2.y = p2y
                subInput.maxFraction = maxFraction

                val value = callback.raycastCallback(subInput, node.id)

                if (value == 0.0) {
                    // The client has terminated the ray cast.
                    return
                }

                if (value > 0.0) {
                    // Update segment bounding box.
                    maxFraction = value
                    // temp.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
                    // Vec2.minToOut(p1, temp, segAABB.lowerBound);
                    // Vec2.maxToOut(p1, temp, segAABB.upperBound);
                    tempx = (p2x - p1x) * maxFraction + p1x
                    tempy = (p2y - p1y) * maxFraction + p1y
                    segAABB.min.x = if (p1x < tempx) p1x else tempx
                    segAABB.min.y = if (p1y < tempy) p1y else tempy
                    segAABB.max.x = if (p1x > tempx) p1x else tempx
                    segAABB.max.y = if (p1y > tempy) p1y else tempy
                }
            } else {
                if (nodeStack.size - nodeStackIndex - 2 <= 0) {
                    val newBuffer = arrayOfNulls<DynamicTreeNode>(
                        nodeStack.size * 2
                    )
                    copy(nodeStack, newBuffer)
                    nodeStack = newBuffer
                }
                nodeStack[nodeStackIndex++] = node.child1
                nodeStack[nodeStackIndex++] = node.child2
            }
        }
    }

    override fun computeHeight(): Int {
        return computeHeight(root!!)
    }

    private fun computeHeight(node: DynamicTreeNode): Int {
        assert { node.id in 0 until nodeCapacity }

        if (node.child1 == null) {
            return 0
        }
        val height1 = computeHeight(node.child1!!)
        val height2 = computeHeight(node.child2!!)
        return 1 + max(height1, height2)
    }

    /**
     * Validate this tree. For testing.
     */
    fun validate() {
        validateStructure(root)
        validateMetrics(root)

        var freeCount = 0
        var freeNode: DynamicTreeNode? =
            if (freeList != NULL_NODE) nodes[freeList] else null
        while (freeNode != null) {
            assert { freeNode!!.id in 0 until nodeCapacity }
            assert { freeNode === nodes[freeNode!!.id] }
            freeNode = freeNode.parent
            ++freeCount
        }

        assert { height == computeHeight() }

        assert { nodeCount + freeCount == nodeCapacity }
    }

    /**
     * Build an optimal tree. Very expensive. For testing.
     */
    fun rebuildBottomUp() {
        val nodes = IntArray(nodeCount)
        var count = 0

        // Build array of leaves. Free the rest.
        for (i in 0 until nodeCapacity) {
            if (this.nodes[i].height < 0) {
                // free node in pool
                continue
            }

            val node = this.nodes[i]
            if (node.child1 == null) {
                node.parent = null
                nodes[count] = i
                ++count
            } else {
                freeNode(node)
            }
        }

        val b = AABB2()
        while (count > 1) {
            var minCost = Double.MAX_VALUE
            var iMin = -1
            var jMin = -1
            for (i in 0 until count) {
                val aabbi = this.nodes[nodes[i]].aabb

                for (j in i + 1 until count) {
                    val aabbj = this.nodes[nodes[j]].aabb
                    b.combine(aabbi, aabbj)
                    val cost = b.perimeter
                    if (cost < minCost) {
                        iMin = i
                        jMin = j
                        minCost = cost
                    }
                }
            }

            val index1 = nodes[iMin]
            val index2 = nodes[jMin]
            val child1 = this.nodes[index1]
            val child2 = this.nodes[index2]

            val parent = allocateNode()
            parent.child1 = child1
            parent.child2 = child2
            parent.height = 1 + max(child1.height, child2.height)
            parent.aabb.combine(child1.aabb, child2.aabb)
            parent.parent = null

            child1.parent = parent
            child2.parent = parent

            nodes[jMin] = nodes[count - 1]
            nodes[iMin] = parent.id
            --count
        }

        root = this.nodes[nodes[0]]

        validate()
    }

    private fun allocateNode(): DynamicTreeNode {
        if (freeList == NULL_NODE) {
            assert { nodeCount == nodeCapacity }

            val old = nodes
            nodeCapacity *= 2
            nodes = Array(nodeCapacity) {
                if (it < old.size) old[it]
                else DynamicTreeNode(it)
            }

            // Build a linked list for the free list.
            for (i in nodeCapacity - 1 downTo nodeCount) {
                nodes[i].parent =
                        if (i == nodeCapacity - 1) null else nodes[i + 1]
                nodes[i].height = -1
            }
            freeList = nodeCount
        }
        val nodeId = freeList
        val treeNode = nodes[nodeId]
        freeList =
                if (treeNode.parent != null) treeNode.parent!!.id else NULL_NODE

        treeNode.parent = null
        treeNode.child1 = null
        treeNode.child2 = null
        treeNode.height = 0
        treeNode.userData = null
        ++nodeCount
        return treeNode
    }

    /**
     * returns a node to the pool
     */
    private fun freeNode(node: DynamicTreeNode) {
        assert { 0 < nodeCount }
        node.parent =
                if (freeList != NULL_NODE) nodes[freeList] else null
        node.height = -1
        freeList = node.id
        nodeCount--
    }

    private fun insertLeaf(leaf_index: Int) {
        val leaf = nodes[leaf_index]
        if (root == null) {
            root = leaf
            leaf.parent = null
            return
        }

        // find the best sibling
        val leafAABB = leaf.aabb
        var index = root!!
        while (index.child1 != null) {
            val node = index
            val child1 = node.child1!!
            val child2 = node.child2!!

            val area = node.aabb.perimeter

            combinedAABB.combine(node.aabb, leafAABB)
            val combinedArea = combinedAABB.perimeter

            // Cost of creating a new parent for this node and the new leaf
            val cost = 2.0 * combinedArea

            // Minimum cost of pushing the leaf further down the tree
            val inheritanceCost = 2.0 * (combinedArea - area)

            // Cost of descending into child1
            val cost1: Double
            cost1 = if (child1.child1 == null) {
                combinedAABB.combine(leafAABB, child1.aabb)
                combinedAABB.perimeter + inheritanceCost
            } else {
                combinedAABB.combine(leafAABB, child1.aabb)
                val oldArea = child1.aabb.perimeter
                val newArea = combinedAABB.perimeter
                newArea - oldArea + inheritanceCost
            }

            // Cost of descending into child2
            val cost2: Double
            cost2 = if (child2.child1 == null) {
                combinedAABB.combine(leafAABB, child2.aabb)
                combinedAABB.perimeter + inheritanceCost
            } else {
                combinedAABB.combine(leafAABB, child2.aabb)
                val oldArea = child2.aabb.perimeter
                val newArea = combinedAABB.perimeter
                newArea - oldArea + inheritanceCost
            }

            // Descend according to the minimum cost.
            if (cost < cost1 && cost < cost2) {
                break
            }

            // Descend
            index = if (cost1 < cost2) child1 else child2
        }

        val sibling = index
        val oldParent = nodes[sibling.id].parent
        val newParent = allocateNode()
        newParent.parent = oldParent
        newParent.userData = null
        newParent.aabb.combine(leafAABB, sibling.aabb)
        newParent.height = sibling.height + 1

        if (oldParent != null) {
            // The sibling was not the root.
            if (oldParent.child1 === sibling) {
                oldParent.child1 = newParent
            } else {
                oldParent.child2 = newParent
            }

            newParent.child1 = sibling
            newParent.child2 = leaf
            sibling.parent = newParent
            leaf.parent = newParent
        } else {
            // The sibling was the root.
            newParent.child1 = sibling
            newParent.child2 = leaf
            sibling.parent = newParent
            leaf.parent = newParent
            root = newParent
        }

        // Walk back up the tree fixing heights and AABBs
        var index2 = leaf.parent
        while (index2 != null) {
            index2 = balance(index2)

            val child1 = index2.child1!!
            val child2 = index2.child2!!

            index2.height = 1 + max(child1.height, child2.height)
            index2.aabb.combine(child1.aabb, child2.aabb)

            index2 = index2.parent
        }
        // validate();
    }

    private fun removeLeaf(leaf: DynamicTreeNode) {
        if (leaf === root) {
            root = null
            return
        }

        val parent = leaf.parent!!
        val grandParent = parent.parent
        val sibling =
            if (parent.child1 === leaf) parent.child2!! else parent.child1!!

        if (grandParent != null) {
            // Destroy parent and connect sibling to grandParent.
            if (grandParent.child1 === parent) {
                grandParent.child1 = sibling
            } else {
                grandParent.child2 = sibling
            }
            sibling.parent = grandParent
            freeNode(parent)

            // Adjust ancestor bounds.
            var index = grandParent
            while (index != null) {
                index = balance(index)

                val child1 = index.child1!!
                val child2 = index.child2!!

                index.aabb.combine(child1.aabb, child2.aabb)
                index.height = 1 + max(child1.height, child2.height)

                index = index.parent
            }
        } else {
            root = sibling
            sibling.parent = null
            freeNode(parent)
        }

        // validate();
    }

    // Perform a left or right rotation if node A is imbalanced.
    // Returns the new root index.
    private fun balance(iA: DynamicTreeNode): DynamicTreeNode {
        if (iA.child1 == null || iA.height < 2) {
            return iA
        }

        val iB = iA.child1!!
        val iC = iA.child2!!
        assert { iB.id in 0 until nodeCapacity }
        assert { iC.id in 0 until nodeCapacity }

        val balance = iC.height - iB.height

        // Rotate C up
        if (balance > 1) {
            val iF = iC.child1!!
            val iG = iC.child2!!
            assert { iF.id in 0 until nodeCapacity }
            assert { iG.id in 0 until nodeCapacity }

            // Swap A and C
            iC.child1 = iA
            iC.parent = iA.parent
            iA.parent = iC

            // A's old parent should point to C
            if (iC.parent != null) {
                if (iC.parent!!.child1 === iA) {
                    iC.parent!!.child1 = iC
                } else {
                    assert { iC.parent!!.child2 === iA }
                    iC.parent!!.child2 = iC
                }
            } else {
                root = iC
            }

            // Rotate
            if (iF.height > iG.height) {
                iC.child2 = iF
                iA.child2 = iG
                iG.parent = iA
                iA.aabb.combine(iB.aabb, iG.aabb)
                iC.aabb.combine(iA.aabb, iF.aabb)

                iA.height = 1 + max(iB.height, iG.height)
                iC.height = 1 + max(iA.height, iF.height)
            } else {
                iC.child2 = iG
                iA.child2 = iF
                iF.parent = iA
                iA.aabb.combine(iB.aabb, iF.aabb)
                iC.aabb.combine(iA.aabb, iG.aabb)

                iA.height = 1 + max(iB.height, iF.height)
                iC.height = 1 + max(iA.height, iG.height)
            }

            return iC
        }

        // Rotate B up
        if (balance < -1) {
            val iD = iB.child1!!
            val iE = iB.child2!!
            assert { iD.id in 0 until nodeCapacity }
            assert { iE.id in 0 until nodeCapacity }

            // Swap A and B
            iB.child1 = iA
            iB.parent = iA.parent
            iA.parent = iB

            // A's old parent should point to B
            if (iB.parent != null) {
                if (iB.parent!!.child1 === iA) {
                    iB.parent!!.child1 = iB
                } else {
                    assert { iB.parent!!.child2 === iA }
                    iB.parent!!.child2 = iB
                }
            } else {
                root = iB
            }

            // Rotate
            if (iD.height > iE.height) {
                iB.child2 = iD
                iA.child1 = iE
                iE.parent = iA
                iA.aabb.combine(iC.aabb, iE.aabb)
                iB.aabb.combine(iA.aabb, iD.aabb)

                iA.height = 1 + max(iC.height, iE.height)
                iB.height = 1 + max(iA.height, iD.height)
            } else {
                iB.child2 = iE
                iA.child1 = iD
                iD.parent = iA
                iA.aabb.combine(iC.aabb, iD.aabb)
                iB.aabb.combine(iA.aabb, iE.aabb)

                iA.height = 1 + max(iC.height, iD.height)
                iB.height = 1 + max(iA.height, iE.height)
            }

            return iB
        }

        return iA
    }

    private fun validateStructure(node: DynamicTreeNode?) {
        if (node == null) {
            return
        }
        assert { node === nodes[node.id] }

        if (node === root) {
            assert { node.parent == null }
        }

        val child1 = node.child1
        val child2 = node.child2

        if (child1 == null) {
            assert { child2 == null }
            assert { node.height == 0 }
            return
        }

        child2!!

        assert { child1.id in 0 until nodeCapacity }
        assert { child2.id in 0 until nodeCapacity }

        assert { child1.parent === node }
        assert { child2.parent === node }

        validateStructure(child1)
        validateStructure(child2)
    }

    private fun validateMetrics(node: DynamicTreeNode?) {
        if (node == null) {
            return
        }

        val child1 = node.child1
        val child2 = node.child2

        if (child1 == null) {
            assert { child2 == null }
            assert { node.height == 0 }
            return
        }

        child2!!

        assert { child1.id in 0 until nodeCapacity }
        assert { child2.id in 0 until nodeCapacity }

        val height1 = child1.height
        val height2 = child2.height
        val height: Int
        height = 1 + max(height1, height2)
        assert { node.height == height }

        val aabb = AABB2()
        aabb.combine(child1.aabb, child2.aabb)

        assert { aabb.min == node.aabb.min }
        assert { aabb.max == node.aabb.max }

        validateMetrics(child1)
        validateMetrics(child2)
    }
}

const val NULL_NODE = -1
