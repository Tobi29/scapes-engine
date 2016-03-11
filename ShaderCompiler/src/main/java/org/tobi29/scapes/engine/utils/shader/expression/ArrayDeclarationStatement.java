package org.tobi29.scapes.engine.utils.shader.expression;

import java.util.Collections;
import java.util.List;

public class ArrayDeclarationStatement extends Statement {
    public final Type type;
    public final Expression length;
    public final List<ArrayDeclaration> declarations;

    public ArrayDeclarationStatement(Type type, Expression length,
            List<ArrayDeclaration> declarations) {
        this.type = type;
        this.length = length;
        this.declarations = Collections.unmodifiableList(declarations);
    }
}
