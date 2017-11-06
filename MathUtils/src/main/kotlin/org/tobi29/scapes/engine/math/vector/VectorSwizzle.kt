/*
 * Copyright 2012-2017 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.math.vector

val Vector2d.xx: Vector2d
    get() = Vector2d(x, x)

val Vector2d.xy: Vector2d
    get() = Vector2d(x, y)

val Vector3d.xz: Vector2d
    get() = Vector2d(x, z)

val Vector2d.yx: Vector2d
    get() = Vector2d(y, x)

val Vector2d.yy: Vector2d
    get() = Vector2d(y, y)

val Vector3d.yz: Vector2d
    get() = Vector2d(y, z)

val Vector3d.zx: Vector2d
    get() = Vector2d(z, x)

val Vector3d.zy: Vector2d
    get() = Vector2d(y, z)

val Vector3d.zz: Vector2d
    get() = Vector2d(z, z)

val Vector2d.xxx: Vector3d
    get() = Vector3d(x, x, x)

val Vector2d.xxy: Vector3d
    get() = Vector3d(x, x, y)

val Vector3d.xxz: Vector3d
    get() = Vector3d(x, x, z)

val Vector2d.xyx: Vector3d
    get() = Vector3d(x, y, x)

val Vector2d.xyy: Vector3d
    get() = Vector3d(x, y, y)

val Vector3d.xyz: Vector3d
    get() = Vector3d(x, y, z)

val Vector3d.xzx: Vector3d
    get() = Vector3d(x, z, x)

val Vector3d.xzy: Vector3d
    get() = Vector3d(x, y, z)

val Vector3d.xzz: Vector3d
    get() = Vector3d(x, z, z)

val Vector2d.yxx: Vector3d
    get() = Vector3d(y, x, x)

val Vector2d.yxy: Vector3d
    get() = Vector3d(y, x, y)

val Vector3d.yxz: Vector3d
    get() = Vector3d(y, x, z)

val Vector2d.yyx: Vector3d
    get() = Vector3d(y, y, x)

val Vector2d.yyy: Vector3d
    get() = Vector3d(y, y, y)

val Vector3d.yyz: Vector3d
    get() = Vector3d(y, y, z)

val Vector3d.yzx: Vector3d
    get() = Vector3d(y, z, x)

val Vector3d.yzy: Vector3d
    get() = Vector3d(y, y, z)

val Vector3d.yzz: Vector3d
    get() = Vector3d(y, z, z)

val Vector3d.zxx: Vector3d
    get() = Vector3d(z, x, x)

val Vector3d.zxy: Vector3d
    get() = Vector3d(z, x, y)

val Vector3d.zxz: Vector3d
    get() = Vector3d(z, x, z)

val Vector3d.zyx: Vector3d
    get() = Vector3d(z, y, x)

val Vector3d.zyy: Vector3d
    get() = Vector3d(z, y, y)

val Vector3d.zyz: Vector3d
    get() = Vector3d(z, y, z)

val Vector3d.zzx: Vector3d
    get() = Vector3d(z, z, x)

val Vector3d.zzy: Vector3d
    get() = Vector3d(z, y, z)

val Vector3d.zzz: Vector3d
    get() = Vector3d(z, z, z)

val Vector2i.xx: Vector2i
    get() = Vector2i(x, x)

val Vector2i.xy: Vector2i
    get() = Vector2i(x, y)

val Vector3i.xz: Vector2i
    get() = Vector2i(x, z)

val Vector2i.yx: Vector2i
    get() = Vector2i(y, x)

val Vector2i.yy: Vector2i
    get() = Vector2i(y, y)

val Vector3i.yz: Vector2i
    get() = Vector2i(y, z)

val Vector3i.zx: Vector2i
    get() = Vector2i(z, x)

val Vector3i.zy: Vector2i
    get() = Vector2i(y, z)

val Vector3i.zz: Vector2i
    get() = Vector2i(z, z)

val Vector2i.xxx: Vector3i
    get() = Vector3i(x, x, x)

val Vector2i.xxy: Vector3i
    get() = Vector3i(x, x, y)

val Vector3i.xxz: Vector3i
    get() = Vector3i(x, x, z)

val Vector2i.xyx: Vector3i
    get() = Vector3i(x, y, x)

val Vector2i.xyy: Vector3i
    get() = Vector3i(x, y, y)

val Vector3i.xyz: Vector3i
    get() = Vector3i(x, y, z)

val Vector3i.xzx: Vector3i
    get() = Vector3i(x, z, x)

val Vector3i.xzy: Vector3i
    get() = Vector3i(x, y, z)

val Vector3i.xzz: Vector3i
    get() = Vector3i(x, z, z)

val Vector2i.yxx: Vector3i
    get() = Vector3i(y, x, x)

val Vector2i.yxy: Vector3i
    get() = Vector3i(y, x, y)

val Vector3i.yxz: Vector3i
    get() = Vector3i(y, x, z)

val Vector2i.yyx: Vector3i
    get() = Vector3i(y, y, x)

val Vector2i.yyy: Vector3i
    get() = Vector3i(y, y, y)

val Vector3i.yyz: Vector3i
    get() = Vector3i(y, y, z)

val Vector3i.yzx: Vector3i
    get() = Vector3i(y, z, x)

val Vector3i.yzy: Vector3i
    get() = Vector3i(y, y, z)

val Vector3i.yzz: Vector3i
    get() = Vector3i(y, z, z)

val Vector3i.zxx: Vector3i
    get() = Vector3i(z, x, x)

val Vector3i.zxy: Vector3i
    get() = Vector3i(z, x, y)

val Vector3i.zxz: Vector3i
    get() = Vector3i(z, x, z)

val Vector3i.zyx: Vector3i
    get() = Vector3i(z, y, x)

val Vector3i.zyy: Vector3i
    get() = Vector3i(z, y, y)

val Vector3i.zyz: Vector3i
    get() = Vector3i(z, y, z)

val Vector3i.zzx: Vector3i
    get() = Vector3i(z, z, x)

val Vector3i.zzy: Vector3i
    get() = Vector3i(z, y, z)

val Vector3i.zzz: Vector3i
    get() = Vector3i(z, z, z)
