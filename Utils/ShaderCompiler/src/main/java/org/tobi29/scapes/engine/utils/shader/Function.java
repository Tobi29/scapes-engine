package org.tobi29.scapes.engine.utils.shader;

public class Function {
    public final FunctionSignature signature;
    public final Expression compound;

    public Function(FunctionSignature signature, Expression compound) {
        assert compound.type == ExpressionType.COMPOUND;
        this.signature = signature;
        this.compound = compound;
    }
}
