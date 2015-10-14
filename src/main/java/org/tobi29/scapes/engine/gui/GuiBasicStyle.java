package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.opengl.texture.TextureManager;
import org.tobi29.scapes.engine.utils.Pair;

public class GuiBasicStyle implements GuiStyle {
    private final FontRenderer font;
    private final TextureManager textures;

    public GuiBasicStyle(FontRenderer font, TextureManager textures) {
        this.font = font;
        this.textures = textures;
    }

    @Override
    public FontRenderer font() {
        return font;
    }

    @Override
    public Pair<VAO, Texture> pane(float width, float height) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addRectangle(0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> button(float width, float height, boolean hover) {
        float a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addRectangle(0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, a);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> border(float width, float height) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> slider(float width, float height,
            boolean horizontal, float value, float size, boolean hover) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addRectangle(0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, 0.2f);
        float a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        if (horizontal) {
            value = value * (width - size);
            mesh.addRectangle(value, 0.0f, value + size, height, 0.0f, 0.0f,
                    0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, a);
        } else {
            value = value * (height - size);
            mesh.addRectangle(0.0f, value, width, value + size, 0.0f, 0.0f,
                    0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, a);
        }
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> separator(float width, float height) {
        float halfHeight = height * 0.5f;
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.1f);
        mesh.addRectangle(0.0f, 0.0f, width, halfHeight, 0.0f, 0.0f, 0.0f, 1.0f,
                0.5f, 0.0f, 0.0f, 0.0f, 0.3f);
        mesh.addRectangle(0.0f, halfHeight, width, height, 0.0f, 0.0f, 0.5f,
                1.0f, 1.0f, 0.2f, 0.2f, 0.2f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> widget(float width, float height) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addRectangle(0.0f, -16.0f, width, height, 0.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> widgetTitle(float width, float height) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, width, height, 0.2f);
        mesh.addRectangle(0.0f, 0.0f, width, height, 0.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }
}
