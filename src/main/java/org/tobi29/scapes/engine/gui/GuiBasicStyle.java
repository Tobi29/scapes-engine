package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.texture.TextureManager;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiBasicStyle implements GuiStyle {
    private final ScapesEngine engine;
    private final FontRenderer font;
    private final TextureManager textures;

    public GuiBasicStyle(ScapesEngine engine, FontRenderer font,
            TextureManager textures) {
        this.engine = engine;
        this.font = font;
        this.textures = textures;
    }

    @Override
    public ScapesEngine engine() {
        return engine;
    }

    @Override
    public FontRenderer font() {
        return font;
    }

    @Override
    public void pane(GuiRenderer renderer, Vector2 size) {
        renderer.texture(textures.empty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.3f);
    }

    @Override
    public void button(GuiRenderer renderer, Vector2 size, boolean hover) {
        renderer.texture(textures.empty(), 0);
        double a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, (float) a);
    }

    @Override
    public void border(GuiRenderer renderer, Vector2 size) {
        renderer.texture(textures.empty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
    }

    @Override
    public void slider(GuiRenderer renderer, Vector2 size, boolean horizontal,
            double value, double sliderSize, boolean hover) {
        renderer.texture(textures.empty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        double a;
        if (hover) {
            a = 0.8f;
        } else {
            a = 0.6f;
        }
        if (horizontal) {
            value = value * (size.doubleX() - sliderSize);
            GuiUtils.rectangle(renderer, (float) value, 0.0f,
                    (float) (value + sliderSize), size.floatY(), 0.0f, 0.0f,
                    0.0f, (float) a);
        } else {
            value = value * (size.doubleY() - sliderSize);
            GuiUtils.rectangle(renderer, 0.0f, (float) value, size.floatX(),
                    (float) (value + sliderSize), 0.0f, 0.0f, 0.0f, (float) a);
        }
    }

    @Override
    public void separator(GuiRenderer renderer, Vector2 size) {
        renderer.texture(textures.empty(), 0);
        GuiUtils.shadow(renderer, 0.0f, 0.0f, size.floatX(), size.floatY(),
                0.0f, 0.0f, 0.0f, 0.2f);
        GuiUtils.rectangle(renderer, 0.0f, 0.0f, size.floatX(),
                size.floatY() * 0.5f, 0.0f, 0.0f, 0.0f, 0.3f);
        GuiUtils.rectangle(renderer, 0.0f, size.floatY() * 0.5f, size.floatX(),
                size.floatY(), 0.2f, 0.2f, 0.2f, 0.3f);
    }

    @Override
    public void widget(GuiRenderer renderer, Vector2 size) {
        pane(renderer, size);
    }

    @Override
    public void widgetTitle(GuiRenderer renderer, Vector2 size) {
        pane(renderer, size);
    }
}
