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

import org.jbox2d.common.Color3f

/**
 * Small color object for each particle
 *
 * @author dmurph
 */
class ParticleColor {
    var r: Byte = 0
    var g: Byte = 0
    var b: Byte = 0
    var a: Byte = 0
    var x: Double
        get() = r.let { if (it < 0) it + 255 else it.toInt() } / 255.0
        set(value) {
            r = (value * 255.0).toByte()
        }
    var y: Double
        get() = g.let { if (it < 0) it + 255 else it.toInt() } / 255.0
        set(value) {
            g = (value * 255.0).toByte()
        }
    var z: Double
        get() = b.let { if (it < 0) it + 255 else it.toInt() } / 255.0
        set(value) {
            b = (value * 255.0).toByte()
        }
    var w: Double
        get() = a.let { if (it < 0) it + 255 else it.toInt() } / 255.0
        set(value) {
            a = (value * 255.0).toByte()
        }

    val isZero: Boolean
        get() = r.toInt() == 0 && g.toInt() == 0 && b.toInt() == 0 && a.toInt() == 0

    constructor() {
        r = 127.toByte()
        g = 127.toByte()
        b = 127.toByte()
        a = 50.toByte()
    }

    constructor(r: Byte,
                g: Byte,
                b: Byte,
                a: Byte) {
        set(r, g, b, a)
    }

    constructor(color: Color3f) {
        set(color)
    }

    fun set(color: Color3f) {
        r = (255 * color.x).toByte()
        g = (255 * color.y).toByte()
        b = (255 * color.z).toByte()
        a = 255.toByte()
    }

    fun set(color: ParticleColor) {
        r = color.r
        g = color.g
        b = color.b
        a = color.a
    }

    operator fun set(r: Byte,
                     g: Byte,
                     b: Byte,
                     a: Byte) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }
}
