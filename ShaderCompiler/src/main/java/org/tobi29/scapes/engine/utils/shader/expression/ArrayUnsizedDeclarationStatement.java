package org.tobi29.scapes.engine.utils.shader.expression;

public class ArrayUnsizedDeclarationStatement extends Statement {
    public final Type type;
    public final String name;
    public final ArrayExpression initializer;

    public ArrayUnsizedDeclarationStatement(Type type, String name,
            ArrayExpression initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }
}
