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

package java.nio.channels

import java.lang.AutoCloseable

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class FileLock : AutoCloseable {
    protected constructor(channel: FileChannel,
                          position: Long,
                          size: Long,
                          shared: Boolean)

    fun position(): Long
    fun size(): Long
    fun overlaps(position: Long,
                 size: Long): Boolean

    fun channel(): FileChannel?
    fun acquiredBy(): Channel

    fun isShared(): Boolean
    abstract fun isValid(): Boolean

    /**
     * @throws IOException
     */
    abstract fun release()
}
