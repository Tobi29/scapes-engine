package org.tobi29.scapes.engine.backends.opengl;

import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.*;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public final class GLOpenGL extends GL {
    private final OpenGL openGL;
    private final OpenGLBind openGLBind;

    public GLOpenGL(ScapesEngine engine, Container container, OpenGL openGL) {
        super(engine, container);
        this.openGL = openGL;
        openGLBind = new OpenGLBind(this, openGL);
    }

    @Override
    public Texture createTexture(int width, int height, ByteBuffer buffer,
            int mipmaps, TextureFilter minFilter, TextureFilter magFilter,
            TextureWrap wrapS, TextureWrap wrapT) {
        return new TextureGL(engine, openGLBind, width, height, buffer, mipmaps,
                minFilter, magFilter, wrapS, wrapT);
    }

    @Override
    public Framebuffer createFramebuffer(int width, int height,
            int colorAttachments, boolean depth, boolean hdr, boolean alpha) {
        return new FBO(engine, openGLBind, width, height, colorAttachments,
                depth, hdr, alpha);
    }

    @Override
    public Model createModelFast(List<ModelAttribute> attributes, int length,
            RenderType renderType) {
        VBO vbo = new VBO(engine, openGLBind, attributes, length);
        return new VAOFast(vbo, length, renderType);
    }

    @Override
    public Model createModelStatic(List<ModelAttribute> attributes, int length,
            int[] index, int indexLength, RenderType renderType) {
        VBO vbo = new VBO(engine, openGLBind, attributes, length);
        return new VAOStatic(vbo, index, indexLength, renderType);
    }

    @Override
    public ModelHybrid createModelHybrid(List<ModelAttribute> attributes,
            int length, List<ModelAttribute> attributesStream, int lengthStream,
            RenderType renderType) {
        VBO vbo = new VBO(engine, openGLBind, attributes, length);
        VBO vboStream =
                new VBO(engine, openGLBind, attributesStream, lengthStream);
        return new VAOHybrid(vbo, vboStream, renderType);
    }

    @Override
    public Shader createShader(CompiledShader shader,
            ShaderCompileInformation information) {
        return new ShaderGL(shader, information, openGLBind);
    }

    @Override
    public void checkError(String message) {
        openGL.checkError(message);
    }

    @Override
    public void clear(float r, float g, float b, float a) {
        openGL.clear(r, g, b, a);
    }

    @Override
    public void clearDepth() {
        openGL.clearDepth();
    }

    @Override
    public void disableCulling() {
        openGL.disableCulling();
    }

    @Override
    public void disableDepthTest() {
        openGL.disableDepthTest();
    }

    @Override
    public void disableDepthMask() {
        openGL.disableDepthMask();
    }

    @Override
    public void disableWireframe() {
        openGL.disableWireframe();
    }

    @Override
    public void disableScissor() {
        openGL.disableScissor();
    }

    @Override
    public void enableCulling() {
        openGL.enableCulling();
    }

    @Override
    public void enableDepthTest() {
        openGL.enableDepthTest();
    }

    @Override
    public void enableDepthMask() {
        openGL.enableDepthMask();
    }

    @Override
    public void enableWireframe() {
        openGL.enableWireframe();
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        double h = engine.container().contentHeight() / 540.0;
        int x2 = (int) (x * h);
        int y2 = (int) ((540.0 - y - height) * h);
        int width2 = (int) (width * h);
        int height2 = (int) (height * h);
        openGL.enableScissor(x2, y2, width2, height2);
    }

    @Override
    public void setBlending(BlendingMode mode) {
        openGL.setBlending(mode);
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        openGL.viewport(x, y, width, height);
    }

    @Override
    public Image screenShot(int x, int y, int width, int height) {
        return openGL.screenShot(x, y, width, height);
    }

    @Override
    public Image screenShotFBO(Framebuffer fbo) {
        return openGL.screenShotFBO(fbo);
    }

    @Override
    public void setAttribute1f(int id, float v0) {
        openGL.setAttribute1f(id, v0);
    }

    @Override
    public void setAttribute2f(int id, float v0, float v1) {
        openGL.setAttribute2f(id, v0, v1);
    }

    @Override
    public void setAttribute3f(int id, float v0, float v1, float v2) {
        openGL.setAttribute3f(id, v0, v1, v2);
    }

    @Override
    public void setAttribute4f(int id, float v0, float v1, float v2, float v3) {
        openGL.setAttribute4f(id, v0, v1, v2, v3);
    }

    @Override
    public void setAttribute2f(int uniform, FloatBuffer values) {
        openGL.setAttribute2f(uniform, values);
    }

    @Override
    public void setAttribute3f(int uniform, FloatBuffer values) {
        openGL.setAttribute3f(uniform, values);
    }

    @Override
    public void setAttribute4f(int uniform, FloatBuffer values) {
        openGL.setAttribute4f(uniform, values);
    }

    @Override
    public void replaceTexture(int x, int y, int width, int height,
            ByteBuffer buffer) {
        openGL.replaceTexture(x, y, width, height, buffer);
    }

    @Override
    public void replaceTextureMipMap(int x, int y, int width, int height,
            ByteBuffer... buffers) {
        openGL.replaceTextureMipMap(x, y, width, height, buffers);
    }

    @Override
    public void activeTexture(int i) {
        openGL.activeTexture(i);
    }
}
