package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;

public class GuiComponentWidgetTitle extends GuiComponent {
    protected final Pair<VAO, Texture> vao;
    protected final GuiComponentText text;

    public GuiComponentWidgetTitle(GuiComponent parent, int x, int y, int width,
            int height, int textSize, String text) {
        this(parent, x, y, width, height, 4, textSize, text);
    }

    public GuiComponentWidgetTitle(GuiComponent parent, int x, int y, int width,
            int height, int textX, int textSize, String text) {
        super(parent, x, y, width, height);
        vao = gui.style().pane(width, height);
        int textY = (height - textSize) / 2;
        this.text = new GuiComponentText(this, textX, textY, width - textX,
                textSize, text);
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double delta) {
        vao.b.bind(gl);
        vao.a.render(gl, shader);
    }
}