package org.tobi29.scapes.engine.utils.shader.expression;

public class MemberExpression extends IdentifierExpression {
    public final Expression member;

    public MemberExpression(String name, Expression member) {
        super(name);
        this.member = member;
    }
}
