package org.tobi29.scapes.engine.utils.shader.expression;

public class ShaderParameter {
    public final Type type;
    public final int id;
    public final String name;
    public final Expression available;

    public ShaderParameter(Type type, int id, String name,
            Expression available) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.available = available;
    }
}
