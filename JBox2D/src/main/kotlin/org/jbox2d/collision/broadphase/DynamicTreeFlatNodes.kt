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
import org.jbox2d.collision.perimeter
import org.jbox2d.common.BufferUtils
import org.jbox2d.common.Settings
import org.tobi29.math.AABB2
import org.tobi29.math.overlaps
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.normalizeSafe
import org.tobi29.stdex.assert
import kotlin.math.abs
import kotlin.math.max

class DynamicTreeFlatNodes : BroadPhaseStrategy {
    var root = 0
        private set
    var aabbs = emptyArray<AABB2>()
        private set
    private var userData = emptyArray<Any?>()
    private var parent = IntArray(0)
    var child1 = IntArray(0)
        private set
    var child2 = IntArray(0)
        private set
    private var heights = IntArray(0)

    private var nodeCount = 0
    private var nodeCapacity = 0

    private var freeList = 0

    private var nodeStack = IntArray(20)
    private var nodeStackIndex = 0

    private val r = MutableVector2d()
    private val aabb = AABB2()
    private val subInput = RaycastInput()

    override val height: Int
        get() = if (root == NULL_NODE) {
            0
        } else heights[root]

    override val maxBalance: Int
        get() {
            var maxBalance = 0
            for (i in 0 until nodeCapacity) {
                if (heights[i] <= 1) {
                    continue
                }

                assert { child1[i] != NULL_NODE }

                val child1 = child1[i]
                val child2 = child2[i]
                val balance = abs(heights[child2] - heights[child1])
                maxBalance = max(maxBalance, balance)
            }

            return maxBalance
        }

    override // Free node in pool
    val areaRatio: Double
        get() {
            if (root == NULL_NODE) {
                return 0.0
            }

            val root = root
            val rootArea = aabbs[root].perimeter

            var totalArea = 0.0
            for (i in 0 until nodeCapacity) {
                if (heights[i] < 0) {
                    continue
                }

                totalArea += aabbs[i].perimeter
            }

            return totalArea / rootArea
        }

    private val combinedAABB = AABB2()

    init {
        root = NULL_NODE
        nodeCount = 0
        nodeCapacity = 16
        expandBuffers(0, nodeCapacity)
    }

    private fun expandBuffers(
        oldSize: Int,
        newSize: Int
    ) {
        aabbs = BufferUtils.reallocateBuffer(
            aabbs, oldSize,
            newSize
        ) { AABB2() }
        userData = BufferUtils.reallocateBuffer(
            userData, oldSize,
            newSize
        ) { null }
        parent = BufferUtils.reallocateBuffer(parent, oldSize, newSize)
        child1 = BufferUtils.reallocateBuffer(child1, oldSize, newSize)
        child2 = BufferUtils.reallocateBuffer(child2, oldSize, newSize)
        heights = BufferUtils.reallocateBuffer(heights, oldSize, newSize)

        // Build a linked list for the free list.
        for (i in oldSize until newSize) {
            aabbs[i] = AABB2()
            parent[i] = if (i == newSize - 1) NULL_NODE else i + 1
            heights[i] = -1
            child1[i] = -1
            child2[i] = -1
        }
        freeList = oldSize
    }

    override fun createProxy(
        aabb: AABB2,
        userData: Any?
    ): Int {
        val node = allocateNode()
        // Fatten the aabb
        val nodeAABB = this.aabbs[node]
        nodeAABB.min.x = aabb.min.x - Settings.aabbExtension
        nodeAABB.min.y = aabb.min.y - Settings.aabbExtension
        nodeAABB.max.x = aabb.max.x + Settings.aabbExtension
        nodeAABB.max.y = aabb.max.y + Settings.aabbExtension
        this.userData[node] = userData

        insertLeaf(node)

        return node
    }

    override fun destroyProxy(proxyId: Int) {
        assert { proxyId in 0 until nodeCapacity }
        assert { child1[proxyId] == NULL_NODE }

        removeLeaf(proxyId)
        freeNode(proxyId)
    }

