/*
 * Copyright 2012-2018 Tobi29
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
package org.tobi29.scapes.engine.graphics

import org.tobi29.arrays.BytesRO

interface Texture : GraphicsObject {
    fun bind(gl: GL)

    fun markDisposed()

    fun width(): Int

    fun height(): Int

    fun setWrap(wrapS: TextureWrap, wrapT: TextureWrap)

    fun setFilter(magFilter: TextureFilter, minFilter: TextureFilter)

    fun buffer(i: Int): BytesRO?

    fun setBuffer(buffer: BytesRO?)

    fun setBuffer(buffer: BytesRO?, width: Int, height: Int)
}

fun Texture?.bind(gl: GL) {
    if (this == null) gl.textureEmpty.bind(gl)
    else bind(gl)
}
