package org.tobi29.scapes.engine.utils.shader.expression;

import java.math.BigInteger;

public class IntegerLiteralExpression extends IntegerExpression {
    public final BigInteger value;

    public IntegerLiteralExpression(BigInteger value) {
        this.value = value;
    }
}
