package org.tobi29.scapes.engine.utils.shader.expression;

public class AssignmentExpression extends Expression {
    public final AssignmentType type;
    public final Expression left, right;

    public AssignmentExpression(AssignmentType type, Expression left,
            Expression right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }
}
