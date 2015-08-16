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

package org.tobi29.scapes.engine.utils.math.noise.layer;

import java.util.Random;

public class RandomNoiseNoiseLayer extends RandomNoiseLayer {
    private final RandomNoiseLayer parent;
    private final int factor;
    private final int[] perm = new int[512];

    public RandomNoiseNoiseLayer(RandomNoiseLayer parent, long seed,
            int factor) {
        this.parent = parent;
        this.factor = factor;
        Random random = new Random(seed);
        int v;
        for (int i = 0; i < 256; i++) {
            v = random.nextInt(256);
            perm[i] = v;
            perm[i + 256] = v;
        }
    }

    @Override
    public synchronized int getInt(int x, int y) {
        int xx = x & 255;
        int yy = y & 255;
        int dx = perm[xx + perm[xx + yy & 255 + perm[yy]]] % factor;
        int dy = perm[xx + yy & 255 + perm[xx + perm[yy]]] % factor;
        switch (perm[xx + perm[yy + perm[xx + yy & 255]]] % 4) {
            case 1:
                return parent.getInt(x - dx, y - dy);
            case 2:
                return parent.getInt(x + dx, y - dy);
            case 3:
                return parent.getInt(x - dx, y + dy);
            default:
                return parent.getInt(x + dx, y + dy);
        }
    }
}
