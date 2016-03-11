package org.tobi29.scapes.engine.utils.shader.expression;

import java8.util.Optional;

public class IfStatement extends Statement {
    public final Expression condition;
    public final Statement statement;
    public final Optional<Statement> statementElse;

    public IfStatement(Expression condition, Statement statement) {
        this(condition, statement, Optional.empty());
    }

    public IfStatement(Expression condition, Statement statement,
            Statement statementElse) {
        this(condition, statement, Optional.of(statementElse));
    }

    public IfStatement(Expression condition, Statement statement,
            Optional<Statement> statementElse) {
        this.condition = condition;
        this.statement = statement;
        this.statementElse = statementElse;
    }
}
