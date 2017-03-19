/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.generation.value

interface ValueNoise {
    fun noiseOctave(x: Double,
                    y: Double,
                    octaves: Int,
                    frequency: Double,
                    amplitude: Double): Double {
        var out = 0.0
        var cFrequency = 1.0
        var cAmplitude = 1.0
        var normal = 0.0
        var i = 0
        while (i < octaves) {
            out += noise(x * cFrequency, y * cFrequency) * cAmplitude
            normal += cAmplitude
            i++
            cFrequency *= frequency
            cAmplitude *= amplitude
        }
        return out / normal
    }

    fun noise(x: Double,
              y: Double): Double

    fun noiseOctave(x: Double,
                    y: Double,
                    z: Double,
                    octaves: Int,
                    frequency: Double,
                    amplitude: Double): Double {
        var out = 0.0
        var cFrequency = 1.0
        var cAmplitude = 1.0
        var normal = 0.0
        var i = 0
        while (i < octaves) {
            out += noise(x * cFrequency, y * cFrequency,
                    z * cFrequency) * cAmplitude
            normal += cAmplitude
            i++
            cFrequency *= frequency
            cAmplitude *= amplitude
        }
        return out / normal
    }

    fun noise(x: Double,
              y: Double,
              z: Double): Double
}
