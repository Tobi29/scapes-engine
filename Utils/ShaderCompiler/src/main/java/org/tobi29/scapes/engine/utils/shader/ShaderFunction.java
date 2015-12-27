package org.tobi29.scapes.engine.utils.shader;

public class ShaderFunction {
    public final ShaderSignature signature;
    public final Expression compound;

    public ShaderFunction(ShaderSignature signature, Expression compound) {
        assert compound.type == ExpressionType.COMPOUND;
        this.signature = signature;
        this.compound = compound;
    }
}
