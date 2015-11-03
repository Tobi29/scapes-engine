package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.BlendingMode;
import org.tobi29.scapes.engine.opengl.FontRenderer;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class GuiComponentEditableText extends GuiComponentText {
    protected final GuiController.TextFieldData data =
            new GuiController.TextFieldData();
    protected final int maxLength;
    protected boolean active;
    protected FontRenderer.Text vaoCursor, vaoSelection;

    public GuiComponentEditableText(GuiLayoutData parent, int textSize,
            String text) {
        this(parent, Integer.MAX_VALUE, textSize, text);
    }

    public GuiComponentEditableText(GuiLayoutData parent, int textSize,
            String text, float r, float g, float b, float a) {
        this(parent, Integer.MAX_VALUE, textSize, text, r, g, b, a);
    }

    public GuiComponentEditableText(GuiLayoutData parent, int width,
            int textSize, String text) {
        this(parent, width, textSize, text, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public GuiComponentEditableText(GuiLayoutData parent, int width,
            int textSize, String text, float r, float g, float b, float a) {
        this(parent, width, textSize, text, Integer.MAX_VALUE, r, g, b, a);
    }

    public GuiComponentEditableText(GuiLayoutData parent, int width,
            int textSize, String text, int maxLength) {
        this(parent, width, textSize, text, maxLength, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public GuiComponentEditableText(GuiLayoutData parent, int width,
            int textSize, String text, int maxLength, float r, float g, float b,
            float a) {
        super(parent, width, textSize, text, r, g, b, a);
        this.maxLength = maxLength;
        updateText();
        data.cursor = data.text.length();
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
    public void renderComponent(GL gl, Shader shader, double delta) {
        super.renderComponent(gl, shader, delta);
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
    protected void updateText() {
        super.updateText();
        if (data == null) {
            return;
        }
        FontRenderer font = gui.style().font();
        data.text.setLength(0);
        data.text.append(text);
        int maxLengthFont = vaoText.length();
        if (data.text.length() > maxLengthFont) {
            data.text = data.text.delete(maxLengthFont, data.text.length());
            data.cursor = FastMath.min(data.cursor, maxLengthFont);
        }
        vaoCursor = font.render(text.substring(0, data.cursor) + '|',
                0.0f - textSize * 0.1f, 0.0f - textSize * 0.1f, textSize,
                textSize * 1.2f, textSize, Float.MAX_VALUE, 1.0f, 1.0f, 1.0f,
                1.0f, data.cursor, data.cursor + 1, false);
        vaoSelection =
                font.render(text, 0.0f, 0.0f, textSize, textSize, textSize,
                        width - 0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        data.selectionStart, data.selectionEnd, true);
    }

    @Override
    public void update(double mouseX, double mouseY, boolean mouseInside,
            ScapesEngine engine) {
        super.update(mouseX, mouseY, mouseInside, engine);
        if (active) {
            if (engine.guiController().processTextField(data, false)) {
                if (data.text.length() > maxLength) {
                    data.text.delete(maxLength, data.text.length());
                    data.cursor = FastMath.min(data.cursor, maxLength);
                }
                setText(data.text.toString());
                updateText();
            }
        } else {
            data.selectionStart = -1;
            data.cursor = data.text.length();
        }
    }
}
