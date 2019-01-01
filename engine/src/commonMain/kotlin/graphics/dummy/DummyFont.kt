/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine.graphics.dummy

import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.gui.GlyphRenderer

object DummyFont : Font {
    override fun createGlyphRenderer(size: Int): GlyphRenderer =
        DummyGlyphRenderer
}

private object DummyGlyphRenderer : GlyphRenderer {
    override fun pageInfo(id: Int): GlyphRenderer.GlyphPage {
        val width = IntArray(1)
        return GlyphRenderer.GlyphPage(width, 1, 1, 1.0)
    }

    override suspend fun page(id: Int) = ByteArray(1 shl 2)

    override fun pageID(character: Char) = 0

    override fun pageCode(character: Char) = 0
}
