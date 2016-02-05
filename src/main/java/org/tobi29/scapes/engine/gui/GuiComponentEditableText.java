package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.BlendingMode;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;

public class GuiComponentEditableText extends GuiComponentText {
    protected final GuiController.TextFieldData data =
            new GuiController.TextFieldData();
    protected final int maxLength;
    protected boolean active, focused;
    protected FontRenderer.Text vaoCursor, vaoSelection;

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
        super(parent, text, r, g, b, a);
        data.text.append(text);
        this.maxLength = maxLength;
        data.cursor = data.text.length();
        dirty.set(true);
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

    @Override
    protected void updateComponent(ScapesEngine engine, double delta, Vector2 size) {
        if (active) {
            if (!focused) {
                engine.guiController().focusTextField(data, false);
                focused = true;
            }
            if (engine.guiController().processTextField(data, false)) {
                if (data.text.length() > maxLength) {
                    data.text.delete(maxLength, data.text.length());
                    data.cursor = FastMath.min(data.cursor, maxLength);
                }
                FontRenderer font = gui.style().font();
                FontRenderer.Text width =
                        font.render(textFilter.filter(data.text.toString()),
                                0.0, 0.0, size.doubleY(), size.doubleX(), r, g,
                                b, a);
                int maxLengthFont = width.length();
                if (data.text.length() > maxLengthFont) {
                    data.text =
                            data.text.delete(maxLengthFont, data.text.length());
                    data.cursor = FastMath.min(data.cursor, maxLengthFont);
                }
                super.setText(data.text.toString());
                dirty.set(true);
            }
        } else {
            data.selectionStart = -1;
            data.cursor = data.text.length();
            focused = false;
        }
    }

    @Override
    public void setText(String text) {
        data.text.setLength(0);
        data.text.append(text);
        super.setText(text);
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double width, double height) {
        super.renderComponent(gl, shader, width, height);
        if (active) {
            if (System.currentTimeMillis() / 600 % 2 == 0) {
                vaoCursor.render(gl, shader);
            }
        }
        gl.textures().unbind(gl);
        gl.setBlending(BlendingMode.INVERT);
        vaoSelection.render(gl, shader, false);
        gl.setBlending(BlendingMode.NORMAL);
    }

    @Override
    protected void updateMesh(Vector2 size) {
        super.updateMesh(size);
        if (data == null) {
            return;
        }
        FontRenderer font = gui.style().font();
        String text = this.text;
        int cursor = data.cursor;
        int selectionStart = data.selectionStart;
        int selectionEnd = data.selectionEnd;
        cursor = FastMath.clamp(cursor, 0, text.length());
        selectionStart = FastMath.clamp(selectionStart, 0, text.length());
        selectionEnd = FastMath.clamp(selectionEnd, 0, text.length());
        vaoCursor = font.render(text.substring(0, cursor) + '|',
                0.0 - size.doubleY() * 0.1, 0.0 - size.doubleY() * 0.1,
                size.doubleY(), size.doubleY() * 1.2, size.doubleY(),
                Float.MAX_VALUE, 1.0, 1.0, 1.0, 1.0, cursor, cursor + 1, false);
        vaoSelection =
                font.render(text, 0.0, 0.0, size.doubleY(), size.doubleY(),
                        size.doubleY(), size.doubleX() - 0.0, 1.0, 1.0, 1.0,
                        1.0, selectionStart, selectionEnd, true);
    }
}
