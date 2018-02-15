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

package org.tobi29.io.classpath

import org.tobi29.io.*

data class ClasspathPath(
    private val classLoader: ClassLoader,
    private val path: String
) : PathLocal {
    override fun get(path: String): PathLocal {
        UnixPathEnvironment.run {
            return ClasspathPath(
                classLoader,
                this@ClasspathPath.path.resolve(path)
            )
        }
    }

    override val parent
        get() = UnixPathEnvironment.run {
            path.parent?.let { ClasspathPath(classLoader, it) }
        }

    override fun channel(): ReadableByteChannel {
        val stream = classLoader.getResourceAsStream(path)
                ?: throw IOException("Classpath entry not found: $path")
        return Channels.newChannel(stream).toChannel()
    }
}
