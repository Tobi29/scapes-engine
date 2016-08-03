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

public class RandomNoiseSmoothLayer implements RandomNoiseLayer {
    private final RandomNoiseLayer parent;

    public RandomNoiseSmoothLayer(RandomNoiseLayer parent) {
        this.parent = parent;
    }

    @Override
    public int getInt(int x, int y) {
        int[] values = new int[9];
        int[] weights = new int[9];
        values[0] = parent.getInt(x - 1, y - 1);
        values[1] = parent.getInt(x, y - 1);
        values[2] = parent.getInt(x + 1, y - 1);
        values[3] = parent.getInt(x - 1, y);
        values[4] = parent.getInt(x, y);
        values[5] = parent.getInt(x + 1, y);
        values[6] = parent.getInt(x - 1, y + 1);
        values[7] = parent.getInt(x, y + 1);
        values[8] = parent.getInt(x + 1, y + 1);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (values[i] == values[j]) {
                    weights[i]++;
                }
            }
        }
        int value = 0;
        int weight = 0;
        for (int i = 0; i < 9; i++) {
            if (weights[i] > weight) {
                value = values[i];
                weight = weights[i];
            }
        }
        return value;
    }
}
