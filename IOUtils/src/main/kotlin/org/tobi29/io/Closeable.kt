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

package org.tobi29.io

expect interface AutoCloseable {
    /**
     * @throws Exception
     */
    fun close()
}

expect interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    override fun close()
}

inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Throwable) {
        closed = true
        if (this != null) {
            try {
                close()
            } catch (closeException: Throwable) {
                // e.addSuppressed(closeException)
            }
        }
        throw e
    } finally {
        if (this != null && !closed) {
            close()
        }
    }
}
