/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.math.vector

// Vector2d
inline val ReadVector2d.xx: Vector2d
    get() = Vector2d(x, x)

inline val ReadVector2d.xy: Vector2d
    get() = Vector2d(x, y)

inline val Vector2d.xy: Vector2d
    get() = this

inline val ReadVector2d.yx: Vector2d
    get() = Vector2d(y, x)

inline val ReadVector2d.yy: Vector2d
    get() = Vector2d(y, y)

inline val ReadVector2d.xxx: Vector3d
    get() = Vector3d(x, x, x)

inline val ReadVector2d.xxy: Vector3d
    get() = Vector3d(x, x, y)

inline val ReadVector2d.xyx: Vector3d
    get() = Vector3d(x, y, x)

inline val ReadVector2d.xyy: Vector3d
    get() = Vector3d(x, y, y)

inline val ReadVector2d.yxx: Vector3d
    get() = Vector3d(y, x, x)

inline val ReadVector2d.yxy: Vector3d
    get() = Vector3d(y, x, y)

inline val ReadVector2d.yyx: Vector3d
    get() = Vector3d(y, y, x)

inline val ReadVector2d.yyy: Vector3d
    get() = Vector3d(y, y, y)

// Vector3d
inline val ReadVector3d.xx: Vector2d
    get() = Vector2d(x, x)

inline val ReadVector3d.xy: Vector2d
    get() = Vector2d(x, y)

inline val ReadVector3d.xz: Vector2d
    get() = Vector2d(x, z)

inline val ReadVector3d.yx: Vector2d
    get() = Vector2d(y, x)

inline val ReadVector3d.yy: Vector2d
    get() = Vector2d(y, y)

inline val ReadVector3d.yz: Vector2d
    get() = Vector2d(y, z)

inline val ReadVector3d.zx: Vector2d
    get() = Vector2d(z, x)

inline val ReadVector3d.zy: Vector2d
    get() = Vector2d(y, z)

inline val ReadVector3d.zz: Vector2d
    get() = Vector2d(z, z)

inline val ReadVector3d.xxx: Vector3d
    get() = Vector3d(x, x, x)

inline val ReadVector3d.xxy: Vector3d
    get() = Vector3d(x, x, y)

inline val ReadVector3d.xxz: Vector3d
    get() = Vector3d(x, x, z)

inline val ReadVector3d.xyx: Vector3d
    get() = Vector3d(x, y, x)

inline val ReadVector3d.xyy: Vector3d
    get() = Vector3d(x, y, y)

inline val ReadVector3d.xyz: Vector3d
    get() = Vector3d(x, y, z)

inline val Vector3d.xyz: Vector3d
    get() = this

inline val ReadVector3d.xzx: Vector3d
    get() = Vector3d(x, z, x)

inline val ReadVector3d.xzy: Vector3d
    get() = Vector3d(x, y, z)

inline val ReadVector3d.xzz: Vector3d
    get() = Vector3d(x, z, z)

inline val ReadVector3d.yxx: Vector3d
    get() = Vector3d(y, x, x)

inline val ReadVector3d.yxy: Vector3d
    get() = Vector3d(y, x, y)

inline val ReadVector3d.yxz: Vector3d
    get() = Vector3d(y, x, z)

inline val ReadVector3d.yyx: Vector3d
    get() = Vector3d(y, y, x)

inline val ReadVector3d.yyy: Vector3d
    get() = Vector3d(y, y, y)

inline val ReadVector3d.yyz: Vector3d
    get() = Vector3d(y, y, z)

inline val ReadVector3d.yzx: Vector3d
    get() = Vector3d(y, z, x)

inline val ReadVector3d.yzy: Vector3d
    get() = Vector3d(y, y, z)

inline val ReadVector3d.yzz: Vector3d
    get() = Vector3d(y, z, z)

inline val ReadVector3d.zxx: Vector3d
    get() = Vector3d(z, x, x)

inline val ReadVector3d.zxy: Vector3d
    get() = Vector3d(z, x, y)

inline val ReadVector3d.zxz: Vector3d
    get() = Vector3d(z, x, z)

inline val ReadVector3d.zyx: Vector3d
    get() = Vector3d(z, y, x)

inline val ReadVector3d.zyy: Vector3d
    get() = Vector3d(z, y, y)

inline val ReadVector3d.zyz: Vector3d
    get() = Vector3d(z, y, z)

inline val ReadVector3d.zzx: Vector3d
    get() = Vector3d(z, z, x)

inline val ReadVector3d.zzy: Vector3d
    get() = Vector3d(z, y, z)

inline val ReadVector3d.zzz: Vector3d
    get() = Vector3d(z, z, z)

