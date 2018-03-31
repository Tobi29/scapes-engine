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
import org.tobi29.math.vector.*
import org.tobi29.stdex.assert

/**
 * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
 * in one-shot cases.
 *
 * @param b
 * @return
 */
fun Matrix3d.solve22(b: Vector2d): Vector2d {
    val x = MutableVector2d()
    solve22ToOut(b, x)
    return x.now()
}

/**
 * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
 * in one-shot cases.
 *
 * @param b
 * @return
 */
fun Matrix3d.solve22ToOut(b: ReadVector2d, out: MutableVector2d) {
    val a11 = xx
    val a12 = yx
    val a21 = xy
    val a22 = yy
    var det = a11 * a22 - a12 * a21
    if (det != 0.0) {
        det = 1.0 / det
    }
    out.x = (det * (a22 * b.x - a12 * b.y))
    out.y = (det * (a11 * b.y - a21 * b.x))
}

// djm pooling from below
/**
 * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
 * in one-shot cases.
 *
 * @param b
 * @return
 */
fun Matrix3d.solve33(b: Vector3d): Vector3d {
    return MutableVector3d().also {
        solve33ToOut(b, it)
    }.now()
}

/**
 * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
 * in one-shot cases.
 *
 * @param b
 * @param out the result
 */
fun Matrix3d.solve33ToOut(b: ReadVector3d, out: MutableVector3d) {
    assert { b !== out }
    cross(yx, yy, yz, zx, zy, zz) { x, y, z -> out.setXYZ(x, y, z) }
    var det = dot(xx, xy, xz, out.x, out.y, out.z)
    if (det != 0.0) {
        det = 1.0 / det
    }
    cross(yx, yy, yz, zx, zy, zz) { x, y, z -> out.setXYZ(x, y, z) }
    val x = det * (b dot out)
    cross(b.x, b.y, b.z, zx, zy, zz) { x, y, z -> out.setXYZ(x, y, z) }
    val y = det * dot(xx, xy, xz, out.x, out.y, out.z)
    cross(yx, yy, yz, b.x, b.y, b.z) { x, y, z -> out.setXYZ(x, y, z) }
    val z = det * dot(xx, xy, xz, out.x, out.y, out.z)
    out.x = x
    out.y = y
    out.z = z
}

fun Matrix3d.getInverse22(M: Matrix3d) {
    val a = xx
    val b = yx
    val c = xy
    val d = yy
    var det = a * d - b * c
    if (det != 0.0) {
        det = 1.0 / det
    }

    M.xx = det * d
    M.yx = -det * b
    M.xz = 0.0
    M.xy = -det * c
    M.yy = det * a
    M.yz = 0.0
    M.zx = 0.0
    M.zy = 0.0
    M.zz = 0.0
}

// / Returns the zero matrix if singular.
fun Matrix3d.getSymInverse33(M: Matrix3d) {
    val bx = yy * zz - yz * zy
    val by = yz * zx - yx * zz
    val bz = yx * zy - yy * zx
    var det = xx * bx + xy * by + xz * bz
    if (det != 0.0) {
        det = 1.0 / det
    }

    val a11 = xx
    val a12 = yx
    val a13 = zx
    val a22 = yy
    val a23 = zy
    val a33 = zz

    M.xx = det * (a22 * a33 - a23 * a23)
    M.xy = det * (a13 * a23 - a12 * a33)
    M.xz = det * (a12 * a23 - a13 * a22)

    M.yx = M.xy
    M.yy = det * (a11 * a33 - a13 * a13)
    M.yz = det * (a13 * a12 - a11 * a23)

    M.zx = M.xz
    M.zy = M.yz
    M.zz = det * (a11 * a22 - a12 * a12)
}
