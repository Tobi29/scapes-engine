package org.tobi29.scapes.engine.utils.shader;

public class ShaderSignature {
    public final String name;
    public final Types returned;
    public final ShaderParameter[] parameters;

    public ShaderSignature(String name, Types returned,
            ShaderParameter... parameters) {
        this.name = name;
        this.returned = returned;
        this.parameters = parameters;
    }
}
