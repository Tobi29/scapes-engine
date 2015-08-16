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

public class RandomNoiseBorderLayer extends RandomNoiseLayer {
    private final RandomNoiseLayer parent;
    private final long seed;
    private final int outside, border, factor;
    private final Random random = new Random();

    public RandomNoiseBorderLayer(RandomNoiseLayer parent, long seed,
            int outside, int border, int factor) {
        this.parent = parent;
        this.seed = seed;
        this.outside = outside;
        this.border = border;
        this.factor = factor;
    }

    @Override
    public synchronized int getInt(int x, int y) {
        random.setSeed(y);
        random.setSeed((long) x + random.nextInt());
        random.setSeed(random.nextInt() + seed);
        int org = parent.getInt(x, y);
        if (org == outside) {
            return outside;
        }
        int value = parent.getInt(x - random.nextInt(factor),
                y - random.nextInt(factor));
        if (value == outside) {
            return border;
        }
        value = parent.getInt(x + random.nextInt(factor),
                y - random.nextInt(factor));
        if (value == outside) {
            return border;
        }
        value = parent.getInt(x - random.nextInt(factor),
                y + random.nextInt(factor));
        if (value == outside) {
            return border;
        }
        value = parent.getInt(x + random.nextInt(factor),
                y + random.nextInt(factor));
        if (value == outside) {
            return border;
        }
        return org;
    }
}
