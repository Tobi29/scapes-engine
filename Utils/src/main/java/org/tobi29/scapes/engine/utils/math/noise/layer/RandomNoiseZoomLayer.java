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

public class RandomNoiseZoomLayer extends RandomNoiseLayer {
    private final RandomNoiseLayer parent;
    private final double zoomFactor;

    public RandomNoiseZoomLayer(RandomNoiseLayer parent, float zoomFactor) {
        this.parent = parent;
        this.zoomFactor = zoomFactor;
    }

    @Override
    public int getInt(int x, int y) {
        return parent.getInt((int) Math.floor(x / zoomFactor),
                (int) Math.floor(y / zoomFactor));
    }
}
