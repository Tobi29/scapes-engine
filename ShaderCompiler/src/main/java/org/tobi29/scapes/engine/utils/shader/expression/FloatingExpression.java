package org.tobi29.scapes.engine.utils.shader.expression;

import java.math.BigDecimal;

public class FloatingExpression extends Expression {
    public final BigDecimal value;

    public FloatingExpression(BigDecimal value) {
        this.value = value;
    }
}
