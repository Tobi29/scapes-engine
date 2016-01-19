package org.tobi29.scapes.engine.utils.shader;

public class ShaderSignature {
    public final String name;
    public final ShaderParameter[] parameters;

    public ShaderSignature(String name, ShaderParameter... parameters) {
        this.name = name;
        this.parameters = parameters;
    }
}
