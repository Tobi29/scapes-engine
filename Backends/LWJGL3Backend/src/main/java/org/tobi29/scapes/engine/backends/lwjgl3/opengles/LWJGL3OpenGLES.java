/*
 * Copyright 2012-2016 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.backends.lwjgl3.opengles;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBImaging;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES20;
import org.lwjgl.opengles.GLES30;
import org.tobi29.scapes.engine.backends.opengl.OpenGL;
import org.tobi29.scapes.engine.graphics.*;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public final class LWJGL3OpenGLES implements OpenGL {
    @Override
    public void checkError(String message) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String errorName;
            switch (error) {
                case GLES20.GL_INVALID_ENUM:
                    errorName = "Enum argument out of range";
                    break;
                case GLES20.GL_INVALID_VALUE:
                    errorName = "Numeric argument out of range";
                    break;
                case GLES20.GL_INVALID_OPERATION:
                    errorName = "Operation illegal in current state";
                    break;
                case GLES20.GL_OUT_OF_MEMORY:
                    errorName = "Not enough memory left to execute command";
                    break;
                case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
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
        GLES20.glClearColor(r, g, b, a);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void clearDepth() {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void disableCulling() {
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    @Override
    public void disableDepthTest() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void disableDepthMask() {
        GLES20.glDepthMask(false);
    }

    @Override
    public void disableWireframe() {
        // TODO: Is this possible at all in ES?
        //GLES20.glPolygonMode(GLES20.GL_FRONT_AND_BACK, GLES20.GL_FILL);
    }

    @Override
    public void disableScissor() {
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    @Override
    public void enableCulling() {
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    @Override
    public void enableDepthTest() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    }

    @Override
    public void enableDepthMask() {
        GLES20.glDepthMask(true);
    }

    @Override
    public void enableWireframe() {
        // TODO: Is this possible at all in ES?
        //GLES20.glPolygonMode(GLES20.GL_FRONT_AND_BACK, GLES20.GL_LINE);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(x, y, width, height);
    }

    @Override
    public void setBlending(BlendingMode mode) {
        switch (mode) {
            case NONE:
                GLES20.glDisable(GLES20.GL_BLEND);
                break;
            case NORMAL:
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
                        GLES20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ADD:
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
                break;
            case INVERT:
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_COLOR,
                        GLES20.GL_ONE_MINUS_SRC_COLOR);
                break;
        }
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
    }

    @Override
    public void drawbuffersFBO(int attachments) {
        GLUtils.drawbuffers(attachments);
    }

    @Override
    public int createFBO() {
        return GLES20.glGenFramebuffers();
    }

    @Override
    public void deleteFBO(int id) {
        GLES20.glDeleteFramebuffers(id);
    }

    @Override
    public void bindFBO(int id) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id);
    }

    @Override
    public void attachColor(int texture, int i) {
        if (i < 0 || i > 31) {
            throw new IllegalArgumentException(
                    "Color Attachment must be 0-31, was " + i);
        }
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0 + i, GLES20.GL_TEXTURE_2D, texture,
                0);
    }

    @Override
    public void attachDepth(int texture) {
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, texture, 0);
    }

    @Override
    public FramebufferStatus checkFBO() {
        return GLUtils.status();
    }

    @Override
    public Image screenShot(int x, int y, int width, int height) {
        GLES30.glReadBuffer(GLES20.GL_FRONT);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height << 2);
        GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer);
        return new Image(width, height, buffer);
    }

    @Override
    public Image screenShotFBO(Framebuffer fbo) {
        // TODO: Implement
        ByteBuffer buffer =
                BufferUtils.createByteBuffer(fbo.width() * fbo.height() << 2);
        //GLES20.glGetTexImage(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
        //        GLES20.GL_UNSIGNED_BYTE, directBuffer);
        return new Image(fbo.width(), fbo.height(), buffer);
    }

    @Override
    public void activateShader(int id) {
        GLES20.glUseProgram(id);
    }

    @Override
    public Pair<Integer, int[]> createProgram(CompiledShader shader,
            Map<String, String> properties) throws IOException {
        return GLUtils.createProgram(shader, properties);
    }

    @Override
    public int createFragmentObject() {
        return GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
    }

    @Override
    public int createVertexObject() {
        return GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    }

    @Override
    public void deleteProgram(int id) {
        GLES20.glDeleteProgram(id);
    }

    @Override
    public int getUniformLocation(int program, String uniform) {
        return GLES20.glGetUniformLocation(program, uniform);
    }

    @Override
    public void setUniform1f(int uniform, float v0) {
        GLES20.glUniform1f(uniform, v0);
    }

    @Override
    public void setUniform2f(int uniform, float v0, float v1) {
        GLES20.glUniform2f(uniform, v0, v1);
    }

    @Override
    public void setUniform3f(int uniform, float v0, float v1, float v2) {
        GLES20.glUniform3f(uniform, v0, v1, v2);
    }

    @Override
    public void setUniform4f(int uniform, float v0, float v1, float v2,
            float v3) {
        GLES20.glUniform4f(uniform, v0, v1, v2, v3);
    }

    @Override
    public void setUniform1i(int uniform, int v0) {
        GLES20.glUniform1i(uniform, v0);
    }

    @Override
    public void setUniform2i(int uniform, int v0, int v1) {
        GLES20.glUniform2i(uniform, v0, v1);
    }

    @Override
    public void setUniform3i(int uniform, int v0, int v1, int v2) {
        GLES20.glUniform3i(uniform, v0, v1, v2);
    }

    @Override
    public void setUniform4i(int uniform, int v0, int v1, int v2, int v3) {
        GLES20.glUniform4i(uniform, v0, v1, v2, v3);
    }

    @Override
    public void setUniform1(int uniform, FloatBuffer values) {
        GLES20.glUniform1fv(uniform, values);
    }

    @Override
    public void setUniform2(int uniform, FloatBuffer values) {
        GLES20.glUniform2fv(uniform, values);
    }

    @Override
    public void setUniform3(int uniform, FloatBuffer values) {
        GLES20.glUniform3fv(uniform, values);
    }

    @Override
    public void setUniform4(int uniform, FloatBuffer values) {
        GLES20.glUniform4fv(uniform, values);
    }

    @Override
    public void setUniform1(int uniform, IntBuffer values) {
        GLES20.glUniform1iv(uniform, values);
    }

    @Override
    public void setUniform2(int uniform, IntBuffer values) {
        GLES20.glUniform2iv(uniform, values);
    }

    @Override
    public void setUniform3(int uniform, IntBuffer values) {
        GLES20.glUniform3iv(uniform, values);
    }

    @Override
    public void setUniform4(int uniform, IntBuffer values) {
        GLES20.glUniform4iv(uniform, values);
    }

    @Override
    public void setUniformMatrix2(int uniform, boolean transpose,
            float[] matrices) {
        GLES20.glUniformMatrix2fv(uniform, transpose, matrices);
    }

    @Override
    public void setUniformMatrix3(int uniform, boolean transpose,
            float[] matrices) {
        GLES20.glUniformMatrix3fv(uniform, transpose, matrices);
    }

    @Override
    public void setUniformMatrix4(int uniform, boolean transpose,
            float[] matrices) {
        GLES20.glUniformMatrix4fv(uniform, transpose, matrices);
    }

    @Override
    public void setAttribute1f(int id, float v0) {
        GLES20.glVertexAttrib1f(id, v0);
    }

    @Override
    public void setAttribute2f(int id, float v0, float v1) {
        GLES20.glVertexAttrib2f(id, v0, v1);
    }

    @Override
    public void setAttribute3f(int id, float v0, float v1, float v2) {
        GLES20.glVertexAttrib3f(id, v0, v1, v2);
    }

    @Override
    public void setAttribute4f(int id, float v0, float v1, float v2, float v3) {
        GLES20.glVertexAttrib4f(id, v0, v1, v2, v3);
    }

    @Override
    public void setAttribute2f(int uniform, FloatBuffer values) {
        GLES20.glVertexAttrib2fv(uniform, values);
    }

    @Override
    public void setAttribute3f(int uniform, FloatBuffer values) {
        GLES20.glVertexAttrib3fv(uniform, values);
    }

    @Override
    public void setAttribute4f(int uniform, FloatBuffer values) {
        GLES20.glVertexAttrib4fv(uniform, values);
    }

    @Override
    public void bindTexture(int id) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
    }

    @Override
    public void bufferTextureMipMap(int width, int height,
            ByteBuffer... buffers) {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAX_LEVEL, buffers.length - 1);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
                height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffers[0]);
        for (int i = 1; i < buffers.length; i++) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, GLES20.GL_RGBA,
                    FastMath.max(width >> i, 1), FastMath.max(height >> i, 1),
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffers[i]);
        }
    }

    @Override
    public void bufferTexture(int width, int height, boolean alpha,
            ByteBuffer buffer) {
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                alpha ? GLES20.GL_RGBA : GLES20.GL_RGB, width, height, 0,
                alpha ? GLES20.GL_RGBA : GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE,
                buffer);
    }

    @Override
    public void bufferTextureFloat(int width, int height, boolean alpha,
            ByteBuffer buffer) {
        if (!GLES.getCapabilities().GL_EXT_color_buffer_float) {
            bufferTexture(width, height, alpha, buffer);
            return;
        }
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                alpha ? GLES30.GL_RGBA16F : GLES30.GL_RGB16F, width, height, 0,
                alpha ? GLES20.GL_RGBA : GLES20.GL_RGB, GLES30.GL_HALF_FLOAT,
                buffer);
    }

    @Override
    public void bufferTextureDepth(int width, int height, ByteBuffer buffer) {
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,
                GLES30.GL_DEPTH_COMPONENT24, width, height, 0,
                GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, buffer);
    }

    @Override
    public int createTexture() {
        return GLES20.glGenTextures();
    }

    @Override
    public void deleteTexture(int id) {
        GLES20.glDeleteTextures(id);
    }

    @Override
    public void magFilter(TextureFilter filter) {
        switch (filter) {
            case NEAREST:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                break;
            case LINEAR:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                break;
            default:
                throw new IllegalArgumentException("Illegal texture-filter!");
        }
    }

    @Override
    public void minFilter(TextureFilter filter, boolean mipmap) {
        if (mipmap) {
            switch (filter) {
                case NEAREST:
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_NEAREST_MIPMAP_LINEAR);
                    break;
                case LINEAR:
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MIN_FILTER,
                            GLES20.GL_LINEAR_MIPMAP_LINEAR);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Illegal texture-filter!");
            }
        } else {
            switch (filter) {
                case NEAREST:
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                    break;
                case LINEAR:
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Illegal texture-filter!");
            }
        }
    }

    @Override
    public void replaceTexture(int x, int y, int width, int height,
            ByteBuffer buffer) {
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public void replaceTextureMipMap(int x, int y, int width, int height,
            ByteBuffer... buffers) {
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffers[0]);
        for (int i = 1; i < buffers.length; i++) {
            int scale = (int) FastMath.pow(2, i);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, i, x / scale,
                    y / scale, FastMath.max(width / scale, 1),
                    FastMath.max(height / scale, 1), GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, buffers[i]);
        }
    }

    @Override
    public void wrapS(TextureWrap wrap) {
        switch (wrap) {
            case REPEAT:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
                break;
            case CLAMP:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                break;
            default:
                throw new IllegalArgumentException("Illegal texture-wrap!");
        }
    }

    @Override
    public void wrapT(TextureWrap wrap) {
        switch (wrap) {
            case REPEAT:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
                break;
            case CLAMP:
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                break;
            default:
                throw new IllegalArgumentException("Illegal texture-wrap!");
        }
    }

    @Override
    public void activeTexture(int i) {
        if (i < 0 || i > 31) {
            throw new IllegalArgumentException(
                    "Active Texture must be 0-31, was " + i);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
    }

    @Override
    public void bindVAO(int id) {
        GLES30.glBindVertexArray(id);
    }

    @Override
    public int createVAO() {
        return GLES30.glGenVertexArrays();
    }

    @Override
    public void deleteVAO(int id) {
        GLES30.glDeleteVertexArrays(id);
    }

    @Override
    public void setAttribute(int id, int size, VertexType vertexType,
            boolean normalized, int divisor, int stride, int offset) {
        GLES20.glEnableVertexAttribArray(id);
        switch (vertexType) {
            case FLOAT:
                GLES20.glVertexAttribPointer(id, size, GLES20.GL_FLOAT,
                        normalized, stride, offset);
                break;
            case HALF_FLOAT:
                GLES20.glVertexAttribPointer(id, size, GLES30.GL_HALF_FLOAT,
                        normalized, stride, offset);
                break;
            case BYTE:
                GLES20.glVertexAttribPointer(id, size, GLES20.GL_BYTE,
                        normalized, stride, offset);
                break;
            case UNSIGNED_BYTE:
                GLES20.glVertexAttribPointer(id, size, GLES20.GL_UNSIGNED_BYTE,
                        normalized, stride, offset);
                break;
            case SHORT:
                GLES20.glVertexAttribPointer(id, size, GLES20.GL_SHORT,
                        normalized, stride, offset);
                break;
            case UNSIGNED_SHORT:
                GLES20.glVertexAttribPointer(id, size, GLES20.GL_UNSIGNED_SHORT,
                        normalized, stride, offset);
                break;
            default:
                throw new IllegalArgumentException("Unknown vertex type!");
        }
        GLES30.glVertexAttribDivisor(id, divisor);
    }

    @Override

    public void bindVBOArray(int id) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, id);
    }

    @Override
    public void bindVBOElement(int id) {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, id);
    }

    @Override
    public void bufferVBODataArray(ByteBuffer buffer) {
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer,
                GLES20.GL_STATIC_DRAW);
    }

    @Override

    public void bufferVBODataElement(ByteBuffer buffer) {
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer,
                GLES20.GL_STATIC_DRAW);
    }

    @Override

    public void replaceVBODataArray(ByteBuffer buffer) {
        // TODO: Optimize, optimize, optimize
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity(),
                GLES20.GL_STREAM_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, buffer);
    }

    @Override
    public int createVBO() {
        return GLES20.glGenBuffers();
    }

    @Override
    public void deleteVBO(int id) {
        GLES20.glDeleteBuffers(id);
    }

    @Override

    public void drawArray(int length, RenderType renderType) {
        GLES20.glDrawArrays(GLUtils.renderType(renderType), 0, length);
    }

    @Override

    public void drawElements(int length, int offset, RenderType renderType) {
        GLES20.glDrawElements(GLUtils.renderType(renderType), length,
                GLES20.GL_UNSIGNED_SHORT, offset);
    }

    @Override
    public void drawArrayInstanced(int length, int count,
            RenderType renderType) {
        GLES30.glDrawArraysInstanced(GLUtils.renderType(renderType), 0, length,
                count);
    }
}
