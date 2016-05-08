package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GuiRenderBatch {
    protected final List<Pair<VAO, Texture>> meshes = new ArrayList<>();
    protected Mesh currentMesh;
    protected Texture currentTexture;

    public Vector3 vector(float x, float y) {
        return new Vector3f(x, y, 0.0f);
    }

    public void texture(Texture texture) {
        if (texture == currentTexture) {
            return;
        }
        if (currentMesh != null) {
            meshes.add(new Pair<>(currentMesh.finish(currentTexture.engine()),
                    currentTexture));
        }
        currentTexture = texture;
        currentMesh = new Mesh(true);
    }

    public Mesh mesh() {
        return currentMesh;
    }

    public List<Pair<VAO, Texture>> finish() {
        List<Pair<VAO, Texture>> meshes =
                new ArrayList<>(this.meshes.size() + 1);
        meshes.addAll(this.meshes);
        if (currentMesh != null) {
            meshes.add(new Pair<>(currentMesh.finish(currentTexture.engine()),
                    currentTexture));
            currentMesh = null;
        }
        currentTexture = null;
        this.meshes.clear();
        return meshes;
    }
}
