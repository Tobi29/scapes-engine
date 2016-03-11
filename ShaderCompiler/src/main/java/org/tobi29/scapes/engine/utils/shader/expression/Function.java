package org.tobi29.scapes.engine.utils.shader.expression;

public class Function {
    public final FunctionSignature signature;
    public final CompoundStatement compound;

    public Function(FunctionSignature signature, CompoundStatement compound) {
        this.signature = signature;
        this.compound = compound;
    }
}
