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

package org.tobi29.scapes.engine.utils.math;

public class PointerPane {
    public final AABB aabb;
    public Face face;
    public int x, y, z;

    public PointerPane() {
        this(new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), Face.NONE, 0, 0, 0);
    }

    public PointerPane(AABB aabb, Face face, int x, int y, int z) {
        this.aabb = aabb;
        this.face = face;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(AABB aabb, Face face, int x, int y, int z) {
        set(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ,
                face, x, y, z);
    }

    public void set(double minX, double minY, double minZ, double maxX,
            double maxY, double maxZ, Face face, int x, int y, int z) {
        aabb.minX = minX;
        aabb.minY = minY;
        aabb.minZ = minZ;
        aabb.maxX = maxX;
        aabb.maxY = maxY;
        aabb.maxZ = maxZ;
        this.face = face;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
