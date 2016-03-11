package org.tobi29.scapes.engine.utils.shader.expression;

import java8.util.Optional;

public class Type {
    public final Types type;
    public final Optional<Expression> array;
    public final boolean constant;

    public Type(Types type) {
        this(type, false);
    }

    public Type(Types type, boolean constant) {
        this(type, Optional.empty(), constant);
    }

    public Type(Types type, Expression array, boolean constant) {
        this(type, Optional.of(array), constant);
    }

    public Type(Types type, Optional<Expression> array, boolean constant) {
        this.type = type;
        this.array = array;
        this.constant = constant;
    }
}
