package org.tobi29.scapes.engine.utils.shader.expression;

import java.util.Collections;
import java.util.List;

public class FunctionExpression extends IdentifierExpression {
    public final List<Expression> args;

    public FunctionExpression(String name, List<Expression> args) {
        super(name);
        this.args = Collections.unmodifiableList(args);
    }
}
