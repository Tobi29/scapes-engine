package org.tobi29.scapes.engine.utils.shader.glsl;

import org.tobi29.scapes.engine.utils.shader.expression.Expression;

public class GLSLExpression extends Expression {
    public final String code;

    public GLSLExpression(String code) {
        this.code = code;
    }
}
