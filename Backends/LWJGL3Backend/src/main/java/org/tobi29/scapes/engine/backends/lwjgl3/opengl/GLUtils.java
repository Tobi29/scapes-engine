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

package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.graphics.FramebufferStatus;
import org.tobi29.scapes.engine.graphics.RenderType;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.ThreadLocalUtil;
import org.tobi29.scapes.engine.utils.shader.CompiledShader;
import org.tobi29.scapes.engine.utils.shader.ShaderGenerateException;
import org.tobi29.scapes.engine.utils.shader.expression.Uniform;
import org.tobi29.scapes.engine.utils.shader.glsl.GLSLGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;

final class GLUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GLUtils.class);
    private static final ThreadLocal<GLSLGenerator> SHADER_GENERATOR =
            ThreadLocalUtil
                    .of(() -> new GLSLGenerator(GLSLGenerator.Version.GL_330));

    private GLUtils() {
    }

    public static int renderType(RenderType renderType) {
        switch (renderType) {
            case TRIANGLES:
                return GL11.GL_TRIANGLES;
            case LINES:
                return GL11.GL_LINES;
            default:
                throw new IllegalArgumentException(
                        "Unknown render type: " + renderType);
        }
    }

    public static FramebufferStatus status() {
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        switch (status) {
            case GL30.GL_FRAMEBUFFER_COMPLETE:
                return FramebufferStatus.COMPLETE;
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED:
                return FramebufferStatus.UNSUPPORTED;
            default:
                return FramebufferStatus.UNKNOWN;
        }
    }

    public static void drawbuffers(int attachments) {
        if (attachments < 0 || attachments > 15) {
            throw new IllegalArgumentException(
                    "Attachments must be 0-15, was " + attachments);
        }
        IntBuffer attachBuffer = BufferUtils.createIntBuffer(16);
        attachBuffer.limit(attachments);
        for (int i = 0; i < attachments; i++) {
            attachBuffer.put(GL30.GL_COLOR_ATTACHMENT0 + i);
        }
        attachBuffer.rewind();
        GL20.glDrawBuffers(attachBuffer);
    }

    public static void printLogShader(int id) {
        int length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
        if (length > 1) {
            IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
            lengthBuffer.put(0, length);
            ByteBuffer buffer = BufferUtils.createByteBuffer(length);
            GL20.glGetShaderInfoLog(id, lengthBuffer, buffer);
            byte[] infoBytes = new byte[length];
            buffer.get(infoBytes);
            String out = new String(infoBytes);
            LOGGER.info("Shader log: {}", out);
        }
    }

    public static void printLogProgram(int id) {
        int length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
        if (length > 1) {
            IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
            lengthBuffer.put(0, length);
            ByteBuffer buffer = BufferUtils.createByteBuffer(length);
            GL20.glGetProgramInfoLog(id, lengthBuffer, buffer);
            byte[] infoBytes = new byte[length];
            buffer.get(infoBytes);
            String out = new String(infoBytes);
            LOGGER.info("Program log: {}", out);
        }
    }

    public static Pair<Integer, int[]> createProgram(CompiledShader shader,
            Map<String, String> properties) throws IOException {
        try {
            GLSLGenerator shaderGenerator = SHADER_GENERATOR.get();
            String vertexSource =
                    shaderGenerator.generateVertex(shader, properties);
            String fragmentSource =
                    shaderGenerator.generateFragment(shader, properties);
            int vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vertex, vertexSource);
            GL20.glCompileShader(vertex);
            printLogShader(vertex);
            int fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fragment, fragmentSource);
            GL20.glCompileShader(fragment);
            printLogShader(fragment);
            int program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) !=
                    GL11.GL_TRUE) {
                LOGGER.error("Failed to link status bar!");
                printLogProgram(program);
            }
            Uniform[] uniforms = shader.uniforms();
            int[] uniformLocations = new int[uniforms.length];
            for (int i = 0; i < uniforms.length; i++) {
                Uniform uniform = uniforms[i];
                if (uniform == null) {
                    uniformLocations[i] = -1;
                } else {
                    uniformLocations[i] =
                            GL20.glGetUniformLocation(program, uniform.name);
                }
            }
            GL20.glDetachShader(program, vertex);
            GL20.glDetachShader(program, fragment);
            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
            return new Pair<>(program, uniformLocations);
        } catch (ShaderGenerateException e) {
            throw new IOException(e);
        }
    }
}
