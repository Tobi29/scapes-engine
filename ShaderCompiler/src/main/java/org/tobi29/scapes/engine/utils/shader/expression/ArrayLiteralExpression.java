package org.tobi29.scapes.engine.utils.shader.expression;

import java.util.Collections;
import java.util.List;

public class ArrayLiteralExpression extends ArrayExpression {
    public final List<Expression> content;

    public ArrayLiteralExpression(List<Expression> content) {
        this.content = Collections.unmodifiableList(content);
    }
}
