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
package org.tobi29.graphics

import org.tobi29.math.Frustum
import org.tobi29.math.FrustumConfiguration
import org.tobi29.math.vector.MutableVector3d
import org.tobi29.math.vector.ReadVector3d
import org.tobi29.math.vector.Vector3d
import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.math.toRad
import kotlin.math.cos
import kotlin.math.sin

class Camera(
    position: ReadVector3d,
    velocity: ReadVector3d,
    rotation: ReadVector3d,
    var configuration: FrustumConfiguration
) {
    val position = MutableVector3d(position)
    val velocity = MutableVector3d(velocity)
    val rotation = MutableVector3d(rotation)

    constructor(
        near: Double,
        far: Double,
        angle: Double = 0.0,
        ratio: Double = 1.0
    ) : this(
        Vector3d.ZERO, Vector3d.ZERO, Vector3d.ZERO,
        FrustumConfiguration(near, far, angle, ratio)
    )

    fun toFrustum(): Frustum = Frustum(
        position = position.now(),
        lookAt = Vector3d(
            position.x + cos(yaw.toRad()) * cos(pitch.toRad()),
            position.y + sin(yaw.toRad()) * cos(pitch.toRad()),
            position.z + sin(pitch.toRad())
        ),
        up = Vector3d(0.0, 0.0, 1.0),
        configuration = configuration
    )
}

inline var Camera.pitch: Double
    get() = rotation.x
    set(value) {
        rotation.x = value
    }

inline var Camera.tilt: Double
    get() = rotation.y
    set(value) {
        rotation.y = value
    }

inline var Camera.yaw: Double
    get() = rotation.z
    set(value) {
        rotation.z = value
    }

inline var Camera.near: Double
    get() = configuration.near
    set(value) {
        configuration = configuration.copy(near = value)
    }

inline var Camera.far: Double
    get() = configuration.far
    set(value) {
        configuration = configuration.copy(far = value)
    }

inline var Camera.angle: Double
    get() = configuration.angle
    set(value) {
        configuration = configuration.copy(angle = value)
    }

inline var Camera.ratio: Double
    get() = configuration.ratio
    set(value) {
        configuration = configuration.copy(ratio = value)
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Camera.setRange(
    near: Double,
    far: Double
) {
    configuration = configuration.copy(
        near = near,
        far = far
    )
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun Camera.setPerspective(
    ratio: Double,
    angle: Double
) {
    configuration = configuration.copy(
        angle = angle,
        ratio = ratio
    )
}
