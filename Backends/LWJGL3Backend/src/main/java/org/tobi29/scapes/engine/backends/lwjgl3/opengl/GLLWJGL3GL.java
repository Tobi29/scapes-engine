package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.tobi29.scapes.engine.Container;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.*;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public final class GLLWJGL3GL extends GL {
    public GLLWJGL3GL(ScapesEngine engine, Container container) {
        super(engine, container);
    }

    @Override
    public Texture createTexture(int width, int height, ByteBuffer buffer,
            int mipmaps, TextureFilter minFilter, TextureFilter magFilter,
            TextureWrap wrapS, TextureWrap wrapT) {
        return new TextureGL(engine, width, height, buffer, mipmaps, minFilter,
                magFilter, wrapS, wrapT);
    }

    @Override
    public Framebuffer createFramebuffer(int width, int height,
            int colorAttachments, boolean depth, boolean hdr, boolean alpha) {
        return new FBO(engine, width, height, colorAttachments, depth, hdr,
                alpha);
    }

    @Override
    public Model createModelFast(List<ModelAttribute> attributes, int length,
            RenderType renderType) {
        VBO vbo = new VBO(engine, attributes, length);
        return new VAOFast(vbo, length, renderType);
    }

    @Override
    public Model createModelStatic(List<ModelAttribute> attributes, int length,
            int[] index, int indexLength, RenderType renderType) {
        VBO vbo = new VBO(engine, attributes, length);
        return new VAOStatic(vbo, index, indexLength, renderType);
    }

    @Override
    public ModelHybrid createModelHybrid(List<ModelAttribute> attributes,
            int length, List<ModelAttribute> attributesStream, int lengthStream,
            RenderType renderType) {
        VBO vbo = new VBO(engine, attributes, length);
        VBO vboStream = new VBO(engine, attributesStream, lengthStream);
        return new VAOHybrid(vbo, vboStream, renderType);
    }

    @Override
    public Shader createShader(CompiledShader shader,
            ShaderCompileInformation information) {
        return new ShaderGL(shader, information);
    }

    @Override
    public void checkError(String message) {
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            String errorName;
            switch (error) {
                case GL11.GL_INVALID_ENUM:
                    errorName = "Enum argument out of range";
                    break;
                case GL11.GL_INVALID_VALUE:
                    errorName = "Numeric argument out of range";
                    break;
                case GL11.GL_INVALID_OPERATION:
                    errorName = "Operation illegal in current state";
                    break;
                case GL11.GL_STACK_OVERFLOW:
                    errorName = "Command would cause a stack overflow";
                    break;
                case GL11.GL_STACK_UNDERFLOW:
                    errorName = "Command would cause a stack underflow";
                    break;
                case GL11.GL_OUT_OF_MEMORY:
                    errorName = "Not enough memory left to execute command";
                    break;
                case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorName = "Framebuffer object is not complete";
                    break;
                case ARBImaging.GL_TABLE_TOO_LARGE:
                    errorName = "The specified table is too large";
                    break;
                default:
                    errorName = "Unknown error code";
                    break;
            }
            throw new GraphicsException(errorName + " in " + message);
        }
    }

    @Override
    public void clear(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void clearDepth() {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    @Override
    public void disableDepthTest() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void disableDepthMask() {
        GL11.glDepthMask(false);
    }

    @Override
    public void disableWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    @Override
    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void enableDepthTest() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public void enableDepthMask() {
        GL11.glDepthMask(true);
    }

    @Override
    public void enableWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        double h = engine.container().contentHeight() / 540.0;
        GL11.glScissor((int) (x * h), (int) ((540.0 - y - height) * h),
                (int) (width * h), (int) (height * h));
    }

    @Override
    public void setBlending(BlendingMode mode) {
        switch (mode) {
            case NONE:
                GL11.glDisable(GL11.GL_BLEND);
                break;
            case NORMAL:
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ADD:
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
                break;
            case INVERT:
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR,
                        GL11.GL_ONE_MINUS_SRC_COLOR);
                break;
        }
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        GL11.glViewport(x, y, width, height);
    }

    @Override
    public Image screenShot(int x, int y, int width, int height) {
        GL11.glReadBuffer(GL11.GL_FRONT);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height << 2);
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, buffer);
        return new Image(width, height, buffer);
    }

    @Override
    public Image screenShotFBO(Framebuffer fbo) {
        ByteBuffer buffer =
                BufferUtils.createByteBuffer(fbo.width() * fbo.height() << 2);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, buffer);
        return new Image(fbo.width(), fbo.height(), buffer);
    }

    @Override
    public void setAttribute1f(int id, float v0) {
        GL20.glVertexAttrib1f(id, v0);
    }

    @Override
    public void setAttribute2f(int id, float v0, float v1) {
        GL20.glVertexAttrib2f(id, v0, v1);
    }

    @Override
    public void setAttribute3f(int id, float v0, float v1, float v2) {
        GL20.glVertexAttrib3f(id, v0, v1, v2);
    }

    @Override
    public void setAttribute4f(int id, float v0, float v1, float v2, float v3) {
        GL20.glVertexAttrib4f(id, v0, v1, v2, v3);
    }

    @Override
    public void setAttribute2f(int uniform, FloatBuffer values) {
        GL20.glVertexAttrib2fv(uniform, values);
    }

    @Override
    public void setAttribute3f(int uniform, FloatBuffer values) {
        GL20.glVertexAttrib3fv(uniform, values);
    }

    @Override
    public void setAttribute4f(int uniform, FloatBuffer values) {
        GL20.glVertexAttrib4fv(uniform, values);
    }

    @Override
    public void replaceTexture(int x, int y, int width, int height,
            ByteBuffer buffer) {
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public void replaceTextureMipMap(int x, int y, int width, int height,
            ByteBuffer... buffers) {
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[0]);
        for (int i = 1; i < buffers.length; i++) {
            int scale = (int) FastMath.pow(2, i);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, x / scale, y / scale,
                    FastMath.max(width / scale, 1),
                    FastMath.max(height / scale, 1), GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, buffers[i]);
        }
    }

    @Override
    public void activeTexture(int i) {
        if (i < 0 || i > 31) {
            throw new IllegalArgumentException(
                    "Active Texture must be 0-31, was " + i);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
    }
}
