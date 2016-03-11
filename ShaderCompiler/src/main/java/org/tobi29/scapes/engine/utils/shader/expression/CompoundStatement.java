package org.tobi29.scapes.engine.utils.shader.expression;

public class CompoundStatement extends Statement {
    public final StatementBlock block;

    public CompoundStatement(StatementBlock block) {
        this.block = block;
    }
}
