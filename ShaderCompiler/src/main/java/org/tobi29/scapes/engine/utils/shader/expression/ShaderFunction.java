package org.tobi29.scapes.engine.utils.shader.expression;

public class ShaderFunction {
    public final ShaderSignature signature;
    public final CompoundStatement compound;

    public ShaderFunction(ShaderSignature signature, CompoundStatement compound) {
        this.signature = signature;
        this.compound = compound;
    }
}
