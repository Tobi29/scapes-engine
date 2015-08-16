/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.utils.graphics;

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.Frustum;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector3;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector3d;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

public class Cam {
    public final Frustum frustum = new Frustum();
    public final MutableVector3 position = new MutableVector3d();
    public final MutableVector3 velocity = new MutableVector3d();
    public float pitch, yaw, tilt, near, far, fov;

    public Cam(float near, float far) {
        this.near = near;
        this.far = far;
    }

    public void setRange(float near, float far) {
        this.near = near;
        this.far = far;
    }

    public void setPerspective(float ratio, float fov) {
        this.fov = fov;
        frustum.setPerspective(fov, ratio, near, far);
    }

    public void setView(float pitch, float yaw, float tilt) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.tilt = tilt;
        double lookX = position.doubleX() +
                FastMath.cosTable(yaw * FastMath.PI / 180) *
                        FastMath.cosTable(pitch * FastMath.PI / 180);
        double lookY = position.doubleY() +
                FastMath.sinTable(yaw * FastMath.PI / 180) *
                        FastMath.cosTable(pitch * FastMath.PI / 180);
        double lookZ = position.doubleZ() +
                FastMath.sinTable(pitch * FastMath.PI / 180);
        frustum.setView(position.doubleX(), position.doubleY(),
                position.doubleZ(), lookX, lookY, lookZ, 0, 0, 1);
    }

    public void setView(Vector3 position, Vector3 velocity, float pitch,
            float yaw, float tilt) {
        this.position.set(position);
        this.velocity.set(velocity);
        setView(pitch, yaw, tilt);
    }
}