    override fun moveProxy(
        proxyId: Int,
        aabb: AABB2,
        displacement: Vector2d
    ): Boolean {
        assert { proxyId in 0 until nodeCapacity }
        assert { child1[proxyId] == NULL_NODE }

        val nodeAABB = this.aabbs[proxyId]
        // if (nodeAABB.contains(aabb)) {
        if (nodeAABB.min.x <= aabb.min.x && nodeAABB.min.y <= aabb.min.y
            && aabb.max.x <= nodeAABB.max.x && aabb.max.y <= nodeAABB.max.y) {
            return false
        }

        removeLeaf(proxyId)

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
        assert { 0 <= proxyId && proxyId < nodeCount }
        return userData[proxyId]
    }

    override fun getFatAABB(proxyId: Int): AABB2 {
        assert { 0 <= proxyId && proxyId < nodeCount }
        return aabbs[proxyId]
    }

    override fun query(
        callback: TreeCallback,
        aabb: AABB2
    ) {
        nodeStackIndex = 0
        nodeStack[nodeStackIndex++] = root

        while (nodeStackIndex > 0) {
            val node = nodeStack[--nodeStackIndex]
            if (node == NULL_NODE) {
                continue
            }

            if (this.aabbs[node] overlaps aabb) {
                val child1 = child1[node]
                if (child1 == NULL_NODE) {
                    val proceed = callback.treeCallback(node)
                    if (!proceed) {
                        return
                    }
                } else {
                    if (nodeStack.size - nodeStackIndex - 2 <= 0) {
                        nodeStack = BufferUtils.reallocateBuffer(
                            nodeStack,
                            nodeStack.size, nodeStack.size * 2
                        )
                    }
                    nodeStack[nodeStackIndex++] = child1
                    nodeStack[nodeStackIndex++] = child2[node]
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
            nodeStack[--nodeStackIndex] = root
            val node = nodeStack[--nodeStackIndex]
            if (node == NULL_NODE) {
                continue
            }

            val nodeAABB = aabbs[node]
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

            val child1 = child1[node]
            if (child1 == NULL_NODE) {
                subInput.p1.x = p1x
                subInput.p1.y = p1y
                subInput.p2.x = p2x
                subInput.p2.y = p2y
                subInput.maxFraction = maxFraction

                val value = callback.raycastCallback(subInput, node)

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
                nodeStack[nodeStackIndex++] = child1
                nodeStack[nodeStackIndex++] = child2[node]
            }
        }
    }

    override fun computeHeight(): Int {
        return computeHeight(root)
    }

    private fun computeHeight(node: Int): Int {
        assert { 0 <= node && node < nodeCapacity }

        if (child1[node] == NULL_NODE) {
            return 0
        }
        val height1 = computeHeight(child1[node])
        val height2 = computeHeight(child2[node])
        return 1 + max(height1, height2)
    }

    /**
     * Validate this tree. For testing.
     */
    fun validate() {
        validateStructure(root)
        validateMetrics(root)

        var freeCount = 0
        var freeNode = freeList
        while (freeNode != NULL_NODE) {
            assert { 0 <= freeNode && freeNode < nodeCapacity }
            freeNode = parent[freeNode]
            ++freeCount
        }

        assert { height == computeHeight() }
        assert { nodeCount + freeCount == nodeCapacity }
    }

    // /**
    // * Build an optimal tree. Very expensive. For testing.
    // */
    // public void rebuildBottomUp() {
    // int[] nodes = new int[m_nodeCount];
    // int count = 0;
    //
    // // Build array of leaves. Free the rest.
    // for (int i = 0; i < m_nodeCapacity; ++i) {
    // if (m_nodes[i].height < 0) {
    // // free node in pool
    // continue;
    // }
    //
    // DynamicTreeNode node = m_nodes[i];
    // if (node.isLeaf()) {
    // node.parent = null;
    // nodes[count] = i;
    // ++count;
    // } else {
    // freeNode(node);
    // }
    // }
    //
    // AABB b = new AABB();
    // while (count > 1) {
    // float minCost = Double.MAX_VALUE;
    // int iMin = -1, jMin = -1;
    // for (int i = 0; i < count; ++i) {
    // AABB aabbi = m_nodes[nodes[i]].aabb;
    //
    // for (int j = i + 1; j < count; ++j) {
    // AABB aabbj = m_nodes[nodes[j]].aabb;
    // b.combine(aabbi, aabbj);
    // float cost = b.getPerimeter();
    // if (cost < minCost) {
    // iMin = i;
    // jMin = j;
    // minCost = cost;
    // }
    // }
    // }
    //
    // int index1 = nodes[iMin];
    // int index2 = nodes[jMin];
    // DynamicTreeNode child1 = m_nodes[index1];
    // DynamicTreeNode child2 = m_nodes[index2];
    //
    // DynamicTreeNode parent = allocateNode();
    // parent.child1 = child1;
    // parent.child2 = child2;
    // parent.height = 1 + MathUtils.max(child1.height, child2.height);
    // parent.aabb.combine(child1.aabb, child2.aabb);
    // parent.parent = null;
    //
    // child1.parent = parent;
    // child2.parent = parent;
    //
    // nodes[jMin] = nodes[count - 1];
    // nodes[iMin] = parent.id;
    // --count;
    // }
    //
    // m_root = m_nodes[nodes[0]];
    //
    // validate();
    // }

    private fun allocateNode(): Int {
        if (freeList == NULL_NODE) {
            assert { nodeCount == nodeCapacity }
            nodeCapacity *= 2
            expandBuffers(nodeCount, nodeCapacity)
        }
        assert { freeList != NULL_NODE }
        val node = freeList
        freeList = parent[node]
        parent[node] = NULL_NODE
        child1[node] = NULL_NODE
        heights[node] = 0
        ++nodeCount
        return node
    }

    /**
     * returns a node to the pool
     */
    private fun freeNode(node: Int) {
        assert { node != NULL_NODE }
        assert { 0 < nodeCount }
        parent[node] =
                if (freeList != NULL_NODE) freeList else NULL_NODE
        heights[node] = -1
        freeList = node
        nodeCount--
    }

    private fun insertLeaf(leaf: Int) {
        if (root == NULL_NODE) {
            root = leaf
            parent[root] = NULL_NODE
            return
        }

        // find the best sibling
        val leafAABB = aabbs[leaf]
        var index = root
        while (child1[index] != NULL_NODE) {
            val node = index
            val child1 = child1[node]
            val child2 = child2[node]
            val nodeAABB = aabbs[node]
            val area = nodeAABB.perimeter

            combinedAABB.combine(nodeAABB, leafAABB)
            val combinedArea = combinedAABB.perimeter

            // Cost of creating a new parent for this node and the new leaf
            val cost = 2.0 * combinedArea

            // Minimum cost of pushing the leaf further down the tree
            val inheritanceCost = 2.0 * (combinedArea - area)

            // Cost of descending into child1
            val cost1: Double
            val child1AABB = aabbs[child1]
            cost1 = if (this.child1[child1] == NULL_NODE) {
                combinedAABB.combine(leafAABB, child1AABB)
                combinedAABB.perimeter + inheritanceCost
            } else {
                combinedAABB.combine(leafAABB, child1AABB)
                val oldArea = child1AABB.perimeter
                val newArea = combinedAABB.perimeter
                newArea - oldArea + inheritanceCost
            }

            // Cost of descending into child2
            val cost2: Double
            val child2AABB = aabbs[child2]
            cost2 = if (this.child1[child2] == NULL_NODE) {
                combinedAABB.combine(leafAABB, child2AABB)
                combinedAABB.perimeter + inheritanceCost
            } else {
                combinedAABB.combine(leafAABB, child2AABB)
                val oldArea = child2AABB.perimeter
                val newArea = combinedAABB.perimeter
                newArea - oldArea + inheritanceCost
            }

            // Descend according to the minimum cost.
            if (cost < cost1 && cost < cost2) {
                break
            }

            // Descend
            index = if (cost1 < cost2) {
                child1
            } else {
                child2
            }
        }

        val sibling = index
        val oldParent = parent[sibling]
        val newParent = allocateNode()
        parent[newParent] = oldParent
        userData[newParent] = null
        aabbs[newParent].combine(leafAABB, aabbs[sibling])
        heights[newParent] = heights[sibling] + 1

        if (oldParent != NULL_NODE) {
            // The sibling was not the root.
            if (child1[oldParent] == sibling) {
                child1[oldParent] = newParent
            } else {
                child2[oldParent] = newParent
            }

            child1[newParent] = sibling
            child2[newParent] = leaf
            parent[sibling] = newParent
            parent[leaf] = newParent
        } else {
            // The sibling was the root.
            child1[newParent] = sibling
            child2[newParent] = leaf
            parent[sibling] = newParent
            parent[leaf] = newParent
            root = newParent
        }

        // Walk back up the tree fixing heights and AABBs
        index = parent[leaf]
        while (index != NULL_NODE) {
            index = balance(index)

            val child1 = child1[index]
            val child2 = child2[index]

            assert { child1 != NULL_NODE }
            assert { child2 != NULL_NODE }

            heights[index] = 1 + max(
                heights[child1],
                heights[child2]
            )
            aabbs[index].combine(aabbs[child1], aabbs[child2])

            index = parent[index]
        }
        // validate();
    }

    private fun removeLeaf(leaf: Int) {
        if (leaf == root) {
            root = NULL_NODE
            return
        }

        val parent = parent[leaf]
        val grandParent = this.parent[parent]
        val parentChild1 = child1[parent]
        val parentChild2 = child2[parent]
        val sibling: Int
        sibling = if (parentChild1 == leaf) {
            parentChild2
        } else {
            parentChild1
        }

        if (grandParent != NULL_NODE) {
            // Destroy parent and connect sibling to grandParent.
            if (child1[grandParent] == parent) {
                child1[grandParent] = sibling
            } else {
                child2[grandParent] = sibling
            }
            this.parent[sibling] = grandParent
            freeNode(parent)

            // Adjust ancestor bounds.
            var index = grandParent
            while (index != NULL_NODE) {
                index = balance(index)

                val child1 = child1[index]
                val child2 = child2[index]

                aabbs[index].combine(aabbs[child1], aabbs[child2])
                heights[index] = 1 + max(
                    heights[child1],
                    heights[child2]
                )

                index = this.parent[index]
            }
        } else {
            root = sibling
            this.parent[sibling] = NULL_NODE
            freeNode(parent)
        }

        // validate();
    }

    // Perform a left or right rotation if node A is imbalanced.
    // Returns the new root index.
    private fun balance(iA: Int): Int {
        assert { iA != NULL_NODE }

        if (child1[iA] == NULL_NODE || heights[iA] < 2) {
            return iA
        }

        val iB = child1[iA]
        val iC = child2[iA]
        assert { 0 <= iB && iB < nodeCapacity }
        assert { 0 <= iC && iC < nodeCapacity }

        val balance = heights[iC] - heights[iB]

        // Rotate C up
        if (balance > 1) {
            val iF = child1[iC]
            val iG = child2[iC]
            // assert (F != null);
            // assert (G != null);
            assert { 0 <= iF && iF < nodeCapacity }
            assert { 0 <= iG && iG < nodeCapacity }

            // Swap A and C
            child1[iC] = iA
            parent[iC] = parent[iA]
            val cParent = parent[iC]
            parent[iA] = iC

            // A's old parent should point to C
            if (cParent != NULL_NODE) {
                if (child1[cParent] == iA) {
                    child1[cParent] = iC
                } else {
                    assert { child2[cParent] == iA }
                    child2[cParent] = iC
                }
            } else {
                root = iC
            }

            // Rotate
            if (heights[iF] > heights[iG]) {
                child2[iC] = iF
                child2[iA] = iG
                parent[iG] = iA
                aabbs[iA].combine(aabbs[iB], aabbs[iG])
                aabbs[iC].combine(aabbs[iA], aabbs[iF])

                heights[iA] = 1 + max(
                    heights[iB],
                    heights[iG]
                )
                heights[iC] = 1 + max(
                    heights[iA],
                    heights[iF]
                )
            } else {
                child2[iC] = iG
                child2[iA] = iF
                parent[iF] = iA
                aabbs[iA].combine(aabbs[iB], aabbs[iF])
                aabbs[iC].combine(aabbs[iA], aabbs[iG])

                heights[iA] = 1 + max(
                    heights[iB],
                    heights[iF]
                )
                heights[iC] = 1 + max(
                    heights[iA],
                    heights[iG]
                )
            }

            return iC
        }

        // Rotate B up
        if (balance < -1) {
            val iD = child1[iB]
            val iE = child2[iB]
            assert { 0 <= iD && iD < nodeCapacity }
            assert { 0 <= iE && iE < nodeCapacity }

            // Swap A and B
            child1[iB] = iA
            parent[iB] = parent[iA]
            val Bparent = parent[iB]
            parent[iA] = iB

            // A's old parent should point to B
            if (Bparent != NULL_NODE) {
                if (child1[Bparent] == iA) {
                    child1[Bparent] = iB
                } else {
                    assert { child2[Bparent] == iA }
                    child2[Bparent] = iB
                }
            } else {
                root = iB
            }

            // Rotate
            if (heights[iD] > heights[iE]) {
                child2[iB] = iD
                child1[iA] = iE
                parent[iE] = iA
                aabbs[iA].combine(aabbs[iC], aabbs[iE])
                aabbs[iB].combine(aabbs[iA], aabbs[iD])

                heights[iA] = 1 + max(
                    heights[iC],
                    heights[iE]
                )
                heights[iB] = 1 + max(
                    heights[iA],
                    heights[iD]
                )
            } else {
                child2[iB] = iE
                child1[iA] = iD
                parent[iD] = iA
                aabbs[iA].combine(aabbs[iC], aabbs[iD])
                aabbs[iB].combine(aabbs[iA], aabbs[iE])

                heights[iA] = 1 + max(
                    heights[iC],
                    heights[iD]
                )
                heights[iB] = 1 + max(
                    heights[iA],
                    heights[iE]
                )
            }

            return iB
        }

        return iA
    }

    private fun validateStructure(node: Int) {
        if (node == NULL_NODE) {
            return
        }

        if (node == root) {
            assert { parent[node] == NULL_NODE }
        }

        val child1 = child1[node]
        val child2 = child2[node]

        if (child1 == NULL_NODE) {
            assert { child1 == NULL_NODE }
            assert { child2 == NULL_NODE }
            assert { heights[node] == 0 }
            return
        }

        assert { child1 != NULL_NODE && 0 <= child1 && child1 < nodeCapacity }
        assert { child2 != NULL_NODE && 0 <= child2 && child2 < nodeCapacity }

        assert { parent[child1] == node }
        assert { parent[child2] == node }

        validateStructure(child1)
        validateStructure(child2)
    }

    private fun validateMetrics(node: Int) {
        if (node == NULL_NODE) {
            return
        }

        val child1 = child1[node]
        val child2 = child2[node]

        if (child1 == NULL_NODE) {
            assert { child1 == NULL_NODE }
            assert { child2 == NULL_NODE }
            assert { heights[node] == 0 }
            return
        }

        assert { child1 != NULL_NODE && 0 <= child1 && child1 < nodeCapacity }
        assert { child2 != child1 && 0 <= child2 && child2 < nodeCapacity }

        val height1 = heights[child1]
        val height2 = heights[child2]
        val height: Int
        height = 1 + max(height1, height2)
        assert { heights[node] == height }

        val aabb = AABB2()
        aabb.combine(this.aabbs[child1], this.aabbs[child2])

        assert { aabb.min == this.aabbs[node].min }
        assert { aabb.max == this.aabbs[node].max }

        validateMetrics(child1)
        validateMetrics(child2)
    }
}
