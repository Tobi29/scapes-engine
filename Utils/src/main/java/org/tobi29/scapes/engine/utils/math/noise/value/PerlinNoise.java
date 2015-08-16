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

public class PerlinNoise extends ValueNoise {
    private static final int[][] GRAD_3 =
            {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1},
                    {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1},
                    {0, 1, -1}, {0, -1, -1}};
    private final int[] perm = new int[512];

    public PerlinNoise(long seed) {
        this(new Random(seed));
    }

    public PerlinNoise(Random random) {
        int v;
        for (int i = 0; i < 256; i++) {
            v = random.nextInt(256);
            perm[i] = v;
            perm[i + 256] = v;
        }
    }

    @Override
    public double noise(double x, double y) {
        int xx = FastMath.floor(x);
        int yy = FastMath.floor(y);
        x = x - xx;
        y = y - yy;
        xx = xx & 255;
        yy = yy & 255;
        int gi000 = perm[xx + perm[yy]] % 12;
        int gi010 = perm[xx + perm[yy + 1]] % 12;
        int gi100 = perm[xx + 1 + perm[yy]] % 12;
        int gi110 = perm[xx + 1 + perm[yy + 1]] % 12;
        double n000 = FastMath.dot(GRAD_3[gi000], x, y);
        double n100 = FastMath.dot(GRAD_3[gi100], x - 1, y);
        double n010 = FastMath.dot(GRAD_3[gi010], x, y - 1);
        double n110 = FastMath.dot(GRAD_3[gi110], x - 1, y - 1);
        double u = FastMath.fade(x);
        double v = FastMath.fade(y);
        double nx00 = FastMath.mix(n000, n100, u);
        double nx10 = FastMath.mix(n010, n110, u);
        return FastMath.mix(nx00, nx10, v);
    }

    @Override
    public double noise(double x, double y, double z) {
        int xx = FastMath.floor(x);
        int yy = FastMath.floor(y);
        int zz = FastMath.floor(z);
        x = x - xx;
        y = y - yy;
        z = z - zz;
        xx = xx & 255;
        yy = yy & 255;
        zz = zz & 255;
        int gi000 = perm[xx + perm[yy + perm[zz]]] % 12;
        int gi001 = perm[xx + perm[yy + perm[zz + 1]]] % 12;
        int gi010 = perm[xx + perm[yy + 1 + perm[zz]]] % 12;
        int gi011 = perm[xx + perm[yy + 1 + perm[zz + 1]]] % 12;
        int gi100 = perm[xx + 1 + perm[yy + perm[zz]]] % 12;
        int gi101 = perm[xx + 1 + perm[yy + perm[zz + 1]]] % 12;
        int gi110 = perm[xx + 1 + perm[yy + 1 + perm[zz]]] % 12;
        int gi111 = perm[xx + 1 + perm[yy + 1 + perm[zz + 1]]] % 12;
        double n000 = FastMath.dot(GRAD_3[gi000], x, y, z);
        double n100 = FastMath.dot(GRAD_3[gi100], x - 1, y, z);
        double n010 = FastMath.dot(GRAD_3[gi010], x, y - 1, z);
        double n110 = FastMath.dot(GRAD_3[gi110], x - 1, y - 1, z);
        double n001 = FastMath.dot(GRAD_3[gi001], x, y, z - 1);
        double n101 = FastMath.dot(GRAD_3[gi101], x - 1, y, z - 1);
        double n011 = FastMath.dot(GRAD_3[gi011], x, y - 1, z - 1);
        double n111 = FastMath.dot(GRAD_3[gi111], x - 1, y - 1, z - 1);
        double u = FastMath.fade(x);
        double v = FastMath.fade(y);
        double w = FastMath.fade(z);
        double nx00 = FastMath.mix(n000, n100, u);
        double nx01 = FastMath.mix(n001, n101, u);
        double nx10 = FastMath.mix(n010, n110, u);
        double nx11 = FastMath.mix(n011, n111, u);
        double nxy0 = FastMath.mix(nx00, nx10, v);
        double nxy1 = FastMath.mix(nx01, nx11, v);
        return FastMath.mix(nxy0, nxy1, w);
    }
}
