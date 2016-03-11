package org.tobi29.scapes.engine.utils.shader.expression;

import java.util.Collections;
import java.util.List;

public class StatementBlock extends Expression {
    public final List<Statement> statements;

    public StatementBlock(List<Statement> statements) {
        this.statements = Collections.unmodifiableList(statements);
    }
}
