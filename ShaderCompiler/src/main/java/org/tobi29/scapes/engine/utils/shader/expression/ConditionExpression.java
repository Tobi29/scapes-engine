package org.tobi29.scapes.engine.utils.shader.expression;

public class ConditionExpression extends Expression {
    public final ConditionType type;
    public final Expression left, right;

    public ConditionExpression(ConditionType type, Expression left,
            Expression right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }
}
