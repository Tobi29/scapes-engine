package org.tobi29.scapes.engine.utils.shader.expression;

public class FunctionSignature {
    public final String name;
    public final Types returned;
    public final Precision returnedPrecision;
    public final Parameter[] parameters;

    public FunctionSignature(String name, Types returned,
            Precision returnedPrecision, Parameter... parameters) {
        this.name = name;
        this.returned = returned;
        this.returnedPrecision = returnedPrecision;
        this.parameters = parameters;
    }
}
