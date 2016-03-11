package org.tobi29.scapes.engine.utils.shader.expression;

public class TernaryExpression extends Expression {
    public final Expression condition, expression, expressionElse;

    public TernaryExpression(Expression condition, Expression expression,
            Expression expressionElse) {
        this.condition = condition;
        this.expression = expression;
        this.expressionElse = expressionElse;
    }
}
