package org.tobi29.scapes.engine.utils.shader.expression;

public class ArrayAccessExpression extends Expression {
    public final Expression name, index;

    public ArrayAccessExpression(Expression name, Expression index) {
        this.name = name;
        this.index = index;
    }
}
