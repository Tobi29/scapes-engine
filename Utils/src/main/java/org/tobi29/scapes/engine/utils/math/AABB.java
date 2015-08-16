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

import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.util.Iterator;

public class AABB {
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public AABB(AABB aabb) {
        this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public AABB(double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    private static double offset(double min1, double min2, double max1,
            double max2, double minH1, double minH2, double maxH1, double maxH2,
            double minV1, double minV2, double maxV1, double maxV2, double base,
            double error) {
        if (maxH1 <= minH2 + error || minH1 >= maxH2 - error) {
            return base;
        }
        if (maxV1 <= minV2 + error || minV1 >= maxV2 - error) {
            return base;
        }
        if (base > 0 && max1 <= min2 + error) {
            double diff = min2 - max1;
            if (diff < base) {
                base = diff;
            }
        }
        if (base < 0 && min1 >= max2 - error) {
            double diff = max2 - min1;
            if (diff > base) {
                base = diff;
            }
        }
        return base;
    }

    public double moveOutX(Iterator<AABB> aabbs, double base) {
        while (aabbs.hasNext()) {
            base = moveOutX(aabbs.next(), base);
        }
        return base;
    }

    public double moveOutY(Iterator<AABB> aabbs, double base) {
        while (aabbs.hasNext()) {
            base = moveOutY(aabbs.next(), base);
        }
        return base;
    }

    public double moveOutZ(Iterator<AABB> aabbs, double base) {
        while (aabbs.hasNext()) {
            base = moveOutZ(aabbs.next(), base);
        }
        return base;
    }

    public AABB copy(AABB aabb) {
        minX = aabb.minX;
        minY = aabb.minY;
        minZ = aabb.minZ;
        maxX = aabb.maxX;
        maxY = aabb.maxY;
        maxZ = aabb.maxZ;
        return this;
    }

    public AABB add(double x, double y, double z) {
        minX += x;
        maxX += x;
        minY += y;
        maxY += y;
        minZ += z;
        maxZ += z;
        return this;
    }

    public AABB subtract(double x, double y, double z) {
        minX -= x;
        maxX -= x;
        minY -= y;
        maxY -= y;
        minZ -= z;
        maxZ -= z;
        return this;
    }

    public double getVertexNX(double normalx) {
        double p = minX;
        if (normalx < 0) {
            p = maxX;
        }
        return p;
    }

    public double getVertexNY(double normaly) {
        double p = minY;
        if (normaly < 0) {
            p = maxY;
        }
        return p;
    }

    public double getVertexNZ(double normalz) {
        double p = minZ;
        if (normalz < 0) {
            p = maxZ;
        }
        return p;
    }

    public double getVertexPX(double normalx) {
        double p = minX;
        if (normalx > 0) {
            p = maxX;
        }
        return p;
    }

    public double getVertexPY(double normaly) {
        double p = minY;
        if (normaly > 0) {
            p = maxY;
        }
        return p;
    }

    public double getVertexPZ(double normalz) {
        double p = minZ;
        if (normalz > 0) {
            p = maxZ;
        }
        return p;
    }

    public AABB grow(double x, double y, double z) {
        minX -= x;
        minY -= y;
        minZ -= z;
        maxX += x;
        maxY += y;
        maxZ += z;
        return this;
    }

    public AABB grow(double x1, double y1, double z1, double x2, double y2,
            double z2) {
        minX -= x1;
        minY -= y1;
        minZ -= z1;
        maxX += x2;
        maxY += y2;
        maxZ += z2;
        return this;
    }

    public AABB scale(double value) {
        return scale(value, value, value);
    }

    public AABB scale(double x, double y, double z) {
        minX *= x;
        minY *= y;
        minZ *= z;
        maxX *= x;
        maxY *= y;
        maxZ *= z;
        return this;
    }

    public boolean inside(Vector3 check) {
        return inside(check.doubleX(), check.doubleY(), check.doubleZ());
    }

    public boolean inside(double x, double y, double z) {
        return !(maxX < x || minX > x) && !(maxY < y || minY > y) &&
                !(maxZ < z || minZ > z);
    }

    public double moveOutX(AABB check, double base) {
        return moveOutX(check, base, 0.00001);
    }

    public double moveOutX(AABB check, double base, double error) {
        return offset(minX, check.minX, maxX, check.maxX, minY, check.minY,
                maxY, check.maxY, minZ, check.minZ, maxZ, check.maxZ, base,
                error);
    }

    public double moveOutY(AABB check, double base) {
        return moveOutY(check, base, 0.00001);
    }

    public double moveOutY(AABB check, double base, double error) {
        return offset(minY, check.minY, maxY, check.maxY, minX, check.minX,
                maxX, check.maxX, minZ, check.minZ, maxZ, check.maxZ, base,
                error);
    }

    public double moveOutZ(AABB check, double base) {
        return moveOutZ(check, base, 0.00001);
    }

    public double moveOutZ(AABB check, double base, double error) {
        return offset(minZ, check.minZ, maxZ, check.maxZ, minX, check.minX,
                maxX, check.maxX, minY, check.minY, maxY, check.maxY, base,
                error);
    }

    public boolean overlay(AABB check) {
        return !(maxX <= check.minX || minX >= check.maxX) &&
                !(maxY <= check.minY || minY >= check.maxY) &&
                !(maxZ <= check.minZ || minZ >= check.maxZ);
    }
}
