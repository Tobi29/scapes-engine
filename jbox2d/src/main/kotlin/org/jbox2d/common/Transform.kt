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
package org.jbox2d.common

import org.tobi29.math.vector.*


// updated to rev 100

/**
 * A transform contains translation and rotation. It is used to represent the position and
 * orientation of rigid frames.
 */
class Transform() {

    /** The translation caused by the transform  */
    val p = MutableVector2d()

    /** A matrix representing a rotation  */
    val q = Rot()

    /** Initialize as a copy of another transform.  */
    constructor(xf: Transform) : this() {
        set(xf)
    }

    /** Set this to equal another transform.  */
    fun set(xf: Transform): Transform {
        p.set(xf.p)
        q.set(xf.q)
        return this
    }

    /**
     * Set this based on the position and angle.
     *
     * @param p
     * @param angle
     */
    fun set(p: ReadVector2d, angle: Double) {
        this.p.set(p)
        q.set(angle)
    }

    /** Set this to the identity transform.  */
    fun setIdentity() {
        p.setXY(0.0, 0.0)
        q.setIdentity()
    }

    override fun toString(): String {
        var s = "XForm:\n"
        s += "Position: $p\n"
        s += "R: \n$q\n"
        return s
    }

    companion object {
        fun mul(
            T: Transform,
            v: ReadVector2d
        ): Vector2d {
            return Vector2d(
                T.q.cos * v.x - T.q.sin * v.y + T.p.x,
                T.q.sin * v.x + T.q.cos * v.y + T.p.y
            )
        }

        fun mulToOut(
            T: Transform,
            v: ReadVector2d,
            out: MutableVector2d
        ) {
            val tempy = T.q.sin * v.x + T.q.cos * v.y + T.p.y
            out.x = T.q.cos * v.x - T.q.sin * v.y + T.p.x
            out.y = tempy
        }

        fun mulTrans(
            T: Transform,
            v: ReadVector2d
        ): Vector2d {
            val px = v.x - T.p.x
            val py = v.y - T.p.y
            return Vector2d(
                T.q.cos * px + T.q.sin * py,
                -T.q.sin * px + T.q.cos * py
            )
        }

        fun mulTransToOut(
            T: Transform,
            v: ReadVector2d,
            out: MutableVector2d
        ) {
            val px = v.x - T.p.x
            val py = v.y - T.p.y
            val tempy = -T.q.sin * px + T.q.cos * py
            out.x = T.q.cos * px + T.q.sin * py
            out.y = tempy
        }

        fun mul(
            A: Transform,
            B: Transform
        ): Transform {
            val C = Transform()
            Rot.mul(A.q, B.q, C.q)
            Rot.mulToOut(A.q, B.p, C.p)
            C.p.add(A.p)
            return C
        }

        private val pool = MutableVector2d()

        fun mulTrans(
            A: Transform,
            B: Transform
        ): Transform {
            val C = Transform()
            Rot.mulTrans(A.q, B.q, C.q)
            pool.set(B.p)
            pool.subtract(A.p)
            Rot.mulTrans(A.q, pool, C.p)
            return C
        }
    }
}
