/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.opengl;

import org.tobi29.scapes.engine.opengl.texture.TextureFilter;
import org.tobi29.scapes.engine.opengl.texture.TextureWrap;
import org.tobi29.scapes.engine.utils.graphics.Image;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface OpenGL {
    int VERTEX_ATTRIBUTE = 0, COLOR_ATTRIBUTE = 1, TEXTURE_ATTRIBUTE = 2,
            NORMAL_ATTRIBUTE = 3;

    @OpenGLFunction
    void checkError(String message);

    /**
     * Clear OpenGL context with color (and stile ;) )
     *
     * @param r Red
     * @param g Green
     * @param b Blue
     * @param a Alpha (Seems useless... use 1 just in case)
     */
    @OpenGLFunction
    void clear(float r, float g, float b, float a);

    /**
     * Clear the depth buffer
     */
    @OpenGLFunction
    void clearDepth();

    @OpenGLFunction
    void disableCulling();

    @OpenGLFunction
    void disableDepthTest();

    @OpenGLFunction
    void disableDepthMask();

    @OpenGLFunction
    void disableWireframe();

    /**
     * Disable the scissor
     */
    @OpenGLFunction
    void disableScissor();

    @OpenGLFunction
    void enableCulling();

    @OpenGLFunction
    void enableDepthTest();

    @OpenGLFunction
    void enableDepthMask();

    @OpenGLFunction
    void enableWireframe();

    /**
     * Enable scissor with the given coordinates
     *
     * @param x      x position of scissor
     * @param y      y position of scissor
     * @param width  width of scissor
     * @param height height of scissor
     */
    @OpenGLFunction
    void enableScissor(int x, int y, int width, int height);

    /**
     * Change the alpha blending
     *
     * @param mode Mode to use for blending
     */
    @OpenGLFunction
    void setBlending(BlendingMode mode);

    @OpenGLFunction
    void viewport(int x, int y, int width, int height);

    @OpenGLFunction
    void drawbuffersFBO(int attachments);

    @OpenGLFunction
    int createFBO();

    @OpenGLFunction
    void deleteFBO(int id);

    @OpenGLFunction
    void bindFBO(int id);

    @OpenGLFunction
    void attachColor(int texture, int i);

    @OpenGLFunction
    void attachDepth(int texture);

    @OpenGLFunction
    FBOStatus checkFBO();

    @OpenGLFunction
    Image screenShot(int x, int y, int width, int height);

    @OpenGLFunction
    Image screenShotFBO(FBO fbo);

    @OpenGLFunction
    void activateShader(int id);

    @OpenGLFunction
    int createProgram();

    @OpenGLFunction
    int createFragmentObject();

    @OpenGLFunction
    int createVertexObject();

    @OpenGLFunction
    void deleteProgram(int id);

    @OpenGLFunction
    void deleteShader(int id);

    @OpenGLFunction
    void attach(int id, int object);

    @OpenGLFunction
    void link(int id);

    @OpenGLFunction
    boolean checkLinkStatus(int id);

    @OpenGLFunction
    void source(int id, String code);

    @OpenGLFunction
    void compile(int id);

    @OpenGLFunction
    void printLogShader(int id);

    @OpenGLFunction
    void printLogProgram(int id);

    @OpenGLFunction
    void bindAttributeLocation(int shader, int id, String name);

    @OpenGLFunction
    void bindFragmentLocation(int shader, int id, String name);

    @OpenGLFunction
    int getUniformLocation(int program, String uniform);

    @OpenGLFunction
    void setUniform1f(int uniform, float v0);

    @OpenGLFunction
    void setUniform2f(int uniform, float v0, float v1);

    @OpenGLFunction
    void setUniform3f(int uniform, float v0, float v1, float v2);

    @OpenGLFunction
    void setUniform4f(int uniform, float v0, float v1, float v2, float v3);

    @OpenGLFunction
    void setUniform1i(int uniform, int v0);

    @OpenGLFunction
    void setUniform2i(int uniform, int v0, int v1);

    @OpenGLFunction
    void setUniform3i(int uniform, int v0, int v1, int v2);

    @OpenGLFunction
    void setUniform4i(int uniform, int v0, int v1, int v2, int v3);

    @OpenGLFunction
    void setUniform1(int uniform, FloatBuffer values);

    @OpenGLFunction
    void setUniform2(int uniform, FloatBuffer values);

    @OpenGLFunction
    void setUniform3(int uniform, FloatBuffer values);

    @OpenGLFunction
    void setUniform4(int uniform, FloatBuffer values);

    @OpenGLFunction
    void setUniform1(int uniform, IntBuffer values);

    @OpenGLFunction
    void setUniform2(int uniform, IntBuffer values);

    @OpenGLFunction
    void setUniform3(int uniform, IntBuffer values);

    @OpenGLFunction
    void setUniform4(int uniform, IntBuffer values);

    @OpenGLFunction
    void setUniformMatrix2(int uniform, boolean transpose,
            FloatBuffer matrices);

    @OpenGLFunction
    void setUniformMatrix3(int uniform, boolean transpose,
            FloatBuffer matrices);

    @OpenGLFunction
    void setUniformMatrix4(int uniform, boolean transpose,
            FloatBuffer matrices);

    @OpenGLFunction
    void setAttribute1f(int id, float v0);

    @OpenGLFunction
    void setAttribute2f(int id, float v0, float v1);

    @OpenGLFunction
    void setAttribute3f(int id, float v0, float v1, float v2);

    @OpenGLFunction
    void setAttribute4f(int id, float v0, float v1, float v2, float v3);

    @OpenGLFunction
    void bindTexture(int id);

    @OpenGLFunction
    void bufferTextureMipMap(int width, int height, ByteBuffer... buffers);

    @OpenGLFunction
    void bufferTexture(int width, int height, boolean alpha, ByteBuffer buffer);

    @OpenGLFunction
    void bufferTextureFloat(int width, int height, boolean alpha,
            ByteBuffer buffer);

    @OpenGLFunction
    void bufferTextureDepth(int width, int height, ByteBuffer buffer);

    @OpenGLFunction
    int createTexture();

    @OpenGLFunction
    void deleteTexture(int id);

    @OpenGLFunction
    void magFilter(TextureFilter filter);

    @OpenGLFunction
    void minFilter(TextureFilter filter, boolean mipmap);

    @OpenGLFunction
    void replaceTexture(int x, int y, int width, int height, ByteBuffer buffer);

    @OpenGLFunction
    void replaceTextureMipMap(int x, int y, int width, int height,
            ByteBuffer... buffers);

    @OpenGLFunction
    void wrapS(TextureWrap wrap);

    @OpenGLFunction
    void wrapT(TextureWrap wrap);

    @OpenGLFunction
    void activeTexture(int i);

    @OpenGLFunction
    void bindVAO(int id);

    @OpenGLFunction
    int createVAO();

    @OpenGLFunction
    void deleteVAO(int id);

    @OpenGLFunction
    void setAttribute(int id, int size, VertexType vertexType,
            boolean normalized, int stride, int offset);

    @OpenGLFunction
    void bindVBOArray(int id);

    @OpenGLFunction
    void bindVBOElement(int id);

    @OpenGLFunction
    void bufferVBODataArray(ByteBuffer buffer);

    @OpenGLFunction
    void bufferVBODataElement(ByteBuffer buffer);

    @OpenGLFunction
    int createVBO();

    @OpenGLFunction
    void deleteVBO(int id);

    @OpenGLFunction
    void drawTriangles(int length, long offset);

    @OpenGLFunction
    void drawLines(int length, long offset);
}
