package org.tobi29.scapes.engine.utils.shader;

import org.antlr.v4.runtime.tree.ParseTree;

public class ShaderCompileException extends Exception {
    public ShaderCompileException(String message, ParseTree context) {
        super(message(message, context));
    }

    public ShaderCompileException(Exception e, ParseTree context) {
        super(message(e.getMessage(), context), e);
    }

    public ShaderCompileException(Exception e) {
        super(e);
    }

    private static String message(String message, ParseTree context) {
        return context.getSourceInterval() + " -> " + message;
    }
}
