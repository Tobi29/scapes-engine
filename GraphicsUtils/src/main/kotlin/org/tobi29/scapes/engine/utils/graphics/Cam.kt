/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.utils.graphics

import org.tobi29.scapes.engine.utils.math.Frustum
import org.tobi29.scapes.engine.utils.math.cos
import org.tobi29.scapes.engine.utils.math.sin
import org.tobi29.scapes.engine.utils.math.toRad
import org.tobi29.scapes.engine.utils.math.vector.MutableVector3d
import org.tobi29.scapes.engine.utils.math.vector.Vector3d

class Cam(var near: Float, var far: Float) {
    val frustum = Frustum()
    val position = MutableVector3d()
    val velocity = MutableVector3d()
    var pitch: Float = 0.toFloat()
    var yaw: Float = 0.toFloat()
    var tilt: Float = 0.toFloat()
    var fov: Float = 0.toFloat()

    fun setRange(near: Float,
                 far: Float) {
        this.near = near
        this.far = far
    }

    fun setPerspective(ratio: Float,
                       fov: Float) {
        this.fov = fov
        frustum.setPerspective(fov.toDouble(), ratio.toDouble(),
                near.toDouble(), far.toDouble())
    }

    fun setView(pitch: Float,
                yaw: Float,
                tilt: Float) {
        this.pitch = pitch
        this.yaw = yaw
        this.tilt = tilt
        val lookX = position.doubleX() + cos(yaw.toRad()) * cos(pitch.toRad())
        val lookY = position.doubleY() + sin(yaw.toRad()) * cos(pitch.toRad())
        val lookZ = position.doubleZ() + sin(pitch.toRad())
        frustum.setView(position.doubleX(), position.doubleY(),
                position.doubleZ(), lookX, lookY, lookZ, 0.0, 0.0, 1.0)
    }

    fun setView(position: Vector3d,
                velocity: Vector3d,
                pitch: Float,
                yaw: Float,
                tilt: Float) {
        this.position.set(position)
        this.velocity.set(velocity)
        setView(pitch, yaw, tilt)
    }
}
