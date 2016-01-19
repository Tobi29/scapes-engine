package org.tobi29.scapes.engine.utils.shader;

public class ShaderGenerateException extends Exception {
    public ShaderGenerateException(String message) {
        super(message);
    }

    public ShaderGenerateException(Exception e) {
        super(e);
    }
}
