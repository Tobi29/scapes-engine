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
import org.tobi29.utils.Pool

/**
 * World pool interface
 * @author Daniel
 */
interface IWorldPool {

    val polyContactStack: Pool<PolygonContact>

    val circleContactStack: Pool<CircleContact>

    val polyCircleContactStack: Pool<PolygonAndCircleContact>

    val edgeCircleContactStack: Pool<EdgeAndCircleContact>

    val edgePolyContactStack: Pool<EdgeAndPolygonContact>

    val chainCircleContactStack: Pool<ChainAndCircleContact>

    val chainPolyContactStack: Pool<ChainAndPolygonContact>

    val collision: Collision

    val timeOfImpact: TimeOfImpact

    val distance: Distance

    fun popMutableVector2d(): MutableVector2d

    fun pushMutableVector2d(num: Int)

    fun popMutableVector3d(): MutableVector3d

    fun pushMutableVector3d(num: Int)

    fun popMatrix2d(): Matrix2d

    fun pushMatrix2d(num: Int)

    fun popMatrix3d(): Matrix3d

    fun pushMatrix3d(num: Int)

    fun popAABB(): AABB2

    fun pushAABB(num: Int)

    fun popRot(): Rot

    fun pushRot(num: Int)

    fun getDoubleArray(argLength: Int): DoubleArray

    fun getIntArray(argLength: Int): IntArray

    fun getMutableVector2dArray(argLength: Int): Array<MutableVector2d>

}
