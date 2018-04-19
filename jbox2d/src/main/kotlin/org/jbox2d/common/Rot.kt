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

import org.tobi29.math.cosTable
import org.tobi29.math.sinTable
import org.tobi29.math.vector.MutableVector2d
import kotlin.math.atan2

/**
 * Represents a rotation
 *
 * @author Daniel
 */
class Rot {
    var sin = 0.0
    var cos = 0.0

    val angle: Double get() = atan2(sin, cos)

    constructor() {
        setIdentity()
    }

    constructor(angle: Double) {
        set(angle)
    }

    override fun toString(): String {
        return "Rot(s:$sin, c:$cos)"
    }

    fun set(angle: Double): Rot {
        sin = sinTable(angle)
        cos = cosTable(angle)
        return this
    }

    fun set(other: Rot): Rot {
        sin = other.sin
        cos = other.cos
        return this
    }

    fun setIdentity(): Rot {
        sin = 0.0
        cos = 1.0
        return this
    }

    fun getXAxis(xAxis: MutableVector2d) {
        xAxis.setXY(cos, sin)
    }

    fun getYAxis(yAxis: MutableVector2d) {
        yAxis.setXY(-sin, cos)
    }

    companion object {
        fun mul(q: Rot,
                r: Rot,
                out: Rot) {
            val tempc = q.cos * r.cos - q.sin * r.sin
            out.sin = q.sin * r.cos + q.cos * r.sin
            out.cos = tempc
        }

        fun mulTrans(q: Rot,
                     r: Rot,
                     out: Rot) {
            val tempc = q.cos * r.cos + q.sin * r.sin
            out.sin = q.cos * r.sin - q.sin * r.cos
            out.cos = tempc
        }

        fun mulToOut(q: Rot,
                     v: MutableVector2d,
                     out: MutableVector2d) {
            val tempy = q.sin * v.x + q.cos * v.y
            out.x = q.cos * v.x - q.sin * v.y
            out.y = tempy
        }

        fun mulTrans(q: Rot,
                     v: MutableVector2d,
                     out: MutableVector2d) {
            val tempy = -q.sin * v.x + q.cos * v.y
            out.x = q.cos * v.x + q.sin * v.y
            out.y = tempy
        }
    }
}
