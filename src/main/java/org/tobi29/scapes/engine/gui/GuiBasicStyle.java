package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.Mesh;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.opengl.texture.TextureManager;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

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
    public Pair<VAO, Texture> pane(Vector2 size) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.2f);
        mesh.addRectangle(0.0f, 0.0f, size.floatX(), size.floatY(), 0.0f, 0.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> button(Vector2 size, boolean hover) {
        double a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.2f);
        mesh.addRectangle(0.0f, 0.0f, size.floatX(), size.floatY(), 0.0f, 0.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, (float) a);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> border(Vector2 size) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.2f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> slider(Vector2 size, boolean horizontal,
            double value, double sliderSize, boolean hover) {
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.2f);
        mesh.addRectangle(0.0f, 0.0f, size.floatX(), size.floatY(), 0.0f, 0.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.2f);
        double a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        if (horizontal) {
            value = value * (size.doubleX() - sliderSize);
            mesh.addRectangle((float) value, 0.0f, (float) (value + sliderSize),
                    size.floatY(), 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, (float) a);
        } else {
            value = value * (size.doubleY() - sliderSize);
            mesh.addRectangle(0.0f, (float) value, size.floatX(),
                    (float) (value + sliderSize), 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, (float) a);
        }
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> separator(Vector2 size) {
        float halfHeight = (float) (size.doubleY() * 0.5);
        Mesh mesh = new Mesh(true);
        GuiUtils.renderShadow(mesh, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.1f);
        mesh.addRectangle(0.0f, 0.0f, size.floatX(), halfHeight, 0.0f, 0.0f,
                0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.3f);
        mesh.addRectangle(0.0f, halfHeight, size.floatX(), size.floatY(), 0.0f,
                0.0f, 0.5f, 1.0f, 1.0f, 0.2f, 0.2f, 0.2f, 0.3f);
        Texture texture = textures.empty();
        return new Pair<>(mesh.finish(), texture);
    }

    @Override
    public Pair<VAO, Texture> widget(Vector2 size) {
        return pane(size);
    }

    @Override
    public Pair<VAO, Texture> widgetTitle(Vector2 size) {
        return pane(size);
    }
}
