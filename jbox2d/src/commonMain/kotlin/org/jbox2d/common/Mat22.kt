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

import org.tobi29.math.matrix.*
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.ReadVector2d

fun Matrix2d.createScaleTransform(scale: Double) = apply {
    xx = scale
    yy = scale
}

fun Matrix2d.solveToOut(
    b: ReadVector2d,
    out: MutableVector2d
) {
    val a11 = xx
    val a12 = yx
    val a21 = xy
    val a22 = yy
    var det = a11 * a22 - a12 * a21
    if (det != 0.0) {
        det = 1.0 / det
    }
    val tempy = det * (a11 * b.y - a21 * b.x)
    out.x = det * (a22 * b.x - a12 * b.y)
    out.y = tempy
}

fun Matrix2d.invertToOut(out: Matrix2d) {
    val a = xx
    val b = yx
    val c = xy
    val d = yy
    var det = a * d - b * c
    // b2Assert(det != 0.0);
    det = 1.0 / det
    out.xx = det * d
    out.yx = -det * b
    out.xy = -det * c
    out.yy = det * a
}
