package org.tobi29.scapes.engine.utils.shader.expression;

import java.util.Collections;
import java.util.List;

public class DeclarationStatement extends Statement {
    public final Type type;
    public final List<Declaration> declarations;

    public DeclarationStatement(Type type, List<Declaration> declarations) {
        this.type = type;
        this.declarations = Collections.unmodifiableList(declarations);
    }
}
