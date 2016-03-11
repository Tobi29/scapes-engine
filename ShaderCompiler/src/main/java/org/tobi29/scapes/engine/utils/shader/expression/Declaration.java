package org.tobi29.scapes.engine.utils.shader.expression;

import java8.util.Optional;

public class Declaration {
    public final String name;
    public final Optional<Expression> initializer;

    public Declaration(String name) {
        this(name, Optional.empty());
    }

    public Declaration(String name, Expression initializer) {
        this(name, Optional.of(initializer));
    }

    public Declaration(String name, Optional<Expression> initializer) {
        this.name = name;
        this.initializer = initializer;
    }
}
