package org.tobi29.scapes.engine.utils.shader.expression;

public class UnaryExpression extends Expression {
    public final UnaryType type;
    public final Expression value;

    public UnaryExpression(UnaryType type, Expression value) {
        this.type = type;
        this.value = value;
    }
}