// Vector2i
inline val ReadVector2i.xx: Vector2i
    get() = Vector2i(x, x)

inline val ReadVector2i.xy: Vector2i
    get() = Vector2i(x, y)

inline val Vector2i.xy: Vector2i
    get() = this

inline val ReadVector2i.yx: Vector2i
    get() = Vector2i(y, x)

inline val ReadVector2i.yy: Vector2i
    get() = Vector2i(y, y)

inline val ReadVector2i.xxx: Vector3i
    get() = Vector3i(x, x, x)

inline val ReadVector2i.xxy: Vector3i
    get() = Vector3i(x, x, y)

inline val ReadVector2i.xyx: Vector3i
    get() = Vector3i(x, y, x)

inline val ReadVector2i.xyy: Vector3i
    get() = Vector3i(x, y, y)

inline val ReadVector2i.yxx: Vector3i
    get() = Vector3i(y, x, x)

inline val ReadVector2i.yxy: Vector3i
    get() = Vector3i(y, x, y)

inline val ReadVector2i.yyx: Vector3i
    get() = Vector3i(y, y, x)

inline val ReadVector2i.yyy: Vector3i
    get() = Vector3i(y, y, y)

// Vector3i
inline val ReadVector3i.xx: Vector2i
    get() = Vector2i(x, x)

inline val ReadVector3i.xy: Vector2i
    get() = Vector2i(x, y)

inline val ReadVector3i.xz: Vector2i
    get() = Vector2i(x, z)

inline val ReadVector3i.yx: Vector2i
    get() = Vector2i(y, x)

inline val ReadVector3i.yy: Vector2i
    get() = Vector2i(y, y)

inline val ReadVector3i.yz: Vector2i
    get() = Vector2i(y, z)

inline val ReadVector3i.zx: Vector2i
    get() = Vector2i(z, x)

inline val ReadVector3i.zy: Vector2i
    get() = Vector2i(y, z)

inline val ReadVector3i.zz: Vector2i
    get() = Vector2i(z, z)

inline val ReadVector3i.xxx: Vector3i
    get() = Vector3i(x, x, x)

inline val ReadVector3i.xxy: Vector3i
    get() = Vector3i(x, x, y)

inline val ReadVector3i.xxz: Vector3i
    get() = Vector3i(x, x, z)

inline val ReadVector3i.xyx: Vector3i
    get() = Vector3i(x, y, x)

inline val ReadVector3i.xyy: Vector3i
    get() = Vector3i(x, y, y)

inline val ReadVector3i.xyz: Vector3i
    get() = Vector3i(x, y, z)

inline val Vector3i.xyz: Vector3i
    get() = this

inline val ReadVector3i.xzx: Vector3i
    get() = Vector3i(x, z, x)

inline val ReadVector3i.xzy: Vector3i
    get() = Vector3i(x, y, z)

inline val ReadVector3i.xzz: Vector3i
    get() = Vector3i(x, z, z)

inline val ReadVector3i.yxx: Vector3i
    get() = Vector3i(y, x, x)

inline val ReadVector3i.yxy: Vector3i
    get() = Vector3i(y, x, y)

inline val ReadVector3i.yxz: Vector3i
    get() = Vector3i(y, x, z)

inline val ReadVector3i.yyx: Vector3i
    get() = Vector3i(y, y, x)

inline val ReadVector3i.yyy: Vector3i
    get() = Vector3i(y, y, y)

inline val ReadVector3i.yyz: Vector3i
    get() = Vector3i(y, y, z)

inline val ReadVector3i.yzx: Vector3i
    get() = Vector3i(y, z, x)

inline val ReadVector3i.yzy: Vector3i
    get() = Vector3i(y, y, z)

inline val ReadVector3i.yzz: Vector3i
    get() = Vector3i(y, z, z)

inline val ReadVector3i.zxx: Vector3i
    get() = Vector3i(z, x, x)

inline val ReadVector3i.zxy: Vector3i
    get() = Vector3i(z, x, y)

inline val ReadVector3i.zxz: Vector3i
    get() = Vector3i(z, x, z)

inline val ReadVector3i.zyx: Vector3i
    get() = Vector3i(z, y, x)

inline val ReadVector3i.zyy: Vector3i
    get() = Vector3i(z, y, y)

inline val ReadVector3i.zyz: Vector3i
    get() = Vector3i(z, y, z)

inline val ReadVector3i.zzx: Vector3i
    get() = Vector3i(z, z, x)

inline val ReadVector3i.zzy: Vector3i
    get() = Vector3i(z, y, z)

inline val ReadVector3i.zzz: Vector3i
    get() = Vector3i(z, z, z)
