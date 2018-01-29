/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.backends.opengles

expect val GL_FRAMEBUFFER: GLEnum
expect val GL_COLOR_BUFFER_BIT: GLEnum
expect val GL_DEPTH_BUFFER_BIT: GLEnum
expect val GL_TEXTURE_2D: GLEnum
expect val GL_RGB: GLEnum
expect val GL_RGBA: GLEnum
expect val GL_RGB16F: GLEnum
expect val GL_RGBA16F: GLEnum
expect val GL_BYTE: GLEnum
expect val GL_SHORT: GLEnum
expect val GL_UNSIGNED_BYTE: GLEnum
expect val GL_UNSIGNED_SHORT: GLEnum
expect val GL_UNSIGNED_INT: GLEnum
expect val GL_FLOAT: GLEnum
expect val GL_HALF_FLOAT: GLEnum
expect val GL_DEPTH_COMPONENT: GLEnum
expect val GL_DEPTH_COMPONENT24: GLEnum
expect fun GL_COLOR_ATTACHMENT(i: Int): GLEnum
expect fun GL_TEXTURE(i: Int): GLEnum
expect val GL_DEPTH_ATTACHMENT: GLEnum
expect val GL_TEXTURE_MAG_FILTER: GLEnum
expect val GL_TEXTURE_MIN_FILTER: GLEnum
expect val GL_TEXTURE_WRAP_S: GLEnum
expect val GL_TEXTURE_WRAP_T: GLEnum
expect val GL_REPEAT: GLEnum
expect val GL_NEAREST: GLEnum
expect val GL_LINEAR: GLEnum
expect val GL_CLAMP_TO_EDGE: GLEnum
expect val GL_NEAREST_MIPMAP_LINEAR: GLEnum
expect val GL_LINEAR_MIPMAP_LINEAR: GLEnum
expect val GL_TEXTURE_MAX_LEVEL: GLEnum
expect val GL_ARRAY_BUFFER: GLEnum
expect val GL_ELEMENT_ARRAY_BUFFER: GLEnum
expect val GL_STATIC_DRAW: GLEnum
expect val GL_STREAM_DRAW: GLEnum
expect val GL_FRONT: GLEnum
expect val GL_FRONT_AND_BACK: GLEnum
expect val GL_VIEWPORT: GLEnum
expect val GL_BLEND: GLEnum
expect val GL_SRC_ALPHA: GLEnum
expect val GL_ONE_MINUS_SRC_ALPHA: GLEnum
expect val GL_DST_ALPHA: GLEnum
expect val GL_ONE_MINUS_DST_COLOR: GLEnum
expect val GL_ONE_MINUS_SRC_COLOR: GLEnum
expect val GL_SCISSOR_TEST: GLEnum
expect val GL_DEPTH_TEST: GLEnum
expect val GL_CULL_FACE: GLEnum
expect val GL_LEQUAL: GLEnum
expect val GL_NO_ERROR: GLEnum
expect val GL_INVALID_ENUM: GLEnum
expect val GL_INVALID_VALUE: GLEnum
expect val GL_INVALID_OPERATION: GLEnum
expect val GL_OUT_OF_MEMORY: GLEnum
expect val GL_INVALID_FRAMEBUFFER_OPERATION: GLEnum
expect val GL_FRAMEBUFFER_COMPLETE: GLEnum
expect val GL_FRAMEBUFFER_UNSUPPORTED: GLEnum
expect val GL_TRIANGLES: GLEnum
expect val GL_LINES: GLEnum
expect val GL_VERTEX_SHADER: GLEnum
expect val GL_FRAGMENT_SHADER: GLEnum
expect val GL_LINK_STATUS: GLEnum
