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

public class Plane {
    public double normalx, normaly, normalz, p2x, p2y, p2z;

    public Plane() {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public Plane(double p1x, double p1y, double p1z, double p2x, double p2y,
            double p2z, double p3x, double p3y, double p3z) {
        set3Points(p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z);
    }

    public void set3Points(double p1x, double p1y, double p1z, double p2x,
            double p2y, double p2z, double p3x, double p3y, double p3z) {
        double e1x = p1x - p2x;
        double e1y = p1y - p2y;
        double e1z = p1z - p2z;
        double e2x = p3x - p2x;
        double e2y = p3y - p2y;
        double e2z = p3z - p2z;
        normalx = e2y * e1z - e2z * e1y;
        normaly = e2z * e1x - e2x * e1z;
        normalz = e2x * e1y - e2y * e1x;
        double l = FastMath.sqrt(normalx * normalx + normaly * normaly +
                normalz * normalz);
        if (FastMath.abs(l) > Float.MIN_NORMAL) {
            normalx /= l;
            normaly /= l;
            normalz /= l;
        }
        this.p2x = p2x;
        this.p2y = p2y;
        this.p2z = p2z;
    }

    public double distance(double x, double y, double z) {
        return -normalx * p2x - normaly * p2y - normalz * p2z +
                normalx * x + normaly * y + normalz * z;
    }
}
