package org.tobi29.scapes.engine.utils.shader.expression;

import java8.util.Optional;

public class Type {
    public final Types type;
    public final Optional<Expression> array;
    public final boolean constant;
    public final Precision precision;

    public Type(Types type, boolean constant, Precision precision) {
        this(type, Optional.empty(), constant, precision);
    }

    public Type(Types type, Expression array, boolean constant,
            Precision precision) {
        this(type, Optional.of(array), constant, precision);
    }

    public Type(Types type, Optional<Expression> array, boolean constant,
            Precision precision) {
        this.type = type;
        this.array = array;
        this.constant = constant;
        this.precision = precision;
    }
}
