package org.tobi29.scapes.engine.utils.shader.expression;

public class Uniform {
    public final Types type;
    public final int id;
    public final String name;

    public Uniform(Types type, int id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }
}
