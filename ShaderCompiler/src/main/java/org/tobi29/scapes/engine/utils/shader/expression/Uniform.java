package org.tobi29.scapes.engine.utils.shader.expression;

public class Uniform {
    public final Type type;
    public final int id;
    public final String name;

    public Uniform(Type type, int id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }
}
