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

package org.tobi29.scapes.engine.backends.opengles

import net.gitout.ktbindings.gles.GLES20
import net.gitout.ktbindings.gles.contextGLES30
import net.gitout.ktbindings.gles.delete
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.GLESBackend
import org.tobi29.scapes.engine.graphics.GL

object GLESBackendLWJGL : GLESBackend {
    override fun createGL(container: Container): GL =
        GLESImpl(::contextGLES30, GLES20::delete, container::isRenderCall)
}
