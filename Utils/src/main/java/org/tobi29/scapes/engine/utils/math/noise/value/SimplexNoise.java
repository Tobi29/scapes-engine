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

package org.tobi29.scapes.engine.utils.math.noise.value;

import org.tobi29.scapes.engine.utils.math.FastMath;

import java.util.Random;

public class SimplexNoise extends ValueNoise {
    private static final double F2 = 0.5 * (1.7320508075688772 - 1.0);
    private static final double G2 = (3.0 - 1.7320508075688772) / 6.0;
    private static final double G22 = (3.0 - 1.7320508075688772) / 3.0;
    private static final double F3 = 1.0 / 3.0;
    private static final double G3 = 1.0 / 6.0;
    private static final double G32 = 1.0 / 3.0;
    private static final double G33 = 0.5;
    private static final int[][] GRAD_3 =
            {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1},
                    {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1},
                    {0, 1, -1}, {0, -1, -1}};
    private final int[] perm = new int[512];

    public SimplexNoise(long seed) {
        this(new Random(seed));
    }

    public SimplexNoise(Random random) {
        int v;
        for (int i = 0; i < 256; i++) {
            v = random.nextInt(256);
            perm[i] = v;
            perm[i + 256] = v;
        }
    }

    @Override
    public double noise(double x, double y) {
        double s = (x + y) * F2;
        int i = FastMath.floor(x + s);
        int j = FastMath.floor(y + s);
        double t = (i + j) * G2;
        double x0 = x - i + t;
        double y0 = y - j + t;
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + G22;
        double y2 = y0 - 1.0 + G22;
        int i2 = i & 255;
        int j2 = j & 255;
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        double n0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            int gi0 = perm[i2 + perm[j2]] % 12;
            n0 = t0 * t0 * FastMath.dot(GRAD_3[gi0], x0, y0);
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        double n1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            int gi1 = perm[i2 + i1 + perm[j2 + j1]] % 12;
            n1 = t1 * t1 * FastMath.dot(GRAD_3[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        double n2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            int gi2 = perm[i2 + 1 + perm[j2 + 1]] % 12;
            n2 = t2 * t2 * FastMath.dot(GRAD_3[gi2], x2, y2);
        }
        return 70.0 * (n0 + n1 + n2);
    }

    @Override
    public double noise(double x, double y, double z) {
        double n0, n1, n2, n3;
        double s = (x + y + z) * F3;
        int i = FastMath.floor(x + s);
        int j = FastMath.floor(y + s);
        int k = FastMath.floor(z + s);
        double t = (i + j + k) * G3;
        double xx0 = i - t;
        double yy0 = j - t;
        double zz0 = k - t;
        double x0 = x - xx0;
        double y0 = y - yy0;
        double z0 = z - zz0;
        int i1, j1, k1;
        int i2, j2, k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }
        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + G32;
        double y2 = y0 - j2 + G32;
        double z2 = z0 - k2 + G32;
        double x3 = x0 - 1.0 + G33;
        double y3 = y0 - 1.0 + G33;
        double z3 = z0 - 1.0 + G33;
        int i3 = i & 255;
        int j3 = j & 255;
        int k3 = k & 255;
        double t0 = 0.5 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            int gi0 = perm[i3 + perm[j3 + perm[k3]]] % 12;
            n0 = t0 * t0 * FastMath.dot(GRAD_3[gi0], x0, y0, z0);
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            int gi1 = perm[i3 + i1 + perm[j3 + j1 + perm[k3 + k1]]] % 12;
            n1 = t1 * t1 * FastMath.dot(GRAD_3[gi1], x1, y1, z1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            int gi2 = perm[i3 + i2 + perm[j3 + j2 + perm[k3 + k2]]] % 12;
            n2 = t2 * t2 * FastMath.dot(GRAD_3[gi2], x2, y2, z2);
        }
        double t3 = 0.5 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            int gi3 = perm[i3 + 1 + perm[j3 + 1 + perm[k3 + 1]]] % 12;
            n3 = t3 * t3 * FastMath.dot(GRAD_3[gi3], x3, y3, z3);
        }
        return 32.0 * (n0 + n1 + n2 + n3);
    }
}
