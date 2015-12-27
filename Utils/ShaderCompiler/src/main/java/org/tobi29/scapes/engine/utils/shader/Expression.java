package org.tobi29.scapes.engine.utils.shader;

public class Expression {
    private static final Expression[] EMPTY_EXPRESSION = {};
    public final ExpressionType type;
    public final Object value;
    public final Expression[] expressions;

    public Expression(ExpressionType type) {
        this(type, "", EMPTY_EXPRESSION);
    }

    public Expression(ExpressionType type, Expression... expressions) {
        this(type, "", expressions);
    }

    public Expression(ExpressionType type, Object value,
            Expression... expressions) {
        this.type = type;
        this.value = value;
        this.expressions = expressions;
    }
}
