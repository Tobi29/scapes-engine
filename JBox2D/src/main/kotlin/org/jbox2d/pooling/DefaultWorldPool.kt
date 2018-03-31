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

package org.jbox2d.pooling

import org.jbox2d.collision.Collision
import org.jbox2d.collision.Distance
import org.jbox2d.collision.TimeOfImpact
import org.jbox2d.common.Rot
import org.jbox2d.dynamics.contacts.*
import org.tobi29.math.AABB2
import org.tobi29.math.matrix.Matrix2d
import org.tobi29.math.matrix.Matrix3d
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.MutableVector3d
import org.tobi29.stdex.assert
import org.tobi29.utils.Pool

/**
 * Provides object pooling for all objects used in the engine. Objects retrieved from here should
 * only be used temporarily, and then pushed back (with the exception of arrays).
 * @author Daniel Murphy
 */
class DefaultWorldPool(
    argContainerSize: Int
) : IWorldPool {
    private val mutableVector2ds = Pool { MutableVector2d() }
        .apply { ensureCapacity(argContainerSize) }
    private val mutableVector3ds = Pool { MutableVector3d() }
        .apply { ensureCapacity(argContainerSize) }
    private val matrix2ds = Pool { Matrix2d() }
        .apply { ensureCapacity(argContainerSize) }
    private val matrix3ds = Pool { Matrix3d() }
        .apply { ensureCapacity(argContainerSize) }
    private val aabbs = Pool { AABB2() }
        .apply { ensureCapacity(argContainerSize) }
    private val rots = Pool { Rot() }
        .apply { ensureCapacity(argContainerSize) }

    private val afloats = HashMap<Int, DoubleArray>()
    private val aints = HashMap<Int, IntArray>()
    private val avecs = HashMap<Int, Array<MutableVector2d>>()

    private val world = this

    override val collision = Collision(this)
    override val timeOfImpact = TimeOfImpact(this)
    override val distance = Distance()

    override val polyContactStack =
        Pool { PolygonContact(world) }

    override val circleContactStack =
        Pool { CircleContact(world) }

    override val polyCircleContactStack =
        Pool { PolygonAndCircleContact(world) }

    override val edgeCircleContactStack =
        Pool { EdgeAndCircleContact(world) }

    override val edgePolyContactStack =
        Pool { EdgeAndPolygonContact(world) }

    override val chainCircleContactStack =
        Pool { ChainAndCircleContact(world) }

    override val chainPolyContactStack =
        Pool { ChainAndPolygonContact(world) }

    override fun popMutableVector2d() = mutableVector2ds.push()

    override fun pushMutableVector2d(num: Int) {
        mutableVector2ds.pop(num)
    }

    override fun popMutableVector3d() = mutableVector3ds.push()

    override fun pushMutableVector3d(num: Int) {
        mutableVector3ds.pop(num)
    }

    override fun popMatrix2d() = matrix2ds.push()

    override fun pushMatrix2d(num: Int) {
        matrix2ds.pop(num)
    }

    override fun popMatrix3d() = matrix3ds.push()

    override fun pushMatrix3d(num: Int) {
        matrix3ds.pop(num)
    }

    override fun popAABB(): AABB2 = aabbs.push()

    override fun pushAABB(num: Int) {
        aabbs.pop(num)
    }

    override fun popRot() = rots.push()

    override fun pushRot(num: Int) {
        rots.pop(num)
    }

    override fun getDoubleArray(argLength: Int): DoubleArray {
        if (!afloats.containsKey(argLength)) {
            afloats[argLength] = DoubleArray(argLength)
        }

        assert { afloats[argLength]!!.size == argLength }
        return afloats[argLength]!!
    }

    override fun getIntArray(argLength: Int): IntArray {
        if (!aints.containsKey(argLength)) {
            aints[argLength] = IntArray(argLength)
        }

        assert { aints[argLength]!!.size == argLength }
        return aints[argLength]!!
    }

    override fun getMutableVector2dArray(argLength: Int): Array<MutableVector2d> {
        if (!avecs.containsKey(argLength)) {
            val ray = Array(argLength) { MutableVector2d() }
            avecs[argLength] = ray
        }

        assert { avecs[argLength]!!.size == argLength }
        return avecs[argLength]!!
    }
}
