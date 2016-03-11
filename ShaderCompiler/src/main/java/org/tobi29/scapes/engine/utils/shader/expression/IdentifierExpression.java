package org.tobi29.scapes.engine.utils.shader.expression;

public abstract class IdentifierExpression extends Expression {
    public final String name;

    protected IdentifierExpression(String name) {
        this.name = name;
    }
}
