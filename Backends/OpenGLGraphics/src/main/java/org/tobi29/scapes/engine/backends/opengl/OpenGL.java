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
package org.tobi29.scapes.engine.backends.opengl;

import org.tobi29.scapes.engine.graphics.*;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public interface OpenGL {
    void checkError(String message);

    void clear(float r, float g, float b, float a);

    void clearDepth();

    void disableCulling();

    void disableDepthTest();

    void disableDepthMask();

    void disableWireframe();

    void disableScissor();

    void enableCulling();

    void enableDepthTest();

    void enableDepthMask();

    void enableWireframe();

    void enableScissor(int x, int y, int width, int height);

    void setBlending(BlendingMode mode);

    void viewport(int x, int y, int width, int height);

    void drawbuffersFBO(int attachments);

    int createFBO();

    void deleteFBO(int id);

    void bindFBO(int id);

    void attachColor(int texture, int i);

    void attachDepth(int texture);

    FramebufferStatus checkFBO();

    Image screenShot(int x, int y, int width, int height);

    Image screenShotFBO(Framebuffer fbo);

    void activateShader(int id);

    Pair<Integer, int[]> createProgram(CompiledShader shader,
            Map<String, String> properties) throws IOException;

    int createFragmentObject();

    int createVertexObject();

    void deleteProgram(int id);

    int getUniformLocation(int program, String uniform);

    void setUniform1f(int uniform, float v0);

    void setUniform2f(int uniform, float v0, float v1);

    void setUniform3f(int uniform, float v0, float v1, float v2);

    void setUniform4f(int uniform, float v0, float v1, float v2, float v3);

    void setUniform1i(int uniform, int v0);

    void setUniform2i(int uniform, int v0, int v1);

    void setUniform3i(int uniform, int v0, int v1, int v2);

    void setUniform4i(int uniform, int v0, int v1, int v2, int v3);

    void setUniform1(int uniform, FloatBuffer values);

    void setUniform2(int uniform, FloatBuffer values);

    void setUniform3(int uniform, FloatBuffer values);

    void setUniform4(int uniform, FloatBuffer values);

    void setUniform1(int uniform, IntBuffer values);

    void setUniform2(int uniform, IntBuffer values);

    void setUniform3(int uniform, IntBuffer values);

    void setUniform4(int uniform, IntBuffer values);

    void setUniformMatrix2(int uniform, boolean transpose, float[] matrices);

    void setUniformMatrix3(int uniform, boolean transpose, float[] matrices);

    void setUniformMatrix4(int uniform, boolean transpose, float[] matrices);

    void setAttribute1f(int id, float v0);

    void setAttribute2f(int id, float v0, float v1);

    void setAttribute3f(int id, float v0, float v1, float v2);

    void setAttribute4f(int id, float v0, float v1, float v2, float v3);

    void setAttribute2f(int uniform, FloatBuffer values);

    void setAttribute3f(int uniform, FloatBuffer values);

    void setAttribute4f(int uniform, FloatBuffer values);

    void bindTexture(int id);

    void bufferTextureMipMap(int width, int height, ByteBuffer... buffers);

    void bufferTexture(int width, int height, boolean alpha, ByteBuffer buffer);

    void bufferTextureFloat(int width, int height, boolean alpha,
            ByteBuffer buffer);

    void bufferTextureDepth(int width, int height, ByteBuffer buffer);

    int createTexture();

    void deleteTexture(int id);

    void magFilter(TextureFilter filter);

    void minFilter(TextureFilter filter, boolean mipmap);

    void replaceTexture(int x, int y, int width, int height, ByteBuffer buffer);

    void replaceTextureMipMap(int x, int y, int width, int height,
            ByteBuffer... buffers);

    void wrapS(TextureWrap wrap);

    void wrapT(TextureWrap wrap);

    void activeTexture(int i);

    void bindVAO(int id);

    int createVAO();

    void deleteVAO(int id);

    void setAttribute(int id, int size, VertexType vertexType,
            boolean normalized, int divisor, int stride, int offset);

    void setAttributeInteger(int id, int size, VertexType vertexType,
            int divisor, int stride, int offset);

    void bindVBOArray(int id);

    void bindVBOElement(int id);

    void bufferVBODataArray(ByteBuffer buffer);

    void bufferVBODataElement(ByteBuffer buffer);

    void replaceVBODataArray(ByteBuffer buffer);

    int createVBO();

    void deleteVBO(int id);

    void drawArray(int length, RenderType renderType);

    void drawElements(int length, int offset, RenderType renderType);

    void drawArrayInstanced(int length, int count, RenderType renderType);
}
