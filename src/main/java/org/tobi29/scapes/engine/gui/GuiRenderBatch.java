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

package org.tobi29.scapes.engine.gui;

import java8.util.concurrent.ConcurrentMaps;
import org.tobi29.scapes.engine.graphics.Mesh;
import org.tobi29.scapes.engine.graphics.Model;
import org.tobi29.scapes.engine.graphics.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class GuiRenderBatch {
    protected final ConcurrentMap<Integer, Map<Texture, Mesh>> meshes =
            new ConcurrentSkipListMap<>();
    protected Mesh currentMesh;
    protected int offset, count;
    protected Vector2 pixelSize;

    public GuiRenderBatch(Vector2 pixelSize) {
        this.pixelSize = pixelSize;
    }

    public Vector3 vector(float x, float y) {
        return new Vector3f(x, y, 0.0f);
    }

    public void texture(Texture texture, int priority) {
        int layer = priority + offset;
        Map<Texture, Mesh> map = ConcurrentMaps
                .computeIfAbsent(meshes, layer, i -> new HashMap<>());
        currentMesh = map.get(texture);
        if (currentMesh == null) {
            currentMesh = new Mesh(true);
            map.put(texture, currentMesh);
            count++;
        }
    }

    public void offset(int offset) {
        this.offset += offset;
    }

    public void setPixelSize(Vector2 pixelSize) {
        this.pixelSize = pixelSize;
    }

    public Vector2 pixelSize() {
        return pixelSize;
    }

    public Mesh mesh() {
        return currentMesh;
    }

    public List<Pair<Model, Texture>> finish() {
        List<Pair<Model, Texture>> meshes = new ArrayList<>(count);
        Streams.forEach(this.meshes.values(),
                map -> Streams.forEach(map.entrySet(), entry -> {
                    Texture texture = entry.getKey();
                    meshes.add(new Pair<>(
                            entry.getValue().finish(texture.engine()),
                            texture));
                }));
        this.meshes.clear();
        count = 0;
        return meshes;
    }
}
