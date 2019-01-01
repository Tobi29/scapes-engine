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

package org.jbox2d.collision.shapes

import org.jbox2d.collision.RayCastOutput
import org.jbox2d.collision.RaycastInput
import org.jbox2d.common.Settings
import org.jbox2d.common.Transform
import org.tobi29.math.AABB2
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.distanceSqr
import org.tobi29.stdex.assert
import org.tobi29.stdex.math.sqr

/**
 * A chain shape is a free form sequence of line segments. The chain has two-sided collision, so you
 * can use inside and outside collision. Therefore, you may use any winding order. Connectivity
 * information is used to create smooth collisions. WARNING: The chain will not collide properly if
 * there are self-intersections.
 *
 * @author Daniel
 */
class ChainShape : Shape(ShapeType.CHAIN) {
    var vertices: Array<Vector2d> = emptyArray()
    var count: Int = 0
    var prevVertex: Vector2d? = null
    var nextVertex: Vector2d? = null

    private val pool0 = EdgeShape()

    override val childCount: Int
        get() = count - 1

    init {
        radius = Settings.polygonRadius
    }

    /**
     * Get a child edge.
     */
    fun getChildEdge(
        edge: EdgeShape,
        index: Int
    ) {
        assert { 0 <= index && index < count - 1 }
        edge.radius = radius

        edge.vertex1 = vertices[index + 0]
        edge.vertex2 = vertices[index + 1]

        if (index > 0) {
            val v = vertices[index - 1]
            edge.vertex0 = v
        } else {
            edge.vertex0 = prevVertex
        }

        if (index < count - 2) {
            val v = vertices[index + 2]
            edge.vertex3 = v
        } else {
            edge.vertex3 = nextVertex
        }
    }

    override fun computeDistanceToOut(
        transform: Transform,
        point: Vector2d,
        childIndex: Int,
        normalOut: MutableVector2d
    ): Double {
        val edge = pool0
        getChildEdge(edge, childIndex)
        return edge.computeDistanceToOut(transform, point, 0, normalOut)
    }

    override fun testPoint(
        xf: Transform,
        p: Vector2d
    ): Boolean {
        return false
    }

    override fun raycast(
        output: RayCastOutput,
        input: RaycastInput,
        transform: Transform,
        childIndex: Int
    ): Boolean {
        assert { childIndex < count }

        val edgeShape = pool0

        var i2 = childIndex + 1
        if (i2 == count) {
            i2 = 0
        }
        val v = vertices[childIndex]
        edgeShape._vertex1.x = v.x
        edgeShape._vertex1.y = v.y
        val v1 = vertices[i2]
        edgeShape._vertex2.x = v1.x
        edgeShape._vertex2.y = v1.y

        return edgeShape.raycast(output, input, transform, 0)
    }

    override fun computeAABB(
        aabb: AABB2,
        transform: Transform,
        childIndex: Int
    ) {
        assert { childIndex < count }
        val lower = aabb.min
        val upper = aabb.max

        var i2 = childIndex + 1
        if (i2 == count) {
            i2 = 0
        }

        val vi1 = vertices[childIndex]
        val vi2 = vertices[i2]
        val xfq = transform.q
        val xfp = transform.p
        val v1x = xfq.cos * vi1.x - xfq.sin * vi1.y + xfp.x
        val v1y = xfq.sin * vi1.x + xfq.cos * vi1.y + xfp.y
        val v2x = xfq.cos * vi2.x - xfq.sin * vi2.y + xfp.x
        val v2y = xfq.sin * vi2.x + xfq.cos * vi2.y + xfp.y

        lower.x = if (v1x < v2x) v1x else v2x
        lower.y = if (v1y < v2y) v1y else v2y
        upper.x = if (v1x > v2x) v1x else v2x
        upper.y = if (v1y > v2y) v1y else v2y
    }

    override fun computeMass(
        massData: MassData,
        density: Double
    ) {
        massData.mass = 0.0
        massData._center.setXY(0.0, 0.0)
        massData.i = 0.0
    }

    override fun clone(): Shape {
        val clone = ChainShape()
        clone.createChain(vertices, count)
        clone.prevVertex = prevVertex
        clone.nextVertex = nextVertex
        return clone
    }

    /**
     * Create a loop. This automatically adjusts connectivity.
     *
     * @param vertices an array of vertices, these are copied
     * @param count the vertex count
     */
    fun createLoop(
        vertices: Array<Vector2d>,
        count: Int
    ) {
        assert { this.count == 0 }
        assert { count >= 3 }
        this.count = count + 1
        for (i in 1 until count) {
            val v1 = vertices[i - 1]
            val v2 = vertices[i]
            // If the code crashes here, it means your vertices are too close together.
            if (v1 distanceSqr v2 < sqr(Settings.linearSlop)) {
                throw RuntimeException(
                    "Vertices of chain shape are too close together"
                )
            }
        }
        val first = vertices[0]
        this.vertices = Array(this.count) {
            if (it == 0 || it == count) first
            else vertices[it]
        }
        prevVertex = this.vertices[this.count - 2]
        nextVertex = this.vertices[1]
    }

    /**
     * Create a chain with isolated end vertices.
     *
     * @param vertices an array of vertices, these are copied
     * @param count the vertex count
     */
    fun createChain(
        vertices: Array<Vector2d>,
        count: Int
    ) {
        assert { this.count == 0 }
        assert { count >= 2 }
        this.count = count
        for (i in 1 until this.count) {
            val v1 = vertices[i - 1]
            val v2 = vertices[i]
            // If the code crashes here, it means your vertices are too close together.
            if (v1 distanceSqr v2 < sqr(Settings.linearSlop)) {
                throw RuntimeException(
                    "Vertices of chain shape are too close together"
                )
            }
        }
        this.vertices = Array(this.count) { vertices[it] }
        prevVertex = null
        nextVertex = null
    }
}
