package org.tobi29.scapes.engine.gui;

import java8.util.concurrent.ConcurrentMaps;
import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
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

    public Mesh mesh() {
        return currentMesh;
    }

    public List<Pair<VAO, Texture>> finish() {
        List<Pair<VAO, Texture>> meshes = new ArrayList<>(count);
        Streams.forEach(this.meshes.values(), map -> {
            Streams.forEach(map.entrySet(), entry -> {
                Texture texture = entry.getKey();
                meshes.add(new Pair<>(entry.getValue().finish(texture.engine()),
                        texture));
            });
        });
        this.meshes.clear();
        count = 0;
        return meshes;
    }
}
