package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.VAO;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.opengl.texture.Texture;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

public class GuiComponentWidgetTitle extends GuiComponentSlab {
    private final GuiComponentText text;
    private Pair<VAO, Texture> vao;

    public GuiComponentWidgetTitle(GuiLayoutData parent, int textSize,
            String text) {
        this(parent, 4, textSize, text);
    }

    public GuiComponentWidgetTitle(GuiLayoutData parent, int textX,
            int textSize, String text) {
        super(parent);
        this.text = addSubHori(textX, 0, -1, textSize,
                p -> new GuiComponentText(p, text));
    }

    @Override
    public void renderComponent(GL gl, Shader shader, double width, double height) {
        vao.b.bind(gl);
        vao.a.render(gl, shader);
    }

    @Override
    public void updateMesh(Vector2 size) {
        vao = gui.style().widgetTitle(size);
    }

    @Override
    protected GuiLayoutManager layoutManager(Vector2 size) {
        return new GuiLayoutManagerHorizontal(Vector2d.ZERO, size, components);
    }
}