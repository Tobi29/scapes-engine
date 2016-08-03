/*
 * Copyright 2012-2016 Tobi29
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

public class RandomNoisePickLayer implements RandomNoiseLayer {
    private final RandomNoiseLayer parent;
    private final int[] pick;
    private final int[] drop;
    private final int stay;

    public RandomNoisePickLayer(RandomNoiseLayer parent, int[] pick, int[] drop,
            int stay) {
        if (pick.length > drop.length) {
            throw new IllegalArgumentException(
                    "Pick can't have more elements than drop!");
        }
        this.parent = parent;
        this.pick = pick;
        this.drop = drop;
        this.stay = stay;
    }

    @Override
    public int getInt(int x, int y) {
        int value = parent.getInt(x, y);
        for (int i = 0; i < pick.length; i++) {
            if (value == pick[i]) {
                return drop[i];
            }
        }
        return stay;
    }
}
