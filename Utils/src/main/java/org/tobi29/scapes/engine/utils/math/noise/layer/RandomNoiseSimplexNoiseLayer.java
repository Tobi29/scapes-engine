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

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.noise.value.SimplexNoise;

public class RandomNoiseSimplexNoiseLayer extends RandomNoiseLayer {
    private final RandomNoiseLayer parent;
    private final SimplexNoise noise;
    private final double factor;

    public RandomNoiseSimplexNoiseLayer(RandomNoiseLayer parent, long seed,
            double factor) {
        this.parent = parent;
        noise = new SimplexNoise(seed);
        this.factor = factor;
    }

    @Override
    public synchronized int getInt(int x, int y) {
        return parent.getInt(FastMath.floor(
                        x + noise.noise(x / factor, y / factor) * factor),
                FastMath.floor(
                        y + noise.noise(x / factor, y / factor) * factor));
    }
}
