package org.tobi29.scapes.engine.utils.shader.expression;

public class LoopFixedStatement extends Statement {
    public final String name;
    public final IntegerExpression start, end;
    public final Statement statement;

    public LoopFixedStatement(String name, IntegerExpression start,
            IntegerExpression end, Statement statement) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.statement = statement;
    }
}
