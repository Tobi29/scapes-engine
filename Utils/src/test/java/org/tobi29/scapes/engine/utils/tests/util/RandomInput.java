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

package org.tobi29.scapes.engine.utils.tests.util;

import java.util.Random;

public final class RandomInput {
    private RandomInput() {
    }

    public static byte[][] createRandomArrays(int amount, int sizeBits) {
        Random random = new Random(0);
        byte[][] arrays = new byte[amount][];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = new byte[i << sizeBits];
            random.nextBytes(arrays[i]);
        }
        return arrays;
    }
}
