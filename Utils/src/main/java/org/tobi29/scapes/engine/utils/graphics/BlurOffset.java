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

import org.tobi29.scapes.engine.utils.math.FastMath;

/**
 * Utility class to create blur offsets for blur shaders
 */
public final class BlurOffset {
    private BlurOffset() {
    }

    /**
     * Creates an array to give the offset for each sample
     *
     * @param samples   Amount of samples per pixel
     * @param magnitude Maximum offset of samples
     * @return An array with a length of {@code samples}
     */
    public static float[] gaussianBlurOffset(int samples, float magnitude) {
        magnitude /= samples;
        int offset = samples >> 1;
        float[] array = new float[samples];
        for (int sample = 0; sample < samples; sample++) {
            array[sample] = (sample - offset) * magnitude;
        }
        return array;
    }

    /**
     * Creates an array to give the weight for each sample
     *
     * @param samples Amount of samples per pixel
     * @param curve   Modifier for each weight value
     * @return An array with a length of {@code samples}
     */
    public static float[] gaussianBlurWeight(int samples, GaussianCurve curve) {
        double scale = 1.0 / samples;
        double magnitude = 0.0;
        int offset = samples >> 1;
        float[] array = new float[samples];
        for (int sample = 0; sample < samples; sample++) {
            double weight = curve.sample(FastMath.abs(sample - offset) * scale);
            array[sample] = (float) weight;
            magnitude += weight;
        }
        for (int sample = 0; sample < samples; sample++) {
            array[sample] /= magnitude;
        }
        return array;
    }

    /**
     * Functional interface to modify blur weights
     */
    @FunctionalInterface
    public interface GaussianCurve {
        /**
         * Modifies blur weight
         *
         * @param sample The sample offset ranging from 0 to 1
         * @return The weight of the sample
         */
        double sample(double sample);
    }
}
