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

public abstract class ValueNoise {
    public double noiseOctave(double x, double y, int octaves, double frequency,
            double amplitude) {
        double out = 0, cFrequency = 1, cAmplitude = 1, normal = 0;
        for (int i = 0; i < octaves;
                i++, cFrequency *= frequency, cAmplitude *= amplitude) {
            out += noise(x * cFrequency, y * cFrequency) * cAmplitude;
            normal += cAmplitude;
        }
        return out / normal;
    }

    public abstract double noise(double x, double y);

    public double noiseOctave(double x, double y, double z, int octaves,
            double frequency, double amplitude) {
        double out = 0, cFrequency = 1, cAmplitude = 1, normal = 0;
        for (int i = 0; i < octaves;
                i++, cFrequency *= frequency, cAmplitude *= amplitude) {
            out += noise(x * cFrequency, y * cFrequency, z * cFrequency) *
                    cAmplitude;
            normal += cAmplitude;
        }
        return out / normal;
    }

    public abstract double noise(double x, double y, double z);
}
