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

public class RandomNoiseRandomLayer extends RandomNoiseLayer {
    private final int maxRandom;
    private final int[] perm = new int[512];

    public RandomNoiseRandomLayer(long seed, int maxRandom) {
        this.maxRandom = maxRandom;
        Random random = new Random(seed);
        int v;
        for (int i = 0; i < 256; i++) {
            v = random.nextInt(256);
            perm[i] = v;
            perm[i + 256] = v;
        }
    }

    @Override
    public int getInt(int x, int y) {
        x = x & 255;
        y = y & 255;
        return perm[x + perm[y + perm[x + y & 255]]] % maxRandom;
    }
}
