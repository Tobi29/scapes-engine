package org.tobi29.scapes.engine.utils.shader.expression;

public class OperationExpression extends Expression {
    public final OperationType type;
    public final Expression left, right;

    public OperationExpression(OperationType type, Expression left,
            Expression right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }
}
