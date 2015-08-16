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

import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public class ColorUtil {
    private ColorUtil() {
    }

    public static Vector3 hsvToRGB(Vector3 color) {
        return hsvToRGB(color.doubleX(), color.doubleY(), color.doubleZ());
    }

    public static Vector3 hsvToRGB(double h, double s, double v) {
        int c = (int) (h * 6.0);
        c %= 6;
        if (c < 0) {
            c += 6;
        }
        double f = h * 6.0 - c;
        double p = v * (1.0 - s);
        double q = v * (1.0 - f * s);
        double t = v * (1.0 - (1.0 - f) * s);
        switch (c) {
            case 0:
                return new Vector3d(v, t, p);
            case 1:
                return new Vector3d(q, v, p);
            case 2:
                return new Vector3d(p, v, t);
            case 3:
                return new Vector3d(p, q, v);
            case 4:
                return new Vector3d(t, p, v);
            case 5:
                return new Vector3d(v, p, q);
            default:
                throw new IllegalArgumentException("Invalid hue: " + h);
        }
    }
}
