package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.BlendingMode;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

import java.util.List;

public class GuiComponentEditableText extends GuiComponentHeavy {
    protected final GuiController.TextFieldData data =
            new GuiController.TextFieldData();
    protected final int maxLength;
    protected final float r, g, b, a;
    protected boolean active, focused;
    protected List<Pair<VAO, Texture>> vaoCursor, vaoSelection;
    protected GuiComponentText.TextFilter textFilter = str -> str;

    public GuiComponentEditableText(GuiLayoutData parent, String text) {
        this(parent, text, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public GuiComponentEditableText(GuiLayoutData parent, String text, float r,
            float g, float b, float a) {
        this(parent, text, Integer.MAX_VALUE, r, g, b, a);
    }

    public GuiComponentEditableText(GuiLayoutData parent, String text,
            int maxLength) {
        this(parent, text, maxLength, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public GuiComponentEditableText(GuiLayoutData parent, String text,
            int maxLength, float r, float g, float b, float a) {
        super(parent);
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        data.text.append(text);
        this.maxLength = maxLength;
        data.cursor = data.text.length();
        dirty();
    }

    public GuiController.TextFieldData data() {
        return data;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String text() {
        return data.text.toString();
    }

    public void setText(String text) {
        if (!data.text.toString().equals(text)) {
            data.text.setLength(0);
            data.text.append(text);
            dirty();
        }
    }

    public void setTextFilter(GuiComponentText.TextFilter textFilter) {
        this.textFilter = textFilter;
        dirty();
    }

    @Override
    protected void updateMesh(GuiRenderer renderer, Vector2 size) {
        if (data == null) {
            return;
        }
        FontRenderer font = gui.style().font();
        String text = data.text.toString();
        font.render(FontRenderer.to(renderer, r, g, b, a),
                textFilter.filter(text), size.floatY(), size.floatX());
        int cursor = data.cursor;
        int selectionStart = data.selectionStart;
        int selectionEnd = data.selectionEnd;
        cursor = FastMath.clamp(cursor, 0, text.length());
        selectionStart = FastMath.clamp(selectionStart, 0, text.length());
        selectionEnd = FastMath.clamp(selectionEnd, 0, text.length());
        GuiRenderBatch batch = new GuiRenderBatch();
        font.render(FontRenderer.to(batch, 0.0f - size.floatY() * 0.1f,
                0.0f - size.floatY() * 0.1f, 1.0f, 1.0f, 1.0f, 1.0f),
                text.substring(0, cursor) + '|', size.floatY(),
                size.floatY() * 1.2f, size.floatY(), Float.MAX_VALUE, cursor,
                cursor + 1);
        vaoCursor = batch.finish();
        font.render(FontRenderer
                        .to(batch, 0.0f, 0.0f, true, 1.0f, 1.0f, 1.0f, 1.0f), text,
                size.floatY(), size.floatY(), size.floatY(), size.floatX(),
                selectionStart, selectionEnd);
        vaoSelection = batch.finish();
    }

    @Override
    protected void updateComponent(ScapesEngine engine, double delta) {
        if (active) {
            if (!focused) {
                engine.guiController().focusTextField(data, false);
                focused = true;
            }
            size(engine).ifPresent(size -> {
                if (engine.guiController().processTextField(data, false)) {
                    if (data.text.length() > maxLength) {
                        data.text.delete(maxLength, data.text.length());
                        data.cursor = FastMath.min(data.cursor, maxLength);
                    }
                    FontRenderer font = gui.style().font();
                    FontRenderer.TextInfo textInfo =
                            font.render(FontRenderer.to(),
                                    textFilter.filter(data.text.toString()),
                                    size.floatY(), size.floatX());
                    int maxLengthFont = textInfo.length();
                    if (data.text.length() > maxLengthFont) {
                        data.text = data.text
                                .delete(maxLengthFont, data.text.length());
                        data.cursor = FastMath.min(data.cursor, maxLengthFont);
                    }
                    dirty();
                }
            });
        } else {
            data.selectionStart = -1;
            data.cursor = data.text.length();
            focused = false;
        }
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double width,
            double height) {
        super.renderComponent(gl, shader, width, height);
        if (active) {
            if (System.currentTimeMillis() / 600 % 2 == 0) {
                Streams.forEach(vaoCursor, mesh -> {
                    mesh.b.bind(gl);
                    mesh.a.render(gl, shader);
                });
            }
        }
        gl.textures().unbind(gl);
        gl.setBlending(BlendingMode.INVERT);
        Streams.forEach(vaoSelection, mesh -> mesh.a.render(gl, shader));
        gl.setBlending(BlendingMode.NORMAL);
    }
}
